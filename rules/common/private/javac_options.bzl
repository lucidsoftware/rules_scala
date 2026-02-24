"""Utilities for transforming javac options."""

def replace_source_target_with_release(opts):
    """Replace -source X -target X with --release X when versions match.

    Bazel's native Java compilation uses JavaBuilder which handles the bootclasspath internally
    via create_compilation_action(). Tools that invoke javac directly, e.g., via
    ToolProvider.getSystemJavaCompiler()) don't have access to this mechanism. --release provides
    equivalent system module configuration: it restricts source syntax, generates the correct
    bytecode, and sets the system module path automatically using the running JDK's built-in ct.sym.

    Note that --release is not strictly equivalent to -source X -target X: it also pins the visible
    API to that specific release's signatures via ct.sym, so code that references JDK APIs newer
    than the targeted release will now be rejected instead of compiling against the running JDK's
    classes. This is the intended, more-correct behavior for cross-compilation, but it is a
    behavioral change for builds that previously relied on the laxer -source/-target combination.

    Because --release is mutually exclusive with -bootclasspath and with a pre-existing --release,
    the original options are returned unchanged when either is already present.

    Args:
        opts: list of javac option strings

    Returns:
        Transformed list with --release replacing -source/-target when applicable, or the original
        list unchanged.
    """

    # --release cannot be combined with -bootclasspath or another --release, so leave
    # opts untouched if either is already present rather than producing an invalid invocation.
    if "--release" in opts or "-bootclasspath" in opts:
        return opts

    source_version = None
    target_version = None
    other_opts = []
    skip_next = False
    for i, opt in enumerate(opts):
        if skip_next:
            skip_next = False
            continue
        if opt == "-source" and i + 1 < len(opts):
            source_version = opts[i + 1]
            skip_next = True
        elif opt == "-target" and i + 1 < len(opts):
            target_version = opts[i + 1]
            skip_next = True
        else:
            other_opts.append(opt)

    if source_version != None and source_version == target_version:
        return ["--release", source_version] + other_opts
    else:
        return opts
