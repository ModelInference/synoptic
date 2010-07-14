package invariants;

import java.util.List;
import java.util.Set;

import model.Action;
import model.interfaces.INode;

/**
 * The interface all temporal invariants must implement.
 * 
 * @author Sigurd Schneider
 * 
 */
public interface TemporalInvariant {
	/**
	 * Use Did/Can LTL instead of LTL. (Not using it is only partially implemented.)
	 */
	public static final boolean useDIDCAN = true;

	/**
	 * Get an LTL representation of the invariant.
	 * 
	 * @return an string representing the invariant in LTL
	 */
	public String getLTLString();

	/**
	 * Shorten a counter-example for this invariant.
	 * 
	 * @param <T>
	 *            the type of nodes in the graph
	 * @param path
	 *            the couter-example path
	 * @return a prefix of {@code path} that still violates the the property
	 */
	public <T extends INode<T>> List<T> shorten(List<T> path);

	/**
	 * get the Büchi-automaton that corresponds to this LTL property. This
	 * method should cache the automaton.
	 * 
	 * @return a Büchi-automaton represented as a graph
	 */
	public gov.nasa.ltl.graph.Graph getAutomaton();

	/**
	 * get the relation whose paths are constrained by this invariant
	 * 
	 * @return the relation occurring in this invariant
	 */
	public Action getRelation();

	/**
	 * Get the set of predicates occurring in this property. Predicates are here
	 * labels of states.
	 * 
	 * @return a set of strings with the labels that occur in this invariant
	 */
	public Set<String> getPredicates();

	/**
	 * Get the short name of the invariant.
	 * 
	 * @return a short invariant name
	 */
	public String getShortName();
}
