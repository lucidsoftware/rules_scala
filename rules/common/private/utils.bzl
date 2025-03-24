load("@bazel_skylib//lib:dicts.bzl", "dicts")
load("@bazel_skylib//lib:paths.bzl", "paths")
load("@bazel_skylib//lib:shell.bzl", "shell")
load("@rules_java//java/common:java_common.bzl", "java_common")

#
# Helper utilities
#

def collect(index, iterable):
    return [entry[index] for entry in iterable]

def strip_margin(str, delim = "|"):
    """
    For every line in str:
      Strip a leading prefix consisting of spaces followed by delim from the line.
    This is extremely similar to Scala's .stripMargin
    """
    return "\n".join([
        _strip_margin_line(line, delim)
        for line in str.splitlines()
    ])

def _strip_margin_line(line, delim):
    trimmed = line.lstrip(" ")
    if trimmed[:1] == delim:
        return trimmed[1:]
    else:
        return line

_SINGLE_JAR_MNEMONIC = "SingleJar"

def _format_mains_file(runfiles_enabled, workspace_prefix, mains_file):
    if runfiles_enabled:
        return "$(head -1 $JAVA_RUNFILES/{}/{})".format(workspace_prefix, mains_file.short_path)
    return "$(head -1 $(rlocation " + paths.normalize(workspace_prefix + mains_file.short_path) + "))"

def _format_jacoco_metadata_file(runfiles_enabled, workspace_prefix, metadata_file):
    if runfiles_enabled:
        return "export JACOCO_METADATA_JAR=\"$JAVA_RUNFILES/{}/{}\"".format(workspace_prefix, metadata_file.short_path)

    return "export JACOCO_METADATA_JAR=$(rlocation " + paths.normalize(workspace_prefix + metadata_file.short_path) + ")"

# This is from the Starlark Java builtins in Bazel
def _format_classpath_entry(runfiles_enabled, workspace_prefix, file):
    if runfiles_enabled:
        return "${RUNPATH}" + file.short_path

    return "$(rlocation " + paths.normalize(workspace_prefix + file.short_path) + ")"

def _format_javabin(java_executable, workspace_prefix, runfiles_enabled):
    if not paths.is_absolute(java_executable):
        java_executable = workspace_prefix + java_executable
    java_executable = paths.normalize(java_executable)

    if runfiles_enabled:
        prefix = "" if paths.is_absolute(java_executable) else "${JAVA_RUNFILES}/"
        return "JAVABIN=${JAVABIN:-" + prefix + java_executable + "}"
    else:
        return "JAVABIN=${JAVABIN:-$(rlocation " + java_executable + ")}"

def write_launcher(
        ctx,
        prefix,
        output,
        runtime_classpath,
        main_class,
        jvm_flags,
        extra = "",
        jacoco_classpath = None,
        mains_file = None):
    """Macro that writes out a launcher script shell script. Some of this is from Bazel's Starlark Java builtins.
      Args:
        runtime_classpath: File containing the classpath required to launch this java target.
        main_class: the main class to launch.
        mains_file: the file containing a string representing the main class to launch
        jvm_flags: The flags that should be passed to the jvm.
        args: Args that should be passed to the Binary.
    """
    workspace_name = ctx.workspace_name
    workspace_prefix = workspace_name + ("/" if workspace_name else "")

    # TODO: can we get this info?
    # runfiles_enabled = ctx.configuration.runfiles_enabled()
    runfiles_enabled = False

    # See https://bazel.build/extending/config#accessing-attributes-with-transitions:
    # "When attaching a transition to an outgoing edge (regardless of whether the transition is a
    # 1:1 or 1:2+ transition), `ctx.attr` is forced to be a list if it isn't already. The order of
    # elements in this list is unspecified."
    java_runtime_info = ctx.attr._target_jdk[0][java_common.JavaRuntimeInfo]
    java_executable = java_runtime_info.java_executable_runfiles_path

    template_dict = ctx.actions.template_dict()
    template_dict.add_joined(
        "%classpath%",
        runtime_classpath,
        map_each = lambda file: _format_classpath_entry(runfiles_enabled, workspace_prefix, file),
        join_with = ctx.configuration.host_path_separator,
        format_joined = "\"%s\"",
        allow_closure = True,
    )

    base_substitutions = {
        "%runfiles_manifest_only%": "1" if runfiles_enabled else "",
        "%workspace_prefix%": workspace_prefix,
        "%javabin%": "{}\n{}".format(_format_javabin(java_executable, workspace_prefix, runfiles_enabled), extra),
        "%needs_runfiles%": "0" if paths.is_absolute(java_runtime_info.java_executable_exec_path) else "1",
        "%jvm_flags%": " ".join(jvm_flags),
        "%test_runtime_classpath_file%": "",
    }

    if jacoco_classpath != None:
        # this file must end in ".txt" to trigger the `isNewImplementation` paths
        # in com.google.testing.coverage.JacocoCoverageRunner
        metadata_file = ctx.actions.declare_file("%s.jacoco_metadata.txt" % ctx.attr.name, sibling = output)
        ctx.actions.write(metadata_file, "\n".join([
            jar.short_path.replace("../", "external/")
            for jar in jacoco_classpath
        ]))
        more_outputs = [metadata_file]

        template_dict.add(
            "%set_jacoco_metadata%",
            _format_jacoco_metadata_file(runfiles_enabled, workspace_prefix, metadata_file),
        )

        more_substitutions = {
            "%java_start_class%": "com.google.testing.coverage.JacocoCoverageRunner",
            "%set_jacoco_java_runfiles_root%": """export JACOCO_JAVA_RUNFILES_ROOT=$JAVA_RUNFILES/{}/""".format(ctx.workspace_name),
            "%set_java_coverage_new_implementation%": """export JAVA_COVERAGE_NEW_IMPLEMENTATION=YES""",
        }

        if mains_file:
            template_dict.add(
                "%set_jacoco_main_class%",
                "export JACOCO_MAIN_CLASS={}".format(
                    _format_mains_file(runfiles_enabled, workspace_prefix, mains_file),
                ),
            )
        else:
            more_substitutions["%set_jacoco_main_class%"] = "export JACOCO_MAIN_CLASS={}".format(main_class)
    else:
        more_outputs = []
        more_substitutions = {
            "%set_jacoco_metadata%": "",
            "%set_jacoco_main_class%": "",
            "%set_jacoco_java_runfiles_root%": "",
            "%set_java_coverage_new_implementation%": "",
        }

        if mains_file:
            template_dict.add(
                "%java_start_class%",
                _format_mains_file(runfiles_enabled, workspace_prefix, mains_file),
            )
        else:
            more_substitutions["%java_start_class%"] = main_class

    ctx.actions.expand_template(
        template = ctx.file._java_stub_template,
        output = output,
        substitutions = dicts.add(base_substitutions, more_substitutions),
        computed_substitutions = template_dict,
        is_executable = True,
    )

    return more_outputs

def safe_name(value):
    return "".join([value[i] if value[i].isalnum() or value[i] == "." else "_" for i in range(len(value))])

def _short_path(file):
    return file.short_path

def _format_resources_item(item):
    key, value = item
    return "\"{}\":\"{}\"".format(value.path, key)

def action_singlejar(
        ctx,
        inputs,
        output,
        main_class = None,
        progress_message = None,
        resources = {},
        compression = False):
    # This calls bazels singlejar utility.
    # For a full list of available command line options see:
    # https://github.com/bazelbuild/bazel/blob/master/src/java_tools/singlejar/java/com/google/devtools/build/singlejar/SingleJar.java#L311
    # The C++ version is being used now, which does not support workers. This is why workers are disabled for SingleJar

    if type(inputs) == "list":
        inputs = depset(inputs)

    args = ctx.actions.args()
    args.add("--exclude_build_data")
    args.add("--normalize")
    if compression:
        args.add("--compression")
    args.add_all("--sources", inputs)
    args.add_all("--resources", resources.items(), map_each = _format_resources_item)
    args.add("--output", output)
    if main_class != None:
        args.add("--main_class", main_class)
    args.set_param_file_format("multiline")
    args.use_param_file("@%s", use_always = True)

    all_inputs = depset(resources.values(), transitive = [inputs])

    ctx.actions.run(
        arguments = [args],
        executable = ctx.executable._singlejar,
        execution_requirements = {
            "supports-workers": "0",
            "supports-path-mapping": "1",
        },
        inputs = all_inputs,
        mnemonic = _SINGLE_JAR_MNEMONIC,
        outputs = [output],
        progress_message = progress_message,
        toolchain = None,
    )

def separate_src_jars_srcs_and_other(files):
    src_jars = []
    srcs = []
    other_srcs = []

    for file in files:
        if _is_src_jar(file):
            src_jars.append(file)
        elif _is_src(file):
            srcs.append(file)
        else:
            other_srcs.append(file)

    return (src_jars, srcs, other_srcs)

def _is_src_jar(file):
    return (file.extension.lower() == "srcjar" or file.short_path.lower().endswith("-sources.jar") or
            file.short_path.lower().endswith("-src.jar"))

def _is_src(file):
    return file.extension.lower() in ["java", "scala"]

def short_path(file):
    """Convenience function for getting the short_path that was being duplicated in a few files"""
    return file.short_path
