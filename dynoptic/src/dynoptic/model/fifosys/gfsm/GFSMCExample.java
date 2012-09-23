package dynoptic.model.fifosys.gfsm;

import java.util.List;
import java.util.Set;

import mcscm.McScMCExample;
import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.invariants.EventuallyHappens;
import dynoptic.invariants.NeverFollowedBy;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;

import synoptic.model.event.DistEventType;

/**
 * Represents a counter-example partitions path in the GFSM, which corresponds
 * to an event-based counter-example that is returned by the McScM model
 * checker.
 */
public class GFSMCExample extends PartialGFSMCExample {
    // Whether or not the path corresponding to cExample is completely
    // initialized (i.e., whether it is of the appropriate length).
    private boolean isInitialized;

    /** Incremental path construction. */
    public GFSMCExample(McScMCExample mcCExample) {
        super(mcCExample);
        this.isInitialized = false;
    }

    /** Adds state to the front of the internal c-example path. */
    public void addToFrontOfPath(GFSMState state) {
        super.addToFrontOfPathNoPostChecks(state);

        if (path.size() == (mcCExample.getEvents().size() + 1)) {
            logger.info("Constructed path = " + path);
            logger.info("Event c-example path = " + mcCExample.toString());
            assert path.get(0).isInitial();
            assert path.get(path.size() - 1).isAccept();
            this.isInitialized = true;
        }
    }

    // /////////////////////////////////////////////////////////////

    /** All-at-once path construction. */
    public GFSMCExample(List<GFSMState> path, McScMCExample cExample) {
        super(path, cExample);
        // A few consistency checks.
        assert path.size() == (cExample.getEvents().size() + 1);
        assert path.get(path.size() - 1).isAccept();
        this.isInitialized = true;
    }

    public List<GFSMState> getPartitionPath() {
        assert isInitialized;
        return super.getPartitionPath();
    }

    public McScMCExample getMcScMCExample() {
        assert isInitialized;

        return super.getMcScMCExample();
    }

    public boolean isResolved() {
        assert isInitialized;

        return super.isResolved();
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
        assert isInitialized;

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

    // /////////////////////////////////////////////////////////////

    /**
     * Resolves an AFby counter-example. For x AFby y, the path includes an x
     * without a following y. We refine partitions that follow the _last_ x
     * along the counter-example path. This is guaranteed to eventually satisfy
     * x AFby y.
     */
    private void resolve(AlwaysFollowedBy inv, GFSM pGraph) {
        DistEventType x = inv.getFirst();

        // Find the last partition along the path that emits the event x.
        int xPartSrcIndex = -1;
        for (int i = 0; i < (path.size() - 1); i++) {
            DistEventType e = mcCExample.getEvents().get(i);
            if (e.equals(x)) {
                xPartSrcIndex = i;
            }
        }
        assert xPartSrcIndex != -1;

        int maxLastStitchPartIndex = findMaxStitchPartIndex(xPartSrcIndex, x);
        assert xPartSrcIndex <= maxLastStitchPartIndex;
        assert maxLastStitchPartIndex < path.size();

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
        DistEventType x = inv.getFirst();
        DistEventType y = inv.getSecond();

        // Find the first partition Px along the path that emits the event x,
        // which is eventually followed by a partition Py that emits y (without
        // another partition that emits x before Py).
        int xPartSrcIndex = -1; // Px
        int yPartSrcIndex = -1; // Py

        for (int i = 0; i < (path.size() - 1); i++) {
            DistEventType e = mcCExample.getEvents().get(i);

            // Match the y first (to handle the x == y case).
            if (e.equals(y) && xPartSrcIndex != -1) {
                yPartSrcIndex = i;
                break;
            }

            if (e.equals(x)) {
                xPartSrcIndex = i;
            }
        }
        assert xPartSrcIndex != -1;
        assert yPartSrcIndex != -1;

        // Make sure that the max stitch part index found is in the range
        // [xPartSrcIndex, yPartSrcIndex] and refine the corresponding
        // partition.

        int maxLastStitchPartIndex = findMaxStitchPartIndex(xPartSrcIndex, x);
        assert xPartSrcIndex <= maxLastStitchPartIndex;
        assert maxLastStitchPartIndex <= yPartSrcIndex;

        // //////////
        // Now refine the partition at maxLastStitchPartIndex.
        refinePartition(pGraph, maxLastStitchPartIndex);
    }

    /**
     * Resolves an "EventuallyHappens y" counter-example path, which does not
     * include a y. Our strategy is to trace back observations from the terminal
     * partition in the counter-example path until we reach the first partition
     * where the observations we've traced were stitched to earlier observations
     * to make a connected counter-example path. We refine this partition to
     * separate the two sets of observations within this partition.
     */
    private void resolve(EventuallyHappens inv, GFSM pGraph) {
        int lastPartIndex = path.size() - 1;
        GFSMState lastPart = path.get(lastPartIndex);

        // The set of observations in yPartSrc that emit y.
        Set<ObsFifoSysState> termObservations = lastPart
                .getTerminalObservations();

        int minLastStitchPartIndex = path.size();
        for (ObsFifoSysState s : termObservations) {
            int newStitchIndex = findMinStitchPartIndex(lastPartIndex, s);
            assert newStitchIndex >= 0;
            assert newStitchIndex <= lastPartIndex;

            if (newStitchIndex < minLastStitchPartIndex) {
                minLastStitchPartIndex = newStitchIndex;
            }
        }

        // //////////
        // Now refine the partition at minLastStitchPartIndex.
        refinePartition(pGraph, minLastStitchPartIndex);
    }

    /**
     * Resolves an AP counter-example. For x AP y, the path includes a y without
     * a preceding x. We refine states that precede the _first_ y along the
     * counter-example path and this is guaranteed to satisfy x AP y.
     */
    private void resolve(AlwaysPrecedes inv, GFSM pGraph) {
        DistEventType y = inv.getSecond();

        // The first partition that emits the event y.
        int yPartSrcIndex = -1;
        for (int i = 0; i < (path.size() - 1); i++) {
            DistEventType e = mcCExample.getEvents().get(i);
            if (e.equals(y)) {
                yPartSrcIndex = i;
                break;
            }
        }
        assert yPartSrcIndex != -1;

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
            int newStitchIndex = findMinStitchPartIndex(yPartSrcIndex, s);
            assert newStitchIndex >= 0;
            assert newStitchIndex <= yPartSrcIndex;

            if (newStitchIndex < minLastStitchPartIndex) {
                minLastStitchPartIndex = newStitchIndex;
            }
        }

        // //////////
        // Now refine the partition at minLastStitchPartIndex.
        refinePartition(pGraph, minLastStitchPartIndex);
    }

    /**
     * Takes the observed state traces that begin in partition at index
     * xPartSrcIndex and emit event x from this partition, and follows these
     * along the counter-example path until we cannot follow it any further. We
     * then return the maximum stitch partition index, which is the max over all
     * these observed state traces we've followed.
     * 
     * @param xPartSrcIndex
     *            The partition we start tracing from
     * @param x
     *            Determines the observed event types in partition at
     *            xPartSrcIndex that we will trace through the c-example path.
     * @return
     */
    private int findMaxStitchPartIndex(int xPartSrcIndex, DistEventType x) {
        GFSMState xPartSrc = path.get(xPartSrcIndex);

        // The set of observations in xPartSrc that emit x.
        Set<ObsFifoSysState> xObsSources = xPartSrc
                .getObservedStatesWithTransition(x);

        // Track forward each observation that emits x from xPartSrcIndex
        // partition along the counter-example path and identify the partition
        // where the observation was 'stitched' onto another observation. The
        // partition farthest along the path is the one we return for
        // refinement.
        int maxLastStitchPartIndex = 0;
        for (ObsFifoSysState xObs : xObsSources) {
            int sPartIndex = xPartSrcIndex;
            ObsFifoSysState s = xObs;
            // Track the observed path containing the observation xObs at the
            // start through the counter-example path.
            while (true) {
                if (sPartIndex == (path.size() - 1)) {
                    // Reached the end of the counter-example path.
                    break;
                }

                GFSMState sPartNext = path.get(sPartIndex + 1);
                DistEventType e = mcCExample.getEvents().get(sPartIndex);
                s = s.getNextState(e);
                if (s == null || s.getParent() != sPartNext) {
                    // If the next observation does not have the right event
                    // (the one required by the counter-example path), or if it
                    // has the right event but the parent of the next
                    // observation is not along the counter-example path, then
                    // stop.
                    break;
                }
                sPartIndex += 1;
            }
            // Update the max index.
            if (sPartIndex > maxLastStitchPartIndex) {
                maxLastStitchPartIndex = sPartIndex;
            }
        }
        return maxLastStitchPartIndex;
    }

}
