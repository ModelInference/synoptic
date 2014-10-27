package synoptic.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

/**
 * An implementation of Tarjan's algorithm for finding the SCC of a graph.
 * Description available at http://en.wikipedia.org/wiki/Tarjan%27
 * s_strongly_connected_components_algorithm
 * 
 * @param <NodeType>
 *            The node type used in the graph
 */
public class StronglyConnectedComponents<NodeType extends INode<NodeType>>
        implements Iterable<Set<NodeType>> {
    private final LinkedHashMap<NodeType, Integer> index = new LinkedHashMap<NodeType, Integer>();
    private final LinkedHashMap<NodeType, Integer> lowlink = new LinkedHashMap<NodeType, Integer>();
    private final Stack<NodeType> stack = new Stack<NodeType>();
    private int currentIndex = 0;
    private final List<Set<NodeType>> sccs = new ArrayList<Set<NodeType>>();

    /**
     * Computes the strongly connected components and stores them in the object.
     * 
     * @param graph
     *            the graph to compute the SCCs for
     */
    public StronglyConnectedComponents(IGraph<NodeType> graph) {
        for (NodeType n : graph.getNodes()) {
            if (!index.containsKey(n)) {
                tarjan(n);
            }
        }
    }

    /**
     * The synoptic.main worker function. See documentation at <a href=
     * "http://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm"
     * >Tarjan's algorithm</a>.
     * 
     * @param n
     */
    private void tarjan(NodeType n) {
        index.put(n, currentIndex);
        lowlink.put(n, currentIndex);
        ++currentIndex;
        stack.push(n);
        for (ITransition<NodeType> t : n.getAllTransitions()) {
            if (!index.containsKey(t.getTarget())) {
                tarjan(t.getTarget());
                lowlink.put(n,
                        Math.min(lowlink.get(n), lowlink.get(t.getTarget())));
            } else if (stack.contains(t.getTarget())) {
                lowlink.put(n,
                        Math.min(lowlink.get(n), index.get(t.getTarget())));
            }
        }
        if (lowlink.get(n) == index.get(n)) {
            LinkedHashSet<NodeType> scc = new LinkedHashSet<NodeType>();
            for (NodeType m = stack.pop(); !m.equals(n); m = stack.pop()) {
                scc.add(m);
            }
            scc.add(n);
            sccs.add(scc);
        }
    }

    /**
     * Return the list of SCCs in the graph.
     * 
     * @return a list of the strongly connected components
     */
    public List<Set<NodeType>> getSCCs() {
        return sccs;
    }

    /**
     * Provided for convenience in for-each loops.
     */
    @Override
    public Iterator<Set<NodeType>> iterator() {
        return sccs.iterator();
    }
}
