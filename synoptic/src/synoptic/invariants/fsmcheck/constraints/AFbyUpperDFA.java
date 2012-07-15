package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.time.ITime;

/**
 * DFA for constrained upper bound threshold AFby invariant.
 */
public class AFbyUpperDFA<Node extends INode<Node>> extends DFA<Node> {

    @SuppressWarnings("rawtypes")
    public AFbyUpperDFA(TempConstrainedInvariant inv) {
        super(inv, UpperBoundConstraint.class);
    }

    @Override
    public void transition(Node target, ITime delta) {
        EventType name = target.getEType();
        switch (this.state) {
        case NIL:
            nilTransition(name);
            break;
        case FIRST_A_REJECT:
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
        default:
            break;
        }
    }

    private void nilTransition(EventType name) {
        if (name.equals(a)) {
            initCurrTime();
            state = State.FIRST_A_REJECT;
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
        if (name.equals(b) && !constraint.evaluate(currTime)) {
            // permanent failure
            state = State.FAIL_B;
        } else if (name.equals(a)) {
        	state = State.NOT_B;
        }
    }
}
