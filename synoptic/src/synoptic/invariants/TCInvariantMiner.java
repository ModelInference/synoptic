package synoptic.invariants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.main.Main;
import synoptic.main.TraceParser;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.interfaces.IGraph;

public class TCInvariantMiner extends InvariantMiner {

    /**
     * Whether or not to use iterative version of warshall's algorithm for TC
     * computation. Yes by default.
     */
    public boolean useWarshall = true;

    public TCInvariantMiner() {
    }

    public TCInvariantMiner(boolean useWarshall) {
        this.useWarshall = useWarshall;
    }

    @Override
    public TemporalInvariantSet computeInvariants(IGraph<LogEvent> g) {
        return computeInvariants(g, true);
    }

    /**
     * Compute invariants of a graph g by mining invariants from the transitive
     * closure using {@code extractInvariantsUsingTC}, which returns an
     * over-approximation of the invariants that hold (i.e. it may return
     * invariants that do not hold, but may not fail to return an invariant that
     * does not hold)
     * 
     * @param <T>
     *            The node type of the graph
     * @param g
     *            the graph of nodes of type T
     * @param filterTautological
     *            whether or not tautological invariants should be filtered out
     * @return the set of temporal invariants the graph satisfies
     */
    public TemporalInvariantSet computeInvariants(IGraph<LogEvent> g,
            boolean filterTautological) {
        // Compute the invariants of the input graph.
        Set<ITemporalInvariant> invariants = computeInvariantsFromTC(g);
        if (filterTautological) {
            filterOutTautologicalInvariants(invariants);
            for (ITemporalInvariant inv : computeINITIALAFbyXInvariants(g)) {
                invariants.add(inv);
            }
        }
        return new TemporalInvariantSet(invariants);

    }

    private Set<ITemporalInvariant> computeInvariantsFromTC(IGraph<LogEvent> g) {
        TimedTask mineInvariants = PerformanceMetrics.createTask(
                "mineInvariants", false);
        try {

            TimedTask itc = PerformanceMetrics.createTask(
                    "invariants_transitive_closure", false);
            AllRelationsTransitiveClosure<LogEvent> transitiveClosure = new AllRelationsTransitiveClosure<LogEvent>(
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
            Set<ITemporalInvariant> overapproximatedInvariantsSet = new LinkedHashSet<ITemporalInvariant>();
            for (String relation : g.getRelations()) {
                for (ITemporalInvariant inv : extractInvariantsFromTC(g,
                        transitiveClosure.get(relation), relation)) {
                    overapproximatedInvariantsSet.add(inv);
                }
            }

            io.stop();
            if (Main.doBenchmarking) {
                logger.info("BENCHM: " + io);
            }

            return overapproximatedInvariantsSet;
        } finally {
            mineInvariants.stop();
        }
    }

    /**
     * Extract an over-approximated set of invariants from the transitive
     * closure {@code tc} of the graph {@code g}.
     * 
     * @param <T>
     *            the node type of the graph
     * @param g
     *            the graph
     * @param tc
     *            the transitive closure (of {@code g}) to mine invariants from
     * @param relation
     *            the relation to consider for the invariants
     * @return the over-approximated set of invariants
     * @throws Exception
     */
    private static Set<ITemporalInvariant> extractInvariantsFromTC(
            IGraph<LogEvent> g, TransitiveClosure<LogEvent> tc, String relation) {
        LinkedHashMap<String, ArrayList<LogEvent>> partitions = new LinkedHashMap<String, ArrayList<LogEvent>>();

        // Initialize the partitions map: each unique label maps to a list of
        // nodes with that label.
        for (LogEvent m : g.getNodes()) {
            if (!partitions.containsKey(m.getLabel())) {
                partitions.put(m.getLabel(), new ArrayList<LogEvent>());
            }
            partitions.get(m.getLabel()).add(m);
        }

        Set<ITemporalInvariant> set = new LinkedHashSet<ITemporalInvariant>();
        for (String label1 : partitions.keySet()) {
            for (String label2 : partitions.keySet()) {
                boolean neverFollowed = true;
                boolean alwaysFollowedBy = true;
                boolean alwaysPreceded = true;
                for (LogEvent node1 : partitions.get(label1)) {
                    boolean followerFound = false;
                    boolean predecessorFound = false;
                    for (LogEvent node2 : partitions.get(label2)) {
                        if (tc.isReachable(node1, node2)) {
                            neverFollowed = false;
                            followerFound = true;
                        }
                        if (tc.isReachable(node2, node1)) {
                            predecessorFound = true;
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
                }
                if (neverFollowed) {
                    set.add(new NeverFollowedInvariant(label1, label2, relation));
                }
                if (alwaysFollowedBy) {
                    set.add(new AlwaysFollowedInvariant(label1, label2,
                            relation));
                }
                if (alwaysPreceded) {
                    set.add(new AlwaysPrecedesInvariant(label2, label1,
                            relation));
                }
            }
        }
        return set;
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
        LinkedHashSet<ITemporalInvariant> invsToRemove = new LinkedHashSet<ITemporalInvariant>();

        LinkedHashSet<String> specialNodes = new LinkedHashSet<String>();
        specialNodes.add(Main.terminalNodeLabel);
        specialNodes.add(Main.initialNodeLabel);

        for (ITemporalInvariant inv : invariants) {
            if (!(inv instanceof BinaryInvariant)) {
                continue;
            }
            String first = ((BinaryInvariant) inv).getFirst();
            String second = ((BinaryInvariant) inv).getSecond();

            if (specialNodes.contains(first) || specialNodes.contains(second)) {
                invsToRemove.add(inv);
            }
        }
        logger.fine("Filtered out " + invsToRemove.size()
                + " tautological invariants.");
        invariants.removeAll(invsToRemove);
    }

    /**
     * Computes the 'INITIAL AFby x' invariants = 'eventually x' invariants. We
     * do this by considering the set of events in one partition, and then
     * removing the events from this set when we do not find them in other
     * partitions. <br/>
     * TODO: this code only works for a single relation
     * (TraceParser.defaultRelation)
     * 
     * @param <T>
     * @param g
     * @return
     */
    private Set<ITemporalInvariant> computeINITIALAFbyXInvariants(
            IGraph<LogEvent> g) {
        LinkedHashSet<ITemporalInvariant> invariants = new LinkedHashSet<ITemporalInvariant>();

        if (!g.getInitialNodes().isEmpty()) {
            LogEvent initNode = g.getInitialNodes().iterator().next();
            if (initNode.getLabel().equals(Main.initialNodeLabel)) {
                if (g instanceof Graph) {
                    LinkedHashMap<String, ArrayList<LogEvent>> partitions = ((Graph<LogEvent>) g)
                            .getPartitions();
                    // NOTE: this would be more efficient if the values in
                    // the map
                    // were sets.
                    LinkedHashSet<String> eventuallySet = null;
                    Iterator<ArrayList<LogEvent>> pIter = partitions.values()
                            .iterator();
                    if (pIter.hasNext()) {
                        // Initialize the set with events from the first
                        // partition.
                        eventuallySet = new LinkedHashSet<String>();
                        for (LogEvent e : pIter.next()) {
                            if (!eventuallySet.contains(e.getLabel())) {
                                eventuallySet.add(e.getLabel());
                            }
                        }
                    }

                    // Now eliminate events from the set that do not appear
                    // in all other partitions.
                    LinkedHashSet<String> eventuallySetNew = null;
                    while (pIter.hasNext()) {
                        eventuallySetNew = new LinkedHashSet<String>();
                        List<LogEvent> partition = pIter.next();

                        for (LogEvent e : partition) {
                            if (eventuallySet.contains(e.getLabel())) {
                                eventuallySetNew.add(e.getLabel());
                            }
                        }
                        eventuallySet = eventuallySetNew;
                    }
                    // Based on eventuallySet generate INITIAL AFby x
                    // invariants.
                    for (String eLabel : eventuallySet) {
                        invariants.add(new AlwaysFollowedInvariant(
                                Main.initialNodeLabel, eLabel,
                                TraceParser.defaultRelation));
                    }
                }
            }
        }
        return invariants;
    }
}
