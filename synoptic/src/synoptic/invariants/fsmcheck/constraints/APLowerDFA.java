package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.time.ITime;

/**
 * DFA for constrained lower bound threshold AP invariant.
 */
public class APLowerDFA<Node extends INode<Node>> extends DFA<Node> {

    @SuppressWarnings("rawtypes")
    public APLowerDFA(TempConstrainedInvariant inv) {
        super(inv, LowerBoundConstraint.class);
    }

    @Override
    public void transition(Node target, ITime delta) {
        EventType name = target.getEType();
        switch (this.state) {
        case NIL:
            nilTransition(name);
            break;
        case FIRST_A_ACCEPT:
            firstATransition(name, delta);
            break;
        case FAIL_B: // permanent failure
            break;
        case NEITHER:
            firstATransition(name, delta);
            break;
        case SUCCESS_B:
            successBTransition(name, delta);
            break;
        default:
            break;
        }
    }

    private void nilTransition(EventType name) {
        if (name.equals(a)) {
            initCurrTime();
            state = State.FIRST_A_ACCEPT;
        } else if (name.equals(b)) {
            state = State.FAIL_B;
        }
    }

    private void firstATransition(EventType name, ITime delta) {
        if (name.equals(a)) {
            // Transition/remain in the current FIRST_A_ACCEPT state: observed
            // a, and no b.
            initCurrTime();
            state = State.FIRST_A_ACCEPT;
            return;
        } else if (name.equals(b)) {
            currTime = currTime.incrBy(delta);
            if (constraint.evaluate(currTime)) {
                state = State.SUCCESS_B;
            } else { // permanent failure
                state = State.FAIL_B;
            }
        } else { // not a and not b
            currTime = currTime.incrBy(delta);
            state = State.NEITHER;
        }
    }

    private void successBTransition(EventType name, ITime delta) {
        if (name.equals(a)) {
            initCurrTime();
            state = State.FIRST_A_ACCEPT;
        }
    }
}
