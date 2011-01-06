package synoptic.model.interfaces;

import java.util.Set;

import synoptic.model.Action;


/**
 * This is the interface all graphs have to provide. The interface has no methods to modify the graph.
 * @author sigurd
 *
 * @param <NodeType> the class of a node in a graph
 */
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
	Set<String> getRelations();
	
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
	Set<NodeType> getInitialNodes(String relation);
}
