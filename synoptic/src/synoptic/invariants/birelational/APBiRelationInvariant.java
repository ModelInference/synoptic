package synoptic.invariants.birelational;

import java.util.List;

import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.INode;

/**
 * Represents a birelational event invariant in the input traces where the first
 * event is always preceded by the second event.
 */
public class APBiRelationInvariant extends BiRelationalInvariant {

    public APBiRelationInvariant(EventType first, EventType second,
            String relation, String orderingRelation) {
        super(first, second, relation, orderingRelation);
    }

    public APBiRelationInvariant(EventType firstEvent, EventType secondEvent,
            String relation) {
        this(firstEvent, secondEvent, relation, Event.defTimeRelationStr);
    }

    public APBiRelationInvariant(EventType firstEvent, String secondEvent,
            String relation) {
        this(firstEvent, new StringEventType(secondEvent), relation,
                Event.defTimeRelationStr);
    }

    public APBiRelationInvariant(String firstEvent, String secondEvent,
            String relation) {
        this(new StringEventType(firstEvent), new StringEventType(secondEvent),
                relation, Event.defTimeRelationStr);
    }

    @Override
    public String getLTLString() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a sub-trace of the violating trace that looks like ...'second'
     * where 'first' APby 'second' is this invariant and where 'first' does
     * _not_ appear in the returned sub-trace at all. The returned sequence
     * includes the entire trace up to the first appearance of 'second'. If the
     * trace has a 'first' before a 'second' then it returns null.
     * 
     * <pre>
     * NOTE: x AP x cannot be true, so we will never have a counter-example
     * in which 'first' == 'second'.
     * </pre>
     * 
     * @param <T>
     *            The node type of the trace
     * @param trace
     *            the trace we are operating on
     * @return the sub-trace described above
     */
    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        return AlwaysPrecedesInvariant.APShorten(path, first, second);
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
        return first + " APby(" + relation + ", " + orderingRelation + ") "
                + second;
    }

}
