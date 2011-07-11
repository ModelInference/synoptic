package synoptic.model.interfaces;

import java.util.Comparator;
import java.util.List;

import synoptic.model.EventType;
import synoptic.model.Partition;
import synoptic.model.WeightedTransition;
import synoptic.util.IIterableIterator;

/**
 * The interface all nodes must implement. The interface does not contain
 * methods that allow modification of the node (with the exception of
 * {@code setParent} and {@code addTransition}).
 * 
 * @author sigurd
 * @param <NodeType>
 *            the interface is parametrized with the type of the implementing
 *            class to allow more specific types
 */
public interface INode<NodeType> {
    /**
     * Returns the label of the node. NOTE: This call is used by the LTLChecker
     * to retrieve the canonical representation of the event type.
     * 
     * @return the node's label
     */
    EventType getEType();

    /**
     * Returns a comparator object that can be used to compare NodeType
     * instances.
     * 
     * @return comparator for NodeType
     */
    Comparator<NodeType> getComparator();

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
     * Returns the set of all outgoing transitions of this node, each of which
     * is "weighted." That is, the transition is annotated with the number of
     * events that take the transition.
     * 
     * @return list of weighted outgoing transition corresponding to this node
     */
    List<WeightedTransition<NodeType>> getWeightedTransitions();

    /**
     * Set the parent partition of this node.
     * 
     * @param parent
     *            the new parent partition
     */
    void setParent(Partition parent);

    /**
     * Add a transition from this node to node dest.
     * 
     * @param dest
     *            The destination of the transition.
     * @param relation
     *            The relation for which this transition is valid
     */
    void addTransition(NodeType dest, String relation);

    /**
     * Get the parent partition of this node.
     * 
     * @return the parent partition
     */
    Partition getParent();

    /**
     * Whether or not this node is a 'terminal' node in some sample traces.
     */
    boolean isTerminal();
}
