#
# PHASE: outputgroupinfo
#
# Generates the `OutputGroupInfo` provider.
#

def phase_outputgroupinfo(ctx, g):
    g.out.providers.append(OutputGroupInfo(**g.out.output_groups))
