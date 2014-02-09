/*
 * This code is in part based on the code from Clemens Hammacher's
 * implementation of a partition refinement algorithm for Bisimulation
 * minimization.
 * 
 * Source: https://ccs.hammacher.name
 * 
 * License: Eclipse Public License v1.0.
 */

package synoptic.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.algorithms.graphops.IOperation;
import synoptic.algorithms.graphops.PartitionMerge;
import synoptic.algorithms.graphops.PartitionMultiSplit;
import synoptic.algorithms.graphops.PartitionSplit;
import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.main.AbstractMain;
import synoptic.main.SynopticMain;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Partition graphs can be transformed using two algorithms -- coarsening and
 * refinement. This class implements refinement using the Bisim algorithm (
 * {@code Bisimulation.splitUntilAllInvsSatisfied}). Coarsening is implemented
 * with a modified version of the kTails algorithm (
 * {@code Bisimulation.mergePartitions}). This algorithm merges partitions in
 * the partition graph without unsatisfying invariants that are satisfied.
 */
public class Bisimulation {
    public static Logger logger = Logger.getLogger("Bisimulation");

    /**
     * Consider incoming transitions for splitting TODO: expose this as a
     * command line option
     */
    private static boolean incomingTransitionSplit = true;

    /** Suppress default constructor for non-instantiability */
    private Bisimulation() {
        throw new AssertionError();
    }

    /**
     * Splits the partitions in {@code pGraph} until ALL synoptic.invariants
     * returned by {@code pGraph.getInvariants()} are satisfied.
     * 
     * @param pGraph
     *            the partition graph to refine\split
     */
    public static void splitUntilAllInvsSatisfied(PartitionGraph pGraph) {
        // TODO: assert that the pGraph represents totally ordered traces.

        TimedTask refinement = PerformanceMetrics.createTask("refinement",
                false);
        SynopticMain syn = SynopticMain.getInstanceWithExistenceCheck();
        if (syn.options.dumpIntermediateStages) {
            syn.exportNonInitialGraph(syn.getIntermediateDumpFilename("r", 0),
                    pGraph);
        }

        int numSplitSteps = 0;
        int prevNumSplitSteps = 0;

        Set<ITemporalInvariant> unsatisfiedInvariants = new LinkedHashSet<ITemporalInvariant>();
        unsatisfiedInvariants.addAll(pGraph.getInvariants().getSet());
        Set<ITemporalInvariant> satisfiedInvariants = new LinkedHashSet<ITemporalInvariant>();

        List<CExamplePath<Partition>> counterExampleTraces = null;

        while (true) {
            // Recompute the counter-examples for the unsatisfied invariants.
            counterExampleTraces = new TemporalInvariantSet(
                    unsatisfiedInvariants).getAllCounterExamples(pGraph);
            logger.fine("Counter-examples: " + counterExampleTraces);

            if (counterExampleTraces == null
                    || counterExampleTraces.size() == 0) {
                logger.fine("Invariants satisfied. Stopping.");
                break;
            }

            // /////////
            // Update the sets with satisfied/unsatisfied invariants.

            // NOTE: By performing a split we might satisfy more than just the
            // invariant the split was intended to satisfy. Therefore, by just
            // considering these invariants we would be under-approximating the
            // invariants we've satisfied, and over-approximating unsatisfied
            // invariants. Instead, we re-compute counter-examples AFTER all the
            // splits and rely on these for determining the set of
            // satisfied/unsatisfied invariants.

            unsatisfiedInvariants.clear();
            for (CExamplePath<Partition> relPath : counterExampleTraces) {
                unsatisfiedInvariants.add(relPath.invariant);
            }
            satisfiedInvariants.clear();
            satisfiedInvariants.addAll(pGraph.getInvariants().getSet());
            satisfiedInvariants.removeAll(unsatisfiedInvariants);
            // /////////
            // logger.fine("New graph size: " + pGraph.getNodes().size()
            // + ", unsat invs remaining: " + unsatisfiedInvariants.size());

            // Perform the splitting.
            prevNumSplitSteps = numSplitSteps;
            numSplitSteps = performSplits(numSplitSteps, pGraph,
                    counterExampleTraces);

            if (numSplitSteps == prevNumSplitSteps) {
                // No splits were performed, which means that we could not
                // eliminate the present counter-examples. Since this function
                // should only be applied to totally ordered traces, this is a
                // bug (this is known to be possible for partially ordered
                // traces).

                throw new InternalSynopticException(
                        "Could not satisfy invariants: "
                                + unsatisfiedInvariants);
            }

        }

        if (syn.options.dumpIntermediateStages) {
            syn.exportNonInitialGraph(
                    syn.getIntermediateDumpFilename("r", numSplitSteps), pGraph);
        }

        PerformanceMetrics.get().record("numOfSplitSteps", numSplitSteps);
        refinement.stop();
    }

    /**
     * Performs a single arbitrary split if we could not find any splits that
     * satisfy a previously unsatisfied invariant. If we did find such splits,
     * then we perform ALL of them. Returns the updated numSplitSteps count,
     * which is incremented by the number of splits applied to the pGraph.
     * 
     * @param numSplitSteps
     *            The number of split steps made so far.
     * @param pGraph
     *            The graph, whose partitions we will split.
     * @param counterExampleTraces
     *            A list of counter-example traces that we attempt to eliminate
     *            by splitting.
     * @return The updated numSplitSteps count.
     */
    public static int performSplits(int numSplitSteps, PartitionGraph pGraph,
            List<CExamplePath<Partition>> counterExampleTraces) {

        // Stores all splits that cause an invariant to be satisfied, indexed by
        // partition to which they are applied.
        LinkedHashMap<Partition, PartitionMultiSplit> splitsToDoByPartition = new LinkedHashMap<Partition, PartitionMultiSplit>();

        // If we have no counterexamples, then we are done.
        if (counterExampleTraces == null || counterExampleTraces.size() == 0) {
            return numSplitSteps;
        }

        // Permute the counter-examples, but do so deterministically for the
        // same random seed argument.
        Collections.shuffle(counterExampleTraces,
                AbstractMain.getInstanceWithExistenceCheck().random);

        // logger.fine("" + counterExampleTraces.size()
        // + " unsatisfied invariants and counter-examples: "
        // + counterExampleTraces);

        // The set of all invariants for which we have a split that makes the
        // graph satisfy the invariant.
        LinkedHashSet<ITemporalInvariant> newlySatisfiedInvariants = new LinkedHashSet<ITemporalInvariant>();

        // Contains the first valid split, which will be performed if no other
        // split (that would resolve an invariant) is available.
        IOperation arbitrarySplit;

        arbitrarySplit = getInvSatisfyingSplits(counterExampleTraces, pGraph,
                splitsToDoByPartition, newlySatisfiedInvariants);

        // String logStr;
        if (splitsToDoByPartition.size() == 0) {
            // We have no splits that resolve invariants. Perform an arbitrary
            // split, if we have one.
            if (arbitrarySplit == null) {
                logger.fine("no valid split available, exiting.");
                return numSplitSteps;
            }
            // logStr = "split[" + numSplitSteps + "] : arbitrary split: "
            // + arbitrarySplit;

            pGraph.apply(arbitrarySplit);

        } else {
            // We have splits that resolve invariants, perform all of them.
            // int i = 0;
            for (PartitionMultiSplit split : splitsToDoByPartition.values()) {
                pGraph.apply(split);
                // logger.fine("split[" + numSplitSteps + "." + i + "] : " +
                // split);
                // i++;
            }

            // logStr = "split[" + numSplitSteps + "] " + "new invs satisfied: "
            // + newlySatisfiedInvariants.size();
        }

        // logger.fine(logStr);

        if (AbstractMain.getInstanceWithExistenceCheck().options.dumpIntermediateStages) {
            AbstractMain.getInstanceWithExistenceCheck()
                    .exportNonInitialGraph(
                            AbstractMain.getInstanceWithExistenceCheck()
                                    .getIntermediateDumpFilename("r",
                                            numSplitSteps + 1), pGraph);
        }

        return numSplitSteps + 1;

    }

    /**
     * Merge partitions in pGraph that are k-equal (kTails equality), with k=0
     * without unsatisfying any of the pGraph invariants..
     * 
     * @param pGraph
     */
    public static void mergePartitions(PartitionGraph pGraph) {
        TemporalInvariantSet invariants = pGraph.getInvariants();
        mergePartitions(pGraph, invariants, 1);
    }

    /**************************************************************************/
    /** Private methods below. */

    /**
     * Compute possible splits to resolve the invariant violation shown by path
     * counterexampleTrace.
     * 
     * @param counterexampleTrace
     *            The path to remove
     * @param pGraph
     *            The graph from which the path shall be removed
     * @return A list of partition splits that resolve the invariant violation
     */
    public static List<PartitionSplit> getSplits(
            CExamplePath<Partition> counterexampleTrace, PartitionGraph pGraph) {

        // Constrained invariant
        if (counterexampleTrace.invariant instanceof TempConstrainedInvariant<?>) {

            // Find splits for the unconstrained version of the invariant first,
            // then find constrained splits if necessary
            List<PartitionSplit> unconstrainedSplits = getSplitsUnconstrained(
                    counterexampleTrace, pGraph);

            if (unconstrainedSplits == null || unconstrainedSplits.isEmpty()) {
                return getSplitsConstrained(counterexampleTrace, pGraph);
            }
            return unconstrainedSplits;
        }

        // Unconstrained invariant
        {
            return getSplitsUnconstrained(counterexampleTrace, pGraph);
        }
    }

    /**
     * Compute possible splits to resolve the constrained invariant violation
     * shown by path counterexampleTrace. This is done by looking at all
     * partitions within the violation subpath and determining if there is a
     * stitch between incoming min/max transitions and outgoing ones.
     */
    private static List<PartitionSplit> getSplitsConstrained(
            CExamplePath<Partition> counterexampleTrace, PartitionGraph pGraph) {

        // Holds the return values.
        List<PartitionSplit> candidateSplits = new ArrayList<PartitionSplit>();

        // This method must only be passed counter-example paths for
        // constrained invariants
        assert counterexampleTrace.invariant instanceof TempConstrainedInvariant<?>;

        // Traverse the violation subpath from its second last partition to its
        // second partition. First and last are not considered for splitting
        // because they cannot possibly contain a stitch: transitions into the
        // start partition have no bearing on the violation, and neither do
        // transitions out of the end partition.
        for (int i = counterexampleTrace.violationEnd - 1; i > counterexampleTrace.violationStart; --i) {

            // Check if partition at i is null
            if (counterexampleTrace.path.get(i) == null) {
                throw new InternalSynopticException(
                        "Counter-example path with a null Partition");
            }

            // Create a split on the partition at i if there is a stitch
            PartitionSplit split = makeConstrainedSplitIfStitch(
                    counterexampleTrace, i);

            // If there was a stitch, and we have a split, store it
            if (split != null) {
                candidateSplits.add(split);
            }
        }

        return candidateSplits;
    }

    /**
     * During constrained refinement, check if the partition at index in the
     * counter-example trace contains a stitch, which means that the targets of
     * all min/max transitions into this partition and the sources of all
     * min/max transitions out of this partition are not equal sets.
     * 
     * @param counterexampleTrace
     *            The trace in which the partition to check exists
     * @param i
     *            The index of the Partition in counterexampleTrace to check
     * @return True if there is a stitch, false otherwise
     */
    public static PartitionSplit makeConstrainedSplitIfStitch(
            CExamplePath<Partition> counterexampleTrace, int i) {

        // Target events of min/max transitions into the partition at i
        HashSet<EventNode> incomingMinMaxEvents = new HashSet<EventNode>();
        // Source events of min/max transitions out of the partition at i
        HashSet<EventNode> outgoingMinMaxEvents = new HashSet<EventNode>();

        // Populate events at which we can arrive from the previous partition in
        // the path
        for (ITransition<EventNode> arrivingTrans : counterexampleTrace.transitionsList
                .get(i)) {
            incomingMinMaxEvents.add(arrivingTrans.getTarget());
        }

        // Populate events from which we can depart to reach the next partition
        // in the path
        for (ITransition<EventNode> departingTrans : counterexampleTrace.transitionsList
                .get(i + 1)) {
            outgoingMinMaxEvents.add(departingTrans.getSource());
        }

        // Equal sets means there is no stitch
        if (incomingMinMaxEvents.equals(outgoingMinMaxEvents)) {
            return null;
        }

        // Non-equal sets means there is a stitch, so make and return a split
        return makeConstrainedSplit(counterexampleTrace.path.get(i),
                incomingMinMaxEvents, outgoingMinMaxEvents);
    }

    /**
     * Creates a partition split during constrained refinement. Keeps in part
     * all events that are targets of min/max transitions into part. Splits off
     * all events that are sources of min/max transitions out of part. Remaining
     * events in part are randomly either kept in part or split off.
     * 
     * @param part
     *            The partition to split
     * @param incomingMinMaxEvents
     *            Events that are targets of min/max transitions into part
     * @param outgoingMinMaxEvents
     *            Events that are sources of min/max transitions out of part
     * @return Split on part
     */
    public static PartitionSplit makeConstrainedSplit(Partition part,
            Set<EventNode> incomingMinMaxEvents,
            Set<EventNode> outgoingMinMaxEvents) {

        PartitionSplit split = new PartitionSplit(part);

        // Get the intersect of incoming and outgoing min/max events
        Set<EventNode> incomingAndOutgoing = new HashSet<EventNode>(
                incomingMinMaxEvents);
        incomingAndOutgoing.retainAll(outgoingMinMaxEvents);

        // Create new incoming/outgoing min/max event sets without the
        // intersected elements
        Set<EventNode> incoming = new HashSet<EventNode>(incomingMinMaxEvents);
        incoming.removeAll(incomingAndOutgoing);
        Set<EventNode> outgoing = new HashSet<EventNode>(outgoingMinMaxEvents);
        outgoing.removeAll(incomingAndOutgoing);

        // Leave incoming min/max events in the original partition, and split
        // away outgoing min/max events
        for (EventNode outEv : outgoing) {
            split.addEventToSplit(outEv);
        }

        // If incoming min/max events (excluding intersect events) is not empty,
        // also split away the events in the intersect of the two sets. If
        // incoming is empty, incoming/outgoing intersect events stay in the
        // original partition to prevent a possible invalid split.
        if (!incoming.isEmpty()) {
            for (EventNode intersectEv : incomingAndOutgoing) {
                split.addEventToSplit(intersectEv);
            }
        }

        Random rand = AbstractMain.getInstanceWithExistenceCheck().random;

        // Get all other events that are neither incoming nor outgoing min/max
        // events
        Set<EventNode> allOtherEvents = new HashSet<EventNode>(
                part.getEventNodes());
        allOtherEvents.removeAll(incomingMinMaxEvents);
        allOtherEvents.removeAll(outgoingMinMaxEvents);

        // Randomly assign other events to one side of the split or
        // the other
        for (EventNode otherEvent : allOtherEvents) {
            if (rand.nextBoolean()) {
                split.addEventToSplit(otherEvent);
            }
        }

        return split;
    }

    /**
     * Compute possible splits to resolve the unconstrained invariant violation
     * shown by path counterexampleTrace. This is done by following the path in
     * the original (event) graph and determining the point where the partition
     * graph allows a transition it should not allow. The event graph is
     * accessed via the events stored by the partitions.
     */
    private static List<PartitionSplit> getSplitsUnconstrained(
            CExamplePath<Partition> counterexampleTrace, PartitionGraph pGraph) {
        /**
         * Holds the return values.
         */
        List<PartitionSplit> candidateSplits = new ArrayList<PartitionSplit>();
        /**
         * The messages (i.e. nodes in the original graph) that are on the
         * counterexampleTrace.
         */
        LinkedHashSet<EventNode> hot = new LinkedHashSet<EventNode>();
        hot.addAll(counterexampleTrace.path.get(0).getEventNodes());
        Partition prevPartition = null;
        Partition nextPartition = null;
        Partition curPartition = null;
        // logger.fine("" + counterexampleTrace.path);

        // TODO: retrieve an interned copy of this set
        String relation = counterexampleTrace.invariant.getRelation();
        Set<String> relationSet = new LinkedHashSet<String>();
        relationSet.add(relation);

        // Walk along the path
        for (Partition part : counterexampleTrace.path) {
            if (part == null) {
                throw new InternalSynopticException(
                        "Relation path with a null Partition");
            }
            prevPartition = curPartition;
            curPartition = nextPartition;
            nextPartition = part;
            hot.retainAll(part.getEventNodes());
            // If we cannot follow further, then we found the partition we need
            // to split.
            if (hot.size() == 0) {
                break;
            }
            // Compute the valid successor messages in the original trace.
            LinkedHashSet<EventNode> successorEvents = new LinkedHashSet<EventNode>();

            for (EventNode m : hot) {
                for (ITransition<EventNode> t : m
                        .getTransitionsWithIntersectingRelations(relationSet)) {
                    // successorEvents.addAll(m.getSuccessors(relations));
                    successorEvents.add(t.getTarget());
                }
            }
            hot = successorEvents;
        }

        ITransition<Partition> outgoingTransition = curPartition
                .getTransitionWithExactRelation(nextPartition, relationSet);
        ITransition<Partition> incomingTransition = null;
        if (prevPartition != null) {
            incomingTransition = prevPartition.getTransitionWithExactRelation(
                    curPartition, relationSet);
        }
        if (outgoingTransition != null) {
            // logger.fine("outgoingTrans:" + outgoingTransition);
            PartitionSplit newSplit = curPartition
                    .getCandidateSplitBasedOnOutgoing(outgoingTransition);
            // logger.fine("outgoingSplit:" + newSplit);
            if (newSplit != null) {
                candidateSplits.add(newSplit);
            }

        }
        if (incomingTransition != null && incomingTransitionSplit) {
            // logger.fine("incomingTrans:" + incomingTransition);

            Set<String> relations = incomingTransition.getRelation();
            PartitionSplit newSplit;
            if (relations.size() == 1) {
                // Single relation case.
                newSplit = curPartition.getCandidateSplitBasedOnIncoming(
                        prevPartition, relations);
            } else {
                // Multi-relational case.
                newSplit = curPartition.getCandidateSplitBasedOnIncoming(
                        prevPartition, relations);
            }

            // logger.fine("incomingSplit:" + newSplit);
            if (newSplit != null) {
                candidateSplits.add(newSplit);
            }
        }
        return candidateSplits;
    }

    /**
     * Performs the splitOp on the pGraph to see whether or not the resulting
     * graph has no other counter-examples for the invariant inv (i.e. whether
     * or not the graph after the split satisfies inv).
     * 
     * @param inv
     *            The invariant to check for satisfiability after the splitOp.
     * @param pGraph
     *            The partition graph to apply to the splitOp to.
     * @param splitOp
     *            The split operation to apply to pGraph
     * @return true if the split makes the graph satisfy the invariant, and
     *         false otherwise.
     */
    private static boolean splitSatisfiesInvariantGlobally(
            ITemporalInvariant inv, PartitionGraph pGraph,
            PartitionMultiSplit splitOp) {

        // Perform the split.
        IOperation rewindOperation = pGraph.apply(splitOp);

        // See if splitting resolved the violation.
        CExamplePath<Partition> violation = TemporalInvariantSet
                .getCounterExample(inv, pGraph);

        // Undo the split (rewind) to get back the input graph.
        pGraph.apply(rewindOperation);

        // The invariant has more violations after the split.
        if (violation != null) {
            return false;
        }
        // The split has no other violations once the split is
        // performed.
        return true;
    }

    /**
     * Performs the splitOp on the pGraph and then checks if there is a
     * violation of invariant inv in a specific subgraph of pGraph starting at
     * startPart and ending at endPart. This is accomplished by localized model
     * checking which starts at startPart (rather than the initial node) and
     * stops at endPart (rather than the terminal node).
     * 
     * @param inv
     *            The invariant to check for satisfiability after the splitOp.
     * @param pGraph
     *            The partition graph to apply to the splitOp to.
     * @param startPart
     *            The partition on which to start model checking
     * @param endPart
     *            The partition on which to end model checking
     * @param splitOp
     *            The split operation to apply to pGraph
     * @return true if the split makes the subgraph between startPart and
     *         endPart satisfy the invariant, and false otherwise.
     */
    private static boolean splitSatisfiesInvariantLocally(
            ITemporalInvariant inv, PartitionGraph pGraph, Partition startPart,
            Partition endPart, PartitionMultiSplit splitOp) {

        // TODO: Implement this.
        return false;
    }

    /**
     * Returns an arbitrary split that resolves an arbitrary counter-example
     * trace in counterexampleTraces. Populates the splitsToDoByPartition map
     * with those splits that make a previously unsatisfied invariant true in
     * the new (refined) graph.
     * 
     * @param counterexampleTraces
     * @param pGraph
     * @param splitsToDoByPartition
     *            The HashMap recording splits by partition -- updated to
     *            include all splits that make the graph satisfy previously
     *            unsatisfied invariants.
     * @param newlySatisfiedInvariants
     * @return an arbitrary split that may be useful in the case that
     *         splitsToDoByPartition is empty and there are no splits that lead
     *         to new invariant satisfaction.
     */
    private static IOperation getInvSatisfyingSplits(
            List<CExamplePath<Partition>> counterexampleTraces,
            PartitionGraph pGraph,
            HashMap<Partition, PartitionMultiSplit> splitsToDoByPartition,
            Set<ITemporalInvariant> newlySatisfiedInvariants) {

        IOperation arbitrarySplit = null;
        AbstractMain main = AbstractMain.getInstanceWithExistenceCheck();

        // TODO: we are considering counter-example traces in an arbitrary
        // order. This heuristic should be turned into a customizable strategy.
        for (CExamplePath<Partition> counterexampleTrace : counterexampleTraces) {
            // logger.fine("Considering counterexample: "
            // + counterexampleTrace.toString());

            // The invariant that we will attempt to satisfy globally with a
            // single split.
            ITemporalInvariant inv = counterexampleTrace.invariant;

            // Skip to next counter-example if we have previously recorded a
            // split that satisfies the invariant corresponding to this
            // counter-example (and which therefore satisfies this
            // counter-example, too).
            if (newlySatisfiedInvariants.contains(inv)) {
                continue;
            }

            // Get the possible splits that might resolve this counter-example.
            List<PartitionSplit> candidateSplits = getSplits(
                    counterexampleTrace, pGraph);

            // Permute the list of candidates.
            Collections.shuffle(candidateSplits, main.random);

            // Save an arbitrary split to return to caller, if we haven't saved
            // one already.
            if (arbitrarySplit == null && !candidateSplits.isEmpty()) {
                arbitrarySplit = candidateSplits.get(0);
            }

            // A split that satisfies the invariant locally (within the subgraph
            // where the violation was found) but not globally (in the entire
            // partition graph). Only applied if a globally-satisfying split is
            // not found
            PartitionMultiSplit locallySatisfyingSplit = null;

            // logger.fine("candidateSplits are: " +
            // candidateSplits.toString());

            // Find a single split in candidateSplits that makes the
            // invariant corresponding to the counter-example true in the
            // entire graph.
            //
            // a. If no such split exists, then continue to the next
            // counter-example.
            //
            // b. If such a split exists, integrate it into whatever splits we
            // might have found earlier (for previous counter-examples).
            //
            for (PartitionSplit candidateSplit : candidateSplits) {
                if (main.options.performExtraChecks) {
                    // getSplits() should never generate invalid splits.
                    if (!candidateSplit.isValid()) {
                        throw new InternalSynopticException(
                                "getSplits() generated an invalid split.");
                    }
                }

                PartitionMultiSplit splitOp = new PartitionMultiSplit(
                        candidateSplit);
                Partition partitionBeingSplit = candidateSplit.getPartition();

                // TODO: we check satisfiability of each split _independently_.
                // This means that we are looking for very rare splits that
                // satisfy _different_ invariants individually. A more realistic
                // search would (1) apply each split that satisfies an
                // invariant, and (2) continue searching for more such splits on
                // the _mutated_ pGraph.

                if (splitSatisfiesInvariantGlobally(inv, pGraph, splitOp)) {
                    // If we already have a split for that partition,
                    // incorporate the new split into it.
                    if (splitsToDoByPartition.containsKey(partitionBeingSplit)) {
                        splitsToDoByPartition.get(partitionBeingSplit)
                                .incorporate(splitOp);
                        logger.fine("Incorporating new split by partition: "
                                + splitOp.toString());
                    } else {
                        // Otherwise, record this split as the only one for this
                        // partition
                        splitsToDoByPartition.put(partitionBeingSplit, splitOp);
                        logger.fine("New split by partition: "
                                + splitOp.toString());
                    }

                    // Remember that we can resolve this invariant
                    // violation.
                    newlySatisfiedInvariants.add(inv);
                    // Found the split that completely satisfies the
                    // invariant, no need to consider other splits.
                    break;

                }

                // Check if split satisfies the invariant locally (specifically
                // in the subgraph where the violation was found)
                else if (locallySatisfyingSplit == null
                        && inv instanceof TempConstrainedInvariant<?>) {

                    // Get start and end of violation subgraph
                    Partition startPart = counterexampleTrace.path
                            .get(counterexampleTrace.violationStart);
                    Partition endPart = counterexampleTrace.path
                            .get(counterexampleTrace.violationEnd);

                    // Store the split if the invariant is locally satisfied
                    if (splitSatisfiesInvariantLocally(inv, pGraph, startPart,
                            endPart, splitOp)) {
                        locallySatisfyingSplit = splitOp;
                    }
                }
            }

            // If we didn't find a globally-satisfying split but did find a
            // locally-satisfying one, record it
            if (locallySatisfyingSplit != null) {

                // Get partition to be split
                Partition partitionBeingSplit = locallySatisfyingSplit
                        .getPartition();

                // If we already have a split for that partition, incorporate
                // the new split into it.
                if (splitsToDoByPartition.containsKey(partitionBeingSplit)) {
                    splitsToDoByPartition.get(partitionBeingSplit).incorporate(
                            locallySatisfyingSplit);
                    logger.fine("Incorporating new locally-satisfying split by partition: "
                            + locallySatisfyingSplit.toString());
                } else {
                    // Otherwise, record this split as the only one for this
                    // partition
                    splitsToDoByPartition.put(partitionBeingSplit,
                            locallySatisfyingSplit);
                    logger.fine("New split by partition: "
                            + locallySatisfyingSplit.toString());
                }
            }
        }
        return arbitrarySplit;
    }

    /**
     * This is basically the k-Tails algorithm except that it respects
     * invariants -- if any are violated during a merge, the particular merge is
     * aborted.
     * 
     * @param pGraph
     *            the graph to coarsen
     * @param invariants
     *            the invariants to maintain during merge, can be null
     * @param k
     *            the k parameter for k-equality
     */
    private static void mergePartitions(PartitionGraph pGraph,
            TemporalInvariantSet invariants, int k) {
        int outerItters = 0;

        // The blacklist keeps a history of partitions we've attempted to merge
        // and which did not work out because they resulted in invariant
        // violations.
        Map<Partition, Set<Partition>> mergeBlacklist = new LinkedHashMap<Partition, Set<Partition>>();

        AbstractMain syn = AbstractMain.getInstanceWithExistenceCheck();
        while (true) {
            if (syn.options.dumpIntermediateStages) {
                syn.exportNonInitialGraph(
                        syn.getIntermediateDumpFilename("c", outerItters),
                        pGraph);
            }
            outerItters++;

            logger.fine("--------------------------------");
            if (!mergePartitions(pGraph, mergeBlacklist, invariants, k)) {
                break;
            }
        }

        if (syn.options.dumpIntermediateStages) {
            syn.exportNonInitialGraph(
                    syn.getIntermediateDumpFilename("c", outerItters), pGraph);
        }
    }

    /**
     * Attempts to merge partitions that are k-equivalent, while respecting
     * invariants. Tries all pairs of partitions from pGraph, except for those
     * that are in the mergeBlacklist (these have been attempted previously and
     * are known to violate invariants). Returns true if at least one merge was
     * performed, otherwise returns false.
     * 
     * @param pGraph
     * @param mergeBlacklist
     * @param invariants
     * @param k
     * @return
     */
    private static boolean mergePartitions(PartitionGraph pGraph,
            Map<Partition, Set<Partition>> mergeBlacklist,
            TemporalInvariantSet invariants, int k) {
        ArrayList<Partition> partitions = new ArrayList<Partition>();
        partitions.addAll(pGraph.getNodes());

        // Attempt to merge all pairs of partitions in the current graph.
        for (Partition p : partitions) {
            for (Partition q : partitions) {
                // 1. Can't merge a partition with itself
                if (p == q) {
                    continue;
                }

                logger.fine("Attempting to merge: " + p + "(hash: "
                        + p.hashCode() + ") + " + q + "(hash: " + q.hashCode()
                        + ")");

                // 2. Only merge partitions that are k-equivalent
                if (!KTails.kEquals(p, q, k)) {
                    logger.fine("Partitions are not k-equivalent(k=" + k + ")");
                    continue;
                }

                // 3. Ignore partition pairs that were previously tried (are
                // in blacklist)
                if ((mergeBlacklist.containsKey(p) && mergeBlacklist.get(p)
                        .contains(q))
                        || (mergeBlacklist.containsKey(q) && mergeBlacklist
                                .get(q).contains(p))) {
                    logger.fine("Partitions are in the merge blacklist.");
                    continue;
                }

                Set<Partition> parts = new LinkedHashSet<Partition>();
                parts.addAll(pGraph.getNodes());
                IOperation rewindOperation = pGraph.apply(new PartitionMerge(p,
                        q));

                CExamplePath<Partition> cExample = null;

                if (invariants != null) {
                    cExample = invariants.getFirstCounterExample(pGraph);
                }

                if (cExample != null) {
                    // The merge created a violation. Remember this pair of
                    // partitions so that we don't try it again.
                    logger.fine("Merge violates invariant: "
                            + cExample.toString());

                    if (!mergeBlacklist.containsKey(p)) {
                        mergeBlacklist.put(p, new LinkedHashSet<Partition>());
                    }
                    mergeBlacklist.get(p).add(q);

                    // Undo the merge.
                    pGraph.apply(rewindOperation);

                    if (AbstractMain.getInstanceWithExistenceCheck().options.performExtraChecks) {
                        pGraph.checkSanity();
                    }

                    // We cannot change the partition sets because we are
                    // iterating over the partitions. Therefore, check that
                    // the resulting partition set is the same as the
                    // original partition set.
                    if (!(parts.containsAll(pGraph.getNodes()) && pGraph
                            .getNodes().containsAll(parts))) {
                        throw new InternalSynopticException(
                                "partition set changed due to rewind: "
                                        + rewindOperation);
                    }

                } else {
                    logger.fine("Merge of partitions " + p.getEType()
                            + " nodes maintains invs, accepted.");
                    return true;
                }
            }
        }

        // Unable to find any k-equivalent partitions; we're done.
        return false;
    }
}
