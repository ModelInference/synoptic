package invariants.fsmcheck;

import invariants.BinaryInvariant;
import model.interfaces.INode;

public class AlwaysPrecedesTracingSet<T extends INode<T>> extends TracingStateSet<T> {
	HistoryNode s1, s2, s3, s4;
	String a, b;
	
	public AlwaysPrecedesTracingSet(String a, String b) {
		this.a = a;
		this.b = b;
	}
	
	public AlwaysPrecedesTracingSet(BinaryInvariant inv) {
		a = inv.getFirst();
		b = inv.getSecond();
	}
	
	public void setInitial(T x) {
		s1 = new HistoryNode(x, null, 1);
		s2 = null;
		s3 = null;
		s4 = null;
	}
	
	public void transition(T x) {
		String name = x.getLabel();
		if (a.equals(name)) {
			s2 = extend(x, preferShorter(s1, s2));
			s4 = extend(x, preferShorter(s3, s4));
			s1 = null;
			s3 = null;
		} else if (b.equals(name)) {
			s3 = extend(x, s2);
			s4 = extend(x, preferShorter(s1, s4));
			s1 = null;
			s2 = null;
		} else {
			if (s1 != null) s1 = extend(x, s1);
			if (s2 != null) s2 = extend(x, s2);
			if (s3 != null) s3 = extend(x, s3);
			if (s4 != null) s4 = extend(x, s4);
		}
	}
	
	public HistoryNode failstate() { return s4; }

	public AlwaysPrecedesTracingSet<T> clone() {
		AlwaysPrecedesTracingSet<T> result = new AlwaysPrecedesTracingSet<T>(a, b);
		result.s1 = s1;
		result.s2 = s2;
		result.s3 = s3;
		result.s4 = s4;
		return result;
	}
	
	public void merge(TracingStateSet<T> other) {
		AlwaysPrecedesTracingSet<T> casted = (AlwaysPrecedesTracingSet<T>) other; 
		s1 = preferShorter(s1, casted.s1);
		s2 = preferShorter(s2, casted.s2);
		s3 = preferShorter(s3, casted.s3);
		s4 = preferShorter(s4, casted.s4);
	}
	
	public boolean isSubset(TracingStateSet<T> other) {
		AlwaysPrecedesTracingSet<T> casted = (AlwaysPrecedesTracingSet<T>) other;
		if (casted.s1 != null) { if (s1 == null) return false; }
		if (casted.s2 != null) { if (s2 == null) return false; }
		if (casted.s3 != null) { if (s3 == null) return false; }
		if (casted.s4 != null) { if (s4 == null) return false; }
		return true;
	}
}
