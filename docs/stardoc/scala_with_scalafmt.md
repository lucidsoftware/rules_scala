<!-- Generated with Stardoc: http://skydoc.bazel.build -->

This extension contains copies of the `scala_binary`, `scala_library`, and `scala_test` rules from
`rules/scala.bzl` that provide formatting capabilities via Scalafmt. They're identical to the
afformentioned rules, but have two additional attributes:
- `config`
- `format`

Additionally, for every target created from one of the rules in this extension
(e.g. `//foo/bar:bizz`), you'll find two additional targets:
- `//foo/bar:bizz.format`
- `//foo/bar:bizz.format-test`

The former runs Scalafmt on the sources of the target, while the latter tests that those sources are
formatted.

<a id="scala_binary"></a>

## scala_binary

<pre>
load("@rules_scala_annex//rules:scala_with_scalafmt.bzl", "scala_binary")

scala_binary(<a href="#scala_binary-name">name</a>, <a href="#scala_binary-deps">deps</a>, <a href="#scala_binary-srcs">srcs</a>, <a href="#scala_binary-data">data</a>, <a href="#scala_binary-resources">resources</a>, <a href="#scala_binary-config">config</a>, <a href="#scala_binary-deps_unused_whitelist">deps_unused_whitelist</a>, <a href="#scala_binary-deps_used_whitelist">deps_used_whitelist</a>,
             <a href="#scala_binary-format">format</a>, <a href="#scala_binary-javacopts">javacopts</a>, <a href="#scala_binary-jvm_flags">jvm_flags</a>, <a href="#scala_binary-main_class">main_class</a>, <a href="#scala_binary-plugins">plugins</a>, <a href="#scala_binary-resource_jars">resource_jars</a>, <a href="#scala_binary-resource_strip_prefix">resource_strip_prefix</a>,
             <a href="#scala_binary-runtime_deps">runtime_deps</a>, <a href="#scala_binary-scala_toolchain_name">scala_toolchain_name</a>, <a href="#scala_binary-scalacopts">scalacopts</a>)
</pre>

Compiles and links a Scala JVM executable.

Produces the following implicit outputs:

  - `<name>_deploy.jar`: a single jar that contains all the necessary information to run the program
  - `<name>.jar`: a jar file that contains the class files produced from the sources
  - `<name>-bin`: the script that's used to run the program in conjunction with the generated runfiles

To run the program: `bazel run <target>`

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scala_binary-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scala_binary-deps"></a>deps |  The JVM library dependencies.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-srcs"></a>srcs |  The source Scala and Java files (and `-sources.jar` `.srcjar` `-src.jar` files of those).   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-data"></a>data |  The additional runtime files needed by this library.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-resources"></a>resources |  The files to include as classpath resources.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-config"></a>config |  The Scalafmt configuration file.   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@scalafmt_default//:config"`  |
| <a id="scala_binary-deps_unused_whitelist"></a>deps_unused_whitelist |  The JVM library dependencies to always consider unused for `scala_deps_direct` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-deps_used_whitelist"></a>deps_used_whitelist |  The JVM library dependencies to always consider used for `scala_deps_used` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-format"></a>format |  Whether to format the target. If this is False, the formatter and format tester will do nothing.   | Boolean | optional |  `True`  |
| <a id="scala_binary-javacopts"></a>javacopts |  The Javac options.   | List of strings | optional |  `[]`  |
| <a id="scala_binary-jvm_flags"></a>jvm_flags |  The JVM runtime flags.   | List of strings | optional |  `[]`  |
| <a id="scala_binary-main_class"></a>main_class |  The main class. If not provided, it will be inferred by its type signature.   | String | optional |  `""`  |
| <a id="scala_binary-plugins"></a>plugins |  The Scalac plugins.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-resource_jars"></a>resource_jars |  The JARs to merge into the output JAR.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-resource_strip_prefix"></a>resource_strip_prefix |  The path prefix to strip from classpath resources.   | String | optional |  `""`  |
| <a id="scala_binary-runtime_deps"></a>runtime_deps |  The JVM runtime-only library dependencies.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-scala_toolchain_name"></a>scala_toolchain_name |  The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)   | String | optional |  `""`  |
| <a id="scala_binary-scalacopts"></a>scalacopts |  The Scalac options.   | List of strings | optional |  `[]`  |


<a id="scala_library"></a>

## scala_library

<pre>
load("@rules_scala_annex//rules:scala_with_scalafmt.bzl", "scala_library")

scala_library(<a href="#scala_library-name">name</a>, <a href="#scala_library-deps">deps</a>, <a href="#scala_library-srcs">srcs</a>, <a href="#scala_library-data">data</a>, <a href="#scala_library-resources">resources</a>, <a href="#scala_library-config">config</a>, <a href="#scala_library-deps_unused_whitelist">deps_unused_whitelist</a>, <a href="#scala_library-deps_used_whitelist">deps_used_whitelist</a>,
              <a href="#scala_library-exports">exports</a>, <a href="#scala_library-format">format</a>, <a href="#scala_library-javacopts">javacopts</a>, <a href="#scala_library-macro">macro</a>, <a href="#scala_library-neverlink">neverlink</a>, <a href="#scala_library-plugins">plugins</a>, <a href="#scala_library-resource_jars">resource_jars</a>,
              <a href="#scala_library-resource_strip_prefix">resource_strip_prefix</a>, <a href="#scala_library-runtime_deps">runtime_deps</a>, <a href="#scala_library-scala_toolchain_name">scala_toolchain_name</a>, <a href="#scala_library-scalacopts">scalacopts</a>)
</pre>

Compiles a Scala JVM library.

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scala_library-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scala_library-deps"></a>deps |  The JVM library dependencies.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-srcs"></a>srcs |  The source Scala and Java files (and `-sources.jar` `.srcjar` `-src.jar` files of those).   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-data"></a>data |  The additional runtime files needed by this library.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-resources"></a>resources |  The files to include as classpath resources.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-config"></a>config |  The Scalafmt configuration file.   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@scalafmt_default//:config"`  |
| <a id="scala_library-deps_unused_whitelist"></a>deps_unused_whitelist |  The JVM library dependencies to always consider unused for `scala_deps_direct` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-deps_used_whitelist"></a>deps_used_whitelist |  The JVM library dependencies to always consider used for `scala_deps_used` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-exports"></a>exports |  The JVM libraries to add as dependencies to any libraries dependent on this one.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-format"></a>format |  Whether to format the target. If this is False, the formatter and format tester will do nothing.   | Boolean | optional |  `True`  |
| <a id="scala_library-javacopts"></a>javacopts |  The Javac options.   | List of strings | optional |  `[]`  |
| <a id="scala_library-macro"></a>macro |  Whether this library provides macros.   | Boolean | optional |  `False`  |
| <a id="scala_library-neverlink"></a>neverlink |  Whether this library should be excluded at runtime.   | Boolean | optional |  `False`  |
| <a id="scala_library-plugins"></a>plugins |  The Scalac plugins.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-resource_jars"></a>resource_jars |  The JARs to merge into the output JAR.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-resource_strip_prefix"></a>resource_strip_prefix |  The path prefix to strip from classpath resources.   | String | optional |  `""`  |
| <a id="scala_library-runtime_deps"></a>runtime_deps |  The JVM runtime-only library dependencies.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-scala_toolchain_name"></a>scala_toolchain_name |  The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)   | String | optional |  `""`  |
| <a id="scala_library-scalacopts"></a>scalacopts |  The Scalac options.   | List of strings | optional |  `[]`  |


<a id="scala_test"></a>

## scala_test

<pre>
load("@rules_scala_annex//rules:scala_with_scalafmt.bzl", "scala_test")

scala_test(<a href="#scala_test-name">name</a>, <a href="#scala_test-deps">deps</a>, <a href="#scala_test-srcs">srcs</a>, <a href="#scala_test-data">data</a>, <a href="#scala_test-resources">resources</a>, <a href="#scala_test-config">config</a>, <a href="#scala_test-deps_unused_whitelist">deps_unused_whitelist</a>, <a href="#scala_test-deps_used_whitelist">deps_used_whitelist</a>,
           <a href="#scala_test-format">format</a>, <a href="#scala_test-frameworks">frameworks</a>, <a href="#scala_test-isolation">isolation</a>, <a href="#scala_test-javacopts">javacopts</a>, <a href="#scala_test-jvm_flags">jvm_flags</a>, <a href="#scala_test-plugins">plugins</a>, <a href="#scala_test-resource_jars">resource_jars</a>,
           <a href="#scala_test-resource_strip_prefix">resource_strip_prefix</a>, <a href="#scala_test-runner">runner</a>, <a href="#scala_test-runtime_deps">runtime_deps</a>, <a href="#scala_test-scala_toolchain_name">scala_toolchain_name</a>, <a href="#scala_test-scalacopts">scalacopts</a>, <a href="#scala_test-shared_deps">shared_deps</a>,
           <a href="#scala_test-subprocess_runner">subprocess_runner</a>)
</pre>

Compiles and links a collection of Scala tests.

To buid and run all tests: `bazel test <target>`

To build and run a specific test: `bazel test <target> --test_filter=<filter_expression>`
<br>(Note: the syntax of the `<filter_expression>` varies by test framework, and not all test frameworks support the `test_filter` option at this time.)

[More Info](/docs/scala.md#tests)

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scala_test-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scala_test-deps"></a>deps |  The JVM library dependencies.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-srcs"></a>srcs |  The source Scala and Java files (and `-sources.jar` `.srcjar` `-src.jar` files of those).   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-data"></a>data |  The additional runtime files needed by this library.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-resources"></a>resources |  The files to include as classpath resources.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-config"></a>config |  The Scalafmt configuration file.   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@scalafmt_default//:config"`  |
| <a id="scala_test-deps_unused_whitelist"></a>deps_unused_whitelist |  The JVM library dependencies to always consider unused for `scala_deps_direct` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-deps_used_whitelist"></a>deps_used_whitelist |  The JVM library dependencies to always consider used for `scala_deps_used` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-format"></a>format |  Whether to format the target. If this is False, the formatter and format tester will do nothing.   | Boolean | optional |  `True`  |
| <a id="scala_test-frameworks"></a>frameworks |  The list of test frameworks to check for. These should conform to the sbt test interface (https://github.com/sbt/test-interface).   | List of strings | optional |  `["org.scalatest.tools.Framework", "org.scalacheck.ScalaCheckFramework", "org.specs2.runner.Specs2Framework", "minitest.runner.Framework", "utest.runner.Framework", "com.novocode.junit.JUnitFramework"]`  |
| <a id="scala_test-isolation"></a>isolation |  The isolation level to apply   | String | optional |  `"none"`  |
| <a id="scala_test-javacopts"></a>javacopts |  The Javac options.   | List of strings | optional |  `[]`  |
| <a id="scala_test-jvm_flags"></a>jvm_flags |  The JVM runtime flags.   | List of strings | optional |  `[]`  |
| <a id="scala_test-plugins"></a>plugins |  The Scalac plugins.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-resource_jars"></a>resource_jars |  The JARs to merge into the output JAR.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-resource_strip_prefix"></a>resource_strip_prefix |  The path prefix to strip from classpath resources.   | String | optional |  `""`  |
| <a id="scala_test-runner"></a>runner |  -   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@rules_scala_annex//src/main/scala/higherkindness/rules_scala/workers/zinc/test"`  |
| <a id="scala_test-runtime_deps"></a>runtime_deps |  The JVM runtime-only library dependencies.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-scala_toolchain_name"></a>scala_toolchain_name |  The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)   | String | optional |  `""`  |
| <a id="scala_test-scalacopts"></a>scalacopts |  Options to pass to scalac.   | List of strings | optional |  `[]`  |
| <a id="scala_test-shared_deps"></a>shared_deps |  If isolation is "classloader", the list of deps to keep loaded between tests   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-subprocess_runner"></a>subprocess_runner |  -   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@rules_scala_annex//src/main/scala/higherkindness/rules_scala/common/sbt-testing:subprocess"`  |


