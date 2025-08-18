package higherkindness.rules_scala.workers.jacoco.instrumenter

import higherkindness.rules_scala.common.args.ArgsUtil
import higherkindness.rules_scala.common.error.AnnexWorkerError
import higherkindness.rules_scala.common.interrupt.InterruptUtil
import higherkindness.rules_scala.common.sandbox.SandboxUtil
import higherkindness.rules_scala.common.worker.{WorkTask, WorkerMain}
import java.io.{BufferedInputStream, BufferedOutputStream}
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.Collections
import java.util.List as JList
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.{ArgumentParser, Namespace}
import org.jacoco.core.instr.Instrumenter
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator
import scala.jdk.CollectionConverters.*
import scala.util.Using

object JacocoInstrumenter extends WorkerMain[Unit] {

  private class JacocoRequest private (
    val jars: List[(Path, Path)],
  )

  private object JacocoRequest {
    def apply(workDir: Path, namespace: Namespace): JacocoRequest = {
      val pathPairs = namespace
        .getList[JList[String]]("jar")
        .asScala
        .flatMap(_.asScala)
        .map(other =>
          other.split("=") match {
            case Array(in, out) =>
              (
                SandboxUtil.getSandboxPath(workDir, Paths.get(in)),
                SandboxUtil.getSandboxPath(workDir, Paths.get(out)),
              )
            case _ =>
              throw new AnnexWorkerError(1, "expected input=output for argument: " + other)
          },
        )
        .toList

      new JacocoRequest(jars = pathPairs)
    }
  }

  private val argParser: ArgumentParser = {
    val parser = ArgumentParsers.newFor("jacoco-instrumenter").addHelp(true).fromFilePrefix("@").build
    parser
      .addArgument("--jar")
      .action(Arguments.append)
      .help("jar to instrument")
      .metavar("inpath=outpath")
      .nargs("+")
    parser
  }

  override def init(args: Option[Array[String]]): Unit = ()

  override def work(task: WorkTask[Unit]): Unit = {
    val workRequest = JacocoRequest(
      task.workDir,
      ArgsUtil.parseArgsOrFailSafe(task.args, argParser, task.output),
    )

    val jacoco = new Instrumenter(new OfflineInstrumentationAccessGenerator)

    workRequest.jars.foreach { case (inPath, outPath) =>
      Using.Manager { use =>
        InterruptUtil.throwIfInterrupted(task.isCancelled)

        val inFS = use(FileSystems.newFileSystem(inPath, null: ClassLoader))
        val outFS =
          use(FileSystems.newFileSystem(URI.create("jar:" + outPath.toUri), Collections.singletonMap("create", "true")))

        val roots = inFS.getRootDirectories.asScala.toList
        val instrumentVisitor = new SimpleFileVisitor[Path] {
          override def visitFile(inPath: Path, attrs: BasicFileAttributes): FileVisitResult = {
            val outPath = outFS.getPath(inPath.toString)
            Files.createDirectories(outPath.getParent)
            if (inPath.toString.endsWith(".class")) {
              Using.Manager { use2 =>
                val inStream = use2(new BufferedInputStream(Files.newInputStream(inPath)))
                val outStream =
                  use2(new BufferedOutputStream(Files.newOutputStream(outPath, StandardOpenOption.CREATE_NEW)))
                jacoco.instrument(inStream, outStream, inPath.toString)
                inStream.close()
                outStream.close()
              }.get
              Files.copy(inPath, outFS.getPath(outPath.toString + ".uninstrumented"))
            } else {
              Files.copy(inPath, outPath)
            }
            FileVisitResult.CONTINUE
          }
        }

        roots.foreach(Files.walkFileTree(_, instrumentVisitor))
      }.get
    }

    InterruptUtil.throwIfInterrupted(task.isCancelled)
  }
}
