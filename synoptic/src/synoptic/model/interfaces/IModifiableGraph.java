package synoptic.model.interfaces;

/**
 * The interface modifiable graphs must implement. It allows nodes to be added,
 * removed, and to be marked as initial nodes.
 * 
 * @author Sigurd Schneider
 * 
 * @param <NodeType>
 *            the type of nodes in the graph
 */
public interface IModifiableGraph<NodeType extends INode<NodeType>> extends
		IGraph<NodeType> {
	/**
	 * Add the node the graph
	 * 
	 * @param node
	 *            the node to add
	 */
	public void add(NodeType node);

	/**
	 * Remove the node from the graph
	 * 
	 * @param node
	 *            the node to remove
	 */
	public void remove(NodeType node);

	/**
	 * Mark {@code initialNode} as initial with respect to {@code relation}.
	 * Implementations should make sure that initialNode is in fact a node of
	 * the graph.
	 * 
	 * @param initialNode
	 *            the node to mark as initial
	 * @param relation
	 *            the relation with respect to which the node should be initial
	 */
	public void addInitial(NodeType initialNode, String relation);
}
