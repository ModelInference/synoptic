package synoptic.invariants.fsmcheck;


import java.util.BitSet;
import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.interfaces.INode;


/**
 * Represents a set of "A always followed by B" synoptic.invariants to simulate.
 * 
 * This finite state machine enters a failure state when A is encountered,
 * and enters a success state when B is encountered.  This means that the
 * failure state upon encountering a final node indicates which, of A
 * and B, was last encountered.
 * 
 * NOTE: ensure this documentation stays consistent with AlwaysFollowedTracingSet.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * 
 * @see AFbyTracingSet
 * @see FsmStateSet
 */
public class AFbyInvFsms<T extends INode<T>> extends FsmStateSet<T> {
	public AFbyInvFsms(int size)                   { super(size, 2); }
	public AFbyInvFsms(List<BinaryInvariant> invs) { super(invs, 2); }
	
	@Override
	public boolean isFail() { return !sets.get(1).isEmpty(); }
	@Override
	public BitSet whichFail() { return (BitSet)sets.get(1).clone(); }
	@Override
	public BitSet whichPermanentFail() { return new BitSet(); }
	
	/*
	 * State 1:
	 * 
	 * (non-a/b preserves state)
	 * 1 -a-> 2
	 * 1 -b-> 1
	 * 2 -a-> 2
	 * 2 -b-> 1
	 */
	 
	@Override
	public void setInitial(T input) {
		BitSet isA = getInputCopy(0, input);
		sets.set(1, (BitSet)isA.clone());
		isA.flip(0, count);
		sets.set(0, isA);
	}
	
	@Override
	public void transition(T input) {
		BitSet isA = getInput(0, input),
			   isB = getInput(1, input),
		       neither = nor(isA, isB, count),
		       s1 = sets.get(0),
		       s2 = sets.get(1);
		
		/* 
		 * neither = !(isA | isB)
		 * (simultaneous assignment - order not significant)
		 * s1 = (s1 & neither) | isB
		 * s2 = (s2 & neither) | isA 
		 */
		
		s1.and(neither);
		s1.or(isB);
		
		s2.and(neither);
		s2.or(isA);
	}
}