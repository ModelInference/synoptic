package synoptic.invariants.miners;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import synoptic.algorithms.TransitiveClosure;
import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.concurrency.AlwaysConcurrentInvariant;
import synoptic.invariants.concurrency.NeverConcurrentInvariant;
import synoptic.invariants.ltlcheck.Pair;
import synoptic.main.SynopticMain;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.TraceGraph;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;

/**
 * Implements an invariant miner for both totally and partially ordered traces
 * that first creates a transitive closure of the TraceGraph, and then considers
 * the edges in the transitive closure to mine invariants. For example, if every
 * instance of EventNode of type "a" has an edge in the transitive closure to an
 * EventNode of type "b", then a AlwaysFollowedBy b is an invariant of the
 * TraceGraph.
 */
public class TransitiveClosureInvMiner extends InvariantMiner implements
        IPOInvariantMiner, ITOInvariantMiner {

    /**
     * Whether or not to use iterative version of warshall's algorithm for TC
     * computation. Yes/true by default.
     */
    public boolean useWarshall = true;

    public TransitiveClosureInvMiner() {
        // Empty constructor for testing.
    }

    public TransitiveClosureInvMiner(boolean useWarshall) {
        this.useWarshall = useWarshall;
    }

    @Override
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations) {
        return computeTransClosureInvariants(g, false);
    }

    @Override
    public TemporalInvariantSet computeInvariants(DAGsTraceGraph g) {
        return computeTransClosureInvariants(g, true);
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
     * @param mineConcurrencyInvariants
     *            whether or not to also mine concurrency invariants
     * @return the set of temporal invariants the graph satisfies
     */
    public TemporalInvariantSet computeTransClosureInvariants(TraceGraph<?> g,
            boolean mineConcurrencyInvariants) {

        TimedTask mineInvariants = PerformanceMetrics.createTask(
                "mineInvariants", false);
        Set<ITemporalInvariant> overapproximatedInvariantsSet;

        SynopticMain syn = SynopticMain.getInstanceWithExistenceCheck();

        // Compute the over-approximated set of invariants for the input graph.
        try {

            TimedTask itc = PerformanceMetrics.createTask(
                    "invariants_transitive_closure", false);

            // Compute the transitive closure.
            AllRelationsTransitiveClosure transitiveClosure = new AllRelationsTransitiveClosure(
                    g);

            // Get the over-approximation.
            itc.stop();
            if (syn.options.doBenchmarking) {
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
            if (syn.options.doBenchmarking) {
                logger.info("BENCHM: " + io);
            }
            // logger.info("Over-approx set: "
            // + overapproximatedInvariantsSet.toString());
        } finally {
            mineInvariants.stop();
        }

        return new TemporalInvariantSet(overapproximatedInvariantsSet);
    }

    /**
     * Maintains state to summarize the ordering relationship between two event
     * types in the transitive closure. This summary is generated by
     * summarizeOrderings() and used in extractInvariantsFromTC().
     */
    private static class EventOrderingSummary {
        public boolean neverFollowedBy = true;
        public boolean alwaysFollowedBy = true;
        public boolean alwaysPrecedes = true;
        // Whether or not every instance of e1 is totally ordered or
        // never ordered with respect to every instance of e2.
        public boolean alwaysOrdered = true;
        public boolean neverOrdered = true;

        public EventOrderingSummary() {
            // Nothing to do here.
        }

        /**
         * Returns true if this ordering summary can no longer change by
         * considering more traces.
         */
        public boolean fixedPoint() {
            return (!neverFollowedBy && !alwaysFollowedBy && !alwaysPrecedes
                    && !alwaysOrdered && !neverOrdered);
        }
    }

    /**
     * Assumes that e1 and e2 are both not initial/terminal event types.
     * 
     * @param traceIdToEventNodesE1
     * @param traceIdToEventNodesE2
     * @param tc
     * @return
     */
    private EventOrderingSummary summarizeOrderings(
            Map<Integer, List<EventNode>> traceIdToEventNodesE1,
            Map<Integer, List<EventNode>> traceIdToEventNodesE2,
            TransitiveClosure tc) {
        EventOrderingSummary order = new EventOrderingSummary();

        for (int tid : traceIdToEventNodesE1.keySet()) {
            // Do not iterate if there are no e2 instances in the trace tid

            for (EventNode node1 : traceIdToEventNodesE1.get(tid)) {
                boolean followerFound = false;
                boolean predecessorFound = false;

                if (traceIdToEventNodesE2.containsKey(tid)) {
                    for (EventNode node2 : traceIdToEventNodesE2.get(tid)) {
                        if (node1 == node2) {
                            continue;
                        }

                        if (tc.isReachable(node1, node2)) {
                            order.neverFollowedBy = false;
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
                            order.alwaysOrdered = false;
                        }
                    }
                }

                // Every node instance with label1 must be followed by a
                // node instance with label2 for label1 AFby label2 to be
                // true.
                if (!followerFound) {
                    order.alwaysFollowedBy = false;
                }
                // Every node instance with label1 must be preceded by a
                // node instance with label2 for label2 AP label1 to be
                // true.
                if (!predecessorFound) {
                    order.alwaysPrecedes = false;
                }

                if (followerFound || predecessorFound) {
                    order.neverOrdered = false;
                }

                // Optimization: if no possibly trace can change the outcome of
                // the ordering summary we have gathered so far, then stop and
                // exit with the summary that we have.
                if (order.fixedPoint()) {
                    return order;
                }
            }
        }

        return order;
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
    private Set<ITemporalInvariant> extractInvariantsFromTC(TraceGraph<?> g,
            TransitiveClosure tc, String relation,
            boolean mineConcurrencyInvariants) {

        // This maintains the mapping from event type to a map of trace ids ->
        // list of event instances in the trace. This is used to check
        // reachability only between event instances that are in the same trace.
        Map<EventType, Map<Integer, List<EventNode>>> etypeToTraceIdToENode = new LinkedHashMap<EventType, Map<Integer, List<EventNode>>>();

        // Initialize the partitions map: each unique label maps to a list of
        // nodes with that label.
        for (EventNode node : g.getNodes()) {
            if (node.getEType().isSpecialEventType()) {
                /**
                 * The inclusion of INITIAL and TERMINAL states in the graphs
                 * generates the following types of "tautological" invariants
                 * (for all event types X):
                 * 
                 * <pre>
                 * - x AP TERMINAL
                 * - INITIAL AP x
                 * - x AP TERMINAL
                 * - x AFby TERMINAL
                 * - TERMINAL NFby INITIAL
                 * </pre>
                 * <p>
                 * NOTE: x AP TERMINAL is not actually a tautological invariant,
                 * but we do not mine/include it because we instead use x AFby
                 * INITIAL.
                 * </p>
                 * <p>
                 * We filter these out by simply ignoring any temporal
                 * invariants of the form x INV y where x or y in {INITIAL,
                 * TERMINAL}. This is useful because it relieves us from
                 * checking/reporting invariants which are true for all graphs
                 * produced with typical construction.
                 **/
                continue;
            }
            Map<Integer, List<EventNode>> map;
            EventType etype = node.getEType();
            if (!etypeToTraceIdToENode.containsKey(etype)) {
                map = new LinkedHashMap<Integer, List<EventNode>>();
                etypeToTraceIdToENode.put(etype, map);
            } else {
                map = etypeToTraceIdToENode.get(etype);
            }

            List<EventNode> list;
            int tid = node.getTraceID();
            if (!map.containsKey(tid)) {
                list = new LinkedList<EventNode>();
                map.put(tid, list);
            } else {
                list = map.get(tid);
            }
            list.add(node);
        }

        Set<ITemporalInvariant> pathInvs = new LinkedHashSet<ITemporalInvariant>();
        Set<ITemporalInvariant> neverConcurInvs = new LinkedHashSet<ITemporalInvariant>();
        Set<ITemporalInvariant> alwaysConcurInvs = new LinkedHashSet<ITemporalInvariant>();

        Set<Pair<EventType, EventType>> observedPairs = new LinkedHashSet<Pair<EventType, EventType>>();
        int numTraces = g.getNumTraces();
        for (Entry<EventType, Map<Integer, List<EventNode>>> e1Entry : etypeToTraceIdToENode
                .entrySet()) {
            EventType e1 = e1Entry.getKey();
            // ///////////////// Determine if "INITIAL AFby e1" is true
            // Check if an e1 node appeared in every trace, if yes then inv
            // true.
            if (e1Entry.getValue().keySet().size() == numTraces) {
                pathInvs.add(new AlwaysFollowedInvariant(StringEventType
                        .newInitialStringEventType(), e1,
                        Event.defTimeRelationStr));
            }
            // /////////////////

            for (Entry<EventType, Map<Integer, List<EventNode>>> e2Entry : etypeToTraceIdToENode
                    .entrySet()) {
                EventType e2 = e2Entry.getKey();
                // If we have done (e1,e2) then do not do (e2,e1) because for
                // pair (e1,e2) we derive orderings and invariants for both
                // (e1,e2) and (e2,e1) -- this is done so that we can do correct
                // subsumption of invariants (and concurrency invariants require
                // symmetrical information).
                if (observedPairs.contains(new Pair<EventType, EventType>(e2,
                        e1))) {
                    continue;
                }
                observedPairs.add(new Pair<EventType, EventType>(e1, e2));

                // ///////////////////////////////
                // Derive the ordering summary between each instance of e1 and
                // every instance of e2.
                EventOrderingSummary E1orderE2 = summarizeOrderings(
                        e1Entry.getValue(), e2Entry.getValue(), tc);
                // Do same for e2,e1.
                EventOrderingSummary E2orderE1 = summarizeOrderings(
                        e2Entry.getValue(), e1Entry.getValue(), tc);
                // ///////////////////////////////

                // Whether or not never ordered invariant was added --
                // determines whether or not NFby invariants are added
                // below.
                boolean addedNeverOrdered = false;

                if (mineConcurrencyInvariants) {
                    // Ignore local versions of alwaysOrdered and
                    // neverOrdered since they are trivially true and false
                    // respectively.
                    if (!((DistEventType) e1).getProcessName().equals(
                            ((DistEventType) e2).getProcessName())) {
                        // Because lack of order is symmetric, it doesn't matter
                        // if we use E1orderE2.neverOrdered or
                        // E2orderE1.neverOrdered.
                        if (E1orderE2.neverOrdered) {
                            alwaysConcurInvs.add(new AlwaysConcurrentInvariant(
                                    (DistEventType) e2, (DistEventType) e1,
                                    relation));
                            addedNeverOrdered = true;
                        }
                        // Because complete order is symmetric, it doesn't
                        // matter if we use E1orderE2.alwaysOrdered or
                        // E2orderE1.alwaysOrdered.
                        if (E1orderE2.alwaysOrdered
                                && !E1orderE2.alwaysPrecedes
                                && !E1orderE2.alwaysFollowedBy
                                && !E2orderE1.alwaysPrecedes
                                && !E2orderE1.alwaysFollowedBy) {
                            neverConcurInvs.add(new NeverConcurrentInvariant(
                                    (DistEventType) e2, (DistEventType) e1,
                                    relation));
                        }
                    }
                }

                if (!addedNeverOrdered) {
                    // Note that ACWith subsumes NFby, which is why we add NFby
                    // if not(ACWith).
                    if (E1orderE2.neverFollowedBy) {
                        pathInvs.add(new NeverFollowedInvariant(e1, e2,
                                relation));
                    }
                    if (E2orderE1.neverFollowedBy) {
                        pathInvs.add(new NeverFollowedInvariant(e2, e1,
                                relation));
                    }
                }

                if (E1orderE2.alwaysFollowedBy) {
                    pathInvs.add(new AlwaysFollowedInvariant(e1, e2, relation));
                }
                if (E2orderE1.alwaysFollowedBy) {
                    pathInvs.add(new AlwaysFollowedInvariant(e2, e1, relation));
                }

                if (E1orderE2.alwaysPrecedes) {
                    pathInvs.add(new AlwaysPrecedesInvariant(e2, e1, relation));
                }
                if (E2orderE1.alwaysPrecedes) {
                    pathInvs.add(new AlwaysPrecedesInvariant(e1, e2, relation));
                }
            }
        }

        // Merge the concurrency and path invariant sets.
        pathInvs.addAll(neverConcurInvs);
        pathInvs.addAll(alwaysConcurInvs);
        return pathInvs;
    }
}
