/** kTail implementation */
package synoptic.algorithms.ktail;

import java.util.Iterator;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;


public class InputEquivalence {
	/**
	 * Determines if two traces starting at the given states are
	 * "input-equivalent"
	 * 
	 * @param i
	 * @param j
	 * @return true if {@code i} and {@code j} are input equivalent
	 */
	public static <T extends INode<T>>  boolean isInputEquivalent(T i, T j) {
		Iterator<? extends ITransition<T>> iIter = i.getTransitionsIterator();
		Iterator<? extends ITransition<T>> jIter = j.getTransitionsIterator();
		while (iIter.hasNext() && jIter.hasNext()) {
			ITransition<T> t1 = iIter.next();
			ITransition<T> t2 = jIter.next();

			// Make sure the graph is a trace
			if (!iIter.hasNext() && jIter.hasNext() ||
				iIter.hasNext() && !jIter.hasNext()	)
				throw new IllegalArgumentException(
				"Cannot determine input equivalence on non-trace graph.");
			// If messages are not the same, the traces are not input
			// equivalent.
			if (!t1.getRelation().equals(t2.getRelation()))
				return false;
			iIter = t1.getTarget().getTransitionsIterator();
			jIter = t2.getTarget().getTransitionsIterator();
		}
		// Both traces must have ended simultaneously.
		return !iIter.hasNext() && !jIter.hasNext();
	}
}
