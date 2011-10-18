package synoptic.invariants;

import java.util.List;

import synoptic.model.EventType;
import synoptic.model.StringEventType;
import synoptic.model.interfaces.INode;

/**
 * An implicit invariant for Synoptic models, true for two events a and b when
 * there exists a trace such that event a is immediately followed event b.
 * 
 * @author Jenny
 */
public class CanImmediatelyFollowedInvariant extends BinaryInvariant {
	
	/* Default relation between events, true for ordered logs */
	private static final String DEFAULT_RELATION = "temporal";

    public CanImmediatelyFollowedInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
    }
    
	/**
	 * Constructs an CIFby invariant with the DEFAULT_RELATION
	 */
	public CanImmediatelyFollowedInvariant(EventType typeFirst, EventType typeSecond) {
		this(typeFirst, typeSecond, DEFAULT_RELATION);
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
		return null;
	}

	@Override
	public <T extends INode<T>> List<T> shorten(List<T> path) {
		return path;
	}
}
