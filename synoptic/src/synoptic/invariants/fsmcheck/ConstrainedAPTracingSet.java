package synoptic.invariants.fsmcheck;


import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITime;

/**
 * Represents a set of constrained "A always precedes B" synoptic.invariants to simulate,
 * recording the shortest historical path to reach a particular state. We can assume
 * that every B has a preceding A since this tracing set is used after model checking 
 * unconstrained invariants. This finite machine enters a failure state when A precedes
 * B in an amount of time that violates threshold constraint of invariant.
 *
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public class ConstrainedAPTracingSet<T extends INode<T>> extends TracingStateSet<T> {
    HistoryNode neitherSeen; // Neither A or B yet seen
    HistoryNode firstA; // A seen before B (permanent success)
    HistoryNode firstB; // B seen before A (permanent failure)
    HistoryNode failB;
    EventType a, b;

    IThresholdConstraint constr;
    ITime currTime;
    
    public ConstrainedAPTracingSet(EventType a, EventType b) {
        this.a = a;
        this.b = b;
    }

    @SuppressWarnings("rawtypes")
	public ConstrainedAPTracingSet(TempConstrainedInvariant inv) {
        this(inv.getFirst(), inv.getSecond());
        this.constr = inv.getConstraint();
        this.currTime = new DTotalTime(0.0);
    }

    @Override
    public void setInitial(T x) {
        EventType name = x.getEType();
        HistoryNode newHistory = new HistoryNode(x, null, 1);
        neitherSeen = firstA = firstB = null;
        if (a.equals(name)) {
            firstA = newHistory;
        } else if (b.equals(name)) {
            firstB = newHistory;
        } else {
            neitherSeen = newHistory;
        }
    }
    
    public void transition(T x, ITime delta) {
    	EventType name = x.getEType();
    	if (a.equals(name)) {
    		firstA = preferShorter(neitherSeen, firstA);
    		neitherSeen = null;
    	} else if (b.equals(name)) {
    		firstB = preferShorter(neitherSeen, firstB);
    		neitherSeen = null;
    	}
    	
    	// An A has been seen, start adding up transition times.
    	if (firstA != null) {
    		currTime = currTime.incrBy(delta);
    	}
      
    	neitherSeen = extend(x, neitherSeen);
    	firstA = extend(x, firstA);
    	firstB = extend(x, firstB);
    	
    	if (!constr.evaluate(currTime)) {
    		failB = firstB; 
    	}
    }

    @Override
    public void transition(T x) {
//        EventType name = x.getEType();
//        if (a.equals(name)) {
//            firstA = preferShorter(neitherSeen, firstA);
//            neitherSeen = null;
//        } else if (b.equals(name)) {
//            firstB = preferShorter(neitherSeen, firstB);
//            neitherSeen = null;
//        }
//        neitherSeen = extend(x, neitherSeen);
//        firstA = extend(x, firstA);
//        firstB = extend(x, firstB);
    }

    @Override
    public HistoryNode failpath() {
        return failB;
    }

    @Override
    public ConstrainedAPTracingSet<T> copy() {
        ConstrainedAPTracingSet<T> result = new ConstrainedAPTracingSet<T>(a, b);
        result.neitherSeen = neitherSeen;
        result.firstA = firstA;
        result.firstB = firstB;
        result.constr = constr;
        result.currTime = currTime;
        result.failB = failB;
        return result;
    }

    @Override
    public void mergeWith(TracingStateSet<T> other) {
        ConstrainedAPTracingSet<T> casted = (ConstrainedAPTracingSet<T>) other;
        neitherSeen = preferShorter(neitherSeen, casted.neitherSeen);
        firstA = preferShorter(firstA, casted.firstA);
        firstB = preferShorter(firstB, casted.firstB);
        failB = preferShorter(failB, casted.failB);
    }

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
        ConstrainedAPTracingSet<T> casted = (ConstrainedAPTracingSet<T>) other;
        if (casted.neitherSeen == null) {
            if (neitherSeen != null) {
                return false;
            }
        }
        if (casted.firstA == null) {
            if (firstA != null) {
                return false;
            }
        }
        if (casted.firstB == null) {
            if (firstB != null) {
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
        result.append("AP: ");
        appendWNull(result, firstB); // Failure case first.
        result.append(" | ");
        appendWNull(result, firstA);
        result.append(" | ");
        appendWNull(result, neitherSeen);
        return result.toString();
    }
}