package model;

import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.KTailInvariant;
import synoptic.invariants.miners.KTail;
import synoptic.model.EventType;

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
    public InvModel(ITemporalInvariant invariant, EventTypeEncodings encodings) {
        super(encodings);

        this.inv = invariant;

        // Construct an encoded regex for the given invariant.
        String re = "";
        if (invariant instanceof BinaryInvariant) {
            BinaryInvariant invar = (BinaryInvariant) inv;

            char first = encodings.getEncoding(invar.getFirst());
            char second = encodings.getEncoding(invar.getSecond());
            re = invar.getRegex(first, second);
        } else if (invariant instanceof KTailInvariant) {
            KTail tail = ((KTailInvariant) inv).getTail();
            StringBuilder expression = new StringBuilder("(");

            List<EventType> tailEvents = tail.getTailEvents();
            String last = "";
            last += encodings.getEncoding(tailEvents.get(0));
            expression.append("[^" + last + "]");
            for (int i = 1; i < tailEvents.size(); i++) {
                char next = encodings.getEncoding(tailEvents.get(i));
                expression.append("|" + last + "[^" + next + "]");
                last += next;
            }

            List<EventType> followEvents = tail.getFollowEvents();
            expression.append("|" + last + "("
                    + encodings.getEncoding(followEvents.get(0)));
            for (int i = 1; i < followEvents.size(); i++) {
                expression.append("|"
                        + encodings.getEncoding(followEvents.get(i)));
            }

            expression.append("))*");
            re = expression.toString();
        }

        super.intersectWithRE(re);
    }

    /** Returns this model's invariant. */
    public ITemporalInvariant getInvariant() {
        return inv;
    }
}
