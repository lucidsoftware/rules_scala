<!-- Generated with Stardoc: http://skydoc.bazel.build -->



<a id="configure_bootstrap_scala"></a>

## configure_bootstrap_scala

<pre>
load("@//rules:scala.bzl", "configure_bootstrap_scala")

configure_bootstrap_scala(<a href="#configure_bootstrap_scala-name">name</a>, <a href="#configure_bootstrap_scala-compiler_classpath">compiler_classpath</a>, <a href="#configure_bootstrap_scala-global_plugins">global_plugins</a>, <a href="#configure_bootstrap_scala-global_scalacopts">global_scalacopts</a>,
                          <a href="#configure_bootstrap_scala-runtime_classpath">runtime_classpath</a>, <a href="#configure_bootstrap_scala-semanticdb_bundle">semanticdb_bundle</a>, <a href="#configure_bootstrap_scala-use_ijar">use_ijar</a>, <a href="#configure_bootstrap_scala-version">version</a>)
</pre>

Configures a Scala compiler that's used for compiling the Scala targets used by `configure_zinc_scala`. You probably want `configure_zinc_scala` instead (even if you're not using incremental compilation), as this rule doesn't support features like dependency checking and compilation workers.

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="configure_bootstrap_scala-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="configure_bootstrap_scala-compiler_classpath"></a>compiler_classpath |  JVM targets that will always be on the compiler classpath. Usually, this is the compiler itself and the standard library.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | required |  |
| <a id="configure_bootstrap_scala-global_plugins"></a>global_plugins |  scalac plugins that will always be enabled.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="configure_bootstrap_scala-global_scalacopts"></a>global_scalacopts |  scalac options that will always be enabled.   | List of strings | optional |  `[]`  |
| <a id="configure_bootstrap_scala-runtime_classpath"></a>runtime_classpath |  JVM targets that will always be on the runtime classpath. Usually, this is the standard library.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | required |  |
| <a id="configure_bootstrap_scala-semanticdb_bundle"></a>semanticdb_bundle |  Whether to bundle SemanticDB files in the resulting JAR. Note that in Scala 2, this requires the SemanticDB compiler plugin.   | Boolean | optional |  `True`  |
| <a id="configure_bootstrap_scala-use_ijar"></a>use_ijar |  Whether to use ijar for this compiler. See https://github.com/bazelbuild/bazel/blob/master/third_party/ijar/README.txt for more information.   | Boolean | optional |  `True`  |
| <a id="configure_bootstrap_scala-version"></a>version |  The Scala version this compiler corresponds to.   | String | required |  |


<a id="scala_binary"></a>

## scala_binary

<pre>
load("@//rules:scala.bzl", "scala_binary")

scala_binary(<a href="#scala_binary-name">name</a>, <a href="#scala_binary-deps">deps</a>, <a href="#scala_binary-srcs">srcs</a>, <a href="#scala_binary-data">data</a>, <a href="#scala_binary-resources">resources</a>, <a href="#scala_binary-deps_unused_whitelist">deps_unused_whitelist</a>, <a href="#scala_binary-deps_used_whitelist">deps_used_whitelist</a>,
             <a href="#scala_binary-javacopts">javacopts</a>, <a href="#scala_binary-jvm_flags">jvm_flags</a>, <a href="#scala_binary-main_class">main_class</a>, <a href="#scala_binary-plugins">plugins</a>, <a href="#scala_binary-resource_jars">resource_jars</a>, <a href="#scala_binary-resource_strip_prefix">resource_strip_prefix</a>,
             <a href="#scala_binary-runtime_deps">runtime_deps</a>, <a href="#scala_binary-scala">scala</a>, <a href="#scala_binary-scalacopts">scalacopts</a>)
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
| <a id="scala_binary-scala"></a>scala |  The Scala compiler to use (a `configure_bootstrap_scala` or `configure_zinc_scala` target). Defaults to the `default_scala` target specified in the WORKSPACE file.   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@//external:default_scala"`  |
| <a id="scala_binary-scalacopts"></a>scalacopts |  The Scalac options.   | List of strings | optional |  `[]`  |


<a id="scala_import"></a>

## scala_import

<pre>
load("@//rules:scala.bzl", "scala_import")

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
load("@//rules:scala.bzl", "scala_library")

scala_library(<a href="#scala_library-name">name</a>, <a href="#scala_library-deps">deps</a>, <a href="#scala_library-srcs">srcs</a>, <a href="#scala_library-data">data</a>, <a href="#scala_library-resources">resources</a>, <a href="#scala_library-deps_unused_whitelist">deps_unused_whitelist</a>, <a href="#scala_library-deps_used_whitelist">deps_used_whitelist</a>,
              <a href="#scala_library-exports">exports</a>, <a href="#scala_library-javacopts">javacopts</a>, <a href="#scala_library-macro">macro</a>, <a href="#scala_library-neverlink">neverlink</a>, <a href="#scala_library-plugins">plugins</a>, <a href="#scala_library-resource_jars">resource_jars</a>, <a href="#scala_library-resource_strip_prefix">resource_strip_prefix</a>,
              <a href="#scala_library-runtime_deps">runtime_deps</a>, <a href="#scala_library-scala">scala</a>, <a href="#scala_library-scalacopts">scalacopts</a>)
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
| <a id="scala_library-scala"></a>scala |  The Scala compiler to use (a `configure_bootstrap_scala` or `configure_zinc_scala` target). Defaults to the `default_scala` target specified in the WORKSPACE file.   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@//external:default_scala"`  |
| <a id="scala_library-scalacopts"></a>scalacopts |  The Scalac options.   | List of strings | optional |  `[]`  |


<a id="scala_repl"></a>

## scala_repl

<pre>
load("@//rules:scala.bzl", "scala_repl")

scala_repl(<a href="#scala_repl-name">name</a>, <a href="#scala_repl-deps">deps</a>, <a href="#scala_repl-data">data</a>, <a href="#scala_repl-jvm_flags">jvm_flags</a>, <a href="#scala_repl-scala">scala</a>, <a href="#scala_repl-scalacopts">scalacopts</a>)
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
| <a id="scala_repl-scala"></a>scala |  The Scala compiler to use (a `configure_bootstrap_scala` or `configure_zinc_scala` target). Defaults to the `default_scala` target specified in the WORKSPACE file.   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@//external:default_scala"`  |
| <a id="scala_repl-scalacopts"></a>scalacopts |  Options to pass to scalac.   | List of strings | optional |  `[]`  |


<a id="scala_test"></a>

## scala_test

<pre>
load("@//rules:scala.bzl", "scala_test")

scala_test(<a href="#scala_test-name">name</a>, <a href="#scala_test-deps">deps</a>, <a href="#scala_test-srcs">srcs</a>, <a href="#scala_test-data">data</a>, <a href="#scala_test-resources">resources</a>, <a href="#scala_test-deps_unused_whitelist">deps_unused_whitelist</a>, <a href="#scala_test-deps_used_whitelist">deps_used_whitelist</a>,
           <a href="#scala_test-frameworks">frameworks</a>, <a href="#scala_test-isolation">isolation</a>, <a href="#scala_test-javacopts">javacopts</a>, <a href="#scala_test-jvm_flags">jvm_flags</a>, <a href="#scala_test-plugins">plugins</a>, <a href="#scala_test-resource_jars">resource_jars</a>, <a href="#scala_test-resource_strip_prefix">resource_strip_prefix</a>,
           <a href="#scala_test-runner">runner</a>, <a href="#scala_test-runtime_deps">runtime_deps</a>, <a href="#scala_test-scala">scala</a>, <a href="#scala_test-scalacopts">scalacopts</a>, <a href="#scala_test-shared_deps">shared_deps</a>, <a href="#scala_test-subprocess_runner">subprocess_runner</a>)
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
| <a id="scala_test-runner"></a>runner |  -   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@//src/main/scala/higherkindness/rules_scala/workers/zinc/test"`  |
| <a id="scala_test-runtime_deps"></a>runtime_deps |  The JVM runtime-only library dependencies.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-scala"></a>scala |  The Scala compiler to use (a `configure_bootstrap_scala` or `configure_zinc_scala` target). Defaults to the `default_scala` target specified in the WORKSPACE file.   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@//external:default_scala"`  |
| <a id="scala_test-scalacopts"></a>scalacopts |  Options to pass to scalac.   | List of strings | optional |  `[]`  |
| <a id="scala_test-shared_deps"></a>shared_deps |  If isolation is "classloader", the list of deps to keep loaded between tests   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scala_test-subprocess_runner"></a>subprocess_runner |  -   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@//src/main/scala/higherkindness/rules_scala/common/sbt-testing:subprocess"`  |


<a id="scaladoc"></a>

## scaladoc

<pre>
load("@//rules:scala.bzl", "scaladoc")

scaladoc(<a href="#scaladoc-name">name</a>, <a href="#scaladoc-deps">deps</a>, <a href="#scaladoc-srcs">srcs</a>, <a href="#scaladoc-compiler_deps">compiler_deps</a>, <a href="#scaladoc-scala">scala</a>, <a href="#scaladoc-scalacopts">scalacopts</a>, <a href="#scaladoc-title">title</a>)
</pre>

Generates Scaladoc.

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="scaladoc-name"></a>name |  A unique name for this target.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="scaladoc-deps"></a>deps |  Dependencies that should be made available to the Scaladoc tool. These may include libraries referenced in Scaladoc or public signatures.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scaladoc-srcs"></a>srcs |  Sources from which to generate Scaladoc. These may include `*.java` files, `*.scala` files, and source JARs.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scaladoc-compiler_deps"></a>compiler_deps |  JVM targets that should be included on the compile classpath.   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="scaladoc-scala"></a>scala |  The Scala compiler to use (a `configure_bootstrap_scala` or `configure_zinc_scala` target). Defaults to the `default_scala` target specified in the WORKSPACE file.   | <a href="https://bazel.build/concepts/labels">Label</a> | optional |  `"@//external:default_scala"`  |
| <a id="scaladoc-scalacopts"></a>scalacopts |  Options to pass to scalac.   | List of strings | optional |  `[]`  |
| <a id="scaladoc-title"></a>title |  The name of the project. If none is provided, the target label will be used.   | String | optional |  `""`  |


<a id="configure_zinc_scala"></a>

## configure_zinc_scala

<pre>
load("@//rules:scala.bzl", "configure_zinc_scala")

configure_zinc_scala(<a href="#configure_zinc_scala-compiler_bridge">compiler_bridge</a>, <a href="#configure_zinc_scala-compiler_classpath">compiler_classpath</a>, <a href="#configure_zinc_scala-version">version</a>, <a href="#configure_zinc_scala-global_plugins">global_plugins</a>,
                     <a href="#configure_zinc_scala-global_scalacopts">global_scalacopts</a>, <a href="#configure_zinc_scala-incremental">incremental</a>, <a href="#configure_zinc_scala-log_level">log_level</a>, <a href="#configure_zinc_scala-runtime_classpath">runtime_classpath</a>, <a href="#configure_zinc_scala-semanticdb_bundle">semanticdb_bundle</a>,
                     <a href="#configure_zinc_scala-use_ijar">use_ijar</a>, <a href="#configure_zinc_scala-kwargs">kwargs</a>)
</pre>

Configures a Scala compiler.

You can use this for a Scala target by setting the `scala` attribute to your `configure_zinc_scala` target.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="configure_zinc_scala-compiler_bridge"></a>compiler_bridge |  The compiler bridge (org.scala-sbt.compiler-bridge).   |  none |
| <a id="configure_zinc_scala-compiler_classpath"></a>compiler_classpath |  JVM targets that will always be on the compiler classpath. Usually, this is the compiler itself and the standard library.   |  none |
| <a id="configure_zinc_scala-version"></a>version |  The Scala version this compiler corresponds to.   |  none |
| <a id="configure_zinc_scala-global_plugins"></a>global_plugins |  scalac plugins that will always be enabled.   |  `[]` |
| <a id="configure_zinc_scala-global_scalacopts"></a>global_scalacopts |  scalac options that will always be enabled.   |  `[]` |
| <a id="configure_zinc_scala-incremental"></a>incremental |  Whether Zinc's incremental compilation will be available for this Zinc compiler. If True, this requires additional configuration to use incremental compilation.   |  `False` |
| <a id="configure_zinc_scala-log_level"></a>log_level |  The compiler log level. One of "error", "warn", "info", "debug", or "none".   |  `"warn"` |
| <a id="configure_zinc_scala-runtime_classpath"></a>runtime_classpath |  JVM targets that will always be on the runtime classpath. Usually, this is the standard library.   |  `[]` |
| <a id="configure_zinc_scala-semanticdb_bundle"></a>semanticdb_bundle |  Whether to bundle SemanticDB files in the resulting JAR. Note that in Scala 2, this requires the SemanticDB compiler plugin.   |  `True` |
| <a id="configure_zinc_scala-use_ijar"></a>use_ijar |  Whether to use ijar for this compiler. See https://github.com/bazelbuild/bazel/blob/master/third_party/ijar/README.txt for more information.   |  `True` |
| <a id="configure_zinc_scala-kwargs"></a>kwargs |  <p align="center"> - </p>   |  none |


<a id="make_scala_binary"></a>

## make_scala_binary

<pre>
load("@//rules:scala.bzl", "make_scala_binary")

make_scala_binary(<a href="#make_scala_binary-extras">extras</a>)
</pre>



**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="make_scala_binary-extras"></a>extras |  <p align="center"> - </p>   |  none |


<a id="make_scala_library"></a>

## make_scala_library

<pre>
load("@//rules:scala.bzl", "make_scala_library")

make_scala_library(<a href="#make_scala_library-extras">extras</a>)
</pre>



**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="make_scala_library-extras"></a>extras |  <p align="center"> - </p>   |  none |


<a id="make_scala_test"></a>

## make_scala_test

<pre>
load("@//rules:scala.bzl", "make_scala_test")

make_scala_test(<a href="#make_scala_test-extras">extras</a>)
</pre>



**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="make_scala_test-extras"></a>extras |  <p align="center"> - </p>   |  none |


