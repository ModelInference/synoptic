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
	HistoryNode aNotSeen;   // A not seen
	HistoryNode aSeen;      // A seen (and no Bs yet after it)
	HistoryNode bSeenAfter; // A seen, followed by B -- failure state
	
	String a, b;
	
	public NFbyTracingSet(String a, String b) {
		this.a = a;
		this.b = b;
	}
	
	public NFbyTracingSet(BinaryInvariant inv) {
		this(inv.getFirst(), inv.getSecond());
	}

	@Override
	public void setInitial(T x) {
		String name = x.getLabel();
		HistoryNode newHistory = new HistoryNode(x, null, 1);
		aNotSeen = aSeen = bSeenAfter = null;
		if (a.equals(name)) {
			aSeen = newHistory;
		} else {
			aNotSeen = newHistory;
		}
	}

	@Override
	public void transition(T x) {
		String name = x.getLabel();
		
		if (b.equals(name)) {
			bSeenAfter = preferShorter(aSeen, bSeenAfter);
			aSeen = null;
		}
		/*
		 * NOTE: there is no else here, because for this invariant, isA and isB
		 * can be simultaneously true (A NFby A, eg, A is singleton).
		 */
		if (a.equals(name)) {
			aSeen = preferShorter(aNotSeen, aSeen);
			aNotSeen = null;
		}
		
		// Advance history for all states.
		aNotSeen = extend(x, aNotSeen);
		aSeen = extend(x, aSeen);
		bSeenAfter = extend(x, bSeenAfter);
	}

	@Override
	public HistoryNode failpath() { return bSeenAfter; }

	@Override
	public NFbyTracingSet<T> copy() {
		NFbyTracingSet<T> result = new  NFbyTracingSet<T>(a, b);
		result.aNotSeen = aNotSeen;
		result.aSeen = aSeen;
		result.bSeenAfter = bSeenAfter;
		return result;
	}

	@Override
	public void mergeWith(TracingStateSet<T> other) {
		NFbyTracingSet<T> casted = (NFbyTracingSet<T>) other;
		aNotSeen = preferShorter(aNotSeen, casted.aNotSeen);
		aSeen = preferShorter(aSeen, casted.aSeen);
		bSeenAfter = preferShorter(bSeenAfter, casted.bSeenAfter);
	}

	@Override
	public boolean isSubset(TracingStateSet<T> other) {
		NFbyTracingSet<T> casted = (NFbyTracingSet<T>) other;
		if (casted.aNotSeen == null) { if (aNotSeen != null) return false; }
		if (casted.aSeen == null) { if (aSeen != null) return false; }
		if (casted.bSeenAfter == null) { if (bSeenAfter != null) return false; }
		return true;
	}
}
