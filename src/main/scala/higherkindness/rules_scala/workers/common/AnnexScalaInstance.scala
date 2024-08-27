package higherkindness.rules_scala
package workers.common

import xsbti.compile.ScalaInstance
import java.io.File
import java.net.URLClassLoader
import java.nio.file.{Files, Path, Paths}
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import scala.collection.immutable.Map

object AnnexScalaInstance {
  // See the comment on getAnnexScalaInstance as to why this is necessary
  private val instanceCache: ConcurrentHashMap[String, AnnexScalaInstance] =
    new ConcurrentHashMap[String, AnnexScalaInstance]()

  /**
   * We only need to care about minimizing the number of AnnexScalaInstances we create if things are being run as a
   * worker. Otherwise just create the AnnexScalaInstance and be done with it because the process won't be long lived.
   */
  def getAnnexScalaInstance(allJars: Array[File], workDir: Path, isWorker: Boolean): AnnexScalaInstance = {
    if (isWorker) {
      getAnnexScalaInstance(allJars, workDir)
    } else {
      new AnnexScalaInstance(allJars)
    }
  }

  /**
   * Given a set of jars, get back an AnnexScalaInstance for those jars. If an instance for those jars is already stored
   * in the instanceCache, then return that instance. Otherwise create a new AnnexScalaInstance for those jars, insert
   * it into the cache, and return it. This function should be thread safe.
   *
   * The goal is we have at most one AnnexScalaInstance per set of input jars, which means we have at most one
   * AnnexScalaInstance per Scala version.
   *
   * Why? Because using the AnnexScalaInstance caching seems to prevent non-determinism in Zinc's analysis store output.
   *
   * As background: Zinc has a class loader cache, which can be disabled when creating the analyzing compiler. The Scala
   * compiler also does some classpath caching, which can be disabled using -YdisableFlatCpCaching.
   *
   * Caveat: the rest of this information is from empirical observation and cursory reading of the Scala and Zinc code.
   * I don't deeply understand why these things are the way they are and deeply understanding these things seems
   * non-trivial.
   *
   * The non-determinism appears to only be relevant for when the ZincRunner is run in worker mode. I imagine there's
   * some kind of issue with either thread safety. Or an issue when Zinc uses a cached classloader from a ScalaInstance
   * from a prior compilation in combination with a new ScalaInstance for the current compilation. Or there's some issue
   * with Scala's classpath caching.
   *
   * If all three caches are enabled (this cache + Zinc claspath + Scala compiler), then the non-determinism seems go
   * away.
   *
   * If this cache is not used, but the Zinc classpath + Scala compiler caches are used, then non-determinsm shows up in
   * the analysis store files in many different places.
   *
   * If this cache is not used and the Zinc cache is not used, but the Scala compiler cache is used then you have a
   * metaspace leak caused by the Scala compiler's cache. It looks like like every instance of the Scala compiler
   * prevents the classloaders from being GC'd. Meaning, for each compilation, the Scala compiler gets a new
   * classloader, loads a bunch of classes from it, but those classes are never cleaned up because the classloader isn't
   * GC'd.
   *
   * Disabling all three caches works, but is super duper slow. At that point there's not much benefit to using the
   * worker strategy.
   *
   * Using this cache and the Scala compiler cache, but disabling Zinc's classloader cache works, but doesn't seem to be
   * any different from leaving Zinc's cache enabled.
   */
  private def getAnnexScalaInstance(allJars: Array[File], workDir: Path): AnnexScalaInstance = {
    // We need to remove the sandbox prefix from the paths in order to compare them.
    val mapBuilder = Map.newBuilder[Path, Path]
    allJars.foreach { jar =>
      val comparableJarPath = jar.toPath().toAbsolutePath().normalize()
      mapBuilder.addOne(jar.toPath -> workDir.toAbsolutePath().normalize().relativize(comparableJarPath))
    }
    val workRequestJarToWorkerJar = mapBuilder.result()

    // Because we're just using file names here, theres's a problem  if the contents of the jars
    // on the classpath for a particular version of the compiler change and we already have a
    // ScalaInstance created and cached for the filenames on that classpath. In that case we'll use
    // the cached ScalaInstance rather than create a new one.
    //
    // We could get around this problem by hashing all the jars and using that as the cache key,
    // but that would require hashing the all the classpath jars on every compilation request. I
    // imagine that would cause a performance hit.
    //
    // I also imagine it is extremeley rare to be mutating the contents of compiler classpath jars
    // while keeping the names the same, e.g., generating new scala library jar for scala 2.13.14.
    // As a result I'm leaving this string based for now.
    val key = workRequestJarToWorkerJar.values.mkString(":")

    Option(instanceCache.get(key)).getOrElse {
      // Copy all the jars to the worker's directory because in a sandboxed world the
      // jars can go away after the work request, so we can't rely on them sticking around.
      // This should only happen once per compiler version, so it shouldn't happen often.
      workRequestJarToWorkerJar.foreach { case (workRequestJar, workerJar) =>
        this.synchronized {
          // Check for existence of the file just in case another request is also writing these jars
          // Copying a file is not atomic, so we don't want to end up in a funky state where two
          // copies of the same file happen at the same time and cause something bad to happen.
          if (!Files.exists(workerJar)) {
            Files.createDirectories(workerJar.getParent())
            Files.copy(workRequestJar, workerJar)
          }
        }
      }

      val instance = new AnnexScalaInstance(Array.from(workRequestJarToWorkerJar.values.map(_.toFile())))
      val instanceInsertedByOtherThreadOrNull = instanceCache.putIfAbsent(key, instance)

      // putIfAbsent is atomic, but there exists time between the get and the putIfAbsent.
      // This handles the scenario in which the AnnexScalaInstance is created and inserted
      // by another thread after we ran our .get.
      // We could also handle this by generating the AnnexScalaInstance every time and only
      // using a putIfAbsent, but that's likely more expensive because of all the classloaders
      // that get constructed when creating an AnnexScalaInstance.
      if (instanceInsertedByOtherThreadOrNull == null) {
        instance
      } else {
        instanceInsertedByOtherThreadOrNull
      }
    }
  }

  // Check the comment on ScalaInstanceDualLoader to understand why this is necessary.
  private def getDualClassLoader(): ScalaInstanceDualLoader = {
    new ScalaInstanceDualLoader(
      // Classloader for xsbti classes
      getClass.getClassLoader,
      // Classloader for all other classes
      ClassLoader.getPlatformClassLoader(),
    )
  }

  /**
   * Convenience function to get a classloader from an Array[File] whose parent is the dualClassLoader, so it will
   * handle xsbti correctly.
   */
  private def getClassLoader(jars: Array[File]): URLClassLoader = {
    new URLClassLoader(
      jars.map(_.toURI.toURL),
      getDualClassLoader(),
    )
  }

  // These are not the most robust checks for these jars, but it is more or
  // less what Zinc and Bloop are doing. They're also fast, so if it works it
  // works.
  private final def isScala2CompilerJar(jar: File): Boolean = {
    val jarName = FileUtil.getNameWithoutRulesJvmExternalStampPrefix(jar)
    jarName.startsWith("scala-compiler") || jarName.startsWith("scala-reflect")
  }

  private final def isScala3CompilerJar(jar: File): Boolean = {
    val jarName = FileUtil.getNameWithoutRulesJvmExternalStampPrefix(jar)
    jarName.startsWith("scala3-compiler") || jarName.startsWith("scala3-interfaces") ||
    jarName.startsWith("tasty-core_3") || jarName.startsWith("scala-asm")
  }

  private final def isScalaLibraryJar(jar: File): Boolean = {
    val jarName = FileUtil.getNameWithoutRulesJvmExternalStampPrefix(jar)
    jarName.startsWith("scala-library") || jarName.startsWith("scala3-library")
  }
}

/**
 * See this interface for comments better explaining the purpose of all the member variables:
 * https://github.com/sbt/zinc/blob/4eacff2a9bf5c8750bfc5096955065ce67f4e68a/internal/compiler-interface/src/main/java/xsbti/compile/ScalaInstance.java
 *
 * This is private to this package, so people use the static getAnnexScalaInstance to get an instance of this class. We
 * need to have only one of these per Scala version to prevent non-determinism. See the comment on that function for
 * more info.
 */
private[common] class AnnexScalaInstance(override val allJars: Array[File]) extends ScalaInstance {

  // Jars for the Scala compiler classes
  // We need to include the full classpath for the Scala 2 or Scala 3 compilers.
  // Thankfully that classpath doesn't seem to change very often.
  override val compilerJars: Array[File] = allJars.filter { jar =>
    AnnexScalaInstance.isScala2CompilerJar(jar) ||
    AnnexScalaInstance.isScala3CompilerJar(jar) ||
    AnnexScalaInstance.isScalaLibraryJar(jar)
  }

  // Jars for the Scala library classes
  override val libraryJars: Array[File] = allJars.filter(AnnexScalaInstance.isScalaLibraryJar)

  // All the jars that are not compiler or library jars
  override val otherJars: Array[File] = allJars.diff(compilerJars ++ libraryJars)

  // Loader for only the classes and resources in the library jars of this Scala instance
  override val loaderLibraryOnly: ClassLoader = AnnexScalaInstance.getClassLoader(libraryJars)

  // Loader for all the classes and resources in all the jars of this Scala instance
  override val loader: ClassLoader = AnnexScalaInstance.getClassLoader(allJars)

  // Loader for all the classes and resources in all the compiler jars of this
  // Scala instance
  override val loaderCompilerOnly: ClassLoader = AnnexScalaInstance.getClassLoader(compilerJars)

  // Version for this Scala instance
  override val actualVersion: String = {
    val stream = loaderCompilerOnly
      .getResourceAsStream("compiler.properties")

    if (stream == null) {
      throw new Exception(
        "The resource stream for the compiler.properties file in the compiler jar is null." +
          " Something went wrong getting the version in that file in the compiler jar. The jars" +
          s" which were searched are as follows: ${compilerJars.mkString}",
      )
    }

    try {
      val props = new Properties
      props.load(stream)
      props.getProperty("version.number")
    } finally stream.close()
  }
  override val version: String = actualVersion

}

/**
 * Load classes that start with xsbti or jline from the sbt class loader
 *   - xsbti is for the compiler bridge
 *   - jline for the sbt provided jline terminal
 * Load all other classes from the other loader Always get resources from the non-sbt loader
 *
 * This is necessary, so we can always make sure that the xsbti and jline classes needed for compilation and the repl
 * come from the same class loader.
 *
 * If the same class is loaded by two separate class loaders you get a linkage error in the JVM.
 *
 * If you set up a class loader that doesn't contain the xsbti and jline classes, then you get an error when you can't
 * find those classes because they are needed by Zinc and our ZincRunner.
 *
 * If you set up one class loader for all the things, then you start leaking any class loaded during any compilation
 * into any compilation happening in the JVM. Considering workers are long lived and handle multiple compilations, that
 * could become a problem.
 *
 * Dotty, Bloop, and Zinc all do very similar things. This approach was heavily inspired by their approaches. For more
 * details about their approaches see the following:
 * https://github.com/scala/scala3/blob/7d26a96db6e63aba8db1362b09f4adf521bc8327/sbt-dotty/src/dotty/tools/sbtplugin/DottyPlugin.scala#L620
 * https://github.com/scala/scala3/blob/cd8c5ed1dc5ee8528861e284490534064ba6d3e5/sbt-bridge/src/xsbt/CompilerClassLoader.java#L11
 * https://github.com/scalacenter/bloop/blob/c505385edf0bbd420e19401ae1beabae8895df8f/backend/src/main/scala/bloop/ScalaInstanceTopLoader.scala#L30
 * https://github.com/sbt/zinc/blob/4eacff2a9bf5c8750bfc5096955065ce67f4e68a/internal/zinc-classpath/src/main/scala/sbt/internal/inc/classpath/DualLoader.scala#L47
 */
class ScalaInstanceDualLoader(sbtClassLoader: ClassLoader, otherLoader: ClassLoader) extends ClassLoader(otherLoader) {
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
