package synoptic.invariants;

import java.util.List;

import synoptic.model.interfaces.INode;

public class AlwaysPrecedesInvariant extends BinaryInvariant {

    public AlwaysPrecedesInvariant(String typeFrist, String typeSecond,
            String relation) {
        super(typeFrist, typeSecond, relation);
    }

    @Override
    public String toString() {
        return first + " AlwaysPrecedes(" + relation + ") " + second;
    }

    @Override
    public String getLTLString() {
        if (useDIDCAN) {
            return "(<>(did(second)))->((!did(first)) U did(second))";
        } else {
            return "(<>(second))->((!first) U second)";
        }
    }

    /**
     * Returns a sub-trace of the violating trace that looks like ...'second'
     * where 'first' APby 'second' is this invariant and where 'first' does
     * _not_ appear in the returned sub-trace at all. The returned sequence
     * includes the entire trace up to the first appearance of 'second'. If the
     * trace has a 'first' before a 'second' then it returns null.
     * 
     * @param <T>
     * @param first_seen
     *            whether or not we've seen' first' in the trace so far
     * @param trace_pos
     *            the position of where we are in the trace so far
     * @param trace
     *            the trace we are operating on
     * @return the sub-trace described above
     */
    private <T extends INode<T>> List<T> shortenImp(boolean first_seen,
            int trace_pos, List<T> trace) {
        if (trace.size() <= trace_pos) {
            return null;
        }
        T message = trace.get(trace_pos);
        if (message.getLabel().equals(first)) {
            first_seen = true;
        }
        if (message.getLabel().equals(second) && !first_seen) {
            return trace.subList(0, trace_pos + 1);
        }
        return shortenImp(first_seen, trace_pos + 1, trace);
    }

    @Override
    public <T extends INode<T>> List<T> shorten(List<T> trace) {
        return shortenImp(false, 0, trace);
    }

    @Override
    public String getShortName() {
        return "AP";
    }
}
