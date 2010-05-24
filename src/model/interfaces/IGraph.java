package model.interfaces;

import java.util.Set;

import model.Action;

public interface IGraph<NodeType extends INode<NodeType>> {
	/**
	 * Get all nodes in this graph
	 * @return the set of nodes in the graph.
	 */
	Set<NodeType> getNodes();
	
	/**
	 * Get all relations that appear in this graph.
	 * @return the set of nodes in the graph
	 */
	Set<Action> getRelations();
	
	/**
	 * Get the union over all relations of the initial nodes
	 * @return the set of initial nodes in the graph
	 */
	Set<NodeType> getInitialNodes();
	
	/**
	 * The nodes in the graph that are initial with respect to the relation
	 * @param relation the relation
	 * @return the set of initial nodes w.r.t. the relation
	 */
	Set<NodeType> getInitialNodes(Action relation);
}
