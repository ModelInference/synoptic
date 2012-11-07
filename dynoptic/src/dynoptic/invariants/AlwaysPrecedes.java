package dynoptic.invariants;

import java.util.List;

import dynoptic.invariants.checkers.APChecker;

import synoptic.model.event.DistEventType;

/** A Dynoptic representation of the AP invariant. */
public class AlwaysPrecedes extends BinaryInvariant {

    public AlwaysPrecedes(DistEventType typeFirst, DistEventType typeSecond) {
        super(typeFirst, typeSecond, "AP");

        // It is impossible to mine x AP x in a linear trace.
        assert !typeFirst.equals(typeSecond);
    }

    @Override
    public String scmBadStateQRe() {
        checkInitialized();

        // There is a 'b' that was never preceded by an 'a'. This 'b' is
        // followed by any number of a's or b's.
        return secondSynthEventsQRe() + "^+ . " + someSynthEventsQRe();
    }

    @Override
    public boolean satisfies(List<DistEventType> eventsPath) {
        // Whether or not we've seen 'first' so far.
        boolean seenFirst = false;
        for (DistEventType e : eventsPath) {
            if (!seenFirst && e.equals(first)) {
                seenFirst = true;
                continue;
            }

            // If we did not see 'first', and e is 'second' then this instance
            // of 'second' is not preceded by a 'first' => ~(first AP second).
            if (!seenFirst && e.equals(second)) {
                return false;
            }
        }

        // Either we never saw 'second', or we saw a 'first' before a first
        // instance of 'second' => first AP second.
        return true;
    }

    @Override
    public APChecker newChecker() {
        return new APChecker(this);
    }
}
