Contents:


# Algorithmic improvements #

Splitting partitions based on incoming edges:
  * Evaluate
  * Formalize
  * Which policy works best (incoming vs. outgoing vs. combined), and when?
  * Fix bugs in the combined policy.

Using other information during splitting:
  * Split partitions not based on behavioral\temporal properties of
the events, but on event annotations in the log, such as performance
metrics (e.g., time between events, CPU load, etc). This would allow
Synoptic to differentiate paths that have different performance, and
not structural, characteristics. This needs more thought since doing
this on the final Synoptic graph will only introduce diamonds. We have
some ideas on where to go with this.

# Choice of invariants #

Determine how appropriate our three types of invariants are for
capturing system properties:
  * We can do this by checking how well they cover popular distributed protocols and algorithms.

Expand invariant set to include the other specification patterns in
the study by Dwyer et. al.. There are quiet a few:
http://patterns.projects.cis.ksu.edu/documentation/patterns.shtml

[new](new.md) Some researchers at OSDI mentioned that we should consider
relaxing our strict invariants to more probabilistic versions. For
example, instead of x AFby y, we might consider x AFby y with
probability of .6. Or, x AFby y in 5 to 10 logical time steps.

[new](new.md) A talk at SSV used an AP invariant with a weak until, like we
had used before in our AP. This was useful in their context of static
model checking (don't do x before you have done y).


# Improving invariant mining #

Eliminating mined redundant invariants that are implied by transitivity:
  * Low hanging fruit is AFby and AP which are both trivially transitive.
> > For example, for a mined invariant set S consider the
> > AFby invariant graph (nodes are event type, and a directed edge
> > connects event types x and y if x AFby y). The edges in the min
> > spanning tree for all connected components of this graph form an
> > invariant set S'. We can show that S' implies S.


> (The goal of this is to reduce the number of invariants that need to be
> checked.  We would need to confirm that that is a bottleneck before
> committing to this task.)

Approximation mining algorithm is exact and eliminated the checking of
individual invariants from Synoptic for linear inputs:
  * We have to prove that this can be lifted to tree and diamond graphs, but we think that this is not hard (approximation alg. simply considers these graphs as a (compressed) set of linear paths).
  * Then, we need to implement it.


# Theoretical understanding #

What are fundamental constraints for algorithms that rely on splitting
and merging of event partitions?

[new](new.md) One way of thinking about the space of models is that its
partitioned into a set of models that satisfy all invariants and
another set of models that satisfies some, but not all invariants. The
space of models that satisfy all invariants is actually an
intersection of the space of models that satisfy each individual
invariant. So, if we have invariants i1, i2, i3 then the intersection
of model sets satisfies i1, i2, and i3 is exactly the set that
satisfies all of them. This way of considering the space of models
refines (no pun intended!) the intuition we have constructed so far.

By working out the space of representations for the counter-example in the SLAML paper we have realized that there is some kind of commutativity pattern relating splits. For example, a split of partition P1 and a split of partition P2 seem to be commutative when their splits do not effect one another. This appears to be more general and requires more thought. This could be useful when we know of a set of splits, each of which satisfies a different invariant. The difficulty is figuring out what order to do them in, and how the latter splits must be changed to account for splits that occurred earlier in the sequence (e.g. a split of a single partition may become a (simultaneous) split of many different partitions).

We have been relying on the CEGAR approach for our split heuristic -- to identify partitions that stand in the way of satisfying an invariant and aimed at eliminating counter examples. Are there improvements we can make to it? Also, we spent little time convincing ourselves that this is the right approach. It might be that the heuristic might have to be more invariant-dependent. At the very least, we have to document why it is appropriate, and work through a few examples that demonstrate its utility.

# Eliminating assumed inputs #

At OSDI we were told that Synoptic has a high barrier to use due to the inputs that it requires (listed below). It would improve usability and make Synoptic more broadly applicable if we could eliminate some of these assumed inputs.

Here are the four major inputs to Synoptic, a description of what's difficult about generating this input, and some ideas on how to either eliminate or make it easier to generate the input.

  * High quality log
    * Logs are often messy. Synoptic assumes that the user is interested in every logged event, this may not be true. The log may also be incomplete -- e.g. it may not capture all events the system may generate. Its unclear how useful Synoptic is in the case of an incomplete log.
    * Work by Ari at SLAML mines identifiers from logs and relates events that include common identifiers. The inputs is a set of identifiers, and an output is a graph linking messages through identifiers. We could target Synoptic to analyze just those events that are present in the same connected component of this graph. This would help to automate the process of deciding which events are relevant and which events are not relevant. The user would only have to specify which clustert of events they are interested in.

  * Relation over events
    * For logs containing timestamps, lack of synchronization between machines may make it impossible to study logs from different machines in combination.
    * Ari's idea (described above) could be used is to disallow the relation to span different connected components. The assumption is that different connected components correspond to different logical components of the system. Synoptic should therefore avoid connecting graphs that correspond to different components.

  * Event instances -> Event type mapping
    * The selection of this granularity is highly nuanced. It requires deep understanding of the system.
    * To eliminate this input we could leverage tools that mine common patterns from logs, such as [loghound](http://ristov.users.sourceforge.net/loghound/loghound.html). We can also eliminate this input by mining printf() statements directly from program source (Google has such a system).

  * Invariant mining granularity
    * The selection of this granularity is highly nuanced. It requires deep understanding of the system.
    * Ari's idea (described above) could be used is to help determine the mining granularity automatically. For example, if we agree that an invariant between two events should only be considered if the two events are in the same connected component of Ari's graph, then we could use these constraints to determine the appropriate mining granularity. This seems hard, but is an interesting take on the problem.


# Synoptic use cases #

[new](new.md) We can compare two Synoptic graphs for structural differences.
(This is easier than graph isomorphism because we have to also match
graph labels). This is useful for:
  * detecting anomalies when all graphs are the same except for the anomalous one (why not just compare invariant sets?)
  * close the loop with program analysis -- whenever the user modifies the code, they can mine printfs, or mine logs to understand whether the new program has the desired new behavior

(Mike thinks this is good, but would hold off on this until we find out
what our users want.  Then we can implement what they need, which (so long
as they are sympathetic and willing to try again when we come back) should
be more effective than speculatively implementing features.)


# Evaluation #

We must find a way to compare Synoptic to previously published
results:
  * Acquire traces for system that have been studied by published log analysis tools.
  * Get code from the GK-Tail authors. They seemed forthcoming over email, but we never followed up with them.


# Improving Synoptic output/UI #

[new](new.md) At OSDI we found out that some google systems have anywhere from
hundreds to thousands of event types. Here is one way to focus Synoptic
on fewer event types that are still meaningful. Consider the invariant
dependency graph for mined invariants (a node is an event type, and
two nodes have an undirected edge if there is at least one invariant
that relates them). It is likely that this graph will have connected
components that relate some cluster of event types. We can
use this to produce graphs that only include event types that have
some relation between them -- essentially we can filter from the log
just the event types in the connected component and run Synoptic on
this new filtered log. The result will be a collection of Synoptic
output graphs, one for each connected component in the invariant
dependency graph.