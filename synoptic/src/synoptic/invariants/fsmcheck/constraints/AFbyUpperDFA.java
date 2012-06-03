package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITime;

/**
 * DFA for constrained upper bound threshold AFby invariant.
 * 
 * @author Kevin
 *
 * @param <Node>
 */
public class AFbyUpperDFA<Node extends INode<Node>> {
	private ITime currTime;
	private AFbyState state;
	
	private EventType a;
	private EventType b;
	private IThresholdConstraint constraint;
	
	public AFbyUpperDFA(TempConstrainedInvariant inv) {
		this.currTime = null;
		this.state = AFbyState.NIL;
		this.a = inv.getFirst();
		this.b = inv.getSecond();
		// TODO check that inv has upper bound constraint
		this.constraint = inv.getConstraint();
	}
	
	public AFbyState getState() {
		return state;
	}
	
	public void transition(Node target, ITime delta) {
		EventType name = target.getEType();
		switch(this.state) {
			case NIL:
				nilTransition(name);
				break;
			case FIRST_A:
				afterATransition(name, delta);
				break;
			case NOT_B:
				afterATransition(name, delta);
				break;
			case SUCCESS_B:
				successBTransition(name, delta);
				break;
			case FAIL_B: // no actions taken, permanent failure state
				break;
			default: break;
		}
	}
	
	private void nilTransition(EventType name) {
		if (name.equals(a)) {
			currTime = new DTotalTime(0);
			state = AFbyState.FIRST_A;
		}
	}
	
	private void afterATransition(EventType name, ITime delta) {
		if (name.equals(b)) {
			if (constraint.evaluate(currTime)) {
				currTime = currTime.incrBy(delta);
				state = AFbyState.SUCCESS_B;
			} else { // permanent failure
				state = AFbyState.FAIL_B;
			}
		} else { // not b
			currTime = currTime.incrBy(delta);
			state = AFbyState.NOT_B;
		}
	}
	
	private void successBTransition(EventType name, ITime delta) {
		if (name.equals(b) && !constraint.evaluate(delta)) { // permanent failure
			state = AFbyState.FAIL_B;
		} else {
			currTime = currTime.incrBy(delta);
		}
	}
}
