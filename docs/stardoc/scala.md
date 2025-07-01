<!-- Generated with Stardoc: http://skydoc.bazel.build -->



<a id="scala_binary"></a>

## scala_binary

<pre>
load("@rules_scala_annex//rules:scala.bzl", "scala_binary")

scala_binary(<a href="#scala_binary-name">name</a>, <a href="#scala_binary-deps">deps</a>, <a href="#scala_binary-srcs">srcs</a>, <a href="#scala_binary-data">data</a>, <a href="#scala_binary-resources">resources</a>, <a href="#scala_binary-deps_unused_whitelist">deps_unused_whitelist</a>, <a href="#scala_binary-deps_used_whitelist">deps_used_whitelist</a>,
             <a href="#scala_binary-javacopts">javacopts</a>, <a href="#scala_binary-jvm_flags">jvm_flags</a>, <a href="#scala_binary-main_class">main_class</a>, <a href="#scala_binary-plugins">plugins</a>, <a href="#scala_binary-resource_jars">resource_jars</a>, <a href="#scala_binary-resource_strip_prefix">resource_strip_prefix</a>,
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
| <a id="scala_binary-deps_unused_whitelist"></a>deps_unused_whitelist |  The JVM library dependencies to always consider unused for `scala_deps_direct` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-deps_used_whitelist"></a>deps_used_whitelist |  The JVM library dependencies to always consider used for `scala_deps_used` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-javacopts"></a>javacopts |  The Javac options.   | List of strings | optional |  `[]`  |
| <a id="scala_binary-jvm_flags"></a>jvm_flags |  The JVM runtime flags.   | List of strings | optional |  `[]`  |
| <a id="scala_binary-main_class"></a>main_class |  The main class. If not provided, it will be inferred by its type signature.   | String | optional |  `""`  |
| <a id="scala_binary-plugins"></a>plugins |  The Scalac plugins.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-resource_jars"></a>resource_jars |  The JARs to merge into the output JAR.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-resource_strip_prefix"></a>resource_strip_prefix |  The path prefix to strip from classpath resources.   | String | optional |  `""`  |
| <a id="scala_binary-runtime_deps"></a>runtime_deps |  The JVM runtime-only library dependencies.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_binary-scala_toolchain_name"></a>scala_toolchain_name |  The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)   | String | optional |  `""`  |
| <a id="scala_binary-scalacopts"></a>scalacopts |  The Scalac options.   | List of strings | optional |  `[]`  |


<a id="scala_import"></a>

## scala_import

<pre>
load("@rules_scala_annex//rules:scala.bzl", "scala_import")

scala_import(<a href="#scala_import-name">name</a>, <a href="#scala_import-deps">deps</a>, <a href="#scala_import-exports">exports</a>, <a href="#scala_import-jars">jars</a>, <a href="#scala_import-neverlink">neverlink</a>, <a href="#scala_import-runtime_deps">runtime_deps</a>, <a href="#scala_import-srcjar">srcjar</a>)
</pre>

Creates a Scala JVM library.

Use this only for libraries with macros. Otherwise, use `java_import`.

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scala_import-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scala_import-deps"></a>deps |  Libraries used by this one.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_import-exports"></a>exports |  Libraries made available by this one. See https://bazel.build/versions/6.0.0/reference/be/java#java_library.exports.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_import-jars"></a>jars |  JAR files to include in this library.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_import-neverlink"></a>neverlink |  Set this to True to exclude this library from the runtime classpath (i.e. if it should only be used at compile-time).   | Boolean | optional |  `False`  |
| <a id="scala_import-runtime_deps"></a>runtime_deps |  Libraries used by this one, but which aren't referenced explicitly and need only be available at runtime.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_import-srcjar"></a>srcjar |  The source JAR for this library.   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `None`  |


<a id="scala_library"></a>

## scala_library

<pre>
load("@rules_scala_annex//rules:scala.bzl", "scala_library")

scala_library(<a href="#scala_library-name">name</a>, <a href="#scala_library-deps">deps</a>, <a href="#scala_library-srcs">srcs</a>, <a href="#scala_library-data">data</a>, <a href="#scala_library-resources">resources</a>, <a href="#scala_library-deps_unused_whitelist">deps_unused_whitelist</a>, <a href="#scala_library-deps_used_whitelist">deps_used_whitelist</a>,
              <a href="#scala_library-exports">exports</a>, <a href="#scala_library-javacopts">javacopts</a>, <a href="#scala_library-macro">macro</a>, <a href="#scala_library-neverlink">neverlink</a>, <a href="#scala_library-plugins">plugins</a>, <a href="#scala_library-resource_jars">resource_jars</a>, <a href="#scala_library-resource_strip_prefix">resource_strip_prefix</a>,
              <a href="#scala_library-runtime_deps">runtime_deps</a>, <a href="#scala_library-scala_toolchain_name">scala_toolchain_name</a>, <a href="#scala_library-scalacopts">scalacopts</a>)
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
| <a id="scala_library-deps_unused_whitelist"></a>deps_unused_whitelist |  The JVM library dependencies to always consider unused for `scala_deps_direct` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-deps_used_whitelist"></a>deps_used_whitelist |  The JVM library dependencies to always consider used for `scala_deps_used` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-exports"></a>exports |  The JVM libraries to add as dependencies to any libraries dependent on this one.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-javacopts"></a>javacopts |  The Javac options.   | List of strings | optional |  `[]`  |
| <a id="scala_library-macro"></a>macro |  Whether this library provides macros.   | Boolean | optional |  `False`  |
| <a id="scala_library-neverlink"></a>neverlink |  Whether this library should be excluded at runtime.   | Boolean | optional |  `False`  |
| <a id="scala_library-plugins"></a>plugins |  The Scalac plugins.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-resource_jars"></a>resource_jars |  The JARs to merge into the output JAR.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-resource_strip_prefix"></a>resource_strip_prefix |  The path prefix to strip from classpath resources.   | String | optional |  `""`  |
| <a id="scala_library-runtime_deps"></a>runtime_deps |  The JVM runtime-only library dependencies.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_library-scala_toolchain_name"></a>scala_toolchain_name |  The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)   | String | optional |  `""`  |
| <a id="scala_library-scalacopts"></a>scalacopts |  The Scalac options.   | List of strings | optional |  `[]`  |


<a id="scala_repl"></a>

## scala_repl

<pre>
load("@rules_scala_annex//rules:scala.bzl", "scala_repl")

scala_repl(<a href="#scala_repl-name">name</a>, <a href="#scala_repl-deps">deps</a>, <a href="#scala_repl-data">data</a>, <a href="#scala_repl-jvm_flags">jvm_flags</a>, <a href="#scala_repl-scala_toolchain_name">scala_toolchain_name</a>, <a href="#scala_repl-scalacopts">scalacopts</a>)
</pre>

Launches a REPL with all given dependencies available.

To run: `bazel run <target>`

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scala_repl-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scala_repl-deps"></a>deps |  Dependencies that should be made available to the REPL.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_repl-data"></a>data |  The additional runtime files needed by this REPL.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_repl-jvm_flags"></a>jvm_flags |  The JVM runtime flags.   | List of strings | optional |  `[]`  |
| <a id="scala_repl-scala_toolchain_name"></a>scala_toolchain_name |  The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)   | String | optional |  `""`  |
| <a id="scala_repl-scalacopts"></a>scalacopts |  Options to pass to scalac.   | List of strings | optional |  `[]`  |


<a id="scala_test"></a>

## scala_test

<pre>
load("@rules_scala_annex//rules:scala.bzl", "scala_test")

scala_test(<a href="#scala_test-name">name</a>, <a href="#scala_test-deps">deps</a>, <a href="#scala_test-srcs">srcs</a>, <a href="#scala_test-data">data</a>, <a href="#scala_test-resources">resources</a>, <a href="#scala_test-deps_unused_whitelist">deps_unused_whitelist</a>, <a href="#scala_test-deps_used_whitelist">deps_used_whitelist</a>,
           <a href="#scala_test-frameworks">frameworks</a>, <a href="#scala_test-isolation">isolation</a>, <a href="#scala_test-javacopts">javacopts</a>, <a href="#scala_test-jvm_flags">jvm_flags</a>, <a href="#scala_test-plugins">plugins</a>, <a href="#scala_test-resource_jars">resource_jars</a>, <a href="#scala_test-resource_strip_prefix">resource_strip_prefix</a>,
           <a href="#scala_test-runner">runner</a>, <a href="#scala_test-runtime_deps">runtime_deps</a>, <a href="#scala_test-scala_toolchain_name">scala_toolchain_name</a>, <a href="#scala_test-scalacopts">scalacopts</a>, <a href="#scala_test-shared_deps">shared_deps</a>, <a href="#scala_test-subprocess_runner">subprocess_runner</a>)
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
| <a id="scala_test-deps_unused_whitelist"></a>deps_unused_whitelist |  The JVM library dependencies to always consider unused for `scala_deps_direct` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-deps_used_whitelist"></a>deps_used_whitelist |  The JVM library dependencies to always consider used for `scala_deps_used` checks.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
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


<a id="scaladoc"></a>

## scaladoc

<pre>
load("@rules_scala_annex//rules:scala.bzl", "scaladoc")

scaladoc(<a href="#scaladoc-name">name</a>, <a href="#scaladoc-deps">deps</a>, <a href="#scaladoc-srcs">srcs</a>, <a href="#scaladoc-compiler_deps">compiler_deps</a>, <a href="#scaladoc-scala_toolchain_name">scala_toolchain_name</a>, <a href="#scaladoc-scalacopts">scalacopts</a>, <a href="#scaladoc-title">title</a>)
</pre>

Generates Scaladoc.

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scaladoc-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scaladoc-deps"></a>deps |  Dependencies that should be made available to the Scaladoc tool. These may include libraries referenced in Scaladoc or public signatures.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scaladoc-srcs"></a>srcs |  Sources from which to generate Scaladoc. These may include `*.java` files, `*.scala` files, and source JARs.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scaladoc-compiler_deps"></a>compiler_deps |  JVM targets that should be included on the compile classpath.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scaladoc-scala_toolchain_name"></a>scala_toolchain_name |  The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)   | String | optional |  `""`  |
| <a id="scaladoc-scalacopts"></a>scalacopts |  Options to pass to scalac.   | List of strings | optional |  `[]`  |
| <a id="scaladoc-title"></a>title |  The name of the project. If none is provided, the target label will be used.   | String | optional |  `""`  |


<a id="make_scala_binary"></a>

## make_scala_binary

<pre>
load("@rules_scala_annex//rules:scala.bzl", "make_scala_binary")

make_scala_binary(<a href="#make_scala_binary-extras">*extras</a>)
</pre>



**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="make_scala_binary-extras"></a>extras |  <p align="center"> - </p>   |  none |


<a id="make_scala_library"></a>

## make_scala_library

<pre>
load("@rules_scala_annex//rules:scala.bzl", "make_scala_library")

make_scala_library(<a href="#make_scala_library-extras">*extras</a>)
</pre>



**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="make_scala_library-extras"></a>extras |  <p align="center"> - </p>   |  none |


<a id="make_scala_test"></a>

## make_scala_test

<pre>
load("@rules_scala_annex//rules:scala.bzl", "make_scala_test")

make_scala_test(<a href="#make_scala_test-extras">*extras</a>)
</pre>



**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="make_scala_test-extras"></a>extras |  <p align="center"> - </p>   |  none |


