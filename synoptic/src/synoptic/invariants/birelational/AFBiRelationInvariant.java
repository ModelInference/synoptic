package synoptic.invariants.birelational;

import java.util.List;

import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.INode;

/**
 * Represents a birelational event invariant in the input traces where the first
 * event is always followed by the second event.
 */
public class AFBiRelationInvariant extends BiRelationalInvariant {

    public AFBiRelationInvariant(EventType firstEvent, EventType secondEvent,
            String relation, String orderingRelation) {
        super(firstEvent, secondEvent, relation, orderingRelation);
    }

    public AFBiRelationInvariant(EventType firstEvent, EventType secondEvent,
            String relation) {
        this(firstEvent, secondEvent, relation, Event.defTimeRelationStr);
    }

    public AFBiRelationInvariant(EventType firstEvent, String secondEvent,
            String relation) {
        this(firstEvent, new StringEventType(secondEvent), relation,
                Event.defTimeRelationStr);
    }

    public AFBiRelationInvariant(String firstEvent, String secondEvent,
            String relation) {
        this(new StringEventType(firstEvent), new StringEventType(secondEvent),
                relation, Event.defTimeRelationStr);
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

    @Override
    public String toString() {
        return first + " AFby(" + relation + ", " + orderingRelation + ") "
                + second;
    }

}
