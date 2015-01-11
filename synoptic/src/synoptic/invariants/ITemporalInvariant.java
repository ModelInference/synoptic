package synoptic.invariants;

import java.util.List;
import java.util.Set;

import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * The interface all temporal synoptic.invariants must implement.
 * 
 */
public interface ITemporalInvariant {
    /**
     * Get an LTL representation of the invariant.
     * 
     * @return an string representing the invariant in LTL
     */
    String getLTLString();

    /**
     * Shorten a counter-example for this invariant.
     * 
     * @param <T>
     *            the type of nodes in the graph
     * @param path
     *            the counter-example path
     * @return a prefix of {@code path} that still violates the the property
     */
    <T extends INode<T>> List<T> shorten(List<T> path);

    /**
     * Get the Buchi-automaton that corresponds to this LTL property. This
     * method should cache the automaton.
     * 
     * @return a Buchi-automaton represented as a graph
     */
    gov.nasa.ltl.graph.Graph getAutomaton();

    /**
     * Get the relation whose paths are constrained by this invariant.
     * 
     * @return the relation occurring in this invariant
     */
    String getRelation();

    /**
     * Get the set of predicates occurring in this property. Predicates are here
     * labels of states.
     * 
     * @return a set of strings with the labels that occur in this invariant
     */
    Set<EventType> getPredicates();

    /**
     * Get the short name of the invariant (e.g. "AP" for "AlwaysPrecedes)
     * 
     * @return a short invariant name string
     */
    String getShortName();

    /**
     * Get the long name of the invariant (e.g. "AlwaysPrecedes")
     * 
     * @return a long invariant name string
     */
    String getLongName();
}
