package dynoptic.invariants;

import java.util.List;

import dynoptic.invariants.checkers.NFbyChecker;

import synoptic.model.event.DistEventType;

/** A Dynoptic representation of the NFby invariant. */
public class NeverFollowedBy extends BinaryInvariant {

    public NeverFollowedBy(DistEventType typeFirst, DistEventType typeSecond) {
        super(typeFirst, typeSecond, "NFby");
    }

    @Override
    public String scmBadStateQRe() {
        checkInitialized();

        // There is an 'a', preceded by any number of 'a' or 'b', but which is
        // followed by at least one 'b'. This last 'b' can be intermingled in
        // any way with 'a' and other 'b' instances.
        return someSynthEventsQRe() + " . " + firstSynthEventsQRe() + " . "
                + someSynthEventsQRe() + " . " + secondSynthEventsQRe() + " . "
                + someSynthEventsQRe();
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

            // If we saw 'first', and e is 'second' then first is followed by
            // second in eventsPath => ~(first NFby second).
            if (seenFirst && e.equals(second)) {
                return false;
            }
        }
        // Never saw 'first' followed by 'second' => first NFby second
        return true;
    }

    @Override
    public NFbyChecker newChecker() {
        return new NFbyChecker(this);
    }
}
