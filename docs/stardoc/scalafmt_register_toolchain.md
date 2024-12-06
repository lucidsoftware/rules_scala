<!-- Generated with Stardoc: http://skydoc.bazel.build -->



<a id="register_scalafmt_toolchain"></a>

## register_scalafmt_toolchain

<pre>
load("@rules_scala_annex//rules/scalafmt:register_toolchain.bzl", "register_scalafmt_toolchain")

register_scalafmt_toolchain(<a href="#register_scalafmt_toolchain-name">name</a>, <a href="#register_scalafmt_toolchain-config">config</a>, <a href="#register_scalafmt_toolchain-visibility">visibility</a>)
</pre>

Declares a Scalafmt toolchain that can be used with the rules in `@rules_scala_annex//rules:scala_with_scalafmt.bzl` or `@rules_scala_annex//rules:scalafmt.bzl`.

See [scalafmt.md](../scalafmt.md) for more information.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="register_scalafmt_toolchain-name"></a>name |  The name of the toolchain.   |  none |
| <a id="register_scalafmt_toolchain-config"></a>config |  The Scalafmt configuration file.   |  none |
| <a id="register_scalafmt_toolchain-visibility"></a>visibility |  The visibility of the toolchain.   |  `["//visibility:public"]` |


