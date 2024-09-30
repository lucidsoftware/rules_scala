load(
    "//rules/common:private/utils.bzl",
    _resolve_execution_reqs = "resolve_execution_reqs",
    _strip_margin = "strip_margin",
)

#
# PHASE: bootstrap compile
#
# An alternative compile phase that shells out to scalac directly
#

def phase_bootstrap_compile(ctx, g):
    if g.classpaths.plugin:
        fail("plugins aren't supported for bootstrap_scala rules")
    if g.classpaths.src_jars:
        fail("source jars supported for bootstrap_scala rules")

    inputs = depset(
        ctx.files.srcs,
        transitive = [
            ctx.attr._jdk[java_common.JavaRuntimeInfo].files,
            g.classpaths.compile,
            g.classpaths.compiler,
        ],
    )

    tmp = ctx.actions.declare_directory("{}/tmp/classes".format(ctx.label.name))

    scala_configuration = g.javainfo.scala_info.scala_configuration

    main_class = "scala.tools.nsc.Main"
    if int(scala_configuration.version[0]) >= 3:
        main_class = "dotty.tools.dotc.Main"

    compiler_classpath = g.classpaths.compiler.to_list()
    compile_classpath = g.classpaths.compile.to_list()

    args = ctx.actions.args()
    args.add("--java", ctx.attr._jdk[java_common.JavaRuntimeInfo].java_executable_exec_path)
    args.add("--main_class", main_class)
    args.add("--jar_creator", ctx.executable._jar_creator)
    args.add("--output_jar", g.classpaths.jar)

    if compiler_classpath:
        args.add_joined("--compiler_classpath", compiler_classpath, join_with = ":")
    else:
        fail("Compiler classpath  missing for bootstrap compiler")

    if compile_classpath:
        args.add_joined("--compile_classpath", compile_classpath, join_with = ":")

    args.add_all("--tmp", [tmp], expand_directories = False)

    if scala_configuration.global_scalacopts:
        args.add_joined("--global_scalacopts", scala_configuration.global_scalacopts, join_with = " ")

    if ctx.attr.scalacopts:
        args.add_joined("--scalacopts", ctx.attr.scalacopts, join_with = " ")

    if g.classpaths.srcs:
        args.add_joined("--srcs", g.classpaths.srcs, join_with = " ")
    else:
        fail("Empty srcs list passed to bootstrap compiler")

    command = _strip_margin(
        """
            |set -eo pipefail
            |while [ $# -gt 0 ]; do
            |  case "${1}" in
            |    --java)
            |      java="${2}"
            |      ;;
            |    --compiler_classpath)
            |      compiler_classpath="${2}"
            |      ;;
            |    --main_class)
            |      main_class="${2}"
            |      ;;
            |    --compile_classpath)
            |      compile_classpath="${2}"
            |      ;;
            |    --tmp)
            |      tmp="${2}"
            |      ;;
            |    --global_scalacopts)
            |      global_scalacopts="${2}"
            |      ;;
            |    --scalacopts)
            |      scalacopts="${2}"
            |      ;;
            |    --srcs)
            |      srcs="${2}"
            |      ;;
            |    --jar_creator)
            |      jar_creator="${2}"
            |      ;;
            |    --output_jar)
            |      output_jar="${2}"
            |      ;;
            |    *)
            |      echo "Error: Invalid argument."
            |      exit 1
            |  esac
            |  shift
            |  shift
            |done
            |echo "${srcs}"
            |"${java}" \\
            |  -cp "${compiler_classpath}" \\
            |  "${main_class}" \\
            |  -cp "${compile_classpath}" \\
            |  -d "${tmp}" \\
            |  ${global_scalacopts} \\
            |  ${scalacopts} \\
            |  ${srcs}
            |
            |"${jar_creator}" "${output_jar}" "${tmp}" 2> /dev/null
            |""",
    )

    ctx.actions.run_shell(
        arguments = [args],
        inputs = inputs,
        tools = [ctx.executable._jar_creator],
        mnemonic = "BootstrapScalacompile",
        outputs = [g.classpaths.jar, tmp],
        command = command,
        execution_requirements = _resolve_execution_reqs(
            ctx,
            {
                "supports-path-mapping": "1",
            },
        ),
    )
