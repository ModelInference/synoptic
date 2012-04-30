package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.INode;
import synoptic.util.InternalSynopticException;

public class AlwaysPrecedesInvariant extends BinaryInvariant {

    public AlwaysPrecedesInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
        if (typeFirst.equals(typeSecond)) {
            throw new InternalSynopticException(
                    "x AlwaysPrecedes x can never be true");
        }
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public AlwaysPrecedesInvariant(String typeFirst, String typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), new StringEventType(typeSecond),
                relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public AlwaysPrecedesInvariant(StringEventType typeFirst,
            String typeSecond, String relation) {
        this(typeFirst, new StringEventType(typeSecond), relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public AlwaysPrecedesInvariant(String typeFirst,
            StringEventType typeSecond, String relation) {
        this(new StringEventType(typeFirst), typeSecond, relation);
    }

    // ///////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return first.toString() + " AlwaysPrecedes(" + relation.toString()
                + ") " + second.toString();
    }

    @Override
    public String getLTLString() {
        // Weak-until version:
        // return "!(did(" + second + ")) W (did(" + first + "))";

        /**
         * Note that we do not need a "<> TERMINAL ->" prefix in front of the AP
         * LTL formula. This is because an infinite (unfair) loop can only be a
         * part of the counter-example if it contains a 'second' without ever
         * going through a 'first'. And if this is the case then its a valid
         * counter-example, which will be shortened. Therefore we do not need to
         * worry about creating a fairness constraint as with AFby.
         */
        return "((<>(did(" + second.toString() + ")))->((!did("
                + second.toString() + ")) U did(" + first.toString() + ")))";
    }

    /**
     * Returns a sub-trace of the violating trace that looks like ...'second'
     * where 'first' APby 'second' is this invariant and where 'first' does
     * _not_ appear in the returned sub-trace at all. The returned sequence
     * includes the entire trace up to the first appearance of 'second'. If the
     * trace has a 'first' before a 'second' then it returns null.
     * 
     * <pre>
     * NOTE: x AP x cannot be true, so we will never have a counter-example
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
        return APShorten(trace, first, second);
    }

    public static <T extends INode<T>> List<T> APShorten(List<T> trace,
            EventType firstEvent, EventType secondEvent) {
        for (int trace_pos = 0; trace_pos < trace.size(); trace_pos++) {
            T message = trace.get(trace_pos);
            if (message.getEType().equals(firstEvent)) {
                // We found a 'first' before a 'second' (we are assuming that
                // 'second' does exist later on in the trace).
                return null;
            }
            if (message.getEType().equals(secondEvent)) {
                // We found a 'second' before a 'first'.
                return trace.subList(0, trace_pos + 1);
                // return BinaryInvariant.removeLoops(..);
            }
        }
        // We found neither a 'first' nor a 'second'.
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
