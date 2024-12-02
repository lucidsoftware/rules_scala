<!-- Generated with Stardoc: http://skydoc.bazel.build -->



<a id="emulate_rules_scala"></a>

## emulate_rules_scala

<pre>
load("@rules_scala_annex//rules:rules_scala.bzl", "emulate_rules_scala")

emulate_rules_scala(<a href="#emulate_rules_scala-scalatest">scalatest</a>, <a href="#emulate_rules_scala-extra_deps">extra_deps</a>)
</pre>



**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="emulate_rules_scala-scalatest"></a>scalatest |  <p align="center"> - </p>   |  none |
| <a id="emulate_rules_scala-extra_deps"></a>extra_deps |  <p align="center"> - </p>   |  `[]` |


<a id="emulate_rules_scala_repository"></a>

## emulate_rules_scala_repository

<pre>
load("@rules_scala_annex//rules:rules_scala.bzl", "emulate_rules_scala_repository")

emulate_rules_scala_repository(<a href="#emulate_rules_scala_repository-name">name</a>, <a href="#emulate_rules_scala_repository-extra_deps">extra_deps</a>, <a href="#emulate_rules_scala_repository-repo_mapping">repo_mapping</a>)
</pre>

**ATTRIBUTES**


| Name  | Description | Type | Mandatory | Default |
| :------------- | :------------- | :------------- | :------------- | :------------- |
| <a id="emulate_rules_scala_repository-name"></a>name |  A unique name for this repository.   | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required |  |
| <a id="emulate_rules_scala_repository-extra_deps"></a>extra_deps |  -   | <a href="https://bazel.build/concepts/labels">List of labels</a> | optional |  `[]`  |
| <a id="emulate_rules_scala_repository-repo_mapping"></a>repo_mapping |  In `WORKSPACE` context only: a dictionary from local repository name to global repository name. This allows controls over workspace dependency resolution for dependencies of this repository.<br><br>For example, an entry `"@foo": "@bar"` declares that, for any time this repository depends on `@foo` (such as a dependency on `@foo//some:target`, it should actually resolve that dependency within globally-declared `@bar` (`@bar//some:target`).<br><br>This attribute is _not_ supported in `MODULE.bazel` context (when invoking a repository rule inside a module extension's implementation function).   | <a href="https://bazel.build/rules/lib/dict">Dictionary: String -> String</a> | optional |  |


