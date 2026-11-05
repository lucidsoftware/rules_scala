package higherkindness.rules_scala.workers.zinc.compile

import higherkindness.rules_scala.common.error.AnnexWorkerError
import higherkindness.rules_scala.common.interrupt.InterruptUtil
import higherkindness.rules_scala.common.worker.WorkTask
import higherkindness.rules_scala.workers.common.{CommonArguments, FileUtil, JavaBuilderArguments}
import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*

object JavaBuilderCompiler {
  def compile(
    task: WorkTask[Unit],
    arguments: CommonArguments,
    builder: JavaBuilderArguments,
    sources: List[String],
    classesDirectory: Path,
  ): Option[Path] = {
    val scratch = Files.createDirectories(classesDirectory.resolveSibling("javabuilder")).toAbsolutePath
    val javaJar = scratch.resolve("java.jar")
    val javaDeps = arguments.outputUsedJarsForInputPruning.map(_ => scratch.resolve("java.jdeps"))
    val params = scratch.resolve("arguments")
    val log = scratch.resolve("output.log")
    val flags = List(
      "--output",
      javaJar.toString,
      "--target_label",
      arguments.label,
      "--strict_java_deps",
      "OFF",
      "--reduce_classpath_mode",
      "NONE",
      "--classpath",
    ) ++ (classesDirectory +: arguments.classpath).map(_.toAbsolutePath.toString) ++
      List("--bootclasspath") ++ builder.bootClasspath.map(_.toAbsolutePath.toString) ++
      List("--sources") ++ sources ++
      javaDeps.toList.flatMap(path => List("--output_deps_proto", path.toString)) ++
      List("--javacopts") ++ builder.javaCompilerOptions ++ List("--")

    Files.write(params, flags.asJava)

    val executable = builder.executable.toAbsolutePath.toString
    val command = if (executable.endsWith(".jar")) {
      List(builder.javaExecutable.toAbsolutePath.toString) ++ builder.jvmOptions ++
        List(s"-Djava.io.tmpdir=$scratch", "-jar", executable)
    } else {
      List(executable)
    }

    val process = new ProcessBuilder((command :+ s"@$params").asJava)
      // JVM options may contain paths relative to the work directory
      .directory(task.workDir.toAbsolutePath.toFile)
      .redirectErrorStream(true)
      .redirectOutput(log.toFile)
      .start()

    try {
      while (!process.waitFor(100, TimeUnit.MILLISECONDS)) {
        InterruptUtil.throwIfInterrupted(task.isCancelled)
      }

      InterruptUtil.throwIfInterrupted(task.isCancelled)

      if (process.exitValue() != 0) {
        throw new AnnexWorkerError(process.exitValue())
      }
    } finally {
      if (process.isAlive) {
        process.destroyForcibly()
        process.waitFor()
      }

      val reader = Files.newBufferedReader(log)

      try {
        reader.lines().iterator().asScala.foreach(task.output.println)
      } finally {
        reader.close()
      }
    }

    FileUtil.extractZip(javaJar, classesDirectory)

    javaDeps
  }
}
