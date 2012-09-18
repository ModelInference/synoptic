package synoptic.invariants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.fsmcheck.FsmModelChecker;
import synoptic.invariants.ltlchecker.GraphLTLChecker;
import synoptic.main.SynopticMain;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

/**
 * Maintains a set of temporal invariants.
 */
public class TemporalInvariantSet implements Iterable<ITemporalInvariant> {
    private static Logger logger = Logger.getLogger("TemporalInvSet Logger");

    static {
        logger.setLevel(Level.FINE);
    }

    LinkedHashSet<ITemporalInvariant> invariants = new LinkedHashSet<ITemporalInvariant>();

    public TemporalInvariantSet() {
        // Empty constructor for use by unit tests.
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

    public void remove(ITemporalInvariant invariant) {
        invariants.remove(invariant);
    }

    public void addAll(Collection<ITemporalInvariant> invs) {
        this.invariants.addAll(invs);
    }

    /**
     * Removes all invariants from the set.
     */
    public void removeAll(Collection<ITemporalInvariant> invs) {
        this.invariants.removeAll(invs);
    }

    public void add(TemporalInvariantSet set) {
        invariants.addAll(set.invariants);
    }

    @Override
    public Iterator<ITemporalInvariant> iterator() {
        return invariants.iterator();
    }

    @Override
    public String toString() {
        return invariants.toString();
    }

    /**
     * Prints list of invariants separated by newlines.
     */
    public String toPrettyString() {
        String ret = "";
        for (ITemporalInvariant inv : invariants) {
            ret += (inv.toString() + "\n");
        }
        return ret;
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
    public static <T extends INode<T>> CExamplePath<T> getCounterExample(
            ITemporalInvariant inv, IGraph<T> g) {
        TimedTask refinement = PerformanceMetrics.createTask(
                "getCounterExample", true);
        try {
            if (SynopticMain.getInstanceWithExistenceCheck().options.useFSMChecker) {
                return FsmModelChecker.getCounterExample((BinaryInvariant) inv,
                        g);
            }
            GraphLTLChecker<T> ch = new GraphLTLChecker<T>();
            return ch.getCounterExample(inv, g);
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
    public <T extends INode<T>> List<CExamplePath<T>> getAllCounterExamples(
            IGraph<T> graph) {
        TimedTask violations = PerformanceMetrics.createTask(
                "getAllCounterExamples", false);
        SynopticMain syn = SynopticMain.getInstanceWithExistenceCheck();
        try {
            List<CExamplePath<T>> paths = null;
            if (syn.options.useFSMChecker) {
                paths = new ArrayList<CExamplePath<T>>();
                for (ITemporalInvariant tinv : invariants) {
                    CExamplePath<T> path = FsmModelChecker.getCounterExample(
                            (BinaryInvariant) tinv, graph);
                    if (path != null) {
                        paths.add(path);
                    }
                }
            } else {
                // Use the LTL checker instead.
                paths = new ArrayList<CExamplePath<T>>();
                GraphLTLChecker<T> checker = new GraphLTLChecker<T>();
                for (ITemporalInvariant inv : invariants) {
                    CExamplePath<T> path = checker
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

            Collections.sort(paths, new Comparator<CExamplePath<T>>() {
                @Override
                public int compare(CExamplePath<T> o1, CExamplePath<T> o2) {
                    return Integer.valueOf(o1.path.size()).compareTo(
                            o2.path.size());
                }
            });

            return paths;
        } finally {
            violations.stop();
            if (syn.options.doBenchmarking) {
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
    public <T extends INode<T>> CExamplePath<T> getFirstCounterExample(
            IGraph<T> g) {
        TimedTask violations = PerformanceMetrics.createTask(
                "getFirstCounterExample", false);
        try {
            if (SynopticMain.getInstanceWithExistenceCheck().options.useFSMChecker) {
                for (ITemporalInvariant tinv : invariants) {
                    CExamplePath<T> path = FsmModelChecker.getCounterExample(
                            (BinaryInvariant) tinv, g);
                    if (path != null) {
                        return path;
                    }
                }
                return null;
            }
            GraphLTLChecker<T> c = new GraphLTLChecker<T>();
            for (ITemporalInvariant i : invariants) {
                CExamplePath<T> result = c.getCounterExample(i, g);
                if (result != null) {
                    return result;
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
        // TODO: is using containsAll correct? (it relies on HashCode)
        // shouldn't we use equals() instead?
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

    private static <T extends INode<T>> void printStats(IGraph<T> g,
            TemporalInvariantSet overapproximatedInvariantsSet,
            int overapproximatedInvariantsSetSize) {
        Set<EventType> labels = new LinkedHashSet<EventType>();
        for (T n : g.getNodes()) {
            labels.add(n.getEType());
        }
        int possibleInvariants = 3 /* invariant types */
                * labels.size() * labels.size(); /*
                                                  * reflexive invariants are
                                                  * allowed
                                                  */

        int percentReduction = possibleInvariants == 0 ? 0 : 100
                - overapproximatedInvariantsSetSize * 100 / possibleInvariants;

        if (SynopticMain.getInstanceWithExistenceCheck().options.doBenchmarking) {
            logger.info("BENCHM: "
                    + overapproximatedInvariantsSet.numInvariants()
                    + " true invariants, approximation guessed "
                    + overapproximatedInvariantsSetSize
                    + ", max possible invariants " + possibleInvariants + " ("
                    + percentReduction + "% reduction through approximation).");
        }

        PerformanceMetrics.get().record("true_invariants",
                overapproximatedInvariantsSet.numInvariants());
        PerformanceMetrics.get().record("approx_invariants",
                overapproximatedInvariantsSetSize);
        PerformanceMetrics.get().record("max_possible_invariants",
                possibleInvariants);
        PerformanceMetrics.get().record("percentReduction", percentReduction);
    }

    // public TraceGraph getInvariantGraph(String shortName) {
    // LinkedHashMap<EventType, EventNode> messageMap = new
    // LinkedHashMap<EventType, EventNode>();
    // for (ITemporalInvariant i : invariants) {
    // for (EventType label : i.getPredicates()) {
    // if (!messageMap.containsKey(label)) {
    // messageMap.put(label, new EventNode(new Event(label)));
    // }
    // }
    // }
    //
    // for (ITemporalInvariant i : invariants) {
    // if (i instanceof BinaryInvariant
    // && (shortName == null || i.getShortName().equals(shortName))) {
    // BinaryInvariant bi = (BinaryInvariant) i;
    // messageMap.get(bi.getFirst()).addTransition(
    // messageMap.get(bi.getSecond()), bi.getShortName());
    // }
    // }
    //
    // return new TraceGraph(messageMap.values());
    // }

    /**
     * Outputs the set of invariants to a file -- one invariant per line, and
     * the set of invariants is canonically sorted.
     * 
     * @param fileName
     *            The filename to use for outputting the invariants.
     * @throws FileNotFoundException
     */
    public void outputToFile(String fileName) throws FileNotFoundException {
        LinkedList<String> invariantsStr = new LinkedList<String>();
        // Construct a list of invariants' String representations
        for (ITemporalInvariant inv : invariants) {
            invariantsStr.add(inv.toString());
        }
        // Sort the list of string invariants and output it to the file.
        Collections.sort(invariantsStr);
        File f = new File(fileName);
        PrintWriter writer = new PrintWriter(f);
        for (String s : invariantsStr) {
            writer.write(s + "\n");
        }
        writer.close();
    }
}
