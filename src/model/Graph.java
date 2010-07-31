package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import model.interfaces.IModifiableGraph;
import model.interfaces.INode;
import model.interfaces.ITransition;

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
		Set<String> relations = new LinkedHashSet<String>();
		for (NodeType node : nodes)
			for (Iterator<? extends ITransition<NodeType>> iter = node
					.getTransitionsIterator(); iter.hasNext();)
				relations.add(iter.next().getAction());
		return relations;
	}

	@Override
	public void add(NodeType node) {
		nodes.add(node);
	}

	@Override
	public void remove(NodeType node) {
		nodes.remove(node);
	}

	@Override
	public void addInitial(NodeType initialNode, String relation) {
		if (initialNode == null)
			throw new IllegalArgumentException("argument was null");
		if (!initialNodes.containsKey(relation))
			initialNodes.put(relation, new HashSet<NodeType>());
		initialNodes.get(relation).add(initialNode);
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
	}

}
