package dynoptic.invariants.checkers;

import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.invariants.EventuallyHappens;
import dynoptic.invariants.NeverFollowedBy;

import synoptic.model.event.DistEventType;

/**
 * Factory of binary invariant checkers, as well as the base class for all of
 * these checkers.
 */
public abstract class BinChecker {

    public static BinChecker newChecker(BinaryInvariant inv) {
        assert inv != null;

        if (inv instanceof AlwaysFollowedBy) {
            return new AFbyChecker(inv);
        }
        if (inv instanceof NeverFollowedBy) {
            return new NFbyChecker(inv);
        }
        if (inv instanceof AlwaysPrecedes) {
            return new APChecker(inv);
        }
        if (inv instanceof EventuallyHappens) {
            return new EventuallyChecker(inv);
        }
        throw new IllegalArgumentException("Invariant " + inv.toString()
                + " has an unsupported type.");
    }

    // ////////////////////////////////////////////////////////////////

    /** The invariant that this checker corresponds to. */
    protected final BinaryInvariant inv;

    public BinChecker(BinaryInvariant inv) {
        this.inv = inv;
    }

    /**
     * @return whether or not the new state is an accepting state.
     */
    abstract public boolean transition(DistEventType e);

}
