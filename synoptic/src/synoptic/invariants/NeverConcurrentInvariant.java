package synoptic.invariants;

import synoptic.model.DistEventType;
import synoptic.util.InternalSynopticException;
import synoptic.util.NotImplementedException;

/**
 * Represents the "x is never concurrent with y" invariant for some two
 * distributed event types x and y. What this means is that for all the inputs
 * traces to Synoptic:
 * 
 * <pre>
 * 1) There was at least one trace in which x and y co-appeared.
 * 2) Whenever x and y co-appeared in a trace, every instance of x and every
 *    instance of y were ordered. That is, for every instance of x and every
 *    instance of y, either (x < y) or (y < x).
 * </pre>
 */
public class NeverConcurrentInvariant extends ConcurrencyInvariant {
    public NeverConcurrentInvariant(DistEventType typeFirst,
            DistEventType typeSecond, String relation) {
        super(typeFirst, typeSecond, relation);
    }

    @Override
    public String toString() {
        String f = first.toString();
        String s = second.toString();
        if (f.hashCode() <= s.hashCode()) {
            return f + " NeverConcurrentWith(" + relation + ") " + s;
        }
        return s + " NeverConcurrentWith(" + relation + ") " + f;

    }

    @Override
    public String getShortName() {
        return "NCwith";
    }

    @Override
    public String getLongName() {
        return "NeverConcurrentWith";
    }

    @Override
    public String getLTLString() {
        throw new InternalSynopticException(
                "LTL string cannot be composed for concurrency invariants");
    }

	@Override
	public String getRegex(char firstC, char secondC) {
		throw new NotImplementedException();
	}
}