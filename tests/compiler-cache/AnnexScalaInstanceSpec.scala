package higherkindness.rules_scala.workers.common

import com.google.devtools.build.lib.worker.WorkerProtocol
import com.google.protobuf.ByteString
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}
import java.util.HexFormat
import java.util.jar.{JarEntry, JarOutputStream}
import org.junit.Assert.assertThrows
import org.junit.Test
import scala.util.Using

class AnnexScalaInstanceSpec {
  private val compilerPath = Path.of("external/scala-compiler/scala3-compiler.jar")

  private def compilerJar(workDir: Path, version: String, path: Path = compilerPath): Path = {
    val jarPath = workDir.resolve(path)
    Files.createDirectories(jarPath.getParent)
    Using.resource(new JarOutputStream(Files.newOutputStream(jarPath))) { jar =>
      val entry = new JarEntry("compiler.properties")
      entry.setTime(0L)
      jar.putNextEntry(entry)
      jar.write(s"version.number=$version\n".getBytes(UTF_8))
      jar.closeEntry()
    }
    jarPath
  }

  private def input(path: Path, digest: ByteString): WorkerProtocol.Input =
    WorkerProtocol.Input.newBuilder().setPath(path.toString).setDigest(digest).build()

  private def workDir(): Path = Files.createTempDirectory(Path.of(sys.env("TEST_TMPDIR")), "compiler-cache-")

  @Test def stagesChangedCompilerAtSamePath(): Unit = {
    val firstWorkDir = workDir()
    val secondWorkDir = workDir()
    val repeatWorkDir = workDir()
    val firstJar = compilerJar(firstWorkDir, "first")
    val secondJar = compilerJar(secondWorkDir, "second")
    val repeatJar = compilerJar(repeatWorkDir, "second")
    val firstInput = input(compilerPath, ByteString.copyFromUtf8("first-digest"))
    val secondInput = input(compilerPath, ByteString.copyFromUtf8("second-digest"))

    val first = AnnexScalaInstance.getAnnexScalaInstance(
      Array(firstJar.toFile),
      firstWorkDir,
      inputs = Some(List(firstInput)),
    )
    val second = AnnexScalaInstance.getAnnexScalaInstance(
      Array(secondJar.toFile),
      secondWorkDir,
      inputs = Some(List(secondInput)),
    )
    val repeated = AnnexScalaInstance.getAnnexScalaInstance(
      Array(repeatJar.toFile),
      repeatWorkDir,
      inputs = Some(List(input(repeatJar, secondInput.getDigest))),
    )

    assert(first.actualVersion == "first")
    assert(second.actualVersion == "second")
    assert(first.compilerJars.head != second.compilerJars.head)
    assert(
      second.compilerJars.head.toPath.getParent.getFileName.toString ==
        HexFormat.of().formatHex(secondInput.getDigest.toByteArray),
    )
    assert(Files.mismatch(firstJar, first.compilerJars.head.toPath) == -1L)
    assert(Files.mismatch(secondJar, second.compilerJars.head.toPath) == -1L)
    assert(repeated eq second)
  }

  @Test def matchesAnUnmappedDigestToAMappedCompilerPath(): Unit = {
    val requestWorkDir = workDir()
    val mappedPath = Path.of("bazel-out/cfg/bin/scala3-compiler.jar")
    val jar = compilerJar(requestWorkDir, "mapped", mappedPath)
    val digest = ByteString.copyFromUtf8("mapped-digest")
    val unmapped = input(Path.of("bazel-out/k8-fastbuild/bin/scala3-compiler.jar"), digest)
    val instance = AnnexScalaInstance.getAnnexScalaInstance(Array(jar.toFile), requestWorkDir, Some(List(unmapped)))

    assert(instance.actualVersion == "mapped")
    assert(instance.compilerJars.head.toPath.getParent.getFileName.toString == HexFormat.of().formatHex(digest.toByteArray))

    val conflicting = input(Path.of("bazel-out/k8-opt/bin/scala3-compiler.jar"), ByteString.copyFromUtf8("other"))
    val failure = assertThrows(
      classOf[IllegalStateException],
      () => {
        AnnexScalaInstance.getAnnexScalaInstance(Array(jar.toFile), requestWorkDir, Some(List(unmapped, conflicting)))
        ()
      },
    )
    assert(failure.getMessage.contains(s"Ambiguous Bazel WorkRequest digests for compiler JAR: $mappedPath"))
  }

  @Test def rejectsMissingOrEmptyCompilerDigest(): Unit = {
    val requestWorkDir = workDir()
    val jar = compilerJar(requestWorkDir, "first")
    val inputLists = List(Nil, List(input(compilerPath, ByteString.EMPTY)))

    inputLists.foreach { inputs =>
      val failure = assertThrows(
        classOf[IllegalStateException],
        () => {
          AnnexScalaInstance.getAnnexScalaInstance(Array(jar.toFile), requestWorkDir, Some(inputs))
          ()
        },
      )
      assert(failure.getMessage.contains(compilerPath.toString))
    }
  }

  @Test def loadsCompilerWithoutADigestInStandaloneMode(): Unit = {
    val requestWorkDir = workDir()
    val jar = compilerJar(requestWorkDir, "standalone")
    val instance = AnnexScalaInstance.getAnnexScalaInstance(Array(jar.toFile), requestWorkDir, None)

    assert(instance.actualVersion == "standalone")
    assert(instance.compilerJars.head.toPath == jar)
  }
}
