package synoptic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import synoptic.model.interfaces.IModifiableGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.Pair;
import synoptic.util.Predicate;
import synoptic.util.Predicate.IBinary;


/**
 * A graph implementation that provides a merge operation to merge another graph
 * into it. The graph can be modified by adding nodes to it. Whether edges can
 * be added depends on the capability of the {@code NodeType} class.
 * 
 * @author sigurd
 * 
 * @param <NodeType> the class of a node in the graph
 */
public class Graph<NodeType extends INode<NodeType>> implements
		IModifiableGraph<NodeType> {
	/**
	 * The nodes of the graph. The edges are managed by the nodes.
	 */
	private Set<NodeType> nodes = new HashSet<NodeType>();

	/**
	 * The initial nodes of the graph, with respect to which they are initial.
	 */
	private final Map<String, Set<NodeType>> initialNodes = new HashMap<String, Set<NodeType>>();

	private Set<String> cachedRelations = null;

	/**
	 * Create a graph from nodes.
	 * 
	 * @param nodes
	 *            the nodes of the graph
	 */
	public Graph(Collection<NodeType> nodes) {
		this.nodes.addAll(nodes);
	}

	/**
	 * Create an empty graph.
	 */
	public Graph() {
	}

	@Override
	public Set<NodeType> getInitialNodes() {
		Set<NodeType> nodes = new HashSet<NodeType>();
		for (Set<NodeType> v : initialNodes.values())
			nodes.addAll(v);
		return nodes;
	}

	@Override
	public Set<NodeType> getInitialNodes(String relation) {
		if (!initialNodes.containsKey(relation))
			return Collections.emptySet();
		return initialNodes.get(relation);
	}

	@Override
	public Set<NodeType> getNodes() {
		return nodes;
	}

	@Override
	public Set<String> getRelations() {
		if (cachedRelations != null)
			return cachedRelations;
		cachedRelations = new LinkedHashSet<String>();
		for (NodeType node : nodes)
			for (Iterator<? extends ITransition<NodeType>> iter = node
					.getTransitionsIterator(); iter.hasNext();)
				cachedRelations.add(iter.next().getRelation());
		return cachedRelations;
	}

	@Override
	public void add(NodeType node) {
		nodes.add(node);
		cachedRelations = null;
	}

	@Override
	public void remove(NodeType node) {
		nodes.remove(node);
		cachedRelations = null;
	}

	@Override
	public void addInitial(NodeType initialNode, String relation) {
		if (initialNode == null)
			throw new IllegalArgumentException("argument was null");
		if (!initialNodes.containsKey(relation))
			initialNodes.put(relation, new HashSet<NodeType>());
		initialNodes.get(relation).add(initialNode);
		cachedRelations = null;
	}

	/**
	 * Merge {@code graph} into this graph.
	 * 
	 * @param graph
	 *            the graph to merge into this one
	 */
	public void merge(Graph<NodeType> graph) {
		nodes.addAll(graph.getNodes());
		for (String key : graph.initialNodes.keySet()) {
			if (!initialNodes.containsKey(key))
				initialNodes.put(key, new HashSet<NodeType>());
			initialNodes.get(key).addAll(graph.initialNodes.get(key));
		}
		cachedRelations = null;
	}

	
	// generic graph equality
	
	public boolean equalsWith(Graph<NodeType> other, Predicate.IBinary<NodeType, NodeType> np) {
		return equalsWith(other, np, new Predicate.BinaryTrue());
	}
	
	public boolean equalsWith(Graph<NodeType> other,
			Predicate.IBinary<NodeType, NodeType> np, Predicate.IBinary<String, String> rp) {
		Set<NodeType> unusedOther = other.getInitialNodes();
		for (NodeType n1 : this.getInitialNodes()) {
			boolean foundMatch = false;
			for (NodeType n2 : unusedOther) {
				if (np.eval(n1, n2) && transitionEquality(n1, n2, np, rp)) {
					foundMatch = true;
					unusedOther.remove(n2);
					break;
				}
			}
			if (!foundMatch) return false;
		}
		return true;
	}
	
	// Helper for equalsWith.
	private boolean transitionEquality(NodeType a, NodeType b,
			Predicate.IBinary<NodeType, NodeType> np, Predicate.IBinary<String, String> rp) {
		Set<NodeType> visited = new HashSet<NodeType>();
		Stack<synoptic.util.Pair<NodeType,NodeType>> toVisit = new Stack<synoptic.util.Pair<NodeType,NodeType>>();
		toVisit.push(new Pair<NodeType,NodeType>(a, b));
		while (!toVisit.isEmpty()) {
			Pair<NodeType,NodeType> tv = toVisit.pop();
			visited.add(tv.getLeft());
			for(ITransition<NodeType> trans1 : tv.getLeft().getTransitions()) {
				boolean foundMatch = false;
				for (ITransition<NodeType> trans2 : tv.getRight().getTransitions()) {
					//System.out.println("comparing " + trans1.getRelation() + " with " + 
					//		trans2.getRelation());
					if (rp.eval(trans1.getRelation(), trans2.getRelation()) &&
						np.eval(trans1.getTarget(), trans2.getTarget())) {
						if (!visited.contains(trans1.getTarget())) {
							toVisit.push(new Pair<NodeType,NodeType>(trans1.getTarget(), trans2.getTarget()));
						}
						foundMatch = true;
						break;
					}
				}
				if (!foundMatch) return false;
			}
		}
		return true;
	}
}
