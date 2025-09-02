original_scala_toolchain_setting = "@rules_scala_annex//rules/scala:original-scala-toolchain"
original_scalafmt_toolchain_setting = "@rules_scala_annex//rules/scalafmt:original-scalafmt-toolchain"
scala_toolchain_setting = "@rules_scala_annex//rules/scala:scala-toolchain"
scalafmt_toolchain_setting = "@rules_scala_annex//rules/scalafmt:scalafmt-toolchain"

def _scala_incoming_transition_impl(settings, attr):
    result = dict(settings)

    if attr.scala_toolchain_name != "" and attr.scala_toolchain_name != settings[scala_toolchain_setting]:
        # We set `original_scala_toolchain_setting` so we can reset the toolchain to its
        # original value in `scala_outgoing_transition`. That way, we can ensure every target is
        # built under a single toolchain, thus preventing duplicate builds.
        #
        # We do not do this work when the toolchain name is set, but is no different than what is
        # already set. By having that check we avoid the failure mode where the original toolchain
        # name gets set equal to the current toolchain name and destroys whatever the actual original
        # toolchain name was. For example
        #  State 1:              State 2:          State 3:
        #    Setting: A      =>    Setting: B  =>    Setting: B  => Game over
        #    Original: Unset       Original: A       Original: B
        #
        # Note that the check described above should ideally not be required due to outgoing
        # transitions but it is, so something is going wrong. As a result, the check is probably
        # temporary, but who knows.
        #
        # This is inspired by what the rules_go folks are doing.
        result[original_scala_toolchain_setting] = settings[scala_toolchain_setting]
        result[scala_toolchain_setting] = attr.scala_toolchain_name

    if (hasattr(attr, "scalafmt_toolchain_name") and attr.scalafmt_toolchain_name != "" and
        attr.scalafmt_toolchain_name != settings[scalafmt_toolchain_setting]):
        result[original_scalafmt_toolchain_setting] = settings[scalafmt_toolchain_setting]
        result[scalafmt_toolchain_setting] = attr.scalafmt_toolchain_name

    return result

scala_incoming_transition = transition(
    implementation = _scala_incoming_transition_impl,
    inputs = [
        original_scala_toolchain_setting,
        original_scalafmt_toolchain_setting,
        scala_toolchain_setting,
        scalafmt_toolchain_setting,
    ],
    outputs = [
        original_scala_toolchain_setting,
        original_scalafmt_toolchain_setting,
        scala_toolchain_setting,
        scalafmt_toolchain_setting,
    ],
)

def _scala_outgoing_transition_impl(settings, _):
    result = dict(settings)
    original_scala_toolchain = settings[original_scala_toolchain_setting]
    original_scalafmt_toolchain = settings[original_scalafmt_toolchain_setting]

    # Although `original_scala_toolchain_setting` and `original_scalafmt_toolchain_setting` will be
    # overridden in the incoming transition, we set them to "" so non-Scala targets aren't built
    # under different values of these settings. That way, they aren't built multiple times.
    if original_scala_toolchain != "":
        result[original_scala_toolchain_setting] = ""
        result[scala_toolchain_setting] = original_scala_toolchain

    if original_scalafmt_toolchain != "":
        result[original_scalafmt_toolchain_setting] = ""
        result[scalafmt_toolchain_setting] = original_scalafmt_toolchain

    return result

scala_outgoing_transition = transition(
    implementation = _scala_outgoing_transition_impl,
    inputs = [
        original_scala_toolchain_setting,
        original_scalafmt_toolchain_setting,
        scala_toolchain_setting,
        scalafmt_toolchain_setting,
    ],
    outputs = [
        original_scala_toolchain_setting,
        original_scalafmt_toolchain_setting,
        scala_toolchain_setting,
        scalafmt_toolchain_setting,
    ],
)

scala_toolchain_attributes = {
    "scala_toolchain_name": attr.string(
        doc = "The name of the Scala toolchain to use for this target (as provided to `register_*_toolchain`)",
    ),
}
