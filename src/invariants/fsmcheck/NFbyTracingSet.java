package invariants.fsmcheck;

import invariants.BinaryInvariant;
import model.interfaces.INode;

/**
 * Represents a set of "A never followed by B" invariants to simulate, recording
 * the shortest historical path to reach a particular state.
 * 
 * This finite state machine enters a particular state (s2) when A is provided.
 * If we are in this state when a B is provided, then we enter into a failure
 * state.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 *  
 * @see NFbyInvFsms
 * @see FsmStateSet
 */
public class NFbyTracingSet<T extends INode<T>> extends TracingStateSet<T> {
	HistoryNode s1, s2, s3;
	String a, b;
	
	public NFbyTracingSet(String a, String b) {
		this.a = a;
		this.b = b;
	}
	
	public NFbyTracingSet(BinaryInvariant inv) {
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
		else
			s1 = newHistory;
	}

	@Override
	public void transition(T x) {
		/* (non-a/b preserves state)
		 * 1 -a-> 2
		 * 1 -b-> 1
		 * 2 -a-> 2
		 * 2 -b-> 3
		 */
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

	@Override
	public HistoryNode failstate() { return s3; }

	@Override
	public NFbyTracingSet<T> clone() {
		NFbyTracingSet<T> result = new  NFbyTracingSet<T>(a, b);
		result.s1 = s1;
		result.s2 = s2;
		result.s3 = s3;
		return result;
	}

	@Override
	public void merge(TracingStateSet<T> other) {
		NFbyTracingSet<T> casted = (NFbyTracingSet<T>) other;
		s1 = preferShorter(s1, casted.s1);
		s2 = preferShorter(s2, casted.s2);
		s3 = preferShorter(s3, casted.s3);
	}

	@Override
	public boolean isSubset(TracingStateSet<T> other) {
		NFbyTracingSet<T> casted = (NFbyTracingSet<T>) other;
		if (casted.s1 == null) { if (s1 != null) return false; }
		if (casted.s2 == null) { if (s2 != null) return false; }
		if (casted.s3 == null) { if (s3 != null) return false; }
		return true;
	}
}
