<!-- Generated with Stardoc: http://skydoc.bazel.build -->



<a id="scala_artifacts"></a>

## scala_artifacts

<pre>
load("@//rules/scala:workspace.bzl", "scala_artifacts")

scala_artifacts()
</pre>





<a id="scala_register_toolchains"></a>

## scala_register_toolchains

<pre>
load("@//rules/scala:workspace.bzl", "scala_register_toolchains")

scala_register_toolchains(<a href="#scala_register_toolchains-default_scala_toolchain_name">default_scala_toolchain_name</a>, <a href="#scala_register_toolchains-toolchains">toolchains</a>)
</pre>

Registers the provided Scala toolchains with Bazel and sets a default one to use.

**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="scala_register_toolchains-default_scala_toolchain_name"></a>default_scala_toolchain_name |  The name of the default Scala toolchain to use.   |  none |
| <a id="scala_register_toolchains-toolchains"></a>toolchains |  The toolchains to register.   |  `[]` |


<a id="scala_repositories"></a>

## scala_repositories

<pre>
load("@//rules/scala:workspace.bzl", "scala_repositories")

scala_repositories(<a href="#scala_repositories-java_launcher_version">java_launcher_version</a>, <a href="#scala_repositories-java_launcher_template_sha">java_launcher_template_sha</a>)
</pre>



**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="scala_repositories-java_launcher_version"></a>java_launcher_version |  <p align="center"> - </p>   |  `"7.4.1"` |
| <a id="scala_repositories-java_launcher_template_sha"></a>java_launcher_template_sha |  <p align="center"> - </p>   |  `"ee4aa47ae5e639632c67be5cc0ccbc4e941a67a1b884a1ce0c4329357a4b62b2"` |


