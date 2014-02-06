package model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extends the EncodedAutomaton class to encode the intersection of multiple
 * InvModel Automatons.
 * 
 */
public class InvsModel extends EncodedAutomaton {

    // The set of invariants represented by this Automaton.
    private Set<InvModel> invariants;

    /**
     * Constructs a new InvsModel that accepts all strings.
     */
    public InvsModel(EventTypeEncodings encodings) {
        super(encodings);
        invariants = new HashSet<InvModel>();
    }

    /**
     * Returns the set of ITemporalInvariants composing this model.
     */
    public Set<InvModel> getInvariants() {
        return Collections.unmodifiableSet(invariants);
    }

    /**
     * Intersects this InvsModel with all of the given InvModels and adds each
     * to this model's list of invariants.
     */
    public void intersectWith(List<InvModel> invs) {
        for (InvModel inv : invs) {
            this.intersectWith(inv);
        }
    }

    /**
     * Intersects this InvsModel with the given InvsModel, maintaining
     * invariants of both.
     */
    public void intersectWith(InvsModel other) {
        this.invariants.addAll(other.invariants);
        super.intersectWith(other, "Intersecting two InvsModels");
    }

    /**
     * Intersects this InvsModel with the given InvModel and adds the invariant
     * to this model's list of invariants.
     */
    public void intersectWith(InvModel inv) {
        invariants.add(inv);
        super.intersectWith(inv,
                "Intersecting model with " + inv.getInvariant());
    }
}
