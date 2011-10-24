package synoptic.invariants;

import java.util.List;

import synoptic.model.EventType;
import synoptic.model.StringEventType;
import synoptic.model.interfaces.INode;

/**
 * An implicit invariant for Synoptic models, true for events a and b 
 * when for all input traces there is never an instance where event a
 * is immediately followed by event b.
 * 
 * @author Jenny
 */
public class NeverImmediatelyFollowedInvariant extends BinaryInvariant {

    public NeverImmediatelyFollowedInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
    }
    
    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public NeverImmediatelyFollowedInvariant(String typeFirst, String typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), new StringEventType(typeSecond),
                relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public NeverImmediatelyFollowedInvariant(StringEventType typeFirst, String typeSecond,
            String relation) {
        this(typeFirst, new StringEventType(typeSecond), relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public NeverImmediatelyFollowedInvariant(String typeFirst, StringEventType typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), typeSecond, relation);
    }
    
    @Override
    public String toString() {
        return first.toString() + " IsNeverImmediatelyFollowedBy "
                + second.toString();
    }

	@Override
	public String getLongName() {
		return "IsNeverImmediatelyFollowedBy";
	}

	@Override
	public String getShortName() {
		return "NIFby";
	}
	
	/* This invariant is not used during refinement or
	   coarsening, so LTL has been left undefined */
	@Override
	public String getLTLString() {
		return null;
	}

	@Override
	public <T extends INode<T>> List<T> shorten(List<T> path) {
		return path;
	}
}
