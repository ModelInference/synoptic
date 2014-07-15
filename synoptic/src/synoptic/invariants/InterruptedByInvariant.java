package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.INode;
import synoptic.util.InternalSynopticException;

/**
 * The interrupter invariant 'x Intr y' holds if for every pair of two instances
 * of 'x' there is at least one instance of 'y' in between those two instances
 * of 'x'. This invariant could be useful because it captures more use cases
 * than the existing ones, e.g. after a 'Login' event occurred, there must be at
 * least one 'Logout' event before the next 'Login' event can occur.
 * 
 * @author sfiss
 */
public class InterruptedByInvariant extends BinaryInvariant {

    public InterruptedByInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
        if (typeFirst.equals(typeSecond)) {
            throw new InternalSynopticException("x IntrBy x can never be true");
        }
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public InterruptedByInvariant(String typeFirst, String typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), new StringEventType(typeSecond),
                relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public InterruptedByInvariant(StringEventType typeFirst, String typeSecond,
            String relation) {
        this(typeFirst, new StringEventType(typeSecond), relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public InterruptedByInvariant(String typeFirst, StringEventType typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), typeSecond, relation);
    }

    // ///////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return first.toString() + " IntrBy(" + relation.toString() + ") "
                + second.toString();
    }

    @Override
    public String getLTLString() {
        /**
         * Add TERMINAL to prevent endless loops (check if required). did(A) ->
         * !(did(A) W did(B)
         */
        return "(<> (did("
                + StringEventType.newTerminalStringEventType().toString()
                + "))) -> (" + "(did(" + first.toString() + ")) -> ((!did("
                + first.toString() + ")) W (did(" + second.toString() + ")))"
                + ")";
    }

    /**
     * Shorten the trace.
     * 
     * <pre>
     * NOTE: x Intr x cannot be true, so we will never have a counter-example
     * in which 'first' == 'second'.
     * </pre>
     * 
     * @param <T>
     *            The node type of the trace
     * @param trace
     *            the trace we are operating on
     * @return the sub-trace described above
     */
    @Override
    public <T extends INode<T>> List<T> shorten(List<T> trace) {
        return shorten(trace, first, second);
    }

    /**
     * Shortens the IntrBy trace. Returns the path "... a ... a", where there is
     * no "b" between the two "a". If there are no such "a", there is no counter
     * example
     * 
     * @param trace
     * @param firstEvent
     * @param secondEvent
     * @return
     */
    public static <T extends INode<T>> List<T> shorten(List<T> trace,
            EventType firstEvent, EventType secondEvent) {
        boolean first_seen = false;
        for (int trace_pos = 0; trace_pos < trace.size(); trace_pos++) {
            T message = trace.get(trace_pos);
            // a seen
            if (message.getEType().equals(firstEvent) && !first_seen) {
                first_seen = true;
            }

            // a seen again
            if (message.getEType().equals(firstEvent) && first_seen) {
                return trace.subList(0, trace_pos + 1);
            }

            // b seen, so reset to initial state
            if (message.getEType().equals(secondEvent)) {
                first_seen = false;
            }
        }
        return null;
    }

    @Override
    public String getShortName() {
        return "IntrBy";
    }

    @Override
    public String getLongName() {
        return "InterruptedBy";
    }

    /**
     * Returns a regular expressions describing this invariant for first AP
     * second. The expression is "[^y]*(x.*)*".
     * 
     * @param first
     *            a character representation of first
     * @param second
     *            a character representation of second
     * @return a regex for this invariant
     */
    @Override
    public String getRegex(char firstC, char secondC) {
        return "[^" + secondC + "]*(" + firstC + ".*)?";
    }
}
