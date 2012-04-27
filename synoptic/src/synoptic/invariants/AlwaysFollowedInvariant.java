package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.INode;

/**
 * @author Sigurd Schneider
 */
public class AlwaysFollowedInvariant extends BinaryInvariant {

    public AlwaysFollowedInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public AlwaysFollowedInvariant(String typeFirst, String typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), new StringEventType(typeSecond),
                relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public AlwaysFollowedInvariant(StringEventType typeFirst,
            String typeSecond, String relation) {
        this(typeFirst, new StringEventType(typeSecond), relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public AlwaysFollowedInvariant(String typeFirst,
            StringEventType typeSecond, String relation) {
        this(new StringEventType(typeFirst), typeSecond, relation);
    }

    // ///////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return first.toString() + " AlwaysFollowedBy(" + relation.toString()
                + ") " + second.toString();
    }

    @Override
    public String getLTLString() {
        /**
         * Version 1:
         * 
         * <pre>
         * [] ( did(first) -> <> did(second) )
         * </pre>
         * 
         * Can loop infinitely in a loop that does not reach a terminal node. In
         * a sense it is completely unfair -- it has no fairness constraints.
         */
        /**
         * Version 2:
         * 
         * <pre>
         * (<>(did(TERMINAL))) -> [] ( did(first) -> <> did(second) )
         * </pre>
         * 
         * Only considers paths that can reach the TERMINAL node, and only then
         * checks the AFby invariant along those paths. WARNING: this version
         * does not work (at all) for non-terminating traces!
         */
        /**
         * For more information see: http://mitpress.
         * mit.edu/catalog/item/default.asp?ttype=2&tid=11481
         */
        // Using Version 2:

        // NOTE: because our formulas are strings, we cannot compare
        // EventType objects directly, so we compare the String
        // representations of EventTypes, instead.
        return "(<> (did("
                + StringEventType.newTerminalStringEventType().toString()
                + "))) -> ([](did(" + first.toString() + ") -> (<> (did("
                + second.toString() + ")))))";

        // return "[](did(" + first + ") -> <> did(" + second + ")))";
        // return "<> did(" + second + ")";
    }

    /**
     * Unlike the other types of invariants' counter-example paths, an AFby
     * counter-example path cannot be trivially shortened because it must
     * include the entire path to the TERMINAL node.
     */
    @Override
    public <T extends INode<T>> List<T> shorten(List<T> trace) {
        return trace;
        // return BinaryInvariant.removeLoops(trace);
    }

    @Override
    public String getShortName() {
        return "AFby";
    }

    @Override
    public String getLongName() {
        return "AlwaysFollowedBy";
    }

    /**
     * Returns a regular expressions describing this invariant for first AFby
     * second. The expression is "([^x]*|(x[^y]*y))*".
     * 
     * @param firstC
     *            a character representation of first
     * @param secondC
     *            a character representation of second
     * @return a regex for this invariant
     */
    @Override
    public String getRegex(char firstC, char secondC) {
        return "([^" + firstC + "]*|(" + firstC + "[^" + secondC + "]*"
                + secondC + "))*";
    }
}
