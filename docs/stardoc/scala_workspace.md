<!-- Generated with Stardoc: http://skydoc.bazel.build -->



<a id="scala_register_toolchains"></a>

## scala_register_toolchains

<pre>
load("@rules_scala_annex//rules/scala:workspace.bzl", "scala_register_toolchains")

scala_register_toolchains(<a href="#scala_register_toolchains-default_scala_toolchain_name">default_scala_toolchain_name</a>, <a href="#scala_register_toolchains-toolchains">toolchains</a>)
</pre>

Registers the provided Scala toolchains with Bazel and sets a default one to use.

**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="scala_register_toolchains-default_scala_toolchain_name"></a>default_scala_toolchain_name |  The name of the default Scala toolchain to use.   |  none |
| <a id="scala_register_toolchains-toolchains"></a>toolchains |  The toolchains to register.   |  `[]` |


