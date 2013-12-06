package synoptic.invariants.birelational;

import java.util.List;

import synoptic.invariants.NeverFollowedInvariant;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.INode;

/**
 * Represents a birelational event invariant in the input traces where the first
 * event is never followed by the second event.
 */
public class NFBiRelationInvariant extends BiRelationalInvariant {

    public NFBiRelationInvariant(EventType first, EventType second,
            String relation, String orderingRelation) {
        super(first, second, relation, orderingRelation);
    }

    public NFBiRelationInvariant(EventType firstEvent, EventType secondEvent,
            String relation) {
        this(firstEvent, secondEvent, relation, Event.defTimeRelationStr);
    }

    public NFBiRelationInvariant(EventType firstEvent, String secondEvent,
            String relation) {
        this(firstEvent, new StringEventType(secondEvent), relation,
                Event.defTimeRelationStr);
    }

    public NFBiRelationInvariant(String firstEvent, String secondEvent,
            String relation) {
        this(new StringEventType(firstEvent), new StringEventType(secondEvent),
                relation, Event.defTimeRelationStr);
    }

    @Override
    public String getLTLString() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a sub-trace of the input violating trace that looks like
     * ...'first' ... 'second' where 'first' NFby 'second' is this invariant. It
     * includes the section of the trace that precedes 'first' and ignores the
     * section of the trace that follows 'second'. If the trace is not a
     * counter-example trace (and therefore does not contain such a sequence)
     * then it returns null.
     * 
     * <pre>
     * NOTE: x NFby x is tricky
     * </pre>
     * 
     * @param <T>
     *            Type of the node in the trace
     * @param trace
     *            the trace we are operating on
     * @return the sub-trace described above
     */
    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        return NeverFollowedInvariant.NFShorten(path, first, second);
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
        return first + " NFby(" + relation + ", " + orderingRelation + ") "
                + second;
    }

}
