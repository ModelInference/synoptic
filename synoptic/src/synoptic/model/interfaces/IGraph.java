package synoptic.model.interfaces;

import java.util.List;
import java.util.Set;

/**
 * This is the interface all graphs have to provide. It requires the notions of
 * relations, initial nodes, and terminal nodes.
 * 
 * @author sigurd
 * @param <NodeType>
 *            the class of a node in a graph
 */
public interface IGraph<NodeType extends INode<NodeType>> {
    // ///////////////////////////////////////////////////////////////////////
    // Methods to get information about the graph:
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Get all nodes in this graph
     * 
     * @return the set of nodes in the graph.
     */
    Set<NodeType> getNodes();

    /**
     * Get all relations that appear in this graph.
     * 
     * @return the set of nodes in the graph
     */
    Set<String> getRelations();

    /**
     * Get the union over all relations of the initial nodes
     * 
     * @return the set of initial nodes in the graph
     */
    Set<NodeType> getInitialNodes();

    /**
     * The nodes in the graph that are initial with respect to the relation
     * 
     * @param relation
     *            the relation
     * @return the set of initial nodes w.r.t. the relation
     */
    Set<NodeType> getInitialNodes(String relation);

    /**
     * Returns a list of nodes that are adjacent to node.
     */
    List<NodeType> getAdjacentNodes(NodeType node);

    // ///////////////////////////////////////////////////////////////////////
    // Methods to modify the graph:
    // ///////////////////////////////////////////////////////////////////////

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
    public void tagInitial(NodeType initialNode, String relation);

    /**
     * Mark {@code terminalNode} as terminal with respect to {@code relation}.
     * Implementations should make sure that terminalNode is in fact a node of
     * the graph.
     * 
     * @param terminalNode
     *            the node to mark as terminal
     * @param relation
     *            the relation with respect to which the node should be terminal
     */
    public void tagTerminal(NodeType terminalNode, String relation);
}
