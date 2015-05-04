# Introduction #

Suppose there are some traces coming from a very slow machine and some coming from a very fast machine, but both exhibit the same relative timing patterns. Normalizing such traces would mean the constrained invariants would actually capture patterns present in the log rather than finding outlier maxes and mins that don't say much about the system.

# Example #
Consider the following outputs using two simple traces. The right graph was produced using Perfume with trace normalization, and the left one was produced with normalization disabled.

![http://wiki.synoptic.googlecode.com/hg/images/perfume-without-normalization.png](http://wiki.synoptic.googlecode.com/hg/images/perfume-without-normalization.png)

![http://wiki.synoptic.googlecode.com/hg/images/perfume-with-normalization.png](http://wiki.synoptic.googlecode.com/hg/images/perfume-with-normalization.png)

While it seems on the left side that the transition from b to c always takes 4 time units, the right graph hints that the transition does not normally take the same amount of time. The fact that the transition from a to b takes 1 time units in the first trace and 4 in the second one could mean that the traces come from systems which differ in performance, so care has to be taken when assuming that an invariant has been found there.

# Details #
To perform the trace normalization, every trace gets uniformly transformed into the interval [0,1] as a pre-processing step. This will always result in a graph which is at least as big as if no normalization were used, if not bigger.

# Usage #

When using Perfume, add the `-trace-norm` flag to the passed arguments.