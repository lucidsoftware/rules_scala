package higherkindness.rules_scala.workers.zinc.compile.usedjarreporter

import dotty.tools.dotc.CompilationUnit
import dotty.tools.dotc.core.Contexts.*
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.SymDenotations.LazyType
import dotty.tools.dotc.core.SymbolLoaders
import dotty.tools.dotc.core.Types.*
import dotty.tools.dotc.plugins.{PluginPhase, StandardPlugin}
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
 * This plugin is used to record the JARs used by the compiler for the purposes of input pruning.
 *
 * It does so by inspecting the symbols loaded by the compiler and recording the JARs they come from. This is very
 * similar to what the compiler bridge does (which `ScalaCheckDeps` uses), but slightly different in that it's a bit
 * noiser. See [zinc#150](https://github.com/sbt/zinc/issues/150) for one example of how.
 *
 * For this reason, it's best that we use the list of used JARs produced by this plugin for input pruning, where we need
 * an absolutely comprehensive list. We use the compiler bridge's list for dependency checking so the list of required
 * direct dependencies doesn't blow up.
 *
 * NOTE: This plugin is specific to Scala 3.7.x.
 */
class AnnexUsedJarReporterPlugin extends StandardPlugin {
  val name = "annex-used-jar-reporter"
  val description = "Record the JARs containing symbols loaded by the compiler."

  override def initialize(options: List[String])(implicit context: Context): List[PluginPhase] = {
    require(options.size == 1, "annex-used-jar-reporter requires one output path")

    val output = Paths.get(options.head)
    val jars = mutable.Set.empty[String]

    def collect()(implicit context: Context): Unit = {
      val visited = mutable.Set.empty[Symbol]

      def visit(symbol: Symbol): Unit = {
        if (symbol.isClass && visited.add(symbol)) {
          val info = symbol.lastKnownDenotation.infoOrCompleter

          // Don't use `symbol.info` or `symbol.unforcedDecls`; they can force unused classpath entries to load.
          if (info.isInstanceOf[ClassInfo] || info.isInstanceOf[SymbolLoaders.SecondCompleter]) {
            Option(symbol.associatedFile).flatMap(_.underlyingSource).foreach { archive =>
              jars += Paths.get(archive.path).toAbsolutePath.normalize.toString
            }
          }

          info match {
            case info: ClassInfo => info.decls.iterator.foreach(visit)
            case info: LazyType  => info.decls.iterator.foreach(visit)
            case _               =>
          }
        }
      }

      visit(defn.RootClass)
      visit(defn.EmptyPackageClass)
    }

    def phase(phaseName0: String, after: String, write: Boolean): PluginPhase = new PluginPhase {
      override val phaseName: String = phaseName0
      override val runsAfter: Set[String] = Set(after)
      override def runOn(units: List[CompilationUnit])(implicit context: Context): List[CompilationUnit] = {
        collect()

        if (write) {
          // Macro suspension (macro evaluation) can cause the compiler plugin to execute multiple times within a
          // single compilation unit
          if (Files.exists(output)) jars ++= Files.readAllLines(output).asScala

          Files.write(output, jars.toList.sorted.asJava)
        }

        units
      }
    }

    List(
      phase("annexUsedJarReporterAfterTyper", "typer", false),
      phase("annexUsedJarReporter", "genBCode", true),
    )
  }
}
