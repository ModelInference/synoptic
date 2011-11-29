package model;

import synoptic.invariants.ITemporalInvariant;

/**
 * Extends the EncodedAutomaton class to encode a single ITemporalInvariant.
 * 
 * @author Jenny
 */
public class InvModel extends EncodedAutomaton {

    // The invariant represented with this Automaton.
    private ITemporalInvariant inv;

    /**
     * Generates an EncodedAutomaton for the given invariant. Encodes the names
     * of both EventTypes composing the invariant and constructs the Automaton
     * by using those characters in a regex representing the invariant.
     */
    public InvModel(ITemporalInvariant invariant) {
        this.inv = invariant;

        // Construct an encoded regex for the given invariant.
        char first = super.getEncoding(inv.getFirst().toString());
        char second = super.getEncoding(inv.getSecond().toString());
        String re = inv.getRegex(first, second);

        super.intersectWithRE(re);
    }

    /** Returns this model's invariant. */
    public ITemporalInvariant getInvariant() {
        return inv;
    }
}
