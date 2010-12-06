package invariants.fsmcheck;

import invariants.BinaryInvariant;
import model.interfaces.INode;

public class AlwaysFollowedTracingSet<T extends INode<T>> extends TracingStateSet<T> {
	HistoryNode s1, s2;
	String a, b;
	
	public AlwaysFollowedTracingSet(String a, String b) {
		this.a = a;
		this.b = b;
	}
	
	public AlwaysFollowedTracingSet(BinaryInvariant inv) {
		a = inv.getFirst();
		b = inv.getSecond();
	}
	
	public void setInitial(T x) {
		String name = x.getLabel();
		HistoryNode newHistory = new HistoryNode(x, null, 1);
		if (name.equals(a)) {
			s1 = null;
			s2 = newHistory;
		} else {
			s1 = newHistory;
			s2 = null;
		}
	}
	
	public void transition(T x) {
		String name = x.getLabel();
		if (a.equals(name)) {
			s2 = preferShorter(s1, s2);
			s1 = null;
		} else if (b.equals(name)) {
			s1 = preferShorter(s2, s1);
			s2 = null;
		}
		s1 = extend(x, s1);
		s2 = extend(x, s2);
	}
	
	public HistoryNode failstate() { return s2; }
	
	public AlwaysFollowedTracingSet<T> clone() {
		AlwaysFollowedTracingSet<T> result = new AlwaysFollowedTracingSet<T>(a, b);
		result.s1 = s1;
		result.s2 = s2;
		return result;
	}
	
	public void merge(TracingStateSet<T> other) {
		AlwaysFollowedTracingSet<T> casted = (AlwaysFollowedTracingSet<T>) other;
		s1 = preferShorter(s1, casted.s1);
		s2 = preferShorter(s2, casted.s2);
	}
	
	// Returns true if this is a subset of other.
	public boolean isSubset(TracingStateSet<T> other) {
		AlwaysFollowedTracingSet<T> casted = (AlwaysFollowedTracingSet<T>) other;
		if (casted.s1 == null) { if (s1 != null) return false; }
		if (casted.s2 == null) { if (s2 != null) return false; }
		return true;
	}
}
