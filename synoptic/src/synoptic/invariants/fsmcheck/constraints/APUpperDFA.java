package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.FTotalTime;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;

/**
 * DFA for constrained upper bound threshold AP invariant.
 */
public class APUpperDFA<Node extends INode<Node>> implements IDFA<Node> {
	private ITime currTime;
	private State state;
	
	private EventType a;
	private EventType b;
	private IThresholdConstraint constraint;
	
	@SuppressWarnings("rawtypes")
	public APUpperDFA(TempConstrainedInvariant inv) {
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
			Class<?> clazz = constraint.getThreshold().getClass();
			if (clazz.equals(DTotalTime.class)) {
				currTime = new DTotalTime(0);
			} else if (clazz.equals(FTotalTime.class)) {
				currTime = new FTotalTime(0);
			} else if (clazz.equals(ITotalTime.class)) {
				currTime = new ITotalTime(0);
			}
			state = State.FIRST_A;
		} else if (name.equals(b)) { 
			state = State.FAIL_B;
		} 
	}
	
	private void firstATransition(EventType name, ITime delta) {
		currTime = currTime.incrBy(delta);
		state = State.SUCCESS_B;
		
	}

	private void successBTransition(EventType name, ITime delta) {
		currTime = currTime.incrBy(delta);
		if (name.equals(b) && !constraint.evaluate(delta)) {
			state = State.FAIL_B;
		}
	}
}
