package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.FTotalTime;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;

/**
 * Defines state and implements some basic functionality of IDFA that all the
 * constrained invariant DFAs share.
 */
public abstract class DFA<Node extends INode<Node>> implements IDFA<Node> {
    protected ITime currTime;
    protected State state;

    protected EventType a;
    protected EventType b;
    protected IThresholdConstraint constraint;

    @SuppressWarnings("rawtypes")
    public DFA(TempConstrainedInvariant inv, Class expectedConstrClass) {
        this.currTime = null;
        this.state = State.NIL;
        this.a = inv.getFirst();
        this.b = inv.getSecond();

        IThresholdConstraint constr = inv.getConstraint();
        // Check that the invariant has the appropriate constraint type.
        if (!constr.getClass().equals(expectedConstrClass)) {
            throw new IllegalArgumentException(
                    "TempConstrainedInvariant must have a constraint of type "
                            + expectedConstrClass.toString());
        }
        this.constraint = constr;
    }

    @Override
    public State getState() {
        return state;
    }

    /**
     * Initializes the current time to "0" using an appropriate time type.
     */
    protected void initCurrTime() {
        Class<?> clazz = constraint.getThreshold().getClass();
        if (clazz.equals(DTotalTime.class)) {
            currTime = new DTotalTime(0);
        } else if (clazz.equals(FTotalTime.class)) {
            currTime = new FTotalTime(0);
        } else if (clazz.equals(ITotalTime.class)) {
            currTime = new ITotalTime(0);
        }
    }

}
