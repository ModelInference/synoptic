package synoptic.invariants.ltlcheck;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;

public class PersistenceChecker {
    private Counterexample counterexample = null;

    private final Set<Node> visitedOuter = new HashSet<Node>(); // set of
                                                                // visited
    // states in the outer
    // DFS
    private final Set<Node> unvisitedInitials; // set of unvisited initial
                                               // states
    private final Stack<Node> outerDFS = new Stack<Node>(); // stack for the
                                                            // outer DFS
    private final Set<Node> visitedInner = new HashSet<Node>(); // set of
                                                                // visited
    // states in the inner
    // DFS
    private final Stack<Node> innerDFS = new Stack<Node>(); // stack for the
                                                            // inner DFS
    boolean cycleFound = false;

    public PersistenceChecker(GeneralGraph g) {
        unvisitedInitials = g.getInitialNodes();
    }

    public PersistenceChecker(Graph g) {
        unvisitedInitials = new HashSet<Node>();
        unvisitedInitials.add(g.getInit());
    }

    public void run() {
        while (!unvisitedInitials.isEmpty() && !cycleFound) {
            reachableCycle(unvisitedInitials.iterator().next()); // explore the
            // reachable
            // fragment
            // with outer
            // DFS
        }

        if (!cycleFound) {
            counterexample = null; // YES
            return;
        }

        // NO; save counterexample (reverse(V.U))
        // Get prefix
        List<Edge> prefix = new ArrayList<Edge>(outerDFS.size() + 1);
        for (int i = 0; i < outerDFS.size() - 1; ++i) {
            for (Edge e : outerDFS.get(i).getOutgoingEdges()) {
                if (e.getNext().equals(outerDFS.get(i + 1))) {
                    prefix.add(e);
                    break;
                }
            }
        }

        // Add action to get from last state of prefix to first state of cycle
        if (!outerDFS.empty() && !innerDFS.empty()) {
            for (Edge e : outerDFS.get(outerDFS.size() - 1).getOutgoingEdges()) {
                if (e.getNext().equals(innerDFS.get(0))) {
                    prefix.add(e);
                    break;
                }
            }
        }

        // Get cycle
        List<Edge> cycle = new ArrayList<Edge>(innerDFS.size());
        for (int i = 0; i < innerDFS.size() - 1; ++i) {
            for (Edge e : innerDFS.get(i).getOutgoingEdges()) {
                if (e.getNext().equals(innerDFS.get(i + 1))) {
                    cycle.add(e);
                    break;
                }
            }
        }

        // If a suffix of the prefix is part (a "suffix", in fact) of the cycle
        // we found, remove that suffix
        // foundSuffix <=> last transition of cycle is last transition of prefix
        // as well
        boolean foundSuffix = prefix.size() > 0
                && cycle.get(cycle.size() - 1).equals(
                        prefix.get(prefix.size() - 1));
        while (foundSuffix) {
            Edge e = cycle.get(cycle.size() - 1);
            cycle.remove(cycle.size() - 1);
            cycle.add(0, e);
            prefix.remove(prefix.size() - 1);

            foundSuffix = prefix.size() > 0
                    && cycle.get(cycle.size() - 1).equals(
                            prefix.get(prefix.size() - 1));
        }

        // Set counterexample
        counterexample = new Counterexample(prefix, cycle);
    }

    private void reachableCycle(Node n) {
        outerDFS.push(n);
        visitedOuter.add(n);
        unvisitedInitials.remove(n);

        do {
            Node nn = outerDFS.peek();

            // Find an unvisited successor of nn
            Node unvisitedSuccessor = null;
            for (Edge e : nn.getOutgoingEdges()) {
                if (!visitedOuter.contains(e.getNext())) {
                    unvisitedSuccessor = e.getNext();
                    break;
                }
            }

            if (unvisitedSuccessor != null) {
                // successor found, explore
                outerDFS.push(unvisitedSuccessor); // push the unvisited
                // successor on outerDFS
                visitedOuter.add(unvisitedSuccessor); // and mark it visited
                unvisitedInitials.remove(visitedOuter);
            } else {
                // outer DFS is finished for nn
                outerDFS.pop();
                if (nn.getBooleanAttribute("accepting")) {
                    // proceed with the inner DFS in nn
                    cycleFound = cycleCheck(nn);
                }
            }
        } while (!outerDFS.isEmpty() && !cycleFound);
    }

    private boolean cycleCheck(Node n) {
        innerDFS.push(n);
        visitedInner.add(n);

        do {
            Node nn = innerDFS.peek();

            // Check whether we already found a cycle
            for (Edge e : nn.getOutgoingEdges()) {
                if (e.getNext().equals(n)) {
                    // if n in Post(nn), a cycle is found
                    innerDFS.push(n);
                    return true;
                }
            }

            // No cycle found yet
            // Find an unvisited successor of nn
            Node unvisitedSuccessor = null;
            for (Edge e : nn.getOutgoingEdges()) {
                if (!visitedInner.contains(e.getNext())) {
                    unvisitedSuccessor = e.getNext();
                    break;
                }
            }

            if (unvisitedSuccessor != null) {
                // successor found, explore
                innerDFS.push(unvisitedSuccessor); // push the unvisited
                // successor on outerDFS
                visitedInner.add(unvisitedSuccessor); // and mark it visited
            } else {
                // Cycle search unsuccessful for nn
                innerDFS.pop();
            }
        } while (!innerDFS.isEmpty());

        return false;
    }

    public Counterexample getCounterexample() {
        return counterexample;
    }
}
