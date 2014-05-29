package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.INode;

public class NeverFollowedInvariant extends BinaryInvariant {

    public NeverFollowedInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public NeverFollowedInvariant(String typeFirst, String typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), new StringEventType(typeSecond),
                relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public NeverFollowedInvariant(StringEventType typeFirst, String typeSecond,
            String relation) {
        this(typeFirst, new StringEventType(typeSecond), relation);
    }

    /**
     * Assumes the invariant is between two StringEventTypes
     */
    public NeverFollowedInvariant(String typeFirst, StringEventType typeSecond,
            String relation) {
        this(new StringEventType(typeFirst), typeSecond, relation);
    }

    // ///////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return first.toString() + " NeverFollowedBy(" + relation.toString()
                + ") " + second.toString();
    }

    @Override
    public String getLTLString() {
        /**
         * Note that we do not need a "<> TERMINAL ->" prefix in front of the
         * NFby LTL formula. This is because counter examples of this formula
         * cannot get stuck in an infinite (unfair) loop, unless the loop itself
         * contains a 'second'. In which case, the infinite loop is a valid
         * counter-examples which will be shortened.
         */
        return "[](did(" + first.toString() + ") -> X([] !(did("
                + second.toString() + "))))";
    }

    /**
     * Returns a sub-trace of the input violating trace that looks like
     * ...'first' ... 'second' where 'first' NFby 'second' is this invariant. It
     * includes the section of the trace that precedes 'first' and ignores the
     * section of the trace that follows 'second'. If the trace is not a
     * counter-example trace (and therefore does not contain such a sequence)
     * then it returns null.
     * 
     * <pre>
     * NOTE: x NFby x is tricky
     * </pre>
     * 
     * @param <T>
     *            Type of the node in the trace
     * @param trace
     *            the trace we are operating on
     * @return the sub-trace described above
     */
    @Override
    public <T extends INode<T>> List<T> shorten(List<T> trace) {
        return NFShorten(trace, first, second);
    }

    public static <T extends INode<T>> List<T> NFShorten(List<T> trace,
            EventType firstEvent, EventType secondEvent) {
        boolean first_seen = false;
        for (int trace_pos = 0; trace_pos < trace.size(); trace_pos++) {
            T message = trace.get(trace_pos);
            if (message.getEType().equals(firstEvent) && !first_seen) {
                first_seen = true;
            } else if (message.getEType().equals(secondEvent) && first_seen) {
                return trace.subList(0, trace_pos + 1);
            }
        }
        return null;
    }

    @Override
    public String getShortName() {
        return "NFby";
    }

    @Override
    public String getLongName() {
        return "NeverFollowedBy";
    }

    /**
     * Returns a regular expressions describing this invariant for first NFby
     * second. The expression is "[^x]*(x[^y]*)?".
     * 
     * @param firstC
     *            a character representation of first
     * @param secondC
     *            a character representation of second
     * @return a regex for this invariant
     */
    @Override
    public String getRegex(char firstC, char secondC) {
        return "[^" + firstC + "]*(" + firstC + "[^" + secondC + "]*)?";
    }
}
