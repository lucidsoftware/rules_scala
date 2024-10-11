package higherkindness.rules_scala
package workers.deps

import common.args.ArgsUtil
import common.args.ArgsUtil.PathArgumentType
import common.args.implicits._
import common.interrupt.InterruptUtil
import common.worker.WorkerMain
import common.sandbox.SandboxUtil
import workers.common.AnnexMapper
import workers.common.FileUtil
import java.io.{File, PrintStream}
import java.nio.file.{FileAlreadyExistsException, Files, Path, Paths}
import java.util.Collections
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.Namespace
import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._

object DepsRunner extends WorkerMain[Unit] {

  private[this] class DepsRunnerRequest private (
    val checkDirect: Boolean,
    val checkUsed: Boolean,
    val directDepLabels: List[String],
    val groups: List[Group],
    val label: String,
    val usedDepWhitelist: List[String],
    val unusedDepWhitelist: List[String],
    val usedDepsFile: Path,
    val successFile: Path,
  )

  private[this] object DepsRunnerRequest {
    def apply(workDir: Path, namespace: Namespace): DepsRunnerRequest = {
      val groups = Option(namespace.getList[java.util.List[String]]("group"))
        .map(_.asScala)
        .getOrElse(List.empty)
        .view
        .map { group =>
          group.asScala match {
            case Buffer(label, jars @ _*) => Group.apply(workDir, label, jars)
            case _                        => throw new Exception(s"Unexpected case in DepsRunner")
          }
        }
        .toList

      new DepsRunnerRequest(
        checkDirect = namespace.getBoolean("check_direct"),
        checkUsed = namespace.getBoolean("check_used"),
        directDepLabels = namespace.getList[String]("direct").asScala.view.map(_.tail).toList,
        groups = groups,
        label = namespace.getString("label").tail,
        usedDepsFile = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("used")),
        usedDepWhitelist = namespace.getList[String]("used_whitelist").asScala.view.map(_.tail).toList,
        unusedDepWhitelist = namespace.getList[String]("unused_whitelist").asScala.view.map(_.tail).toList,
        successFile = SandboxUtil.getSandboxPath(workDir, namespace.get[Path]("success")),
      )
    }
  }

  private[this] class Group private (
    val label: String,
    val jars: Set[String],
  )

  private[this] object Group {
    def apply(workDir: Path, prependedLabel: String, jars: Seq[String]): Group = {
      new Group(
        prependedLabel.tail,
        jars.toSet,
      )
    }
  }

  private[this] val argParser = {
    val parser = ArgumentParsers.newFor("deps").addHelp(true).fromFilePrefix("@").build
    parser.addArgument("--check_direct").`type`(Arguments.booleanType)
    parser.addArgument("--check_used").`type`(Arguments.booleanType)
    parser
      .addArgument("--direct")
      .help("Labels of direct deps")
      .metavar("label")
      .nargs("*")
      .setDefault_(Collections.emptyList())
    parser
      .addArgument("--group")
      .action(Arguments.append)
      .help("Label and manifest of jars")
      .metavar("label [path [path ...]]")
      .nargs("+")
    parser.addArgument("--label").help("Label of current target").metavar("label").required(true)
    parser
      .addArgument("--used_whitelist")
      .help("Whitelist of labels to ignore for unused deps")
      .metavar("label")
      .nargs("*")
      .setDefault_(Collections.emptyList)
    parser
      .addArgument("--unused_whitelist")
      .help("Whitelist of labels to ignore for direct deps")
      .metavar("label")
      .nargs("*")
      .setDefault_(Collections.emptyList)
    parser.addArgument("used").help("Manifest of used").`type`(PathArgumentType.apply())
    parser.addArgument("success").help("Success file").`type`(PathArgumentType.apply())
    parser
  }

  override def init(args: Option[Array[String]]): Unit = ()

  override def work(ctx: Unit, args: Array[String], out: PrintStream, workDir: Path, verbosity: Int): Unit = {
    val workRequest = DepsRunnerRequest(workDir, ArgsUtil.parseArgsOrFailSafe(args, argParser, out))
    InterruptUtil.throwIfInterrupted()

    val groupLabelToJarPaths = workRequest.groups.map { group =>
      group.label -> group.jars
    }.toMap
    def pathsForLabel(depLabel: String): List[String] = {
      // A label could have no @ prefix, a single @ prefix, or a double @@ prefix.
      // In an ideal world, the label we see here would always match the label in
      // the --group, but that's not always the case. So we need to be able to handle
      // moving from any of the forms to any of the other forms.
      val potentialLabels = if (depLabel.startsWith("@@")) {
        List(depLabel.stripPrefix("@@"), depLabel.stripPrefix("@"), depLabel)
      } else if (depLabel.startsWith("@")) {
        List(depLabel.stripPrefix("@"), depLabel, s"@${depLabel}")
      } else {
        List(depLabel, s"@${depLabel}", s"@@${depLabel}")
      }

      potentialLabels.collect(groupLabelToJarPaths).flatten
    }
    val readWriteMappers = AnnexMapper.mappers(workDir, isIncremental = false)
    val readMapper = readWriteMappers.getReadMapper()

    InterruptUtil.throwIfInterrupted()
    val usedPaths = Files
      .readAllLines(workRequest.usedDepsFile)
      .asScala
      .view
      // Get the short path, so we can compare paths without the Bazel configuration sensitive
      // parts of the path. We can handle the Zinc machine sensitive parts of the path with the
      // AnnexMapper, but we don't have an equivalent for the Bazel configuration sensitive parts.
      // So we just toss both out and compare the short path.
      // Note that this is dependent upon the Bazel file output structure not changing.
      .map { usedDep =>
        val usedDepPath = Paths.get(usedDep.stripPrefix(AnnexMapper.rootPlaceholder.toString() + "/"))
        val nameCount = usedDepPath.getNameCount()

        val shortPath = if (usedDepPath.startsWith("bazel-out") && nameCount >= 4) {
          usedDepPath.subpath(3, nameCount).toString()
        } else {
          usedDepPath.toString()
        }

        // Handle difference between Bazel's external directory being referred to as .. in the short_path
        if (shortPath.startsWith("external")) {
          shortPath.replaceFirst("external", "..")
        } else {
          shortPath
        }
      }
      .toSet
    val labelsToRemove = if (workRequest.checkUsed) {
      workRequest.directDepLabels
        .diff(workRequest.usedDepWhitelist)
        .filterNot(label => pathsForLabel(label).exists(usedPaths))
    } else {
      Nil
    }
    labelsToRemove.foreach { depLabel =>
      out.println(s"Target '$depLabel' not used, please remove it from the deps.")
      out.println(s"You can use the following buildozer command:")
      out.println(s"buildozer 'remove deps $depLabel' ${workRequest.label}")
    }

    InterruptUtil.throwIfInterrupted()
    val labelsToAdd = if (workRequest.checkDirect) {
      (usedPaths -- (workRequest.directDepLabels :++ workRequest.unusedDepWhitelist).flatMap(pathsForLabel))
        .flatMap { path =>
          groupLabelToJarPaths.collectFirst { case (myLabel, paths) if paths(path) => myLabel }.orElse {
            System.err
              .println(s"Warning: There is a reference to $path, but no dependency of ${workRequest.label} provides it")
            None
          }
        }
    } else {
      Nil
    }
    labelsToAdd.foreach { depLabel =>
      out.println(s"Target '$depLabel' is used but isn't explicitly declared, please add it to the deps.")
      out.println(s"You can use the following buildozer command:")
      out.println(s"buildozer 'add deps $depLabel' ${workRequest.label}")
    }

    if (labelsToAdd.isEmpty && labelsToRemove.isEmpty) {
      try Files.createFile(workRequest.successFile)
      catch { case _: FileAlreadyExistsException => }
    }
    InterruptUtil.throwIfInterrupted()
  }
}
