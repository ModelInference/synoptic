package dynoptic.model.fifosys.gfsm;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import mcscm.McScMCExample;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.main.DynopticMain;
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
        boolean partialPath = (path.size() != (mcCExample.getEvents().size() + 1));
        String ret = "CExample[partial=" + partialPath + "] : ";
        int i = 0;
        for (GFSMState p : path) {
            ret += p.toString();
            if (i != path.size() - 1) {
                ret += "-- " + mcCExample.getEvents().get(i).toString()
                        + " --> ";
            }
            i += 1;
        }
        return ret;
    }

    // /////////////////////////////////////////////////////////////

    /**
     * Finds a complete GFSM counter-example path that extends this partial
     * counter-example path with events that appear in the McScM spurious
     * counter-example path (spurious since this counter-example is incomplete,
     * or partial).
     */
    public CompleteGFSMCExample extendToCompletePath(BinaryInvariant inv) {
        List<GFSMState> partsPath = new ArrayList<GFSMState>(path);

        List<DistEventType> events = mcCExample.getEvents();
        int eNextIndex = path.size() - 1;

        // The set of events that remain to construct the suffix to partialPath,
        // the spurious counter-example.
        List<DistEventType> eventsRemaining = new ArrayList<DistEventType>(
                events.subList(eNextIndex, events.size()));

        // The set of events that were used to construct partsPath
        List<DistEventType> eventsPath = new ArrayList<DistEventType>(
                mcCExample.getEvents().subList(0, partsPath.size() - 1));

        assert (partsPath.size() == eventsPath.size() + 1);

        return findCompletePathFromPartialPath(inv, partsPath, eventsPath,
                eventsRemaining, path.get(path.size() - 1));
    }

    /**
     * DFS traversal of the GFSM to extend the partial c-example to a complete
     * c-example, constrained by the list of events that we are allowed to use
     * in the constructed suffix.
     */
    private CompleteGFSMCExample findCompletePathFromPartialPath(
            BinaryInvariant inv, List<GFSMState> partsPath,
            List<DistEventType> eventsPath,
            List<DistEventType> eventsRemaining, GFSMState gfsmState) {
        // If we ran out of events to try, then we have constructed a path that
        // is as long as the spurious McScM counter-example.
        if (eventsRemaining.size() == 0) {
            // Basic checks of partitions path and events path lengths.
            assert partsPath.size() == mcCExample.getEvents().size() + 1;
            assert eventsPath.size() == mcCExample.getEvents().size();

            // To be a valid complete path, the last partition must be
            // accepting.
            if (!gfsmState.isAccept()) {
                return null;
            }

            // To be a valid counter-example the events path must fail to
            // satisfy the corresponding invariant.
            if (inv.satisfies(eventsPath)) {
                return null;
            }

            return new CompleteGFSMCExample(partsPath, mcCExample);
        }

        Set<DistEventType> nextEvents = new LinkedHashSet<DistEventType>(
                gfsmState.getTransitioningEvents());
        nextEvents.retainAll(eventsRemaining);

        // Explore possible events from gfsmState that appeared in the spurious
        // counter-example.
        for (DistEventType nextE : nextEvents) {
            assert eventsRemaining.remove(nextE);
            eventsPath.add(nextE);
            for (GFSMState nextState : gfsmState.getNextStates(nextE)) {
                partsPath.add(nextState);

                // Try to continue building the non-spurious counter-example by
                // traversing nextE.
                CompleteGFSMCExample ret = findCompletePathFromPartialPath(inv,
                        partsPath, eventsPath, eventsRemaining, nextState);

                // If we have reached a complete counter-example below this
                // node, then we simply return it -- we just need to find one,
                // not all such paths.
                if (ret != null) {
                    return ret;
                }
                partsPath.remove(partsPath.size() - 1);
            }
            eventsRemaining.add(nextE);
            eventsPath.remove(eventsPath.size() - 1);
        }
        return null;
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

                int newStitchIndex = findMinStitchPartIndex(lastPartIndex - 1,
                        s);
                assert newStitchIndex >= 0;
                assert newStitchIndex < lastPartIndex;

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
        DistEventType e = mcCExample.getEvents().get(sPartIndex - 1);

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

        // We can't refine a partition if it contains just a single observation.
        assert part.getObservedStates().size() > 1;

        logger.info("Refining partition: " + part);

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
        assert setRight.size() > 0;

        // Construct setLeft.
        Set<ObsFifoSysState> setLeft;

        // ///////////////
        // For simplicity, we construct setLeft to be the complement of
        // setRight.
        setLeft = new LinkedHashSet<ObsFifoSysState>();
        for (ObsFifoSysState s : part.getObservedStates()) {
            if (!setRight.contains(s)) {
                setLeft.add(s);
            }
        }

        // ///////////////
        // TODO: more advanced setLeft construction, which does not work when
        // there are self-loops along the counter-example path.
        //
        // if (partIndex == 0) {
        // // Part is the first (initial) partition in path, so we want to
        // // isolate the initial observations in this partition from those
        // // that generate the counter-example path.
        // setLeft = part.getInitialObservations();
        // } else {
        // // As above in determining setRight, except we head to the left
        // // and build a set of observations in part that can be reached
        // // from the previous partition along the counter-example path.
        // setLeft = new LinkedHashSet<ObsFifoSysState>();
        //
        // DistEventType ePrev = mcCExample.getEvents().get(partIndex - 1);
        // GFSMState partPrev = path.get(partIndex - 1);
        // for (ObsFifoSysState s : partPrev
        // .getObservedStatesWithTransition(ePrev)) {
        // if (s.getNextState(ePrev).getParent() == part) {
        // setLeft.add(s.getNextState(ePrev));
        // }
        // }
        // }
        assert setLeft.size() > 0;

        if (DynopticMain.assertsOn) {
            // Make sure that the two sets are disjoint.
            for (ObsFifoSysState s : setLeft) {
                if (setRight.contains(s)) {
                    assert !setRight.contains(s);
                }
            }
            for (ObsFifoSysState s : setRight) {
                assert !setLeft.contains(s);
            }
        }

        pGraph.refineWithRandNonRelevantObsAssignment(part, setLeft, setRight);
    }
}
