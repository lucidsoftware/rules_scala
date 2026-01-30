package sbt.internal.inc.classfile

import java.io.File
import java.nio.file.Path
import sbt.internal.inc.classpath.ClasspathUtil
import sbt.util.Logger
import xsbti.compile.SingleOutput
import xsbti.{AnalysisCallback, VirtualFile, VirtualFileRef}

/**
 * A public wrapper around `sbt.internal.inc.JavaAnalyze` that produces analysis information from class files generated
 * by the Java compiler and is designed to be called outside of [[sbt.internal.inc.javac.AnalyzingJavaCompiler]]. It
 * makes certain assumptions about the way in which Java code is compiled:
 *   - The output is a directory of class files, not a JAR file
 *   - That this method will be called once per compilation unit and therefore that all the setup can be done within it
 *     because there's no use in reusing components
 *
 * Much of the setup in the implementation of this method is borrowed from
 * [[sbt.internal.inc.javac.AnalyzingJavaCompiler]].
 */
def analyzeJavaClasses(
  javaClasses: Seq[Path],
  sources: Seq[VirtualFile],
  classpath: Seq[Path],
  outputDirectory: Path,
  logger: Logger,
  analysisCallback: AnalysisCallback,
): Unit = {
  val output = new SingleOutput {
    override def getOutputDirectory: File = outputDirectory.toFile
  }

  val classloader = ClasspathUtil.toLoader(outputDirectory +: classpath)

  /**
   * TODO: Make this more similar to the `readAPI` defined in [[sbt.internal.inc.javac.AnalyzingJavaCompiler]].
   *
   * This method is supposed to use [[sbt.internal.inc.ClassToAPI]] to analyze the list of provided classes and is
   * supposed to return a set of inheritance pairs (`subclass -> superclass`, but it currently does nothing because I
   * can't get it to work with `ijar`. [[sbt.internal.inc.ClassToAPI]] attempts to load the methods of the classes
   * provided to it, which results in an error like this:
   *
   * {{{
   *   java.lang.ClassFormatError: Absent Code attribute in method that is not native or abstract in class file ...
   * }}}
   *
   * I'm not sure how this worked when we used Zinc instead of the compiler bridge directly.
   */
  def readAPI(source: VirtualFileRef, classes: Seq[Class[?]]): Set[(String, String)] = Set.empty

  JavaAnalyze(javaClasses, sources, logger, output, finalJarOutput = None)(analysisCallback, classloader, readAPI)
}
