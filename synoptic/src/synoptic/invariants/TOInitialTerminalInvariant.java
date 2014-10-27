package synoptic.invariants;

import java.util.List;

import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * An implicit invariant for totally ordered Synoptic models that encodes the
 * constraint that (1) every trace must start with an Initial event, (2) end
 * with a Terminal event, and (3) have no intermediate Terminal events.
 */
public class TOInitialTerminalInvariant extends BinaryInvariant {

    public TOInitialTerminalInvariant(EventType typeFirst,
            EventType typeSecond, String relation) {
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
        return "^Initial[^(Initial|Terminal)]*Terminal$";
    }

    @Override
    public String getLongName() {
        return getShortName();
    }

    @Override
    public String getRegex(char firstC, char secondC) {
        return firstC + "[^" + firstC + secondC + "]*" + secondC;
    }

}
