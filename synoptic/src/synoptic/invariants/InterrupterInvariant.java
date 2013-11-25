package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.INode;
import synoptic.util.InternalSynopticException;

/**
 * TODO: document this
 * 
 * @author sfiss
 */
public class InterrupterInvariant extends BinaryInvariant {

    public InterrupterInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
        if (typeFirst.equals(typeSecond)) {
            throw new InternalSynopticException("x Intr x can never be true");
        }
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public InterrupterInvariant(String typeFirst, String typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), new StringEventType(typeSecond),
                relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public InterrupterInvariant(StringEventType typeFirst, String typeSecond,
            String relation) {
        this(typeFirst, new StringEventType(typeSecond), relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public InterrupterInvariant(String typeFirst, StringEventType typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), typeSecond, relation);
    }

    // ///////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return first.toString() + " Intr(" + relation.toString() + ") "
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
     * TODO: document this
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

    public static <T extends INode<T>> List<T> shorten(List<T> trace,
            EventType firstEvent, EventType secondEvent) {
        boolean firstFound = false;
        for (int trace_pos = 0; trace_pos < trace.size(); trace_pos++) {
            T message = trace.get(trace_pos);
            if (message.getEType().equals(firstEvent)) {
                if (firstFound) {
                    return trace.subList(0, trace_pos + 1);
                }
                firstFound = true;
            }
            if (message.getEType().equals(secondEvent)) {
                firstFound = false;
            }
        }
        return null;
    }

    @Override
    public String getShortName() {
        return "AP";
    }

    @Override
    public String getLongName() {
        return "AlwaysPrecedes";
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
