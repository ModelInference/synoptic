package synoptic.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Set;

import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.Pair;

/**
 * Topological sort. Algorithm from wikipedia.
 * 
 * @param <NodeType>
 */
public class TopologicalSort<NodeType extends INode<NodeType>> {
    LinkedHashMap<Integer, Set<NodeType>> lattice = new LinkedHashMap<Integer, Set<NodeType>>();
    ArrayList<NodeType> sort = new ArrayList<NodeType>();

    public TopologicalSort(IGraph<NodeType> graph) {
        sort(graph);
    }

    public ArrayList<NodeType> getSort() {
        return sort;
    }

    public LinkedHashMap<Integer, Set<NodeType>> getLattice() {
        return lattice;
    }

    private void sort(IGraph<NodeType> graph) {
        // Pair is parameterized with <int, NodeType> = <level, node>
        PriorityQueue<Pair<Integer, NodeType>> active = new PriorityQueue<Pair<Integer, NodeType>>(
                10, new Comparator<Pair<Integer, NodeType>>() {
                    @Override
                    public int compare(Pair<Integer, NodeType> arg0,
                            Pair<Integer, NodeType> arg1) {
                        return Integer.valueOf(arg0.getLeft()).compareTo(
                                arg1.getLeft());
                    }
                });
        for (NodeType n : getSourceNodes(graph)) {
            active.add(new Pair<Integer, NodeType>(0, n));
        }
        LinkedHashSet<ITransition<NodeType>> seen = new LinkedHashSet<ITransition<NodeType>>();
        while (!active.isEmpty()) {
            Pair<Integer, NodeType> pair = active.poll();
            sort.add(pair.getRight());
            if (!lattice.containsKey(pair.getLeft())) {
                lattice.put(pair.getLeft(), new LinkedHashSet<NodeType>());
            }
            lattice.get(pair.getLeft()).add(pair.getRight());
            for (ITransition<NodeType> t : pair.getRight().getAllTransitions()) {
                if (!seen.add(t)) {
                    continue;
                }
                if (containsAllIncommingTransitions(graph, seen, t.getTarget())) {
                    active.add(new Pair<Integer, NodeType>(pair.getLeft() + 1,
                            t.getTarget()));
                }
            }
        }
    }

    private boolean containsAllIncommingTransitions(IGraph<NodeType> graph,
            LinkedHashSet<ITransition<NodeType>> seen, NodeType target) {
        for (NodeType node : graph.getNodes()) {
            for (ITransition<NodeType> t : node.getAllTransitions()) {
                if (!seen.contains(t) && t.getTarget() == target) {
                    return false;
                }
            }
        }
        return true;
    }

    public static <T extends INode<T>> Set<T> getSourceNodes(IGraph<T> graph) {
        Set<T> sources = new LinkedHashSet<T>(graph.getNodes());
        for (T node : graph.getNodes()) {
            for (ITransition<T> t : node.getAllTransitions()) {
                sources.remove(t.getTarget());
            }
        }
        return sources;
    }
}
