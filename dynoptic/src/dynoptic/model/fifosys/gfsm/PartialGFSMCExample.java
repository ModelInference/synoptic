package dynoptic.model.fifosys.gfsm;

import java.util.ArrayList;
import java.util.List;

import mcscm.McScMCExample;

/**
 * Represents a _partial_ counter-example partitions path in the GFSM. This
 * means that it does not completely correspond to the event-based
 * counter-example that is returned by the McScM model checker.
 */
public class PartialGFSMCExample {
    private final List<GFSMState> path;
    private final McScMCExample cExample;

    /** Incremental path construction. */
    public PartialGFSMCExample(McScMCExample cExample) {
        this.path = new ArrayList<GFSMState>();
        this.cExample = cExample;
    }

    /** Adds state to the front of the internal c-example path. */
    public void addToFrontOfPath(GFSMState state) {
        assert this.path != null;
        assert this.path.size() < (cExample.getEvents().size() + 1);

        path.add(0, state);

        // Make sure that this remains a partial GFSMCExample.
        assert path.size() < (cExample.getEvents().size() + 1);
    }

    public int pathLength() {
        return path.size();
    }

    @Override
    public String toString() {
        String ret = "PartialCExample : ";
        int i = 0;
        for (GFSMState p : path) {
            ret += p.toShortString();
            if (i != path.size() - 1) {
                ret += "-- " + cExample.getEvents().get(i).toString() + " --> ";
            }
            i += 1;
        }
        return ret;
    }
}
