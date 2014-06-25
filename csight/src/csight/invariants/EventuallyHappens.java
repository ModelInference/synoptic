package csight.invariants;

import java.util.List;

import csight.invariants.checkers.EventuallyChecker;

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
    public String promelaNeverClaim() {

        // The never claim will be accepted if we reach the end without event b.

        String ret = "";
        ret += String.format("never { /* !(<>(%s)) */\n",
                second.toPromelaString());

        ret += "need_b:\n";
        ret += "    do\n";
        ret += String.format("      :: (! %s ) -> goto need_b;\n",
                secondNeverEvent());
        // If we reach the end state of b's process and we don't see b for the
        // last step, then the invariant is invalid.
        ret += String
                .format("      :: ( ENDSTATECHECK && EMPTYCHANNELCHECK && !%s ) -> break;\n",
                        secondNeverEvent());
        // We don't have a condition for matching b.
        // Never claims will not accept anything when b has been seen.
        ret += "    od;\n";

        ret += "}\n";
        return ret;
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
