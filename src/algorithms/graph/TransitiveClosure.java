package algorithms.graph;

import java.util.HashMap;
import java.util.Iterator;

import model.interfaces.IGraph;
import model.interfaces.INode;
import model.interfaces.ITransition;

/**
 * Code taken from
 * http://www.cs.princeton.edu/courses/archive/fall05/cos226/lectures
 * /digraph.pdf
 * Computes the transitive closue.
 * @author Sigurd
 * 
 */
public class TransitiveClosure<NodeType extends INode<NodeType>> {
	private HashMap<NodeType, HashMap<NodeType, Boolean>> tc = new HashMap<NodeType, HashMap<NodeType, Boolean>>();
	private String relation;
	private IGraph<NodeType> graph;

	/**
	 * Create the transitive closure of {@code graph} for the Relation {@code relation}.
	 * @param graph the graph 
	 * @param relation the relation
	 */
	public TransitiveClosure(IGraph<NodeType> graph, String relation) {
		this.relation = relation;
		this.graph = graph;
		for (NodeType m : graph.getNodes())
			for (Iterator<? extends ITransition<NodeType>> i = m
					.getTransitionsIterator(relation); i.hasNext();) {
				ITransition<NodeType> t = i.next();
				if (!graph.getNodes().contains(t.getTarget()))
					continue;
				dfs(m, t.getTarget());
			}
	}

	/**
	 * Mark that {@code m} can reach {@code n}, and start a dfs at {@code m}.
	 * @param m the node to start dfs at
	 * @param n a node that can be reached from m
	 */
	private void dfs(NodeType m, NodeType n) {
		if (!tc.containsKey(m))
			tc.put(m, new HashMap<NodeType, Boolean>());
		tc.get(m).put(n, true);
		for (Iterator<? extends ITransition<NodeType>> i = n
				.getTransitionsIterator(relation); i.hasNext();) {
			ITransition<NodeType> t = i.next();
			if (!graph.getNodes().contains(t.getTarget()))
				continue;
			Boolean r = tc.get(m).get(t.getTarget());
			if (r == null || r == false)
				dfs(m, t.getTarget());
		}
	}

	/**
	 * Check whether there is an edge in the transitive closure between {@code m} and {@code n}.
	 * @param m a node
	 * @param n a node
	 * @return true if {@code m} can reach {@code n}
	 */
	public boolean isReachable(NodeType m, NodeType n) {
		HashMap<NodeType, Boolean> i = tc.get(m);
		if (i == null)
			return false;
		Boolean r = tc.get(m).get(n);
		if (r == null)
			return false;
		return r;
	}

	/**
	 * Equality for transitive closure
	 * @param o
	 * @return if {@code o} describes the same relation is {@code this}
	 */
	public boolean isEqual(TransitiveClosure<NodeType> o) {
		for (NodeType u : tc.keySet()) {
			for (NodeType v : tc.get(u).keySet()) {
				if (isReachable(u, v) != o.isReachable(u, v))
					return false;
			}
		}
		return true;
	}

}
