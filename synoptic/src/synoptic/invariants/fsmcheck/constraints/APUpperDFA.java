package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITime;

/**
 * DFA for constrained upper bound threshold AP invariant.
 * 
 * @author Kevin
 *
 * @param <Node>
 */
public class APUpperDFA<Node extends INode<Node>> {
	private ITime currTime;
	private APState state;
	
	private EventType a;
	private EventType b;
	private IThresholdConstraint constraint;
	
	public APUpperDFA(TempConstrainedInvariant inv) {
		this.currTime = null;
		this.state = APState.NIL;
		this.a = inv.getFirst();
		this.b = inv.getSecond();
		// TODO check that inv has upper bound constraint
		this.constraint = inv.getConstraint();
	}
	
	public APState getState() {
		return state;
	}
	
	public void transition(Node target, ITime delta) {
		EventType name = target.getEType();
		switch(this.state) {
			case NIL:
				nilTransition(name);
				break;
			case FIRST_A:
				firstATransition(name, delta);
				break;
			case FAIL_B: // permanent failure
				break;
			case SUCCESS_B:
				successBTransition(name, delta);
				break;
			default: break;
		}
	}
	
	private void nilTransition(EventType name) {
		if (name.equals(a)) {
			currTime = new DTotalTime(0);
			state = APState.FIRST_A;
		} else if (name.equals(b)) { 
			state = APState.FAIL_B;
		} 
	}
	
	private void firstATransition(EventType name, ITime delta) {
		currTime = currTime.incrBy(delta);
		state = APState.SUCCESS_B;
		
	}

	private void successBTransition(EventType name, ITime delta) {
		currTime = currTime.incrBy(delta);
		if (name.equals(b) && !constraint.evaluate(delta)) {
			state = APState.FAIL_B;
		}
	}
}
