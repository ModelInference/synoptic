package invariants.fsmcheck;

import java.util.ArrayList;

import invariants.TemporalInvariant;
import invariants.TemporalInvariantSet.RelationPath;

// NFA state set which keeps the shortest history justifying a given state.
public abstract class TracingStateSet<T> {
	public class HistoryNode implements Comparable<HistoryNode> {
		T node;
		HistoryNode previous;
		int depth;
		
		HistoryNode(T node, HistoryNode previous, int depth) {
			this.node = node;
			this.previous = previous;
			this.depth = depth;
		}
		
		public int compareTo(HistoryNode other) {
			if (this == other) return 0;
			return this.depth - other.depth;
		}
		
		public RelationPath<T> toCounterexample(TemporalInvariant inv) {
			RelationPath<T> result = new RelationPath<T>();
			result.path = new ArrayList<T>();
			HistoryNode cur = this;
			while (cur != null) {
				result.path.add(cur.node);
				cur = cur.previous;
			}
			//TODO: reverse list here?
			result.invariant = inv;
			return result;
		}
	}
	
	public HistoryNode extend(T node, HistoryNode prior) {
		return new HistoryNode(node, prior, prior.depth + 1);
	}
	
	public HistoryNode preferShorter(HistoryNode a, HistoryNode b) {
		if (b == null) return a;
		if (a == null) return b;
		if (a.depth < b.depth) return a;
		return b;
	}
	
	public abstract void setInitial(T x);
	public abstract void transition(T x);
	public abstract boolean isSubset(TracingStateSet<T> other);
	public abstract void merge(TracingStateSet<T> other);
	public abstract HistoryNode failstate();
	public abstract TracingStateSet<T> clone();
}
;