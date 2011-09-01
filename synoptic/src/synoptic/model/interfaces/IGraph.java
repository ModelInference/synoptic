package synoptic.model.interfaces;

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
     * @return the set of relations in the graph
     */
    Set<String> getRelations();

    /**
     * Get the union over all relations of the initial nodes
     * 
     * @return the set of initial nodes in the graph
     */
    Set<NodeType> getDummyInitialNodes();

    /**
     * The nodes in the graph that are initial with respect to the relation
     * 
     * @param relation
     *            the relation
     * @return the set of initial nodes w.r.t. the relation
     */
    NodeType getDummyInitialNode(String relation);

    /**
     * Returns a list of nodes that are adjacent to node.
     */
    Set<NodeType> getAdjacentNodes(NodeType node);

    // ///////////////////////////////////////////////////////////////////////
    // Methods to modify the graph:
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Add the node the graph
     * 
     * @param node
     *            the node to add
     */
    void add(NodeType node);

}
