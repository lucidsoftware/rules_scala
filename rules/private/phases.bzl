load(":phases/api.bzl", _adjust_phases = "adjust_phases", _run_phases = "run_phases")
load(":phases/phase_binary_deployjar.bzl", _phase_binary_deployjar = "phase_binary_deployjar")
load(":phases/phase_binary_launcher.bzl", _phase_binary_launcher = "phase_binary_launcher")
load(":phases/phase_bootstrap_compile.bzl", _phase_bootstrap_compile = "phase_bootstrap_compile")
load(":phases/phase_classpaths.bzl", _phase_classpaths = "phase_classpaths")
load(":phases/phase_coda.bzl", _phase_coda = "phase_coda")
load(":phases/phase_coverage_jacoco.bzl", _phase_coverage_jacoco = "phase_coverage_jacoco")
load(":phases/phase_ijinfo.bzl", _phase_ijinfo = "phase_ijinfo")
load(":phases/phase_javainfo.bzl", _phase_javainfo = "phase_javainfo")
load(":phases/phase_library_defaultinfo.bzl", _phase_library_defaultinfo = "phase_library_defaultinfo")
load(":phases/phase_noop.bzl", _phase_noop = "phase_noop")
load(":phases/phase_resources.bzl", _phase_resources = "phase_resources")
load(":phases/phase_scalafmt_nondefault_outputs.bzl", _phase_scalafmt_nondefault_outputs = "phase_scalafmt_nondefault_outputs")
load(":phases/phase_semanticdb.bzl", _phase_semanticdb = "phase_semanticdb")
load(":phases/phase_singlejar.bzl", _phase_singlejar = "phase_singlejar")
load(":phases/phase_test_launcher.bzl", _phase_test_launcher = "phase_test_launcher")
load(":phases/phase_zinc_compile.bzl", _phase_zinc_compile = "phase_zinc_compile")
load(":phases/phase_zinc_depscheck.bzl", _phase_zinc_depscheck = "phase_zinc_depscheck")

adjust_phases = _adjust_phases

run_phases = _run_phases

phase_binary_deployjar = _phase_binary_deployjar

phase_binary_launcher = _phase_binary_launcher

phase_bootstrap_compile = _phase_bootstrap_compile

phase_classpaths = _phase_classpaths

phase_coda = _phase_coda

phase_coverage_jacoco = _phase_coverage_jacoco

phase_ijinfo = _phase_ijinfo

phase_javainfo = _phase_javainfo

phase_library_defaultinfo = _phase_library_defaultinfo

phase_noop = _phase_noop

phase_resources = _phase_resources

phase_scalafmt_nondefault_outputs = _phase_scalafmt_nondefault_outputs

phase_semanticdb = _phase_semanticdb

phase_singlejar = _phase_singlejar

phase_test_launcher = _phase_test_launcher

phase_zinc_compile = _phase_zinc_compile

phase_zinc_depscheck = _phase_zinc_depscheck
