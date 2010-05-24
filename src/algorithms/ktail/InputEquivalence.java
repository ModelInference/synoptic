package algorithms.ktail;

import java.util.Iterator;

import algorithms.graph.StateMerge;
import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.INode;
import model.interfaces.ITransition;

public class InputEquivalence {
	/**
	 * Determines if two traces starting at the given states are
	 * "input-equivalent"
	 * 
	 * @param i
	 * @param j
	 * @return
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
			if (!t1.getAction().equals(t2.getAction()))
				return false;
			iIter = t1.getTarget().getTransitionsIterator();
			jIter = t2.getTarget().getTransitionsIterator();
		}
		// Both traces must have ended simoultaneously.
		return !iIter.hasNext() && !jIter.hasNext();
	}
	
	/**
	 * Merges two input equivalent SystemStates. The second state will be removed from the graph.
	 * @param i
	 * @param j
	 */
	public static void mergeTrace(PartitionGraph g, SystemState<Partition> i, SystemState<Partition> j) {
		if (!isInputEquivalent(i, j))
			throw new IllegalArgumentException("input traces are not input equivalent");
		
		Iterator<ITransition<SystemState<Partition>>> iIter = i.getTransitionsIterator();
		Iterator<ITransition<SystemState<Partition>>> jIter = j.getTransitionsIterator();
		// Merge the first states of the two traces
		g.apply(new StateMerge(i,j));
		while (iIter.hasNext() && jIter.hasNext()) {
			ITransition<SystemState<Partition>> t1 = iIter.next();
			ITransition<SystemState<Partition>> t2 = jIter.next();
			// Now, merge the two targetStates 
			g.apply(new StateMerge(t1.getTarget(), t2.getTarget()));
			// and get the next two transitions
			iIter = t1.getTarget().getTransitionsIterator();
			jIter = t2.getTarget().getTransitionsIterator();
		}
	}
	
	/**
	 * Perfoms merging of input equivalent traces as described in the GK-Tail
	 * paper. Fails if the current graph is not a partitioned trace graph (eg if
	 * it does not look like a set of linear traces)
	 */
	public static void mergeTraces(PartitionGraph g) {
		out: for (SystemState<Partition> i : g.getSystemStateGraph().getInitialNodes()) {
			for (SystemState<Partition> j : g.getSystemStateGraph().getInitialNodes()) {
				if (InputEquivalence.isInputEquivalent(i, j)) {
					InputEquivalence.mergeTrace(g, i, j);
					break out;
				}
			}
		}
	}
}
