<!-- Generated with Stardoc: http://skydoc.bazel.build -->



<a id="scala_format_test"></a>

## scala_format_test

<pre>
load("@rules_scala_annex//rules:scalafmt.bzl", "scala_format_test")

scala_format_test(<a href="#scala_format_test-name">name</a>, <a href="#scala_format_test-srcs">srcs</a>, <a href="#scala_format_test-scala_version">scala_version</a>)
</pre>



**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scala_format_test-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scala_format_test-srcs"></a>srcs |  The Scala files.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_format_test-scala_version"></a>scala_version |  The Scala version to use, e.g., '3', '2.13', '3.3.7', or '<prefix>_<version>' for prefixed versions.   | String | optional |  `""`  |


