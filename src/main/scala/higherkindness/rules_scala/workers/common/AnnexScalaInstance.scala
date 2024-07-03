package higherkindness.rules_scala
package workers.common

import xsbti.compile.ScalaInstance
import java.io.File
import java.net.URLClassLoader
import java.util.Properties

object AnnexScalaInstance {
  // Check the comment on ScalaInstanceDualLoader to understand why this is necessary.
  private val dualClassLoader: ScalaInstanceDualLoader = {
    new ScalaInstanceDualLoader(
      // Classloader for xsbti classes
      getClass.getClassLoader,
      // Classloader for all other classes
      ClassLoader.getPlatformClassLoader(),
    )
  }

  /**
   * Convenience function to get a classloader from an Array[File] whose parent
   * is the dualClassLoader, so it will handle xsbti correctly.
   */
  private def getClassLoader(jars: Array[File]): URLClassLoader = {
    new URLClassLoader(
      jars.map(_.toURI.toURL),
      dualClassLoader,
    )
  }

  // These are not the most robust checks for these jars, but it is more or
  // less what Zinc and Bloop are doing. They're also fast, so if it works it
  // works.

  private final def isScala2CompilerJar(jarName: String): Boolean = {
    jarName.startsWith("scala-compiler") || jarName.startsWith("scala-reflect")
  }

  private final def isScala3CompilerJar(jarName: String): Boolean = {
    jarName.startsWith("scala3-compiler") || jarName.startsWith("scala3-interfaces") ||
      jarName.startsWith("tasty-core_3") || jarName.startsWith("scala-asm")
  }

  private final def isScalaLibraryJar(jarName: String): Boolean = {
    jarName.startsWith("scala-library") || jarName.startsWith("scala3-library")
  }
}

// See this interface for comments better explaining the purpose of all the
// member variables:
// https://github.com/sbt/zinc/blob/4eacff2a9bf5c8750bfc5096955065ce67f4e68a/internal/compiler-interface/src/main/java/xsbti/compile/ScalaInstance.java
final class AnnexScalaInstance(override val allJars: Array[File]) extends ScalaInstance {

  // Jars for the Scala compiler classes
  // We need to include the full classpath for the Scala 2 or Scala 3 compilers.
  // Thankfully that classpath doesn't seem to change very often.
  override val compilerJars: Array[File] = allJars.filter { jar =>
    val jarName = jar.getName
    AnnexScalaInstance.isScala2CompilerJar(jarName) ||
      AnnexScalaInstance.isScala3CompilerJar(jarName) ||
      AnnexScalaInstance.isScalaLibraryJar(jarName)
  }

  // Jars for the Scala library classes
  override val libraryJars: Array[File] =
    allJars.filter(jar => AnnexScalaInstance.isScalaLibraryJar(jar.getName))

  // All the jars that are not compiler or library jars
  override val otherJars: Array[File] = allJars.diff(compilerJars ++ libraryJars)

  // Version for this Scala instance
  override val actualVersion: String = {
    val stream = AnnexScalaInstance.getClassLoader(compilerJars)
      .getResourceAsStream("compiler.properties")

    try {
      val props = new Properties
      props.load(stream)
      props.getProperty("version.number")
    } finally stream.close()
  }
  override val version: String = actualVersion

  // Loader for only the classes and resources in the library jars of this Scala instance
  override val loaderLibraryOnly: ClassLoader = AnnexScalaInstance.getClassLoader(libraryJars)

  // Loader for all the classes and resources in all the jars of this Scala instance
  override val loader: ClassLoader = AnnexScalaInstance.getClassLoader(allJars)

  // Loader for all the classes and resources in all the compiler jars of this
  // Scala instance
  override val loaderCompilerOnly: ClassLoader = AnnexScalaInstance.getClassLoader(compilerJars)
}

/**
 * Load classes that start with xsbti or jline from the sbt class loader
 *   - xsbti is for the compiler bridge
 *   - jline for the sbt provided jline terminal
 * Load all other classes from the other loader
 * Always get resources from the non-sbt loader
 *
 * This is necessary, so we can always make sure that the xsbti and jline
 * classes needed for compilation and the repl come from the same class loader.
 *
 * If the same class is loaded by two separate class loaders you get a linkage
 * error in the JVM.
 *
 * If you set up a class loader that doesn't contain the xsbti and jline
 * classes, then you get an error when you can't find those classes because
 * they are needed by Zinc and our ZincRunner.
 *
 * If you set up one class loader for all the things, then you start leaking
 * any class loaded during any compilation into any compilation happening in
 * the JVM. Considering workers are long lived and handle multiple compilations,
 * that could become a problem.
 *
 * Dotty, Bloop, and Zinc all do very similar things. This approach was heavily
 * inspired by their approaches. For more details about their approaches see
 * the following:
 * https://github.com/scala/scala3/blob/7d26a96db6e63aba8db1362b09f4adf521bc8327/sbt-dotty/src/dotty/tools/sbtplugin/DottyPlugin.scala#L620
 * https://github.com/scala/scala3/blob/cd8c5ed1dc5ee8528861e284490534064ba6d3e5/sbt-bridge/src/xsbt/CompilerClassLoader.java#L11
 * https://github.com/scalacenter/bloop/blob/c505385edf0bbd420e19401ae1beabae8895df8f/backend/src/main/scala/bloop/ScalaInstanceTopLoader.scala#L30
 * https://github.com/sbt/zinc/blob/4eacff2a9bf5c8750bfc5096955065ce67f4e68a/internal/zinc-classpath/src/main/scala/sbt/internal/inc/classpath/DualLoader.scala#L47
 */
class ScalaInstanceDualLoader(sbtClassLoader: ClassLoader, otherLoader: ClassLoader)
    extends ClassLoader(otherLoader) {
  override final def loadClass(name: String, resolve: Boolean): Class[_] = {
    if (name.startsWith("xsbti.") || name.startsWith("org.jline.")) {
      val klass = sbtClassLoader.loadClass(name)
      if (resolve) {
        resolveClass(klass)
      }
      klass
    } else {
      super.loadClass(name, resolve)
    }
  }
}
