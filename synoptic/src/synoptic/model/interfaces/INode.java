package synoptic.model.interfaces;

import java.util.List;
import java.util.Set;

import synoptic.model.Partition;

/**
 * The interface all nodes must implement. The interface does not contain
 * methods that allow modification of the node (with the exception of
 * {@code setParent} and {@code addTransition}).
 * 
 * @param <NodeType>
 *            the interface is parametrized with the type of the implementing
 *            class to allow more specific types
 */
public interface INode<NodeType> extends Comparable<NodeType> {
    /**
     * Returns a string representation of all of this partition's event types,
     * only one unless this is a variable node that allows types that are
     * non-identical but considered equal
     */
    public String eTypeStr();

    public Set<NodeType> getAllSuccessors();

    /**
     * Returns the set of all outgoing transitions of this node (across all
     * relations). The difference to {@code getTransitionsIterator} is that this
     * call forces all transitions to be pre-computed.
     * 
     * @return the set of all outgoing transitions
     */
    List<? extends ITransition<NodeType>> getAllTransitions();

    List<? extends ITransition<NodeType>> getTransitionsWithExactRelations(
            Set<String> relations);

    List<? extends ITransition<NodeType>> getTransitionsWithSubsetRelations(
            Set<String> relations);

    List<? extends ITransition<NodeType>> getTransitionsWithIntersectingRelations(
            Set<String> relations);

    /**
     * Returns the set of all outgoing transitions of this node (across all
     * relations), each of which is "weighted." That is, the transition is
     * annotated with the number of events that take the transition.
     * 
     * @return list of weighted outgoing transition corresponding to this node
     */
    List<? extends ITransition<NodeType>> getWeightedTransitions();

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

    /**
     * Compare the EventType(s) of this node and {@code other}, returning a
     * negative, zero, or positive number if this node's EventType(s) are
     * considered less than, equal to, or greater than {@code other}'s type(s),
     * respectively
     */
    int compareETypes(INode<?> other);

    /**
     * Whether the EventType(s) of this node and {@code other} are equal
     */
    boolean eTypesEqual(INode<?> other);
}
