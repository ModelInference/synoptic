package synoptic.invariants.fsmcheck;

import java.util.List;

import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

public interface IStateSet<T, StateSetType> {
    /**
     * Resets the stateset to be as if it started on the passed node. This
     * should be called for all initial statesets.
     * 
     * @param transitions
     *            This partition's list of transitions, currently only used for
     *            constrained invariant FSM state sets
     */
    <Node extends INode<Node>> void setInitial(T input, List<? extends ITransition<Node>> transitions);

    /**
     * Mutates the StateSet, according to which states could be inhabited after
     * the given input is provided.
     * 
     * @param input
     *            The input event to transition the FSM.
     * @param transitions
     *            This partition's list of transitions, currently only used for
     *            constrained invariant FSM state sets
     */
    <Node extends INode<Node>> void transition(T input, List<? extends ITransition<Node>> transitions);

    /**
     * Merges this stateset with another, such that all states inhabited by
     * 'other' will also now be inhabited by this.
     */
    void mergeWith(StateSetType other);

    /**
     * Queries whether this stateset is a subset of another, in other words, if
     * every state inhabited by this set is also inhabited by the other. Another
     * way of thinking about this is that merging 'this' into 'other' does not
     * affect other when this is a subset.
     * 
     * @param other
     *            The set to compare against - superset, if result is true.
     * @return If this is a subset of other.
     */
    boolean isSubset(StateSetType other);

    /**
     * Checks if this stateset inhabits any failure states.
     */
    boolean isFail();

    /**
     * Version of clone which doesn't require unsafe casting.
     */
    StateSetType copy();
}
