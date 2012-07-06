package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.time.ITime;

/**
 * DFA for constrained upper bound threshold AP invariant.
 */
public class APUpperDFA<Node extends INode<Node>> extends DFA<Node> {

    @SuppressWarnings("rawtypes")
    public APUpperDFA(TempConstrainedInvariant inv) {
        super(inv, UpperBoundConstraint.class);
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
        case SUCCESS_B:
            firstATransition(name, delta);
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
        currTime = currTime.incrBy(delta);
        if (name.equals(b)) {
            if (constraint.evaluate(delta)) {
                state = State.SUCCESS_B;
            } else {
                state = State.FAIL_B;
            }
        } else {
            state = State.SUCCESS_B;
        }
    }
}
