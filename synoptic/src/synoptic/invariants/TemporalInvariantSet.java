package synoptic.invariants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.fsmcheck.FsmModelChecker;
import synoptic.invariants.ltlchecker.GraphLTLChecker;
import synoptic.main.Main;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.input.GraphBuilder;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

// import daikonizer.Daikonizer;

/**
 * Maintains a set of temporal invariants.
 */
public class TemporalInvariantSet implements Iterable<ITemporalInvariant> {
    private static Logger logger = Logger.getLogger("TemporalInvSet Logger");

    /**
     * Enable Daikon support to extract structural synoptic.invariants (alpha)
     */
    public static boolean generateStructuralInvariants = false;
    LinkedHashSet<ITemporalInvariant> invariants = new LinkedHashSet<ITemporalInvariant>();

    public TemporalInvariantSet() {
    }

    /**
     * Build an invariant set based on an existing invariant set.
     * 
     * @param invariants
     */
    public TemporalInvariantSet(Set<ITemporalInvariant> invariants) {
        this.invariants.addAll(invariants);
    }

    /**
     * @return The set of invariants.
     */
    public Set<ITemporalInvariant> getSet() {
        return invariants;
    }

    /**
     * @return The number of invariants in this set.
     */
    public int numInvariants() {
        return invariants.size();
    }

    public <T extends INode<T>> boolean check(IGraph<T> g) throws Exception {
        TemporalInvariantSet set = computeInvariants(g);
        boolean result = set.invariants.containsAll(invariants);
        if (!result) {
            logger.info(getUnsatisfiedInvariants(g).toString());
        }
        return result;
    }

    public <T extends INode<T>> TemporalInvariantSet getUnsatisfiedInvariants(
            IGraph<T> g) throws Exception {
        TemporalInvariantSet set = computeInvariants(g);
        TemporalInvariantSet res = new TemporalInvariantSet();
        res.invariants.addAll(invariants);
        res.invariants.removeAll(set.invariants);
        return res;
    }

    public void addAll(Collection<ITemporalInvariant> invariants) {
        this.invariants.addAll(invariants);
    }

    public void add(TemporalInvariantSet set) {
        invariants.addAll(set.invariants);
    }

    public Iterator<ITemporalInvariant> iterator() {
        return invariants.iterator();
    }

    @Override
    public String toString() {
        return invariants.toString();
    }

    public void add(ITemporalInvariant inv) {
        invariants.add(inv);
    }

    /**
     * Returns a path that violates a specific invariant in a graph. Uses the
     * model checker designated by the Main.UseFSMChecker variable.
     * 
     * @param <T>
     *            the type of nodes in graph g
     * @param inv
     *            invariant for which we are to find a violating path
     * @param g
     *            the graph within which the violating path must be found
     * @return a path in g that violates inv or null if one doesn't exist
     */
    public <T extends INode<T>> RelationPath<T> getCounterExample(
            ITemporalInvariant inv, IGraph<T> g) {
        TimedTask refinement = PerformanceMetrics.createTask(
                "getCounterExample", true);
        try {
            if (Main.useFSMChecker) {
                return FsmModelChecker.getCounterExample((BinaryInvariant) inv,
                        g);
            } else {
                GraphLTLChecker<T> ch = new GraphLTLChecker<T>();
                return ch.getCounterExample(inv, g);
            }
        } finally {
            refinement.stop();
        }
    }

    /**
     * Returns a list of paths, each of which violates an invariant maintained
     * by this invariant set (i.e. each of which is a counter-example).
     * 
     * @param <T>
     *            the type of nodes in graph g
     * @param graph
     *            the graph within which the violating paths must be found
     * @return a list of violating paths
     */
    public <T extends INode<T>> List<RelationPath<T>> getAllCounterExamples(
            IGraph<T> graph) {
        TimedTask violations = PerformanceMetrics.createTask(
                "getAllCounterExamples", false);
        try {
            List<RelationPath<T>> paths = null;
            if (Main.useFSMChecker) {
                paths = new ArrayList<RelationPath<T>>();
                for (ITemporalInvariant tinv : invariants) {
                    RelationPath<T> path = FsmModelChecker.getCounterExample(
                            (BinaryInvariant) tinv, graph);
                    if (path != null) {
                        paths.add(path);
                    }
                }
            } else {
                // Use the LTL checker instead.
                paths = new ArrayList<RelationPath<T>>();
                GraphLTLChecker<T> checker = new GraphLTLChecker<T>();
                for (ITemporalInvariant inv : invariants) {
                    RelationPath<T> path = checker
                            .getCounterExample(inv, graph);
                    if (path != null) {
                        paths.add(path);
                    }
                }
            }

            if (paths.size() == 0) {
                // Did not find any counter-examples.
                return null;
            }

            Collections.sort(paths, new Comparator<RelationPath<T>>() {
                @Override
                public int compare(RelationPath<T> o1, RelationPath<T> o2) {
                    return new Integer(o1.path.size())
                            .compareTo(o2.path.size());
                }
            });

            return paths;
        } finally {
            violations.stop();
            if (Main.doBenchmarking) {
                logger.info("BENCHM: " + violations.toString());
            }
        }
    }

    /**
     * Returns the first counter-example encountered in the graph g. The order
     * of exploration is unspecified.
     * 
     * @param <T>
     *            the node type
     * @param g
     *            the graph to check
     * @return null if no violation is found, the counter-example path otherwise
     */
    public <T extends INode<T>> RelationPath<T> getFirstCounterExample(
            IGraph<T> g) {
        TimedTask violations = PerformanceMetrics.createTask(
                "getFirstCounterExample", false);
        try {
            if (Main.useFSMChecker) {
                for (ITemporalInvariant tinv : invariants) {
                    RelationPath<T> path = FsmModelChecker.getCounterExample(
                            (BinaryInvariant) tinv, g);
                    if (path != null) {
                        return path;
                    }
                }
                return null;
            } else {
                GraphLTLChecker<T> c = new GraphLTLChecker<T>();
                for (ITemporalInvariant i : invariants) {
                    RelationPath<T> result = c.getCounterExample(i, g);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        } finally {
            violations.stop();
        }
    }

    /**
     * Tests whether two invariant sets are equivalent.
     * 
     * @param set2
     *            the other set to test equality with.
     * @return true if the two sets are equal, false otherwise.
     */
    public boolean sameInvariants(TemporalInvariantSet set2) {
        boolean ret = invariants.containsAll(set2.invariants);
        boolean ret2 = set2.invariants.containsAll(invariants);
        if (!ret || !ret2) {
            ArrayList<ITemporalInvariant> foo = new ArrayList<ITemporalInvariant>();
            foo.addAll(invariants);
            foo.removeAll(set2.invariants);
            logger.fine("Not remotely contained: " + foo);
            foo = new ArrayList<ITemporalInvariant>();
            foo.addAll(set2.invariants);
            foo.removeAll(invariants);
            logger.fine("Not locally contained: " + foo);
        }
        return ret && ret2;
    }

    /**
     * The inclusion of INITIAL and TERMINAL states in the graphs generates the
     * following types of tautological invariants (for all event types X):
     * 
     * <pre>
     * - INITIAL AFby X
     * - INITIAL AP X
     * - X AP TERMINAL
     * - X AFby TERMINAL
     * - TERMINAL NFby INITIAL
     * </pre>
     * 
     * We filter these out by simply ignoring any temporal invariants of the
     * form x INV y where x or y in {INITIAL, TERMINAL}. This filtering is
     * useful because it relieves us from checking invariants which are true for
     * all graphs produced with typical construction.<br />
     * <br />
     * Note that this filtering, however, could filter out more invariants then
     * the ones above. We rely on unit tests to make sure that the two sets are
     * equivalent.
     * 
     * <pre>
     * TODO: Final graphs should always satisfy all these invariants.
     *       Convert this observation into an extra sanity check.
     * </pre>
     */
    public void filterOutTautologicalInvariants() {
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
     * Compute invariants of a graph g. Enumerating all possibly invariants
     * syntactically, and then checking them was considered too costly (although
     * we never benchmarked it!). So we are mining synoptic.invariants from the
     * transitive closure using {@code extractInvariantsForAllRelations}, which
     * is supposed to return an over-approximation of the synoptic.invariants
     * that hold (i.e. it may return synoptic.invariants that do not hold, but
     * may not fail to return an invariant that does not hold)
     * 
     * @param <T>
     *            The node type of the graph
     * @param g
     *            the graph of nodes of type T
     * @return the set of temporal synoptic.invariants the graph satisfies
     */
    static public <T extends INode<T>> TemporalInvariantSet computeInvariants(
            IGraph<T> g) {
        TimedTask mineInvariants = PerformanceMetrics.createTask(
                "mineInvariants", false);
        try {

            TimedTask itc = PerformanceMetrics.createTask(
                    "invariants_transitive_closure", false);
            AllRelationsTransitiveClosure<T> transitiveClosure = new AllRelationsTransitiveClosure<T>(
                    g);

            // Get the over-approximation.
            itc.stop();
            if (Main.doBenchmarking) {
                logger.info("BENCHM: " + itc);
            }
            TimedTask io = PerformanceMetrics.createTask(
                    "invariants_approximation", false);
            TemporalInvariantSet overapproximatedInvariantsSet = extractInvariantsForAllRelations(
                    g, transitiveClosure);
            io.stop();
            if (Main.doBenchmarking) {
                logger.info("BENCHM: " + io);
            }

            return overapproximatedInvariantsSet;
        } finally {
            mineInvariants.stop();
        }
    }

    private static <T extends INode<T>> void printStats(IGraph<T> g,
            TemporalInvariantSet overapproximatedInvariantsSet,
            int overapproximatedInvariantsSetSize) {
        Set<String> labels = new HashSet<String>();
        for (T n : g.getNodes()) {
            labels.add(n.getLabel());
        }
        int possibleInvariants = 3 /* invariant types */
                * labels.size() * labels.size(); /*
                                                  * reflexive
                                                  * synoptic.invariants are
                                                  * allowed
                                                  */

        int percentReduction = possibleInvariants == 0 ? 0 : 100
                - overapproximatedInvariantsSetSize * 100 / possibleInvariants;

        if (Main.doBenchmarking) {
            logger.info("BENCHM: "
                    + overapproximatedInvariantsSet.numInvariants()
                    + " true synoptic.invariants, approximation guessed "
                    + overapproximatedInvariantsSetSize
                    + ", max possible synoptic.invariants "
                    + possibleInvariants + " (" + percentReduction
                    + "% reduction through approximation).");
        }

        PerformanceMetrics.get().record("true_invariants",
                overapproximatedInvariantsSet.numInvariants());
        PerformanceMetrics.get().record("approx_invariants",
                overapproximatedInvariantsSetSize);
        PerformanceMetrics.get().record("max_possible_invariants",
                possibleInvariants);
        PerformanceMetrics.get().record("percentReduction", percentReduction);
    }

    /**
     * Extract synoptic.invariants for all relations, iteratively. Since we are
     * not considering synoptic.invariants over multiple relations, this is
     * sufficient.
     * 
     * @param <T>
     *            the node type of the graph
     * @param g
     *            the graph
     * @param tcs
     *            the transitive closure to mine synoptic.invariants from
     * @return the mined synoptic.invariants
     * @throws Exception
     */
    private static <T extends INode<T>> TemporalInvariantSet extractInvariantsForAllRelations(
            IGraph<T> g, AllRelationsTransitiveClosure<T> tcs) {
        TemporalInvariantSet invariants = new TemporalInvariantSet();
        for (String relation : g.getRelations()) {
            invariants.add(extractInvariants(g, tcs.get(relation), relation));
        }
        return invariants;
    }

    /**
     * Extract an over-approximated set of synoptic.invariants from the
     * transitive closure {@code tc} of the graph {@code g}.
     * 
     * @param <T>
     *            the node type of the graph
     * @param g
     *            the graph
     * @param tc
     *            the transitive closure (of {@code g}) to mine
     *            synoptic.invariants from
     * @param relation
     *            the relation to consider for the synoptic.invariants
     * @return the over-approximated set of synoptic.invariants
     * @throws Exception
     */
    private static <T extends INode<T>> TemporalInvariantSet extractInvariants(
            IGraph<T> g, TransitiveClosure<T> tc, String relation) {
        HashMap<String, ArrayList<T>> partitions = new HashMap<String, ArrayList<T>>();
        for (T m : g.getNodes()) {
            if (!partitions.containsKey(m.getLabel())) {
                partitions.put(m.getLabel(), new ArrayList<T>());
            }
            partitions.get(m.getLabel()).add(m);
        }
        TemporalInvariantSet set = new TemporalInvariantSet();
        for (String label1 : partitions.keySet()) {
            for (String label2 : partitions.keySet()) {
                Set<T> hasPredecessor = new HashSet<T>();
                Set<T> hasNoPredecessor = new HashSet<T>();
                Set<T> isPredecessor = new HashSet<T>();
                Set<T> isNoPredecessor = new HashSet<T>();
                boolean neverFollowed = true;
                boolean alwaysFollowedBy = true;
                boolean alwaysPreceded = true;
                for (T node1 : partitions.get(label1)) {
                    boolean followerFound = false;
                    boolean predecessorFound = false;
                    for (T node2 : partitions.get(label2)) {
                        if (tc.isReachable(node1, node2)) {
                            neverFollowed = false;
                            followerFound = true;
                        }
                        if (tc.isReachable(node2, node1)) {
                            predecessorFound = true;
                            hasPredecessor.add(node1);
                            isPredecessor.add(node2);
                        } else {
                            isNoPredecessor.add(node2);
                        }
                    }
                    if (!followerFound) {
                        alwaysFollowedBy = false;
                    }
                    if (!predecessorFound) {
                        alwaysPreceded = false;
                        hasNoPredecessor.add(node1);
                    }
                }
                if (neverFollowed) {
                    set
                            .add(new NeverFollowedInvariant(label1, label2,
                                    relation));
                }
                if (alwaysFollowedBy) {
                    set.add(new AlwaysFollowedInvariant(label1, label2,
                            relation));
                }
                if (alwaysPreceded) {
                    set.add(new AlwaysPrecedesInvariant(label2, label1,
                            relation));
                } else if (generateStructuralInvariants) {
                    try {
                        // TODO
                        // Daikonizer.generateStructuralInvaraints(hasPredecessor,
                        // hasNoPredecessor,
                        // isPredecessor, isNoPredecessor, partitions, label1,
                        // label2);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return set;
    }

    public Graph<LogEvent> getInvariantGraph(String shortName) {
        HashMap<String, LogEvent> messageMap = new HashMap<String, LogEvent>();
        for (ITemporalInvariant i : invariants) {
            for (String label : i.getPredicates()) {
                if (!messageMap.containsKey(label)) {
                    messageMap.put(label, new LogEvent(new Action(label)));
                }
            }
        }

        for (ITemporalInvariant i : invariants) {
            if (i instanceof BinaryInvariant
                    && (shortName == null || i.getShortName().equals(shortName))) {
                BinaryInvariant bi = (BinaryInvariant) i;
                messageMap.get(bi.getFirst()).addTransition(
                        messageMap.get(bi.getSecond()), bi.getShortName());
            }
        }

        return new Graph<LogEvent>(messageMap.values());
    }

    public static TemporalInvariantSet computeInvariantsSplt(Graph<LogEvent> g,
            String label) throws Exception {
        Graph<LogEvent> g2 = splitAndDuplicate(g, label);
        GraphVizExporter.quickExport("output/traceCondenser/test.dot", g2);
        return computeInvariants(g2);
    }

    private static Graph<LogEvent> splitAndDuplicate(Graph<LogEvent> g,
            String label) {
        GraphBuilder b = new GraphBuilder();
        for (LogEvent m : g.getInitialNodes()) {
            b.split();
            LogEvent cur = m;
            while (cur != null) {
                if (cur.getAction().getLabel().equals(label)) {
                    b.split();
                }
                b.append(cur.getAction());
                if (cur.getTransitions().size() == 1) {
                    cur = cur.getTransitions().iterator().next().getTarget();
                } else {
                    cur = null;
                }
            }
        }
        return b.getGraph();
    }
}
