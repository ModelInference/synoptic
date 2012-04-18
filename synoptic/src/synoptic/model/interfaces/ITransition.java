package synoptic.model.interfaces;

import java.util.Set;

/**
 * A generic interface for a transition. This interface provides method to
 * modify the transition.
 * 
 * @author sigurd
 * @param <NodeType>
 *            the type of the nodes which are connected by this transition
 */
public interface ITransition<NodeType> extends
        Comparable<ITransition<NodeType>> {
    /**
     * Get the target node.
     * 
     * @return the target node
     */
    NodeType getTarget();

    /**
     * Get the source node.
     * 
     * @return the source node
     */
    NodeType getSource();

    /**
     * get the label of the transition (i.e. the relation)
     * 
     * @return the name of the relation
     */
    Set<String> getRelations();

    /**
     * Set the target node.
     * 
     * @param target
     *            the new target node
     */
    void setTarget(NodeType target);

    /**
     * Set the source node.
     * 
     * @param source
     *            the new source node
     */
    void setSource(NodeType source);

    /**
     * Get a short description of the transition
     * 
     * @return a short description
     */
    String toStringConcise();
}
