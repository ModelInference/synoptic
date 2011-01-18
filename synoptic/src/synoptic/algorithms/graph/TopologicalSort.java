package synoptic.algorithms.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import synoptic.util.Pair;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

/**
 * Topological sort. Algorithm from wikipedia.
 * @author sigurd
 *
 * @param <NodeType>
 */
public class TopologicalSort<NodeType extends INode<NodeType>> {
	HashMap<Integer, Set<NodeType>> lattice = new HashMap<Integer, Set<NodeType>>();
	ArrayList<NodeType> sort = new ArrayList<NodeType>();

	public TopologicalSort(IGraph<NodeType> graph) {
		sort(graph);
	}

	public ArrayList<NodeType> getSort() {
		return sort;
	}
	
	public HashMap<Integer, Set<NodeType>> getLattice() {
		return lattice;
	}

	private void visit(NodeType n, Set<NodeType> seen, int level) {
		if (!seen.add(n))
			return;

		if (!lattice.containsKey(level)) {
			lattice.put(level, new HashSet<NodeType>());
		}
		lattice.get(level).add(n);
		
		for (ITransition<NodeType> t : n.getTransitionsIterator()) {
			visit(t.getTarget(), seen, level+1);
		}
		sort.add(0,n);
	}
	
	private void sort(IGraph<NodeType> graph) {
		// Pair is parameterized with <int, NodeType> = <level, node>
		PriorityQueue<Pair<Integer, NodeType>> active = new PriorityQueue<Pair<Integer, NodeType>>(10, new Comparator<Pair<Integer, NodeType>>() {
			@Override
			public int compare(Pair<Integer, NodeType> arg0, Pair<Integer, NodeType> arg1) {
				return new Integer(arg0.getLeft()).compareTo(arg1.getLeft());
			}
		});
		for (NodeType n : getSourceNodes(graph))
			active.add(new Pair<Integer, NodeType>(0, n));
		HashSet<ITransition<NodeType>> seen = new HashSet<ITransition<NodeType>>();
		while (!active.isEmpty()) {
			Pair<Integer, NodeType> pair = active.poll();
			sort.add(pair.getRight());
			if (!lattice.containsKey(pair.getLeft()))
				lattice.put(pair.getLeft(), new HashSet<NodeType>());
			lattice.get(pair.getLeft()).add(pair.getRight());
			for (ITransition<NodeType> t : pair.getRight().getTransitionsIterator()) {
				if (!seen.add(t))
					continue;
				if (containsAllIncommingTransitions(graph, seen, t.getTarget()))
					active.add(new Pair<Integer, NodeType>(pair.getLeft() + 1, t.getTarget()));
			}
		}
	}

	private boolean containsAllIncommingTransitions(
			IGraph<NodeType> graph, HashSet<ITransition<NodeType>> seen, NodeType target) {
		for (NodeType node : graph.getNodes()) {
			for (ITransition<NodeType> t : node.getTransitionsIterator())
				if (!seen.contains(t) && t.getTarget() == target)
					return false;
		}
		return true;
	}

	public static <T extends INode<T>> Set<T> getSourceNodes(IGraph<T> graph) {
		Set<T> sources = new HashSet<T>(graph.getNodes());
		for (T node : graph.getNodes()) {
			for (ITransition<T> t : node.getTransitionsIterator())
				sources.remove(t.getTarget());
		}
		return sources;
	}
}
