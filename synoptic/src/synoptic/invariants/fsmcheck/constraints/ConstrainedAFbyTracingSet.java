package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.invariants.fsmcheck.HistoryNode;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
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
//	HistoryNode<T> wasA; // Indicates that A was seen more recently than B (failing
//    // state)
//    HistoryNode<T> wasB; // Indicates that B was seen more recently than A
    
    HistoryNode<T> history;
    
    EventType a, b;
   
    IDFA<T> dfa;
    IThresholdConstraint constr;

    public ConstrainedAFbyTracingSet(EventType a, EventType b) {
        this.a = a;
        this.b = b; 
    }
    
    @SuppressWarnings("rawtypes")
	public ConstrainedAFbyTracingSet(TempConstrainedInvariant inv) {
        this(inv.getFirst(), inv.getSecond());
        
        this.constr = inv.getConstraint();
        if (constr.getClass().equals(LowerBoundConstraint.class)) {
        	dfa = new AFbyLowerDFA<T>(inv);
        } else if (constr.getClass().equals(UpperBoundConstraint.class)) {
        	dfa = new AFbyUpperDFA<T>(inv);
        }
    }

    @Override
    public void setInitial(T x) {
//        EventType name = x.getEType();
        HistoryNode<T> newHistory = new HistoryNode<T>(x, null, 1);
//        wasA = wasB = null;
//        if (name.equals(a)) {
//            wasA = newHistory;
//        } else if (name.equals(b)) {
//            wasB = newHistory;
//        }
        history = newHistory;
    }
    
    @Override
    public void transition(T x, ITransition<T> trans) {
    	ITime time;
    	if (constr.getClass().equals(LowerBoundConstraint.class)) {
    		time = trans.getDeltaSeries().getMinDelta();
    	} else {
    		time = trans.getDeltaSeries().getMaxDelta();
    	}
    	
    	dfa.transition(x, time);
    	history = extend(x, history);
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
    	if (dfa.getState().isSuccess()) {
    		return null;
    	}
    	return history;
//        return wasA;
    }

    @Override
    public ConstrainedAFbyTracingSet<T> copy() {
        ConstrainedAFbyTracingSet<T> result = new ConstrainedAFbyTracingSet<T>(a, b);
//        result.wasA = wasA;
//        result.wasB = wasB;
        result.constr = constr;
        result.dfa = dfa;
        result.history = history;
        return result;
    }

    @Override
    public void mergeWith(ConstrainedTracingStateSet<T> other) {
        ConstrainedAFbyTracingSet<T> casted = (ConstrainedAFbyTracingSet<T>) other;
//        wasA = preferShorter(wasA, casted.wasA);
//        wasB = preferShorter(wasB, casted.wasB);
        history = preferShorter(history, casted.history);
    }

    @Override
    public boolean isSubset(ConstrainedTracingStateSet<T> other) {
        ConstrainedAFbyTracingSet<T> casted = (ConstrainedAFbyTracingSet<T>) other;
//        if (casted.wasA == null) {
//            if (wasA != null) {
//                return false;
//            }
//        }
//        if (casted.wasB == null) {
//            if (wasB != null) {
//                return false;
//            }
//        }
        if (casted.history == null) {
        	if (history != null) {
        		return false;
        	}
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("ConstrainedAFby: ");
        appendWNull(result, history);
//        appendWNull(result, wasA); // Failure case first.
//        result.append(" | ");
//        appendWNull(result, wasB);
        return result.toString();
    }
}

