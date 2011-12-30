package synoptic.invariants;

import java.util.List;

import synoptic.model.EventType;
import synoptic.model.StringEventType;
import synoptic.model.interfaces.INode;
import synoptic.util.NotImplementedException;

/**
 * An implicit invariant for Synoptic models, true for events a and b when for
 * all input traces event a is always immediately followed by event b.
 * 
 * @author Jenny
 */
public class AlwaysImmediatelyFollowedInvariant extends BinaryInvariant {

    public AlwaysImmediatelyFollowedInvariant(EventType typeFirst,
            EventType typeSecond, String relation) {
        super(typeFirst, typeSecond, relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public AlwaysImmediatelyFollowedInvariant(String typeFirst,
            String typeSecond, String relation) {
        this(new StringEventType(typeFirst), new StringEventType(typeSecond),
                relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public AlwaysImmediatelyFollowedInvariant(StringEventType typeFirst,
            String typeSecond, String relation) {
        this(typeFirst, new StringEventType(typeSecond), relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public AlwaysImmediatelyFollowedInvariant(String typeFirst,
            StringEventType typeSecond, String relation) {
        this(new StringEventType(typeFirst), typeSecond, relation);
    }

    @Override
    public String toString() {
        return first.toString() + " IsALwaysImmediatelyFollowedBy "
                + second.toString();
    }

    @Override
    public String getLongName() {
        return "IsAlwaysImmediatelyFollowedBy";
    }

    @Override
    public String getShortName() {
        return "AIFby";
    }

    /*
     * This invariant is not used during refinement or coarsening, so LTL has
     * been left undefined
     */
    @Override
    public String getLTLString() {
        throw new NotImplementedException();
    }

    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        return path;
    }

    @Override
    public String getRegex(char firstC, char secondC) {
        return "([^" + firstC + "]|" + firstC + secondC + ")*";
    }
}
