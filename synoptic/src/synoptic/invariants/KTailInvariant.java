package synoptic.invariants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nasa.ltl.graph.Graph;

import synoptic.invariants.miners.KTail;
import synoptic.model.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.NotImplementedException;

/**
 * Temporal Invariant representing a kTail for some length k. Not a binary
 * invariant as tails are of varying length. Currently construction of regular
 * expressions for kTail invariants is handled in the InvariMint.model.InvModel
 * class. TODO: move that logic here.
 * 
 * @author jennyabrahamson
 */
public class KTailInvariant implements ITemporalInvariant {

    // The KTail object associated with this invariant.
    private final KTail tail;

    private final String relation;

    public KTailInvariant(KTail tail, String relation) {
        this.tail = tail;
        this.relation = relation;
    }

    /**
     * Returns the KTail object associated with this invariant.
     */
    public KTail getTail() {
        return tail;
    }

    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        return path;
    }

    @Override
    public String getShortName() {
        return "kTail";
    }

    @Override
    public String getLongName() {
        return "kTail Invariant";
    }

    @Override
    public String toString() {
        return tail.toString();
    }

    @Override
    public String getRelation() {
        return relation;
    }

    @Override
    public Set<EventType> getPredicates() {
        return new HashSet<EventType>(tail.getTailEvents());
    }

    /**
     * This invariant is not used during refinement or coarsening, so LTL has
     * been left undefined
     */
    @Override
    public String getLTLString() {
        throw new NotImplementedException();
    }

    /**
     * This invariant is not used during refinement or coarsening, so LTL has
     * been left undefined
     */
    @Override
    public Graph getAutomaton() {
        throw new NotImplementedException();
    }
}
