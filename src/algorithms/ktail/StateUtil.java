package algorithms.ktail;

import java.util.Iterator;

import algorithms.graph.StateMerge;

import model.Action;
import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.INode;
import model.interfaces.ITransition;

public class StateUtil {
	/**
	 * We perform k-tails comparison on the message graph to the given depth k.
	 * 
	 * @return true if the k-tails are equivalent under the current definition
	 *         of equivalence.
	 */
	static public <NodeType extends INode<NodeType>> boolean kEquals(INode<NodeType> s1, INode<NodeType> s2, int k, boolean subsumption) {
		// base case
		if (k == 0) {
			return true;
		}
		
		for (Iterator<? extends ITransition<NodeType>> i1 = s1.getTransitionsIterator(); i1.hasNext();) {
			ITransition<NodeType> t1 = i1.next();
			boolean notExistent = true;
			for (Iterator<? extends ITransition<NodeType>> i2 = s2.getTransitionsIterator(t1.getAction()); i2.hasNext();) {
				ITransition<NodeType> t2 = i2.next();
				if (!kEquals(t1.getTarget(), t2.getTarget(), k-1, subsumption)) {
					return false;
				}
				notExistent = false;
			}
			if (notExistent) {
				checkNotThere(s2, t1.getAction());
				return false;
			}
		}
		
		// if it is not subsumption, do it the other way arround, too
		if (!subsumption) {
			for (Iterator<? extends ITransition<NodeType>> i2 = s2.getTransitionsIterator(); i2.hasNext();) {
				ITransition<NodeType> t2 = i2.next();
				boolean notExistent = true;
				for (Iterator<? extends ITransition<NodeType>> i1 = s1.getTransitionsIterator(t2.getAction()); i1.hasNext();) {
					ITransition<NodeType> t1 = i1.next();
					if (!kEquals(t1.getTarget(), t2.getTarget(), k-1, subsumption)) {
						return false;
					}
					notExistent = false;
				}
				if (notExistent) {
					checkNotThere(s2, t2.getAction());
					return false;
				}
			}
		}
		
		return true;
	}


	private static <NodeType extends INode<NodeType>> void checkNotThere(INode<NodeType> s, Action action) {
		for (Iterator<? extends ITransition<NodeType>> i1 = s.getTransitionsIterator(); i1.hasNext();) {
			if (i1.next().getAction().equals(action)){
				throw new RuntimeException("inconsistent");
			}
		}
	}


	/**
	 * Merge two states to depth k.
	 * 
	 * @param s
	 * @param k
	 */
	public static void kMerge(PartitionGraph g, SystemState<Partition> s1, SystemState<Partition> s2, int k) {
		if (k!=1) {
			throw new IllegalArgumentException("k>1 is not yet supported");
		}
		StateMerge merge = new StateMerge(s1, s2);
		g.apply(merge);
	}
}
