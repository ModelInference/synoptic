package invariants.fsmcheck;

import invariants.BinaryInvariant;
import model.interfaces.INode;
/**
 * Represents a set of "A always precedes B" invariants to simulate, recording
 * the shortest historical path to reach a particular state.
 * 
 * This finite state machine enters a permanent success state upon encountering
 * A, and enters a permanent failure state upon encountering B.  This reflects
 * the fact that the first of the two events encountered is the only thing
 * relevant to the failure state of the invariant.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 *
 * @param <T> The node type, used as an input, and stored in path-history.
 * 
 * @see APInvFsms
 * @see TracingStateSet
 */
public class APTracingSet<T extends INode<T>> extends TracingStateSet<T> {
	HistoryNode s1, s2, s3;
	String a, b;
	
	public APTracingSet(String a, String b) {
		this.a = a;
		this.b = b;
	}
	
	public APTracingSet(BinaryInvariant inv) {
		a = inv.getFirst();
		b = inv.getSecond();
	}
	
	@Override
	public void setInitial(T x) {
		String name = x.getLabel();
		HistoryNode newHistory = new HistoryNode(x, null, 1);
		s1 = s2 = s3 = null;
		if (a.equals(name))
			s2 = newHistory;
		else if (b.equals(name))
			s3 = newHistory;
		else
			s1 = newHistory;
	}
	
	@Override
	public void transition(T x) {
		/*
		 * (non-a/b preserves state)
		 *  1 -a-> 2
		 *  1 -b-> 3
		 */
		String name = x.getLabel();
		if (a.equals(name)) {
			s2 = preferShorter(s1, s2);
			s1 = null;
		} else if (b.equals(name)) {
			s3 = preferShorter(s1, s3);
			s1 = null;
		}
		s1 = extend(x, s1);
		s2 = extend(x, s2);
		s3 = extend(x, s3);
	}

	@Override
	public HistoryNode failstate() { return s3; }

	@Override
	public APTracingSet<T> clone() {
		APTracingSet<T> result = new APTracingSet<T>(a, b);
		result.s1 = s1;
		result.s2 = s2;
		result.s3 = s3;
		return result;
	}
	
	@Override
	public void merge(TracingStateSet<T> other) {
		APTracingSet<T> casted = (APTracingSet<T>) other;
		s1 = preferShorter(s1, casted.s1);
		s2 = preferShorter(s2, casted.s2);
		s3 = preferShorter(s3, casted.s3);
	}

	@Override
	public boolean isSubset(TracingStateSet<T> other) {
		APTracingSet<T> casted = (APTracingSet<T>) other;
		if (casted.s1 == null) { if (s1 != null) return false; }
		if (casted.s2 == null) { if (s2 != null) return false; }
		if (casted.s3 == null) { if (s3 != null) return false; }
		return true;
	}
}
