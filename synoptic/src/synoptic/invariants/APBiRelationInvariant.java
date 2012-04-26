package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

public class APBiRelationInvariant extends BinaryInvariant {

    public APBiRelationInvariant(EventType first, EventType second, 
            String relation, String orderingRelation) {
        super(first, second, relation);
        this.orderingRelation = orderingRelation;
    }
    
    @Override
    public String getLTLString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        // TODO Auto-generated method stub
        return null;
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
