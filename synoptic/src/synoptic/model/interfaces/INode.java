package synoptic.model.interfaces;

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
public interface INode<NodeType> extends Comparable<NodeType> {
    /**
     * Returns the label of the node. NOTE: This call is used by the LTLChecker
     * to retrieve the canonical representation of the event type.
     * 
     * @return the node's label
     */
    EventType getEType();

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
     * Returns the set of all outgoing transitions of this node (across all
     * relations). The difference to {@code getTransitionsIterator} is that this
     * call forces all transitions to be pre-computed.
     * 
     * @return the set of all outgoing transitions
     */
    List<? extends ITransition<NodeType>> getTransitions();

    /**
     * Returns the set of outgoing transitions of this node for a specific
     * relation. The difference to {@code getTransitionsIterator} is that this
     * call forces all transitions to be pre-computed.
     * 
     * @return the set of outgoing transitions for relation
     */
    List<? extends ITransition<NodeType>> getTransitions(String relation);

    /**
     * Returns the set of all outgoing transitions of this node (across all
     * relations), each of which is "weighted." That is, the transition is
     * annotated with the number of events that take the transition.
     * 
     * @return list of weighted outgoing transition corresponding to this node
     */
    List<WeightedTransition<NodeType>> getWeightedTransitions();

    /**
     * Returns the set of outgoing transitions of this node for a specific
     * relation, each of which is "weighted." That is, the transition is
     * annotated with the number of events that take the transition.
     * 
     * @return list of weighted outgoing transition for relation corresponding
     *         to this node
     */
    List<WeightedTransition<NodeType>> getWeightedTransitions(String relation);

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
     * Whether or not this is the dummy terminal node.
     */
    boolean isTerminal();

    /**
     * Whether or not this is the dummy initial node.
     */
    boolean isInitial();
}
