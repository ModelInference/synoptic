package synoptic.invariants;

import java.util.List;

import synoptic.model.interfaces.INode;

public class NeverFollowedInvariant extends BinaryInvariant {

    public NeverFollowedInvariant(String typeFrist, String typeSecond,
            String relation) {
        super(typeFrist, typeSecond, relation);
    }

    @Override
    public String toString() {
        return first + " neverFollowedBy(" + relation + ") " + second;
    }

    @Override
    public String getLTLString() {
        if (useDIDCAN) {
            return "[](did(" + first + ") -> X([] !(did(" + second + "))))";
        } else {
            return "[](\"" + first + "\" -> X([] !(\"" + second + "\")))";
        }
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
        boolean first_seen = false;
        for (int trace_pos = 0; trace_pos < trace.size(); trace_pos++) {
            T message = trace.get(trace_pos);
            if (message.getLabel().equals(first) && !first_seen) {
                first_seen = true;
            } else if (message.getLabel().equals(second) && first_seen) {
                return trace.subList(0, trace_pos + 1);
            }
        }
        return null;
    }

    @Override
    public String getShortName() {
        return "NFby";
    }
}
