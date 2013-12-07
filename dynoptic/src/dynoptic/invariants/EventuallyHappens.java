package dynoptic.invariants;

import java.util.List;

import dynoptic.invariants.checkers.EventuallyChecker;

import synoptic.model.event.DistEventType;

/**
 * Represents an "X eventually happens" invariant type.
 */
public class EventuallyHappens extends BinaryInvariant {

    public EventuallyHappens(DistEventType event) {
        super(DistEventType.INITIALEventType, event, "Eventually");
    }

    public DistEventType getEvent() {
        return this.getSecond();
    }

    @Override
    public void checkInitialized() {
        // Override the initialization checking to just check for the first
        // synthetic events pair, since this invariant type does not use the
        // second pair.
        assert firstSynth1 != null;
        assert secondSynth1 != null;
    }

    @Override
    public String scmBadStateQRe() {
        // There are 0 occurrences of 'X'.
        return "_";
    }

    @Override
    public boolean satisfies(List<DistEventType> eventsPath) {
        for (DistEventType e : eventsPath) {
            if (e.equals(second)) {
                return true;
            }
        }
        // Never saw 'second' => eventually 'second' is false.
        return false;
    }

    @Override
    public EventuallyChecker newChecker() {
        return new EventuallyChecker(this);
    }
}
