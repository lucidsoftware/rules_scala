<!-- Generated with Stardoc: http://skydoc.bazel.build -->



<a id="register_scalafmt_toolchain"></a>

## register_scalafmt_toolchain

<pre>
load("@rules_scala_annex//rules/scalafmt:register_toolchain.bzl", "register_scalafmt_toolchain")

register_scalafmt_toolchain(<a href="#register_scalafmt_toolchain-name">name</a>, <a href="#register_scalafmt_toolchain-config">config</a>, <a href="#register_scalafmt_toolchain-scala_versions">scala_versions</a>, <a href="#register_scalafmt_toolchain-jvm_flags">jvm_flags</a>, <a href="#register_scalafmt_toolchain-visibility">visibility</a>)
</pre>

Declares a Scalafmt toolchain that matches targets by Scala version.

See [scalafmt.md](../../docs/scalafmt.md) for more information.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="register_scalafmt_toolchain-name"></a>name |  The name of the toolchain.   |  none |
| <a id="register_scalafmt_toolchain-config"></a>config |  The Scalafmt configuration file.   |  none |
| <a id="register_scalafmt_toolchain-scala_versions"></a>scala_versions |  A list of Scala version strings this toolchain is compatible with. Use the same full version constants as your Zinc toolchains, e.g., "2.13.16", "3.3.7", For prefixed variants, concatenate the prefix, e.g., "semanticdb_2.13.16".   |  none |
| <a id="register_scalafmt_toolchain-jvm_flags"></a>jvm_flags |  JVM options to pass when invoking Scalafmt.   |  `[]` |
| <a id="register_scalafmt_toolchain-visibility"></a>visibility |  The visibility of the toolchain.   |  `["//visibility:public"]` |


