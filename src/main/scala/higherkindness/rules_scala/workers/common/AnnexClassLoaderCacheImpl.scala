package higherkindness.rules_scala.workers.common

import java.io.File
import java.net.URLClassLoader
import sbt.internal.inc.classpath.AbstractClassLoaderCache
import scala.collection.concurrent

/**
 * [[AnnexClassLoaderCacheImpl]] is mostly identical to [[sbt.internal.inc.classpath.ClassLoaderCacheImpl]], with a few
 * exceptions:
 *   - It doesn't check the modification time of the compiler classpath files. This is because within the context of
 *     [[higherkindness.rules_scala.workers.zinc.compile.ZincRunner]], file paths being equal implies the contents of
 *     those files are equal and checking the modification time is unnecessary overhead.
 *   - It doesn't use [[java.lang.ref.SoftReference]]s, to minimize the reloading of the compiler
 *   - It doesn't do any locking
 */
class AnnexClassLoaderCacheImpl(override val commonParent: ClassLoader) extends AbstractClassLoaderCache {
  private val delegate = new concurrent.TrieMap[List[File], ClassLoader]()

  override def apply(files: List[File]): ClassLoader =
    cachedCustomClassloader(files, () => new URLClassLoader(files.map(_.toURI.toURL).toArray, commonParent))

  override def cachedCustomClassloader(files: List[File], makeClassLoader: () => ClassLoader): ClassLoader =
    delegate.getOrElseUpdate(files, makeClassLoader())

  override def close(): Unit = {
    delegate.values.foreach {
      case classLoader: AutoCloseable => classLoader.close()
      case _                          =>
    }

    delegate.clear()
  }
}
