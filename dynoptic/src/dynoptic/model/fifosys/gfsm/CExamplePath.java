package dynoptic.model.fifosys.gfsm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import mcscm.CounterExample;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;

import synoptic.model.event.DistEventType;

/**
 * Represents a counter-example path in the GFSM, which corresponds to an
 * event-based counter-example that is returned by the McScM model checker.
 */
public class CExamplePath {
    private final List<GFSMState> path;
    private final CounterExample cExample;

    // Maps each event in cExample to the lowest value index in the path list of
    // the state that emits the event.
    private Map<DistEventType, Integer> eToSrcStateIndexMap;

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
            setUpIndexMap();
        }
    }

    private void setUpIndexMap() {
        assert this.isInitialized;

        this.eToSrcStateIndexMap = new LinkedHashMap<DistEventType, Integer>();

        for (int i = 0; i < path.size(); i++) {
            if (i < cExample.getEvents().size()) {
                DistEventType e = cExample.getEvents().get(i);
                // Only record the event if no prior record exists (i.e., if
                // it's the first observation of this event along cExample).
                if (!eToSrcStateIndexMap.containsKey(e)) {
                    eToSrcStateIndexMap.put(e, i);
                }
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
        setUpIndexMap();
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

        // For x AP y, the path includes a y without a preceding x.
        // Refine the state that emits the y to eliminate the
        // counter-example path.
        if (inv instanceof AlwaysPrecedes) {
            DistEventType y = inv.getSecond();
            assert eToSrcStateIndexMap.containsKey(y);

            // The partition that emits the event y.
            int yPartSrcIndex = eToSrcStateIndexMap.get(y);
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
            // Now refine the partition at minLastStitchPartIndex. The goal is
            // to isolate setLeft and setRight of observations in this
            // partition.

            // Construct setRight.
            Set<ObsFifoSysState> setRight;

            GFSMState part = path.get(minLastStitchPartIndex);

            if (minLastStitchPartIndex == path.size()) {
                // Part is the last (terminal) partition in path, so we want to
                // isolate the observations that allow the counter-example path
                // to terminate at this partition from events that have
                // transitioned the path into this partition.
                setRight = part.getTerminalObservations();
            } else {
                // Construct setRight to contain observations that transition
                // from part to partNext in the counter-example path.
                setRight = new LinkedHashSet<ObsFifoSysState>();
                DistEventType eNext = cExample.getEvents().get(
                        minLastStitchPartIndex);
                GFSMState partNext = path.get(minLastStitchPartIndex + 1);
                for (ObsFifoSysState s : part
                        .getObservedStatesWithTransition(eNext)) {
                    if (s.getNextState(eNext).getParent() == partNext) {
                        setRight.add(s);
                    }
                }
            }

            // Construct setLeft.
            Set<ObsFifoSysState> setLeft;

            if (minLastStitchPartIndex == 0) {
                // Part is the first (initial) partition in path, so we want to
                // isolate the initial observations in this partition from those
                // that generate the counter-example path.
                setLeft = part.getInitialObservations();
            } else {
                // As above for determining setRight, but we head to the left
                // and build a set of observations in part that can be reached
                // from the previous partition along the counter-example path.

                setLeft = new LinkedHashSet<ObsFifoSysState>();

                DistEventType ePrev = cExample.getEvents().get(
                        minLastStitchPartIndex - 1);
                GFSMState partPrev = path.get(minLastStitchPartIndex - 1);
                for (ObsFifoSysState s : partPrev
                        .getObservedStatesWithTransition(ePrev)) {
                    if (s.getNextState(ePrev).getParent() == part) {
                        setLeft.add(s.getNextState(ePrev));
                    }
                }
            }

            // We know that setLeft and setRight have to be isolated, but what
            // about the observations in part that in neither of these two sets?
            // Our strategy is to assign them at random, either to setLeft or
            // setRight (and hope for the best).
            Random rand = new Random();

            for (ObsFifoSysState s : part.getObservedStates()) {
                if (!setLeft.contains(s) && !setRight.contains(s)) {
                    // Assign s to setLeft or setRight at random.
                    if (rand.nextInt(2) == 0) {
                        setLeft.add(s);
                    } else {
                        setRight.add(s);
                    }
                }
            }

            // Perform the refinement.
            pGraph.refine(part, setRight);
            this.isResolved = true;
        }
        return;
    }

    /**
     * Follows the observed state s back along the partition in the
     * counter-example path. Once we find that we cannot follow it back any
     * further, we return the partition index, or the min stitch partition index
     * for s.
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
        // to s, so we explore all of them and return the min index.
        for (ObsFifoSysState p : prevPart.getObservedStatesWithTransition(e)) {
            // The observed fifo sys instances are deterministic, so there is
            // just one transition on e from p.
            if (p.getNextState(e).equals(s)) {
                int newStitchIndex = findMinStitchPartIndex(sPartIndex - 1, p);
                if (newStitchIndex < minIndex) {
                    minIndex = newStitchIndex;
                }
            }
        }
        return minIndex;
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
