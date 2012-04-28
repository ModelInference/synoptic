package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * Represents a birelational event invariant in the input 
 * traces where the first event is always followed by the 
 * second event.
 */
public class AFBiRelationInvariant extends BiRelationalInvariant {

    public AFBiRelationInvariant(EventType first, EventType second,
            String relation, String orderingRelation) {
        super(first, second, relation, orderingRelation);
    }

    @Override
    public String getLTLString() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unlike the other types of invariants' counter-example paths, an AFby
     * counter-example path cannot be trivially shortened because it must
     * include the entire path to the TERMINAL node.
     */
    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        return path;
    }

    @Override
    public String getShortName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLongName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRegex(char firstC, char secondC) {
        throw new UnsupportedOperationException();
    }

}
