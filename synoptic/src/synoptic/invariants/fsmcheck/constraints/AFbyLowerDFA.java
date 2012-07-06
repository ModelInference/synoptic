package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.time.ITime;

/**
 * DFA for constrained lower bound threshold AFby invariant.
 */
public class AFbyLowerDFA<Node extends INode<Node>> extends DFA<Node> {

    @SuppressWarnings("rawtypes")
    public AFbyLowerDFA(TempConstrainedInvariant inv) {
        super(inv, LowerBoundConstraint.class);
    }

    @Override
    public void transition(Node target, ITime delta) {
        EventType name = target.getEType();
        switch (this.state) {
        case NIL:
            nilTransition(name);
            break;
        case FIRST_A_REJECT:
            firstATransition(name, delta);
            break;
        case NOT_B:
            notBTransition(name, delta);
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

    private void firstATransition(EventType name, ITime delta) {
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

    private void notBTransition(EventType name, ITime delta) {
        if (name.equals(a)) {
            nilTransition(name);
        } else if (name.equals(b)) {
            firstATransition(name, delta);
        } else { // not a or b
            // stay in NOT_B state
            currTime = currTime.incrBy(delta);
        }
    }

    private void successBTransition(EventType name, ITime delta) {
        if (name.equals(a)) {
            nilTransition(name);
        }
    }
}
