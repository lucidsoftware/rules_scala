package higherkindness.rules_scala.common.sandbox

import java.nio.file.Paths
import org.scalatest.flatspec.AnyFlatSpec

class PathResolverSpec extends AnyFlatSpec {
  behavior of "PathResolver.forPersistentWorker"

  it should "resolve a relative path against the work directory" in {
    val resolver = PathResolver.forPersistentWorker(Paths.get("/tmp/sandbox/worker-1"))

    assert(
      resolver.resolve(Paths.get("external/foo/bar.jar")) == Paths.get("/tmp/sandbox/worker-1/external/foo/bar.jar"),
    )
  }

  it should "leave an absolute path unchanged" in {
    val resolver = PathResolver.forPersistentWorker(Paths.get("/tmp/sandbox/worker-1"))

    assert(resolver.resolve(Paths.get("/etc/hosts")) == Paths.get("/etc/hosts"))
  }

  it should "be a no-op when the work directory is empty (non-multiplex sandboxed worker)" in {
    val resolver = PathResolver.forPersistentWorker(Paths.get(""))

    assert(resolver.resolve(Paths.get("relative/path.jar")) == Paths.get("relative/path.jar"))
  }

  behavior of "PathResolver.forBinaryRunner"

  it should "resolve against the provided working directory instead of `user.dir`" in {
    val resolver = PathResolver.forBinaryRunner(
      workingDirectory = Some("/exec/root"),
      runPath = "bazel-out/runfiles/_main",
    )

    assert(
      resolver.resolve(Paths.get("external/specs2/specs2.jar")) ==
        Paths.get("/exec/root/bazel-out/runfiles/_main/external/specs2/specs2.jar"),
    )
  }

  it should "normalize traversals so they happen in the logical tree, not the physical one" in {
    val resolver = PathResolver.forBinaryRunner(
      workingDirectory = Some("/exec/root"),
      runPath = "bin/test.runfiles/_main",
    )

    assert(
      resolver.resolve(Paths.get("../rules_jvm_external++maven+mvn/org/specs2/specs2-core.jar")) ==
        Paths.get("/exec/root/bin/test.runfiles/rules_jvm_external++maven+mvn/org/specs2/specs2-core.jar"),
    )
  }

  it should "fall back to the JVM's working directory when PWD is unset" in {
    val resolver = PathResolver.forBinaryRunner(workingDirectory = None, runPath = "runfiles/_main")

    assert(
      resolver.resolve(Paths.get("foo.jar")) ==
        Paths.get("").toAbsolutePath.resolve("runfiles/_main/foo.jar").normalize(),
    )
  }

  it should "leave an absolute path unchanged" in {
    val resolver = PathResolver.forBinaryRunner(workingDirectory = Some("/exec/root"), runPath = "runfiles/_main")

    assert(resolver.resolve(Paths.get("/etc/hosts")) == Paths.get("/etc/hosts"))
  }
}
