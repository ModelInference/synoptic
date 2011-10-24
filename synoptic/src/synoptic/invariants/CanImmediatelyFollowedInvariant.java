package synoptic.invariants;

import java.util.List;

import synoptic.model.EventType;
import synoptic.model.StringEventType;
import synoptic.model.interfaces.INode;
import synoptic.util.NotImplementedException;

/**
 * An implicit invariant for Synoptic models, true for two events a and b when
 * there exists a trace such that event a is immediately followed by event b.
 * 
 * @author Jenny
 */
public class CanImmediatelyFollowedInvariant extends BinaryInvariant {

    public CanImmediatelyFollowedInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
    }
    
    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public CanImmediatelyFollowedInvariant(String typeFirst, String typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), new StringEventType(typeSecond),
                relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public CanImmediatelyFollowedInvariant(StringEventType typeFirst, String typeSecond,
            String relation) {
        this(typeFirst, new StringEventType(typeSecond), relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public CanImmediatelyFollowedInvariant(String typeFirst, StringEventType typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), typeSecond, relation);
    }
    
    @Override
    public String toString() {
        return first.toString() + " CanBeImmediatelyFollowedBy "
                + second.toString();
    }

	@Override
	public String getLongName() {
		return "CanBeImmediatelyFollowedBy";
	}

	@Override
	public String getShortName() {
		return "CIFby";
	}

	/* This invariant is not used during refinement or
	   coarsening, so LTL has been left undefined */
	@Override
	public String getLTLString() {
		throw new NotImplementedException();
	}

	@Override
	public <T extends INode<T>> List<T> shorten(List<T> path) {
		return path;
	}
}
