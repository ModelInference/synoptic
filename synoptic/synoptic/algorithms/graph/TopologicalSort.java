package algorithms.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import model.interfaces.IGraph;
import model.interfaces.INode;
import model.interfaces.ITransition;
/**
 * Topological sort. Algorithm form wikipedia.
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
	
	class Pair {
		public int level;
		public NodeType node;
		public Pair(int level, NodeType node) {
			this.level = level;
			this.node = node;
		}
	}
	
	private void sort(IGraph<NodeType> graph) {
		PriorityQueue<Pair> active = new PriorityQueue<Pair>(10, new Comparator<Pair>() {
			@Override
			public int compare(Pair arg0, Pair arg1) {
				return new Integer(arg0.level).compareTo(arg1.level);
			}
		});
		for (NodeType n : getSourceNodes(graph))
			active.add(new Pair(0, n));
		HashSet<ITransition<NodeType>> seen = new HashSet<ITransition<NodeType>>();
		while (!active.isEmpty()) {
			Pair pair = active.poll();
			sort.add(pair.node);
			if (!lattice.containsKey(pair.level))
				lattice.put(pair.level, new HashSet<NodeType>());
			lattice.get(pair.level).add(pair.node);
			for (ITransition<NodeType> t : pair.node.getTransitionsIterator()) {
				if (!seen.add(t))
					continue;
				if (containsAllIncommingTransitions(graph, seen, t.getTarget()))
					active.add(new Pair(pair.level+1, t.getTarget()));
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
