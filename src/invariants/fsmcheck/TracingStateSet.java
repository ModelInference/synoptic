package invariants.fsmcheck;

import java.util.ArrayList;
import java.util.Collections;

import invariants.TemporalInvariant;
import invariants.TemporalInvariantSet.RelationPath;

/**
 * Abstract NFA state set which keeps the shortest path justifying a given
 * state being inhabited.  This allows for model checking which yields
 * short counterexample paths for failing invariants.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 *
 * @param <T> The node type, used as an input, and stored in path-history.
 */
public abstract class TracingStateSet<T> {
	/**
	 * HistoryNode class used to construct a linked-list path through the
	 * model graph.  This linked list structure is used, rather than explicit
	 * lists, in order to benefit from the sharing of prefixes.
	 */
	public class HistoryNode implements Comparable<HistoryNode> {
		T node;
		HistoryNode previous;
		int count;
		
		HistoryNode(T node, HistoryNode previous, int count) {
			this.node = node;
			this.previous = previous;
			this.count = count;
		}
		
		public int compareTo(HistoryNode other) {
			if (this == other) return 0;
			return this.count - other.count;
		}
		
		// Converts this chain into a RelationPath list.
		public RelationPath<T> toCounterexample(TemporalInvariant inv) {
			RelationPath<T> result = new RelationPath<T>();
			result.path = new ArrayList<T>();
			HistoryNode cur = this;
			while (cur != null) {
				result.path.add(cur.node);
				cur = cur.previous;
			}
			Collections.reverse(result.path);
			result.invariant = inv;
			return result;
		}
	}
	
	/*
	 * Helper to extend this history path with another node.
	 * If the passed in path is null, then null is yielded.
	 */
	public HistoryNode extend(T node, HistoryNode prior) {
		if (prior == null) return null;
		return new HistoryNode(node, prior, prior == null ? 1 : prior.count + 1);
	}
	
	/*
	 * Helper to yield the shortest non-null path of the two passed in.
	 */
	public HistoryNode preferShorter(HistoryNode a, HistoryNode b) {
		if (b == null) return a;
		if (a == null) return b;
		if (a.count < b.count) return a;
		return b;
	}
	
	/**
	 * Sets the states of the stateset to their initial conditions.
	 * @param x The node that this state starts on (to initialize history).
	 */
	public abstract void setInitial(T x);
	
	/**
	 * Mutates this stateset, given the input which determines its next states,
	 * appending to the history necessary to get to those states.
	 * @param x The input which is provided to the state machine
	 */
	public abstract void transition(T x);
	
	/**
	 * Queries whether this stateset is a subset of another, in other words,
	 * if every state inhabited by this set is also inhabited by the other.
	 * Another way of thinking about this is that merging 'this' into 'other'
	 * does not affect other when this is a subset.
	 *  
	 * @param other The set to compare against - superset, if result is true.
	 * @return If this is a subset of other.
	 */
	public abstract boolean isSubset(TracingStateSet<T> other);
	
	/**
	 * Merges the 'other' stateset into this, preferring the shorter paths,
	 * when a choice is possible.
	 * @param other The other stateSet to use, left unmutated.
	 */
	public abstract void merge(TracingStateSet<T> other);
	
	/**
	 * Queries the state for the shortest path which leads to a failing state.
	 * @return The HistoryNode at the head of the linked list of nodes within
	 *     the model.
	 */
	public abstract HistoryNode failstate();
	
	/**
	 * Clones the TracingStateSet
	 * @return A copy of this.
	 */
	public abstract TracingStateSet<T> clone();
}
;