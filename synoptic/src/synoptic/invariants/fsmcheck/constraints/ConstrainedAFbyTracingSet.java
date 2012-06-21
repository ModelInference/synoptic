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
        HistoryNode<T> newHistory = new HistoryNode<T>(x, null, 1);
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
    }

    @Override
    public HistoryNode<T> failpath() {
    	if (dfa.getState().isSuccess()) {
    		return null;
    	}
    	return history;
    }

    @Override
    public ConstrainedAFbyTracingSet<T> copy() {
        ConstrainedAFbyTracingSet<T> result = new ConstrainedAFbyTracingSet<T>(a, b);
        result.constr = constr;
        result.dfa = dfa;
        result.history = history;
        return result;
    }

    @Override
    public void mergeWith(ConstrainedTracingStateSet<T> other) {
        ConstrainedAFbyTracingSet<T> casted = (ConstrainedAFbyTracingSet<T>) other;
        history = preferShorter(history, casted.history);
    }

    @Override
    public boolean isSubset(ConstrainedTracingStateSet<T> other) {
        ConstrainedAFbyTracingSet<T> casted = (ConstrainedAFbyTracingSet<T>) other;
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
        return result.toString();
    }
}

