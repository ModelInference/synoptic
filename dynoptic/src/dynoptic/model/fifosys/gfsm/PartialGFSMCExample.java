package dynoptic.model.fifosys.gfsm;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import mcscm.McScMCExample;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;

import synoptic.model.event.DistEventType;

/**
 * Represents a _partial_ counter-example partitions path in the GFSM. This
 * means that it does not completely correspond to the event-based
 * counter-example that is returned by the McScM model checker.
 */
public class PartialGFSMCExample {
    protected static Logger logger = Logger.getLogger("GFSMCExample");

    protected final List<GFSMState> path;
    protected final McScMCExample mcCExample;

    // Whether or not this counter-example path has been resolved (refined) and
    // therefore no longer exists in the corresponding GFSM.
    protected boolean isResolved = false;

    /** Incremental path construction. */
    public PartialGFSMCExample(McScMCExample cExample) {
        this.path = new ArrayList<GFSMState>();
        this.mcCExample = cExample;
    }

    /** All-at-once path construction, used by GFSMCExample. */
    protected PartialGFSMCExample(List<GFSMState> path, McScMCExample cExample) {
        assert path.get(0).isInitial();
        this.path = path;
        this.mcCExample = cExample;
        this.isResolved = false;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public List<GFSMState> getPartitionPath() {
        return path;
    }

    public McScMCExample getMcScMCExample() {
        return mcCExample;
    }

    public int pathLength() {
        return path.size();
    }

    /** Adds state to the front of the internal c-example path. */
    public void addToFrontOfPath(GFSMState state) {
        addToFrontOfPathNoPostChecks(state);

        // Make sure that this remains a partial GFSMCExample.
        assert path.size() < (mcCExample.getEvents().size() + 1);
    }

    /** Adds state to the front of the internal c-example path. */
    protected void addToFrontOfPathNoPostChecks(GFSMState state) {
        assert this.path != null;
        assert this.path.size() < (mcCExample.getEvents().size() + 1);

        path.add(0, state);

    }

    @Override
    public String toString() {
        boolean partialPath = (path.size() == (mcCExample.getEvents().size() + 1));
        String ret = "CExample[partial=" + partialPath + " : ";
        int i = 0;
        for (GFSMState p : path) {
            ret += p.toShortString();
            if (i != path.size() - 1) {
                ret += "-- " + mcCExample.getEvents().get(i).toString()
                        + " --> ";
            }
            i += 1;
        }
        return ret;
    }

    /**
     * To resolve a partial counter-example in a GFSM (regardless of the
     * corresponding invariant type) we find the partition that stitches
     * together observations and is farthest from the end of the partial path
     * and refine this partition.
     * 
     * @param pGraph
     */
    public void resolve(GFSM pGraph) {
        assert path.size() >= 2;

        GFSMState lastPart = path.get(path.size() - 1);
        GFSMState penultPart = path.get(path.size() - 2);

        int lastPartIndex = path.size() - 1;
        int minLastStitchPartIndex = path.size() - 1;

        // Last event transition in the partial path.
        DistEventType e = mcCExample.getEvents().get(path.size() - 2);

        for (ObsFifoSysState s : penultPart.getObservedStatesWithTransition(e)) {

            for (ObsFifoSysState sChild : s.getNextStates(e)) {
                // Ignore observed states that do not transition to the last
                // partition in the partial path.
                if (sChild.getParent() != lastPart) {
                    continue;
                }

                int newStitchIndex = findMinStitchPartIndex(lastPartIndex,
                        sChild);
                assert newStitchIndex >= 0;
                assert newStitchIndex <= lastPartIndex;

                if (newStitchIndex < minLastStitchPartIndex) {
                    minLastStitchPartIndex = newStitchIndex;
                }
            }
        }

        // //////////
        // Now refine the partition at minLastStitchPartIndex.
        refinePartition(pGraph, minLastStitchPartIndex);
    }

    // /////////////////////////////////////////////////////////////

    /**
     * Recursively follows the observed state s back along the partition in the
     * counter-example path. Once we find that we cannot follow it back any
     * further, we return the partition index, or the minimum stitch partition
     * index for s.
     * 
     * @param sPartIndex
     * @param s
     * @return
     */
    protected int findMinStitchPartIndex(int sPartIndex, ObsFifoSysState s) {
        if (sPartIndex == 0) {
            return sPartIndex;
        }
        GFSMState prevPart = path.get(sPartIndex - 1);
        int minIndex = sPartIndex;
        DistEventType e = mcCExample.getEvents().get(sPartIndex);

        // There might be multiple observed states from prevPart that transition
        // to s, so we explore all of them and return the min index we find.
        for (ObsFifoSysState sPred : prevPart
                .getObservedStatesWithTransition(e)) {
            // The observed fifo sys instances are deterministic, so there is
            // just one transition on e from sPred.
            if (sPred.getNextState(e).equals(s)) {
                int newStitchIndex = findMinStitchPartIndex(sPartIndex - 1,
                        sPred);
                if (newStitchIndex < minIndex) {
                    minIndex = newStitchIndex;
                }
            }
        }
        return minIndex;
    }

    /**
     * Refine partition at partIndex in the path of partition along the
     * counter-example path. The goal is to isolate setLeft and setRight of
     * observations in this partition. These two sets are constructed so that
     * after the partitioning this counter-example path is eliminated.
     * Observations that are not pertinent to this refinement in the partition
     * are assigned at random to setLeft and setRight.
     * 
     * @param pGraph
     * @param partIndex
     */
    protected void refinePartition(GFSM pGraph, int partIndex) {
        GFSMState part = path.get(partIndex);

        // Construct setRight.
        Set<ObsFifoSysState> setRight;

        if (partIndex == (path.size() - 1)) {
            // Part is the last (terminal) partition in path, so we want to
            // isolate the observations that allow the counter-example path
            // to terminate at this partition from events that have
            // transitioned the path into this partition.
            setRight = part.getTerminalObservations();
        } else {
            // Construct setRight to contain observations that transition
            // from part to partNext in the counter-example path.
            setRight = new LinkedHashSet<ObsFifoSysState>();
            DistEventType eNext = mcCExample.getEvents().get(partIndex);
            GFSMState partNext = path.get(partIndex + 1);
            for (ObsFifoSysState s : part
                    .getObservedStatesWithTransition(eNext)) {
                if (s.getNextState(eNext).getParent() == partNext) {
                    setRight.add(s);
                }
            }
        }

        // Construct setLeft.
        Set<ObsFifoSysState> setLeft;

        if (partIndex == 0) {
            // Part is the first (initial) partition in path, so we want to
            // isolate the initial observations in this partition from those
            // that generate the counter-example path.
            setLeft = part.getInitialObservations();
        } else {
            // As above for determining setRight, but we head to the left
            // and build a set of observations in part that can be reached
            // from the previous partition along the counter-example path.

            setLeft = new LinkedHashSet<ObsFifoSysState>();

            DistEventType ePrev = mcCExample.getEvents().get(partIndex - 1);
            GFSMState partPrev = path.get(partIndex - 1);
            for (ObsFifoSysState s : partPrev
                    .getObservedStatesWithTransition(ePrev)) {
                if (s.getNextState(ePrev).getParent() == part) {
                    setLeft.add(s.getNextState(ePrev));
                }
            }
        }

        pGraph.refineWithRandNonRelevantObsAssignment(part, setLeft, setRight);
    }
}
