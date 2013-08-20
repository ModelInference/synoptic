package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

public class TOInitialInvariant extends BinaryInvariant {

    public TOInitialInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
    }

    /**
     * This invariant is not used during refinement or coarsening, so LTL has
     * been left undefined
     */
    @Override
    public String getLTLString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        return path;
    }

    @Override
    public String toString() {
        return "Initial event: " + first.toString() + ", Terminal event: "
                + second.toString();
    }

    @Override
    public String getShortName() {
        return "^Initial[^Terminal]*Terminal$";
    }

    @Override
    public String getLongName() {
        return getShortName();
    }

    @Override
    public String getRegex(char firstC, char secondC) {
        return firstC + "[^" + secondC + "]*" + secondC;
    }

}
