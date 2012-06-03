package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.fsmcheck.HistoryNode;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITime;

/**
 * Represents a constrained "A always followed by B" synoptic.invariants to simulate,
 * recording the shortest historical path to reach a particular state. We can assume
 * that every A has a B since this tracing set is used after model checking unconstrained
 * invariants. This finite state machine enters a failure state when B is encountered and
 * the time elapsed since the last was seen is greater than the constrained threshold. 
 * 
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public class ConstrainedAFbyTracingSet<T extends INode<T>> extends ConstrainedTracingStateSet<T>  {
		
	public enum AFbyUpperThreshold {
		
	}
	
	
	HistoryNode<T> wasA; // Indicates that A was seen more recently than B (failing
    // state)
    HistoryNode<T> wasB; // Indicates that B was seen more recently than A
    HistoryNode<T> failB; // Indicates B where state fails since time for B to appear
    // after A is violates threshold constraint
    EventType a, b;
    
    IThresholdConstraint constr; // Threshold constraint
    ITime currTime; // Current accumulated time as we transition to nodes after A

    public ConstrainedAFbyTracingSet(EventType a, EventType b) {
        this.a = a;
        this.b = b; 
    }
    
    @SuppressWarnings("rawtypes")
	public ConstrainedAFbyTracingSet(TempConstrainedInvariant inv) {
        this(inv.getFirst(), inv.getSecond());
        this.constr = inv.getConstraint();
        this.currTime = new DTotalTime(0.0);
    }

    @Override
    public void setInitial(T x) {
        EventType name = x.getEType();
        HistoryNode<T> newHistory = new HistoryNode<T>(x, null, 1);
        wasA = wasB = failB = null;
        if (name.equals(a)) {
            wasA = newHistory;
        } else if (name.equals(b)) {
            wasB = newHistory;
        } 
    }

    public void transition(T x, ITime delta) {
    	EventType name = x.getEType();
    	
    	if (a.equals(name)) {
    		// Reset the time count
    		currTime = new DTotalTime(0.0);
    		wasA = preferShorter(wasB, wasA);
    		wasB = null;
    	} else if (b.equals(name)) {
    		currTime = currTime.incrBy(delta);
    		wasB = preferShorter(wasA, wasB);
    		wasA = null;
    	} else {
    		currTime = currTime.incrBy(delta);
    	}
    	
    	wasA = extend(x, wasA);
    	wasB = extend(x, wasB);
    	
    	// Fail state, currTime greater than time constraint
    	if (!constr.evaluate(currTime)) {
    		failB = wasB;
    	}
    }
    
    @Override
    public void transition(T x, ITransition<T> trans) {
    	ITime time;
    	if (constr.getClass().equals(LowerBoundConstraint.class)) {
    		time = trans.getDeltaSeries().getMinDelta();
    	} else {
    		time = trans.getDeltaSeries().getMaxDelta();
    	}
//    	EventType name = x.getEType();
//        if (a.equals(name)) {
//            wasA = preferShorter(wasB, wasA);
//            wasB = null;
//        } else if (b.equals(name)) {
//            wasB = preferShorter(wasA, wasB);
//            wasA = null;
//        }
//        wasA = extend(x, wasA);
//        wasB = extend(x, wasB);
    }

    @Override
    public HistoryNode<T> failpath() {
        return failB;
    }

    @Override
    public ConstrainedAFbyTracingSet<T> copy() {
        ConstrainedAFbyTracingSet<T> result = new ConstrainedAFbyTracingSet<T>(a, b);
        result.wasA = wasA;
        result.wasB = wasB;
        result.failB = failB;
        result.constr = constr;
        result.currTime = currTime;
        return result;
    }

    @Override
    public void mergeWith(ConstrainedTracingStateSet<T> other) {
        ConstrainedAFbyTracingSet<T> casted = (ConstrainedAFbyTracingSet<T>) other;
        wasA = preferShorter(wasA, casted.wasA);
        wasB = preferShorter(wasB, casted.wasB);
        failB = preferShorter(failB, casted.failB);
    }

    @Override
    public boolean isSubset(ConstrainedTracingStateSet<T> other) {
        ConstrainedAFbyTracingSet<T> casted = (ConstrainedAFbyTracingSet<T>) other;
        if (casted.wasA == null) {
            if (wasA != null) {
                return false;
            }
        }
        if (casted.wasB == null) {
            if (wasB != null) {
                return false;
            }
        }
        if (casted.failB == null) {
        	if (failB != null) {
        		return false;
        	}
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("ConstrainedAFby: ");
        appendWNull(result, wasA); // Failure case first.
        result.append(" | ");
        appendWNull(result, wasB);
        return result.toString();
    }
}

