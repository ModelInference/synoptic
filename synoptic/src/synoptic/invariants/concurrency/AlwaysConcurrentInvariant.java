package synoptic.invariants.concurrency;

import synoptic.model.event.DistEventType;
import synoptic.util.InternalSynopticException;

/**
 * Represents the "x is always concurrent with y" invariant for some two
 * distributed event types x and y. What this means is that for all the inputs
 * traces to Synoptic:
 * 
 * <pre>
 * 1) There was at least one trace in which x and y co-appeared.
 * 2) In all traces in which x and y co-appeared the predicate (x < y) and the
 *    predicate (x > y) were both false. These two statements basically say that
 *    x and y were never ordered.
 * </pre>
 */
public class AlwaysConcurrentInvariant extends ConcurrencyInvariant {
    public AlwaysConcurrentInvariant(DistEventType typeFirst,
            DistEventType typeSecond, String relation) {
        super(typeFirst, typeSecond, relation);
    }

    @Override
    public String toString() {
        String f = first.toString();
        String s = second.toString();
        if (f.hashCode() <= s.hashCode()) {
            return f + " AlwaysConcurrentWith(" + relation.toString() + ") "
                    + s;
        }
        return s + " AlwaysConcurrentWith(" + relation.toString() + ") " + f;
    }

    @Override
    public String getShortName() {
        return "ACwith";
    }

    @Override
    public String getLongName() {
        return "AlwaysConcurrentWith";
    }

    @Override
    public String getLTLString() {
        throw new InternalSynopticException(
                "LTL string cannot be composed for concurrency invariants");
    }

    @Override
    public String getRegex(char firstC, char secondC) {
        throw new UnsupportedOperationException();
    }
}
