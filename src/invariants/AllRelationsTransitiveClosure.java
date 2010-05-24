package invariants;

import java.util.HashMap;
import java.util.Set;

import algorithms.graph.TransitiveClosure;

import model.Action;
import model.interfaces.IGraph;
import model.interfaces.INode;

/**
 * Code taken from
 * http://www.cs.princeton.edu/courses/archive/fall05/cos226/lectures/digraph.pdf
 * @author Sigurd
 *
 */
public class AllRelationsTransitiveClosure<NodeType extends INode<NodeType>> {
	private HashMap<Action, TransitiveClosure<NodeType>> tcs = new HashMap<Action, TransitiveClosure<NodeType>>();

	public AllRelationsTransitiveClosure(IGraph<NodeType> g) {
		for (Action relation : g.getRelations())
			tcs.put(relation, new TransitiveClosure<NodeType>(g, relation));
	}

	public boolean isReachable(NodeType m, NodeType n, Action relation) {
		if (!tcs.containsKey(relation))
			return false;
		return tcs.get(relation).isReachable(m, n);
	}

	public TransitiveClosure<NodeType> get(Action relation) {
		return tcs.get(relation);
	}

	public Set<Action> getRelations() {
		return tcs.keySet();
	}
}
