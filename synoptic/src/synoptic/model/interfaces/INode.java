package synoptic.model.interfaces;

import java.util.List;

import synoptic.model.Partition;
import synoptic.util.IIterableIterator;

/**
 * The interface all nodes must implement. The interface does not contain
 * methods that allow modification of the node (with the exception of {@code
 * setParent}.
 * 
 * @author sigurd
 * @param <NodeType>
 *            the interface is parametrized with the type of the implementing
 *            class to allow more specific types
 */
public interface INode<NodeType> extends Comparable<NodeType> {
    /**
     * Returns the label of the node.
     * 
     * @return the node label
     */
    String getLabel();

    /**
     * Returns an {@code IIterableIterator} over all outgoing transitions of
     * this node. An implementation may delay computation of the transitions
     * until {@code next} is called on the returned iterator.
     * 
     * @return an {@code IIterableIterator} over all outgoing transitions of
     *         this node
     */
    IIterableIterator<? extends ITransition<NodeType>> getTransitionsIterator();

    /**
     * Returns an {@code IIterableIterator} those outgoing transitions of this
     * node that are labeled with {@code relation}.An implementation may delay
     * computation of the transitions until {@code next} is called on the
     * returned iterator.
     * 
     * @return an {@code IIterableIterator} over all outgoing transitions of
     *         this node
     */
    IIterableIterator<? extends ITransition<NodeType>> getTransitionsIterator(
            String relation);

    /**
     * Check to see if a transition to node {@code node} exists that is labeled
     * by {@code relation}
     * 
     * @param node
     *            the target node
     * @param relation
     *            the transition label
     * @return null if no such transition exists, the transition otherwise
     */
    ITransition<NodeType> getTransition(NodeType node, String relation);

    /**
     * Returns the set of all outgoing transitions of this node. The difference
     * to {@code getTransitionsIterator} is that this call forces all
     * transitions to be pre-computed.
     * 
     * @return the set of all outgoing transitions
     */
    List<? extends ITransition<NodeType>> getTransitions();

    /**
     * Set the parent partition of this node.
     * 
     * @param parent
     *            the new parent partition
     */
    void setParent(Partition parent);

    /**
     * Get the parent partition of this node.
     * 
     * @return the parent partition
     */
    Partition getParent();

    /**
     * Get a short string describing this node.
     * 
     * @return a short description
     */
    String toStringConcise();

    /**
     * Gets whether this node is the last in some sample traces.
     */
    boolean isFinal();
}
