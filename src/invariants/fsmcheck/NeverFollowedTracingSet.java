package invariants.fsmcheck;

import invariants.BinaryInvariant;
import model.interfaces.INode;

public class NeverFollowedTracingSet<T extends INode<T>> extends TracingStateSet<T> {
	HistoryNode s1, s2, s3;
	String a, b;
	
	public NeverFollowedTracingSet(String a, String b) {
		this.a = a;
		this.b = b;
	}
	
	public NeverFollowedTracingSet(BinaryInvariant inv) {
		a = inv.getFirst();
		b = inv.getSecond();
	}
	
	public void setInitial(T x) {
		String name = x.getLabel();
		HistoryNode newHistory = new HistoryNode(x, null, 1);
		s1 = s2 = s3 = null;
		if (a.equals(name))
			s2 = newHistory;
		else
			s1 = newHistory;
	}
	
	public void transition(T x) {
		String name = x.getLabel();
		if (a.equals(name)) {
			s2 = preferShorter(s1, s2);
			s1 = null;
		} else if (b.equals(name)) {
			s3 = preferShorter(s2, s3);
			s2 = null;
		}
		s1 = extend(x, s1);
		s2 = extend(x, s2);
		s3 = extend(x, s3);
	}
	
	public HistoryNode failstate() { return s3; }
	
	public NeverFollowedTracingSet<T> clone() {
		NeverFollowedTracingSet<T> result = new  NeverFollowedTracingSet<T>(a, b);
		result.s1 = s1;
		result.s2 = s2;
		result.s3 = s3;
		return result;
	}
	
	public void merge(TracingStateSet<T> other) {
		NeverFollowedTracingSet<T> casted = (NeverFollowedTracingSet<T>) other;
		s1 = preferShorter(s1, casted.s1);
		s2 = preferShorter(s2, casted.s2);
		s3 = preferShorter(s3, casted.s3);
	}
	
	public boolean isSubset(TracingStateSet<T> other) {
		NeverFollowedTracingSet<T> casted = (NeverFollowedTracingSet<T>) other;
		if (casted.s1 == null) { if (s1 != null) return false; }
		if (casted.s2 == null) { if (s2 != null) return false; }
		if (casted.s3 == null) { if (s3 != null) return false; }
		return true;
	}
}
