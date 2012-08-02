package synoptic.tests.units;

import static org.junit.Assert.*;

import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.invariants.fsmcheck.constraints.AFbyLowerDFA;
import synoptic.invariants.fsmcheck.constraints.AFbyUpperDFA;
import synoptic.invariants.fsmcheck.constraints.APLowerDFA;
import synoptic.invariants.fsmcheck.constraints.APUpperDFA;
import synoptic.invariants.fsmcheck.constraints.DFA;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.model.event.StringEventType;
import synoptic.tests.SynopticTest;
import synoptic.util.time.ITotalTime;

/**
 * Tests for constrained DFAs.
 */
public class ConstrainedDFATests extends SynopticTest {
	@Test
	public void APLowerDFASuccess() throws Exception {
		IThresholdConstraint threshold = new LowerBoundConstraint(new ITotalTime(2));
		TempConstrainedInvariant<AlwaysPrecedesInvariant> constrInv = constructAPInvariant(threshold);

		DFA<EventNode> dfa1 = new APLowerDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("a"), new ITotalTime(1));
		dfa1.transition(getEventNode("c"), new ITotalTime(3));
		dfa1.transition(getEventNode("b"), new ITotalTime(3));
		assertTrue("a AP b within time constraint", dfa1.getState().isSuccess());
		
		DFA<EventNode> dfa2 = new APLowerDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("a"), new ITotalTime(1));
		assertTrue("only a", dfa2.getState().isSuccess());
		
		DFA<EventNode> dfa3 = new APLowerDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("c"), new ITotalTime(5));
		dfa2.transition(getEventNode("d"), new ITotalTime(5));
		dfa2.transition(getEventNode("e"), new ITotalTime(5));
		assertTrue("non a's and b's", dfa3.getState().isSuccess());
	}

	@Test
	public void APLowerDFAFail() throws Exception {
		IThresholdConstraint threshold = new LowerBoundConstraint(new ITotalTime(2));
		TempConstrainedInvariant<AlwaysPrecedesInvariant> constrInv = constructAPInvariant(threshold);
		
		DFA<EventNode> dfa1 = new APLowerDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("a"), new ITotalTime(1));
		dfa1.transition(getEventNode("b"), new ITotalTime(1));
		assertTrue("time constraint violation", !dfa1.getState().isSuccess());
	
		DFA<EventNode> dfa2 = new APLowerDFA<EventNode>(constrInv);
		dfa2.transition(getEventNode("b"), new ITotalTime(5));
		assertTrue("only b", !dfa2.getState().isSuccess());
		
		DFA<EventNode> dfa3 = new APLowerDFA<EventNode>(constrInv);
		dfa3.transition(getEventNode("z"), new ITotalTime(3));
		dfa3.transition(getEventNode("x"), new ITotalTime(3));
		dfa3.transition(getEventNode("z"), new ITotalTime(3));
		dfa3.transition(getEventNode("b"), new ITotalTime(3));
		assertTrue("b after non a's and b's", !dfa3.getState().isSuccess());
	
		DFA<EventNode> dfa4 = new APLowerDFA<EventNode>(constrInv);
		dfa4.transition(getEventNode("b"), new ITotalTime(2));
		dfa4.transition(getEventNode("a"), new ITotalTime(2));
		dfa4.transition(getEventNode("b"), new ITotalTime(2));
		assertTrue("a AP b invariant violation", !dfa4.getState().isSuccess());
	}
	
	@Test
	public void APUpperDFASuccess() throws Exception {
		IThresholdConstraint threshold = new UpperBoundConstraint(new ITotalTime(5));
		TempConstrainedInvariant<AlwaysPrecedesInvariant> constrInv = constructAPInvariant(threshold);
		
		DFA<EventNode> dfa1 = new APUpperDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("a"), new ITotalTime(1));
		dfa1.transition(getEventNode("b"), new ITotalTime(2));
		assertTrue("a AP b within time constraint", dfa1.getState().isSuccess());
		
		DFA<EventNode> dfa2 = new APUpperDFA<EventNode>(constrInv);
		dfa2.transition(getEventNode("a"), new ITotalTime(1));
		assertTrue("only a", dfa2.getState().isSuccess());
		
		DFA<EventNode> dfa3 = new APUpperDFA<EventNode>(constrInv);
		dfa3.transition(getEventNode("z"), new ITotalTime(1));
		dfa3.transition(getEventNode("a"), new ITotalTime(1));
		dfa3.transition(getEventNode("j"), new ITotalTime(1));
		dfa3.transition(getEventNode("k"), new ITotalTime(1));
		dfa3.transition(getEventNode("b"), new ITotalTime(1));
		dfa3.transition(getEventNode("b"), new ITotalTime(1));
		assertTrue("a AP b within time constraint", dfa3.getState().isSuccess());
	}
	
	public void APUpperDFAFail() throws Exception {
		IThresholdConstraint threshold = new UpperBoundConstraint(new ITotalTime(3));
		TempConstrainedInvariant<AlwaysPrecedesInvariant> constrInv = constructAPInvariant(threshold);
		
		DFA<EventNode> dfa1 = new APUpperDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("b"), new ITotalTime(2));
		assertTrue("only b", !dfa1.getState().isSuccess());
		
		DFA<EventNode> dfa2 = new APUpperDFA<EventNode>(constrInv);
		dfa2.transition(getEventNode("a"), new ITotalTime(1));
		dfa2.transition(getEventNode("b"), new ITotalTime(5));
		assertTrue("time constraint violation", !dfa2.getState().isSuccess());
		
		DFA<EventNode> dfa3 = new APLowerDFA<EventNode>(constrInv);
		dfa3.transition(getEventNode("b"), new ITotalTime(2));
		dfa3.transition(getEventNode("a"), new ITotalTime(2));
		dfa3.transition(getEventNode("b"), new ITotalTime(2));
		assertTrue("a AP b invariant violation", !dfa3.getState().isSuccess());
	}
	
	public void AFbyLowerDFASuccess() throws Exception {
		IThresholdConstraint threshold = new LowerBoundConstraint(new ITotalTime(2));
		TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv = constructAFbyInvariant(threshold);
		
		DFA<EventNode> dfa1 = new AFbyLowerDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("b"), new ITotalTime(1));
		assertTrue("only b", dfa1.getState().isSuccess());
		
		DFA<EventNode> dfa2 = new AFbyLowerDFA<EventNode>(constrInv);
		dfa2.transition(getEventNode("a"), new ITotalTime(1));
		dfa2.transition(getEventNode("b"), new ITotalTime(2));
		assertTrue("a AFby b within time constraint", dfa2.getState().isSuccess());
	}
	
	public void AFbyLowerDFAFail() throws Exception {
		IThresholdConstraint threshold = new LowerBoundConstraint(new ITotalTime(2));
		TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv = constructAFbyInvariant(threshold);
	
		DFA<EventNode> dfa1 = new AFbyLowerDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("a"), new ITotalTime(1));
		assertTrue("only a", !dfa1.getState().isSuccess());

		DFA<EventNode> dfa2 = new AFbyLowerDFA<EventNode>(constrInv);
		dfa2.transition(getEventNode("a"), new ITotalTime(1));
		dfa2.transition(getEventNode("b"), new ITotalTime(1));
		assertTrue("time constraint violation", !dfa2.getState().isSuccess());
		
		DFA<EventNode> dfa3 = new AFbyLowerDFA<EventNode>(constrInv);
		dfa3.transition(getEventNode("a"), new ITotalTime(1));
		dfa3.transition(getEventNode("b"), new ITotalTime(2));
		dfa3.transition(getEventNode("a"), new ITotalTime(2));
		assertTrue("a AFby b invariant violation", !dfa3.getState().isSuccess());
	}
	
	@Test
	public void AFbyUpperDFASuccess() throws Exception {
		IThresholdConstraint threshold = new UpperBoundConstraint(new ITotalTime(5));
		TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv = constructAFbyInvariant(threshold); 

		DFA<EventNode> dfa1 = new AFbyUpperDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("a"), new ITotalTime(1));
		dfa1.transition(getEventNode("a"), new ITotalTime(2));
		dfa1.transition(getEventNode("b"), new ITotalTime(3));
		dfa1.transition(getEventNode("x"), new ITotalTime(3));
		assertTrue("a AFby b within time constraint", dfa1.getState().isSuccess());
		
		DFA<EventNode> dfa2 = new AFbyUpperDFA<EventNode>(constrInv);
		dfa2.transition(getEventNode("b"), new ITotalTime(1));
		assertTrue("only b", dfa2.getState().isSuccess());
	}
	
	@Test
	public void AFbyUpperDFAFail() throws Exception {
		IThresholdConstraint threshold = new UpperBoundConstraint(new ITotalTime(5));
		TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv = constructAFbyInvariant(threshold); 
		
		DFA<EventNode> dfa1 = new AFbyUpperDFA<EventNode>(constrInv);
		dfa1.transition(getEventNode("x"), new ITotalTime(1));
		dfa1.transition(getEventNode("a"), new ITotalTime(2));
		dfa1.transition(getEventNode("b"), new ITotalTime(3));
		dfa1.transition(getEventNode("x"), new ITotalTime(4));
		dfa1.transition(getEventNode("a"), new ITotalTime(5));
		dfa1.transition(getEventNode("y"), new ITotalTime(6));
		dfa1.transition(getEventNode("w"), new ITotalTime(7));
		assertTrue("a AFby b invariant violation",!dfa1.getState().isSuccess());
		
		DFA<EventNode> dfa2 = new AFbyUpperDFA<EventNode>(constrInv);
		dfa2.transition(getEventNode("a"), new ITotalTime(1));
		dfa2.transition(getEventNode("b"), new ITotalTime(10));
		assertTrue("time constraint violation", !dfa2.getState().isSuccess());
		
		DFA<EventNode> dfa3 = new AFbyUpperDFA<EventNode>(constrInv);
		dfa3.transition(getEventNode("a"), new ITotalTime(1));
		assertTrue("only a", !dfa3.getState().isSuccess());
	}
	
	private TempConstrainedInvariant<AlwaysPrecedesInvariant> constructAPInvariant(IThresholdConstraint threshold) {
		AlwaysPrecedesInvariant inv = new AlwaysPrecedesInvariant(
				new StringEventType("a"), new StringEventType("b"),
                Event.defTimeRelationStr);
				
		TempConstrainedInvariant<AlwaysPrecedesInvariant> constrInv = 
			new TempConstrainedInvariant<AlwaysPrecedesInvariant>(inv, threshold);
		
		return constrInv;
	}
	
	private TempConstrainedInvariant<AlwaysFollowedInvariant> constructAFbyInvariant(IThresholdConstraint threshold) {
		AlwaysFollowedInvariant inv = new AlwaysFollowedInvariant(
				new StringEventType("a"), new StringEventType("b"),
				Event.defTimeRelationStr);
				
		TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv = 
			new TempConstrainedInvariant<AlwaysFollowedInvariant>(inv, threshold);
		
		return constrInv;
	}
	
	private EventNode getEventNode(String type) {
		return new EventNode(new Event(new StringEventType(type)));
	}
}
