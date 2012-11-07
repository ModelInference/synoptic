package dynoptic.invariants;

import java.util.List;

import dynoptic.invariants.checkers.AFbyChecker;

import synoptic.model.event.DistEventType;

/** A Dynoptic representation of the AFby invariant. */
public class AlwaysFollowedBy extends BinaryInvariant {

    public AlwaysFollowedBy(DistEventType typeFirst, DistEventType typeSecond) {
        super(typeFirst, typeSecond, "AFby");

        // It is impossible to mine x AFby x in a linear trace.
        assert !typeFirst.equals(typeSecond);

    }

    @Override
    public String scmBadStateQRe() {
        checkInitialized();

        // There is an 'a' that is preceded by 'a' or 'b' but is not followed
        // any later by any 'b', though it might be followed by more a's.
        return someSynthEventsQRe() + " . " + firstSynthEventsQRe() + "^+";
    }

    @Override
    public boolean satisfies(List<DistEventType> eventsPath) {
        // T: 'first' appears after all 'second'
        // F: 'first' does not appear, or 'second' appears after last 'first'
        boolean lastFirst = false;

        for (DistEventType e : eventsPath) {
            if (e.equals(first)) {
                lastFirst = true;
                continue;
            }

            if (e.equals(second)) {
                lastFirst = false;
            }
        }
        return !lastFirst;
    }

    @Override
    public AFbyChecker newChecker() {
        return new AFbyChecker(this);
    }
}
