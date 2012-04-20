package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.INode;

/**
 * An implicit invariant for Synoptic models, true for events a and b when for
 * all input traces there is never an instance of event a that is immediately
 * followed by an instance of event b.
 */
public class NeverImmediatelyFollowedInvariant extends BinaryInvariant {

    public NeverImmediatelyFollowedInvariant(EventType typeFirst,
            EventType typeSecond, String relation) {
        super(typeFirst, typeSecond, relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public NeverImmediatelyFollowedInvariant(String typeFirst,
            String typeSecond, String relation) {
        this(new StringEventType(typeFirst), new StringEventType(typeSecond),
                relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public NeverImmediatelyFollowedInvariant(StringEventType typeFirst,
            String typeSecond, String relation) {
        this(typeFirst, new StringEventType(typeSecond), relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public NeverImmediatelyFollowedInvariant(String typeFirst,
            StringEventType typeSecond, String relation) {
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

    /**
     * This invariant is not used during refinement or coarsening, so LTL has
     * been left undefined
     */
    @Override
    public String getLTLString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        return path;
    }

    /**
     * Returns a regular expressions describing this invariant for first NIFby
     * second. The expression is "([^x]|x[^y])*x*". The last x* allows a trace
     * ending with x, not followed by anything.
     * 
     * @param firstC
     *            a character representation of first
     * @param secondC
     *            a character representation of second
     * @return a regex for this invariant
     */
    @Override
    public String getRegex(char firstC, char secondC) {
        if (firstC == secondC) {
            return "([^" + firstC + "]|" + firstC + "[^" + firstC + "])*"
                    + firstC + "?";
        }
        return "([^" + firstC + "]|" + firstC + "(" + firstC + ")" + "*" + "[^"
                + secondC + firstC + "])*" + firstC + "*";
    }

}
