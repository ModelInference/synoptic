package synoptic.invariants.miners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.AlwaysConcurrentInvariant;
import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverConcurrentInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.main.TraceParser;
import synoptic.model.DistEventType;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.StringEventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

public class TransitiveClosureInvMiner extends InvariantMiner {

    /**
     * Whether or not to use iterative version of warshall's algorithm for TC
     * computation. Yes by default.
     */
    public boolean useWarshall = true;

    public TransitiveClosureInvMiner() {
    }

    public TransitiveClosureInvMiner(boolean useWarshall) {
        this.useWarshall = useWarshall;
    }

    @Override
    public TemporalInvariantSet computeInvariants(IGraph<EventNode> g) {
        // Determine whether to mine concurrency invariants or not by testing
        // the event type of _some_ node in g -- concurrency invariants are
        // mined for nodes of DistEventType.
        boolean mineConcurrencyInvariants = false;
        if (g.getNodes().iterator().next().getEType() instanceof DistEventType) {
            mineConcurrencyInvariants = true;
        }
        return computeInvariants(g, true, mineConcurrencyInvariants);
    }

    /**
     * Compute invariants of a graph g by mining invariants from the transitive
     * closure using {@code extractInvariantsUsingTC}, which returns an
     * over-approximation of the invariants that hold (i.e. it may return
     * invariants that do not hold, but may not fail to return an invariant that
     * does not hold)
     * 
     * @param g
     *            the graph of nodes of type LogEvent
     * @param filterTautological
     *            whether or not tautological invariants should be filtered out
     * @param mineConcurrencyInvariants
     *            whether or not to also mine concurrency invariants
     * @return the set of temporal invariants the graph satisfies
     */
    public TemporalInvariantSet computeInvariants(IGraph<EventNode> g,
            boolean filterTautological, boolean mineConcurrencyInvariants) {

        TimedTask mineInvariants = PerformanceMetrics.createTask(
                "mineInvariants", false);
        Set<ITemporalInvariant> overapproximatedInvariantsSet;

        // Compute the over-approximated set of invariants for the input graph.
        try {

            TimedTask itc = PerformanceMetrics.createTask(
                    "invariants_transitive_closure", false);

            // Compute the transitive closure.
            AllRelationsTransitiveClosure<EventNode> transitiveClosure = new AllRelationsTransitiveClosure<EventNode>(
                    g, useWarshall);

            // Get the over-approximation.
            itc.stop();
            if (Main.doBenchmarking) {
                logger.info("BENCHM: " + itc);
            }
            TimedTask io = PerformanceMetrics.createTask(
                    "invariants_approximation", false);

            // Extract invariants for all relations, iteratively. Since we are
            // not considering invariants over multiple relations, this is
            // sufficient.
            overapproximatedInvariantsSet = new LinkedHashSet<ITemporalInvariant>();
            for (String relation : g.getRelations()) {
                overapproximatedInvariantsSet.addAll(extractInvariantsFromTC(g,
                        transitiveClosure.get(relation), relation,
                        mineConcurrencyInvariants));
            }

            io.stop();
            if (Main.doBenchmarking) {
                logger.info("BENCHM: " + io);
            }
            // logger.info("Over-approx set: "
            // + overapproximatedInvariantsSet.toString());
        } finally {
            mineInvariants.stop();
        }

        // Optionally filter out the tautological invariants.
        if (filterTautological) {
            filterOutTautologicalInvariants(overapproximatedInvariantsSet);
            for (ITemporalInvariant inv : computeINITIALAFbyXInvariants(g)) {
                overapproximatedInvariantsSet.add(inv);
            }
        }
        return new TemporalInvariantSet(overapproximatedInvariantsSet);
    }

    /**
     * Extract an over-approximated set of invariants from the transitive
     * closure {@code tc} of the graph {@code g}.
     * 
     * @param g
     *            the graph over LogEvent
     * @param tc
     *            the transitive closure (of {@code g}) from which to mine
     *            invariants
     * @param relation
     *            the relation to consider for the invariants
     * @return the over-approximated set of invariants
     * @throws Exception
     */
    private Set<ITemporalInvariant> extractInvariantsFromTC(
            IGraph<EventNode> g, TransitiveClosure<EventNode> tc,
            String relation, boolean mineConcurrencyInvariants) {
        Map<EventType, ArrayList<EventNode>> partitions = new LinkedHashMap<EventType, ArrayList<EventNode>>();

        // Initialize the partitions map: each unique label maps to a list of
        // nodes with that label.
        for (EventNode m : g.getNodes()) {
            if (!partitions.containsKey(m.getEType())) {
                partitions.put(m.getEType(), new ArrayList<EventNode>());
            }
            partitions.get(m.getEType()).add(m);
        }

        Set<ITemporalInvariant> pathInvs = new LinkedHashSet<ITemporalInvariant>();
        Set<ITemporalInvariant> concurInvs = new LinkedHashSet<ITemporalInvariant>();

        for (EventType label1 : partitions.keySet()) {
            for (EventType label2 : partitions.keySet()) {
                boolean neverFollowed = true;
                boolean alwaysFollowedBy = true;
                boolean alwaysPreceded = true;

                // Whether or not every instance of label1 is totally ordered
                // with respect to every instance of label2.
                boolean alwaysOrdered = true;

                boolean neverOrdered = true;
                for (EventNode node1 : partitions.get(label1)) {
                    boolean followerFound = false;
                    boolean predecessorFound = false;

                    // TODO: this could be greatly optimized by iterating
                    // through just EventNodes node2s such that node2 is in the
                    // same trace as node1 -- i.e., by partitioning nodes into
                    // partitions by traceIDs.

                    for (EventNode node2 : partitions.get(label2)) {
                        if (!node1.getEType().isSpecialEventType()
                                && !node2.getEType().isSpecialEventType()
                                && node1.getTraceID() != node2.getTraceID()) {
                            // The last condition is an optimization. The first
                            // two conditions make sure that the optimization is
                            // backward-compatible with
                            // testTautologicalInvariantMining() in
                            // TOLogInvariantMiningTests (TODO: fix the test to
                            // remove this constraint)
                            continue;
                        }

                        if (tc.isReachable(node1, node2)) {
                            neverFollowed = false;
                            followerFound = true;
                        }

                        if (tc.isReachable(node2, node1)) {
                            predecessorFound = true;
                        }

                        // If node1 and node2 belong to same trace then for them
                        // to be alwaysOrdered, there must be a path between
                        // them either from node1 to node2 or from node2 to
                        // node1.
                        if (!tc.isReachable(node1, node2)
                                && !tc.isReachable(node2, node1)) {
                            alwaysOrdered = false;
                        }
                    }
                    // Every node instance with label1 must be followed by a
                    // node instance with label2 for label1 AFby label2 to be
                    // true.
                    if (!followerFound) {
                        alwaysFollowedBy = false;
                    }
                    // Every node instance with label1 must be preceded by a
                    // node instance with label2 for label2 AP label1 to be
                    // true.
                    if (!predecessorFound) {
                        alwaysPreceded = false;
                    }

                    if (followerFound || predecessorFound) {
                        neverOrdered = false;
                    }
                }

                if (neverFollowed) {
                    pathInvs.add(new NeverFollowedInvariant(label1, label2,
                            relation));
                }

                if (alwaysFollowedBy) {
                    pathInvs.add(new AlwaysFollowedInvariant(label1, label2,
                            relation));
                }

                if (alwaysPreceded) {
                    pathInvs.add(new AlwaysPrecedesInvariant(label2, label1,
                            relation));
                }

                // The two labels are always/never ordered across all the
                // system traces.
                if (mineConcurrencyInvariants) {

                    // Ignore local versions of alwaysOrdered and
                    // neverOrdered since they are trivially true and false
                    // respectively.
                    if (!((DistEventType) label1).getPID().equals(
                            ((DistEventType) label2).getPID())) {

                        if (alwaysOrdered) {
                            concurInvs.add(new NeverConcurrentInvariant(
                                    (DistEventType) label2,
                                    (DistEventType) label1, relation));
                        }
                        if (neverOrdered) {
                            concurInvs.add(new AlwaysConcurrentInvariant(
                                    (DistEventType) label2,
                                    (DistEventType) label1, relation));
                        }
                    }
                }
            }
        }

        // Merge the concurrency and path invariant sets.
        mergePathAndConcurrencyInvariants(pathInvs, concurInvs);

        return pathInvs;
    }

    /**
     * The inclusion of INITIAL and TERMINAL states in the graphs generates the
     * following types of invariants (for all event types X):
     * 
     * <pre>
     * - x AP TERMINAL
     * - INITIAL AP x
     * - x AP TERMINAL
     * - x AFby TERMINAL
     * - TERMINAL NFby INITIAL
     * </pre>
     * <p>
     * NOTE: x AP TERMINAL is not considered a tautological invariant, but we
     * filter it out anyway because we reconstruct it later.
     * </p>
     * <p>
     * We filter these out by simply ignoring any temporal invariants of the
     * form x INV y where x or y in {INITIAL, TERMINAL}. This filtering is
     * useful because it relieves us from checking invariants which are true for
     * all graphs produced with typical construction.
     * </p>
     * Note that this filtering, however, could filter out more invariants then
     * the ones above. We rely on unit tests to make sure that the two sets are
     * equivalent.
     * 
     * <pre>
     * TODO: Final graphs should always satisfy all these invariants.
     *       Convert this observation into an extra sanity check.
     * </pre>
     */
    private void filterOutTautologicalInvariants(
            Set<ITemporalInvariant> invariants) {
        Set<ITemporalInvariant> invsToRemove = new LinkedHashSet<ITemporalInvariant>();

        for (ITemporalInvariant inv : invariants) {
            if (!(inv instanceof BinaryInvariant)) {
                continue;
            }
            EventType first = ((BinaryInvariant) inv).getFirst();
            EventType second = ((BinaryInvariant) inv).getSecond();

            if (first.isSpecialEventType() || second.isSpecialEventType()) {
                invsToRemove.add(inv);
            }
        }
        logger.fine("Filtered out " + invsToRemove.size()
                + " tautological invariants.");
        invariants.removeAll(invsToRemove);
    }

    private void addToPartition(Set<EventType> partition, EventNode curNode) {
        partition.add(curNode.getEType());
        for (ITransition<EventNode> childTrans : curNode.getTransitions()) {
            addToPartition(partition, childTrans.getTarget());
        }
    }

    /**
     * Computes the 'INITIAL AFby x' invariants = 'eventually x' invariants. We
     * do this by considering the set of events in one trace, and then removing
     * the events from this set when we do not find them in other traces. <br/>
     * TODO: this code only works for a single relation
     * (TraceParser.defaultRelation)
     * 
     * @param g
     *            graph over LogEvent
     * @return set of InitialAFbyX invariants
     */
    private Set<ITemporalInvariant> computeINITIALAFbyXInvariants(
            IGraph<EventNode> g) {
        Set<ITemporalInvariant> invariants = new LinkedHashSet<ITemporalInvariant>();

        if (g.getInitialNodes().isEmpty()) {
            throw new InternalSynopticException(
                    "Cannot compute invariants over a graph that doesn't have exactly one INITIAL node.");
        }

        EventNode initNode = g.getInitialNodes().iterator().next();
        if (!initNode.getEType().isInitialEventType()) {
            throw new InternalSynopticException(
                    "Cannot compute invariants over a graph that doesn't have exactly one INITIAL node.");
        }

        Set<EventType> eventuallySet = null;

        Map<Integer, Set<EventNode>> traceIdToInitNodes = buildTraceIdToInitNodesMap(initNode);

        // Iterate through all the traces.
        for (Set<EventNode> initTraceNodes : traceIdToInitNodes.values()) {
            Set<EventType> trace = new LinkedHashSet<EventType>();
            // Iterate through the set of initial nodes in each of the traces.
            for (EventNode curNode : initTraceNodes) {
                addToPartition(trace, curNode);
            }

            // Initialize the set with events from the first trace.
            if (eventuallySet == null) {
                eventuallySet = new LinkedHashSet<EventType>(trace);
                for (EventType e : trace) {
                    if (e.isTerminalEventType()) {
                        eventuallySet.remove(e);
                    }
                }
                continue;
            }

            // Now eliminate events from the eventuallySet that do not
            // appear in the traces that follow the first trace.
            for (Iterator<EventType> it = eventuallySet.iterator(); it
                    .hasNext();) {
                EventType e = it.next();
                if (!trace.contains(e)) {
                    it.remove();
                }
            }
        }

        // Based on eventuallySet generate INITIAL AFby x invariants.
        for (EventType eLabel : eventuallySet) {
            invariants.add(new AlwaysFollowedInvariant(StringEventType
                    .NewInitialStringEventType(), eLabel,
                    TraceParser.defaultRelation));
        }

        return invariants;
    }
}
