package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
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
public class AFbyUpperDFA<Node extends INode<Node>> implements IDFA<Node> {
	private ITime currTime;
	private State state;
	
	private EventType a;
	private EventType b;
	private IThresholdConstraint constraint;
	
	@SuppressWarnings("rawtypes")
	public AFbyUpperDFA(TempConstrainedInvariant inv) {
		this.currTime = null;
		this.state = State.NIL;
		this.a = inv.getFirst();
		this.b = inv.getSecond();
		
		IThresholdConstraint constr = inv.getConstraint();
		// check that inv has upper bound constraint
		if (!constr.getClass().equals(UpperBoundConstraint.class)) {
			throw new IllegalArgumentException("TempConstrainedInvariant must be for upper bound");
		}
		this.constraint = constr;
	}
	
	@Override
	public State getState() {
		return state;
	}
	
	@Override
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
			state = State.FIRST_A;
		}
	}
	
	private void afterATransition(EventType name, ITime delta) {
		currTime = currTime.incrBy(delta);
		if (name.equals(b)) {
			if (constraint.evaluate(currTime)) {
				state = State.SUCCESS_B;
			} else { // permanent failure
				state = State.FAIL_B;
			}
		} else { // not b
			state = State.NOT_B;
		}
	}
	
	private void successBTransition(EventType name, ITime delta) {
		currTime = currTime.incrBy(delta);
		if (name.equals(b) && !constraint.evaluate(delta)) { // permanent failure
			state = State.FAIL_B;
		} 
	}
}
