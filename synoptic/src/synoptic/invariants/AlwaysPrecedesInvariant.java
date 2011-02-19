package synoptic.invariants;

import java.util.List;

import synoptic.model.interfaces.INode;
import synoptic.util.InternalSynopticException;

public class AlwaysPrecedesInvariant extends BinaryInvariant {

    public AlwaysPrecedesInvariant(String typeFirst, String typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
        if (typeFirst == typeSecond) {
            throw new InternalSynopticException(
                    "x AlwaysPrecedes x can never be true");
        }
    }

    @Override
    public String toString() {
        return first + " AlwaysPrecedes(" + relation + ") " + second;
    }

    @Override
    public String getLTLString() {
        if (useDIDCAN) {
            // Weak-until version:
            // return "!(did(" + second + ")) W (did(" + first + "))";

            /**
             * Note that we do not need a "<> TERMINAL ->" prefix in front of
             * the AP LTL formula. This is because an infinite (unfair) loop can
             * only be a part of the counter-example if it contains a 'second'
             * without ever going through a 'first'. And if this is the case
             * then its a valid counter-example, which will be shortened.
             * Therefore we do not need to worry about creating a fairness
             * constraint as with AFby.
             */
            return "((<>(did(" + second + ")))->((!did(" + second + ")) U did("
                    + first + ")))";
        } else {
            return "(<>(" + second + "))->((!" + second + ") U " + first + ")";
        }
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
        for (int trace_pos = 0; trace_pos < trace.size(); trace_pos++) {
            T message = trace.get(trace_pos);
            if (message.getLabel().equals(first)) {
                // We found a 'first' before a 'second' (we are assuming that
                // 'second' does exist later on in the trace).
                return null;
            }
            if (message.getLabel().equals(second)) {
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
}
