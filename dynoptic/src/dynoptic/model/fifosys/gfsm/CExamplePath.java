package dynoptic.model.fifosys.gfsm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mcscm.CounterExample;
import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.invariants.EventuallyHappens;
import dynoptic.invariants.NeverFollowedBy;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;

import synoptic.model.event.DistEventType;

/**
 * Represents a counter-example path in the GFSM, which corresponds to an
 * event-based counter-example that is returned by the McScM model checker.
 */
public class CExamplePath {
    private final List<GFSMState> path;
    private final CounterExample cExample;

    // Maps each event in cExample to the _lowest_ value index in the path list
    // of the state that emits the event (i.e., the first occurrence of the
    // event type in the cExample).
    private Map<DistEventType, Integer> eToFirstSrcStateIndexMap;

    // Maps each event in cExample to the _highest_ value index in the path list
    // of the state that emits the event (i.e., the final occurrence of the
    // event type in the cExample).
    private Map<DistEventType, Integer> eToLastSrcStateIndexMap;

    // Whether or not this counter-example path has been resolved (refined) and
    // therefore no longer exists in the corresponding GFSM.
    private boolean isResolved;

    // Whether or not the path corresponding to cExample is completely
    // initialized (i.e., whether it is of the appropriate length).
    private boolean isInitialized;

    /** Incremental path construction. */
    public CExamplePath(CounterExample cExample) {
        this.path = new ArrayList<GFSMState>();
        this.cExample = cExample;
        this.isInitialized = false;
    }

    /** Adds state to the front of the internal c-example path. */
    public void addToFrontOfPath(GFSMState state) {
        assert this.path != null;
        assert this.path.size() < (cExample.getEvents().size() + 1);

        path.add(0, state);
        if (path.size() == (cExample.getEvents().size() + 1)) {
            assert path.get(0).isInitial();
            assert path.get(path.size() - 1).isAccept();
            this.isInitialized = true;
            setUpIndexMaps();
        }
    }

    /**
     * Computes the mapping of event types to their occurrence in the
     * counter-example list of events.
     */
    private void setUpIndexMaps() {
        assert this.isInitialized;

        this.eToFirstSrcStateIndexMap = new LinkedHashMap<DistEventType, Integer>();
        this.eToLastSrcStateIndexMap = new LinkedHashMap<DistEventType, Integer>();

        for (int i = 0; i < path.size(); i++) {
            if (i < cExample.getEvents().size()) {
                DistEventType e = cExample.getEvents().get(i);
                // For 'First' index map only record the event if no prior
                // record exists (i.e., if it's the first observation of this
                // event along cExample).
                if (!eToFirstSrcStateIndexMap.containsKey(e)) {
                    eToFirstSrcStateIndexMap.put(e, i);
                }

                // For 'Last' index map, always record the event.
                eToLastSrcStateIndexMap.put(e, i);
            }
        }
    }

    // /////////////////////////////////////////////////////////////

    /** All-at-once path construction. */
    public CExamplePath(List<GFSMState> path, CounterExample cExample) {
        // A few consistency checks.
        assert path.size() == (cExample.getEvents().size() + 1);
        assert path.get(0).isInitial();
        assert path.get(path.size() - 1).isAccept();

        this.path = path;
        this.cExample = cExample;
        this.isResolved = false;
        this.isInitialized = true;
        setUpIndexMaps();
    }

    /**
     * Resolves the invariant inv counter-example path by refining a GFSMState
     * partition along the path. For each invariant type inv, this performs
     * different kinds of refinement. The basic strategy is to refine one
     * partition, eliminating the counter-example path. Events in the refined
     * partition that are _not_ relevant to the invariant are mapped at random
     * to the two resulting refined partitions.
     */
    public void resolve(BinaryInvariant inv, GFSM pGraph) {
        assert this.isInitialized;

        if (inv instanceof AlwaysPrecedes) {
            resolve((AlwaysPrecedes) inv, pGraph);
        } else if (inv instanceof AlwaysFollowedBy) {
            resolve((AlwaysFollowedBy) inv, pGraph);
        } else if (inv instanceof NeverFollowedBy) {
            resolve((NeverFollowedBy) inv, pGraph);
        } else if (inv instanceof EventuallyHappens) {
            resolve((EventuallyHappens) inv, pGraph);
        }
        this.isResolved = true;
    }

    /**
     * Resolves an AFby counter-example. For x AFby y, the path includes an x
     * without a following y. We refine partitions that follow the _last_ x
     * along the counter-example path. This is guaranteed to eventually satisfy
     * x AFby y.
     */
    private void resolve(AlwaysFollowedBy inv, GFSM pGraph) {
        DistEventType x = inv.getFirst();
        assert eToLastSrcStateIndexMap.containsKey(x);

        // The last partition along the path that emits the event x.
        int xPartSrcIndex = eToLastSrcStateIndexMap.get(x);
        GFSMState xPartSrc = path.get(xPartSrcIndex);

        // The set of observations in xPartSrc that emit x.
        Set<ObsFifoSysState> xObsSources = xPartSrc
                .getObservedStatesWithTransition(x);

        // Determine the partition to refine, by tracking forward each
        // observation that emits x from this partition along the
        // counter-example path and identifying the partition where the
        // observation was 'stitched' onto another observation. The
        // partition of this kind that has maximal index (farthest along the
        // path) is the one we refine.
        int maxLastStitchPartIndex = 0;
        for (ObsFifoSysState s : xObsSources) {
            int i = findMaxStitchPartIndex(xPartSrcIndex, s);
            if (i > maxLastStitchPartIndex) {
                maxLastStitchPartIndex = i;
            }
        }

        // //////////
        // Now refine the partition at maxLastStitchPartIndex.
        refinePartition(pGraph, maxLastStitchPartIndex);
    }

    /**
     * Resolves an NFby counter-example. For x NFby y, the path includes an x
     * that is followed by a y. Refine the partition that emits the x, or if
     * this is not possible, refine a partition after the partition that emits
     * the x (but before a y is emitted). This refinement should be possible.
     */
    private void resolve(NeverFollowedBy inv, GFSM pGraph) {
        // TODO
    }

    /**
     * Resolves an EventuallyHappens counter-example. For EventuallyHappens y,
     * the path does not include a y. Refine the first partition that contains a
     * stitching to the next partition to eliminate the counter-example path.
     */
    private void resolve(EventuallyHappens inv, GFSM pGraph) {
        // TODO
    }

    /**
     * Resolves an AP counter-example. For x AP y, the path includes a y without
     * a preceding x. We refine states that precede the _first_ y along the
     * counter-example path and this is guaranteed to satisfy x AP y.
     */
    private void resolve(AlwaysPrecedes inv, GFSM pGraph) {
        DistEventType y = inv.getSecond();
        assert eToFirstSrcStateIndexMap.containsKey(y);

        // The partition that emits the event y.
        int yPartSrcIndex = eToFirstSrcStateIndexMap.get(y);
        GFSMState yPartSrc = path.get(yPartSrcIndex);

        // The set of observations in yPartSrc that emit y.
        Set<ObsFifoSysState> yObsSources = yPartSrc
                .getObservedStatesWithTransition(y);

        // The simple case is when y is emitted by an observed state whose
        // parent is not in the preceding (along c-example path) partition.
        // Therefore, we can refine ySrc to isolate the y from any observed
        // states that do have a parent in the preceding partition.
        //
        // The more advanced case is when ySrc does have a parent in the
        // preceding partition, so we have to track back this chain until we
        // find a stitching.
        //
        // A few more complications we deal with:
        // - There might be multiple observed states that emit y.
        // - The stitching might be in the first partition, so we have to
        // separate initial observed states from non-initials.
        // - The stitching might be in the last partition, so we have to
        // separate terminal/accepting observed states from the
        // non-terminals.

        // Determine the partition to refine, by tracking back each
        // observation that emits y from this partition along the
        // counter-example path and identifying the partition where the
        // observation was 'stitched' onto another observation. The
        // partition of this kind that has minimal index (first along the
        // path) is the one we refine.
        int minLastStitchPartIndex = path.size();
        for (ObsFifoSysState s : yObsSources) {
            int i = findMinStitchPartIndex(yPartSrcIndex, s);
            if (i < minLastStitchPartIndex) {
                minLastStitchPartIndex = i;
            }
        }

        // //////////
        // Now refine the partition at minLastStitchPartIndex.
        refinePartition(pGraph, minLastStitchPartIndex);
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
    private void refinePartition(GFSM pGraph, int partIndex) {
        GFSMState part = path.get(partIndex);

        // Construct setRight.
        Set<ObsFifoSysState> setRight;

        if (partIndex == path.size()) {
            // Part is the last (terminal) partition in path, so we want to
            // isolate the observations that allow the counter-example path
            // to terminate at this partition from events that have
            // transitioned the path into this partition.
            setRight = part.getTerminalObservations();
        } else {
            // Construct setRight to contain observations that transition
            // from part to partNext in the counter-example path.
            setRight = new LinkedHashSet<ObsFifoSysState>();
            DistEventType eNext = cExample.getEvents().get(partIndex);
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

            DistEventType ePrev = cExample.getEvents().get(partIndex - 1);
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

    /**
     * Follows the observed state s back along the partition in the
     * counter-example path. Once we find that we cannot follow it back any
     * further, we return the partition index, or the minimum stitch partition
     * index for s.
     * 
     * @param sPartIndex
     * @param s
     * @return
     */
    private int findMinStitchPartIndex(int sPartIndex, ObsFifoSysState s) {
        if (sPartIndex == 0) {
            return sPartIndex;
        }
        GFSMState prevPart = path.get(sPartIndex - 1);
        int minIndex = sPartIndex;
        DistEventType e = cExample.getEvents().get(sPartIndex);

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
     * Follows the observed state s back along the partition in the
     * counter-example path. Once we find that we cannot follow it back any
     * further, we return the partition index, or the minimum stitch partition
     * index for s.
     * 
     * @param sPartIndex
     * @param s
     * @return
     */
    private int findMaxStitchPartIndex(int sPartIndex, ObsFifoSysState s) {
        if (sPartIndex == (path.size() - 1)) {
            return sPartIndex;
        }

        GFSMState sPartNext = path.get(sPartIndex + 1);
        DistEventType e = cExample.getEvents().get(sPartIndex);
        ObsFifoSysState sNext = s.getNextState(e);
        if (sNext == null || sNext.getParent() != sPartNext) {
            return sPartIndex;
        }

        return findMaxStitchPartIndex(sPartIndex + 1, sNext);
    }

    public boolean isResolved() {
        assert this.isInitialized;

        return isResolved;
    }

    @Override
    public String toString() {
        assert this.isInitialized;

        String ret = "CExample[";
        int i = 0;
        for (GFSMState p : path) {
            ret += p.toString();
            if (i != path.size() - 1) {
                ret += "-- " + cExample.getEvents().get(i).toString() + " --> ";
            }
            i += 1;
        }
        return ret + "]";
    }
}
