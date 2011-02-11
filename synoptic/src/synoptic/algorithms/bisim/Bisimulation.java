/** Implementation of the synoptic.main algorithms */

package synoptic.algorithms.bisim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.algorithms.graph.IOperation;
import synoptic.algorithms.graph.PartitionMerge;
import synoptic.algorithms.graph.PartitionMultiSplit;
import synoptic.algorithms.graph.PartitionSplit;
import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.RelationPath;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.model.LogEvent;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * This class implements the algorithm BisimH (
 * {@code Bisimulation.splitPartitions}), and a modified version of the
 * algorithm kTail ({@code Bisimulation.mergePartitions}) that considers state
 * labels instead of state transitions. It is based on the code from Clemens
 * Hammacher's implementation of a partition refinement algorithm for
 * bisimulation minimization. Source: https://ccs.hammacher.name License:
 * Eclipse Public License v1.0.
 */
public abstract class Bisimulation {
    public static Logger logger = Logger.getLogger("Bisimulation");
    /**
     * If true, a partition may be split on incoming/outgoing edges at once, if
     * both splits resolve the invariant (when considered separately). TODO:
     * implement this
     */
    private static final boolean fourWaySplit = true;
    /**
     * Set to combine all candidate splits for each partition into a multi
     * split.
     */
    private static final boolean combineCandidates = false;
    /**
     * Perform extra correctness checks. TODO: expose this via command line
     * options, and make it false by default (?)
     */
    private static boolean EXTRA_CHECKS = true;
    /**
     * Consider incoming transitions for splitting TODO: expose this as a
     * command line option
     */
    private static boolean incomingTransitionSplit = true;
    /**
     * Coarsen the representation after each successful split (i.e. each split
     * that caused a previously unsatisfied invariant to become satisfied. This
     * may or may not speed up operation.
     */
    private static boolean interleavedMerging = false;

    private Bisimulation() throws InstantiationException {
        throw new InstantiationException(this.getClass().getCanonicalName()
                + " is supposed to be used as static class only.");
    }

    public static boolean tryAndRecordSplitOp(
            RelationPath<Partition> counterexampleTrace,
            PartitionGraph partitionGraph,
            LinkedHashMap<Partition, PartitionMultiSplit> splitsToDoByPartition,
            Partition partitionBeingSplit, PartitionMultiSplit splitOp) {

        // Perform the split
        IOperation rewindOperation = partitionGraph.apply(splitOp);

        // See if splitting resolved the violation.
        RelationPath<Partition> violation = TemporalInvariantSet
                .getCounterExample(counterexampleTrace.invariant,
                        partitionGraph);

        // Rewind for now
        partitionGraph.apply(rewindOperation);

        // If the invariant has more violations, we did not make progress
        if (violation != null) {
            return false;
        }

        // Otherwise, there are no more violations -- we made progress
        if (splitsToDoByPartition.containsKey(partitionBeingSplit)) {
            // If we already have a split for that partition,
            // incorporate the new split into it.
            splitsToDoByPartition.get(partitionBeingSplit).incorporate(splitOp);
        } else {
            // Otherwise, record this split as the only one for this partition
            splitsToDoByPartition.put(partitionBeingSplit, splitOp);
        }
        return true;
    }

    public static IOperation tryAndRecordCandidateSplits(
            List<RelationPath<Partition>> counterexampleTraces,
            PartitionGraph partitionGraph,
            LinkedHashMap<Partition, PartitionMultiSplit> splitsToDoByPartition,
            ArrayList<ITemporalInvariant> newlySatisfiedInvariants) {

        IOperation arbitrarySplit = null;

        // TODO: we are considering counter-example traces in an arbitrary order
        // there's probably room for a heuristic here.
        for (RelationPath<Partition> counterexampleTrace : counterexampleTraces) {

            // Get the possible splits that might resolve this counter-example.
            List<PartitionSplit> candidateSplits = getSplits(
                    counterexampleTrace, partitionGraph);

            // Permute the list of candidates
            Collections.shuffle(candidateSplits, Main.random);

            if (combineCandidates) {
                PartitionMultiSplit combinedSplit = null;

                for (PartitionSplit candidateSplit : candidateSplits) {
                    if (candidateSplit == null || !candidateSplit.isValid()) {
                        logger.fine("Skipping invalid source: "
                                + candidateSplit);
                        continue;
                    }

                    if (combinedSplit == null) {
                        combinedSplit = new PartitionMultiSplit(candidateSplit);
                    } else {
                        combinedSplit.incorporate(candidateSplit);
                    }
                }

                if (combinedSplit == null) {
                    logger.fine("No valid sources available.");
                    return null;
                }
                if (!combinedSplit.isValid()) {
                    logger.fine("Combined split is invalid.");
                    return null;
                }

                if (arbitrarySplit == null) {
                    arbitrarySplit = combinedSplit;
                }

                if (tryAndRecordSplitOp(counterexampleTrace, partitionGraph,
                        splitsToDoByPartition, combinedSplit.getPartition(),
                        combinedSplit)) {
                    // Remember that we resolved this invariant violation
                    newlySatisfiedInvariants.add(counterexampleTrace.invariant);
                }

            } else {
                for (PartitionSplit candidateSplit : candidateSplits) {
                    // Only consider valid splits
                    if (candidateSplit == null || !candidateSplit.isValid()) {
                        continue;
                    }

                    if (arbitrarySplit == null) {
                        arbitrarySplit = candidateSplit;
                    }

                    if (tryAndRecordSplitOp(counterexampleTrace,
                            partitionGraph, splitsToDoByPartition,
                            candidateSplit.getPartition(),
                            new PartitionMultiSplit(candidateSplit))) {
                        // Remember that we resolved this invariant violation
                        newlySatisfiedInvariants
                                .add(counterexampleTrace.invariant);
                    }
                }
            }
        }
        return arbitrarySplit;
    }

    public static int performOneSplitPartitionsStep(int numSplitSteps,
            PartitionGraph partitionGraph,
            Set<ITemporalInvariant> unsatisfiedInvariants,
            Set<ITemporalInvariant> satisfiedInvariants) {

        List<RelationPath<Partition>> counterexampleTraces = null;

        /**
         * Stores all splits that cause an invariant to be satisfied, indexed by
         * partition to which they are applied.
         */
        LinkedHashMap<Partition, PartitionMultiSplit> splitsToDoByPartition = new LinkedHashMap<Partition, PartitionMultiSplit>();

        // Retrieve the counterexamples for the unsatisfied invariants
        counterexampleTraces = new TemporalInvariantSet(unsatisfiedInvariants)
                .getAllCounterExamples(partitionGraph);

        // TODO: add an EXTRA_CHECKS if clause that will make sure that
        // there are
        // no counter-examples for the satisfiedInvariants list.

        // TODO: check that getViolations returns the list of c-examples
        // deterministically.
        // This is necessary for random seed control to work properly.

        // If we have no counterexamples, then we are done.
        if (counterexampleTraces == null || counterexampleTraces.size() == 0) {
            logger.fine("Invariants statisfied. Stopping.");
            // TODO: is this right?
            return numSplitSteps + 1;
        }
        // Permute the counter-examples so that we process them in a
        // deterministic order for the same random seed argument.
        Collections.shuffle(counterexampleTraces, Main.random);

        logger.fine("" + counterexampleTraces.size()
                + " unsatisfied invariants and counter-examples: "
                + counterexampleTraces);

        // Update the sets with satisfied and unsatisfied
        // synoptic.invariants.
        unsatisfiedInvariants.clear();
        for (RelationPath<Partition> relPath : counterexampleTraces) {
            unsatisfiedInvariants.add(relPath.invariant);
        }
        satisfiedInvariants.clear();
        satisfiedInvariants.addAll(partitionGraph.getInvariants().getSet());
        satisfiedInvariants.removeAll(unsatisfiedInvariants);

        /**
         * Stores all synoptic.invariants for which we have a split that
         * satisfies it.
         */
        ArrayList<ITemporalInvariant> newlySatisfiedInvariants = new ArrayList<ITemporalInvariant>();
        /**
         * Stores the first valid split. This split will be performed if no
         * other split (that would resolve an invariant) is available.
         */
        IOperation arbitrarySplit = tryAndRecordCandidateSplits(
                counterexampleTraces, partitionGraph, splitsToDoByPartition,
                newlySatisfiedInvariants);

        String logStr = "";
        if (splitsToDoByPartition.size() == 0) {
            // we have no splits that make progress. See if we have an
            // arbitrary split.
            if (arbitrarySplit == null) {
                logger.fine("no valid split available, exiting.");
                // TOOD: this isn't right?
                return numSplitSteps + 1;
            }
            logStr += "split[" + numSplitSteps + "] : arbitrary split: "
                    + arbitrarySplit;

            partitionGraph.apply(arbitrarySplit);

        } else {
            // we have splits to do, perform them
            int i = 0;
            for (PartitionMultiSplit currentSplit : splitsToDoByPartition
                    .values()) {
                partitionGraph.apply(currentSplit);
                logger.fine("split[" + numSplitSteps + "." + i + "] : "
                        + currentSplit);
                i++;
            }
            // update synoptic.invariants
            satisfiedInvariants.addAll(newlySatisfiedInvariants);
            unsatisfiedInvariants.removeAll(newlySatisfiedInvariants);

            // handle interleaved merging
            if (interleavedMerging) {
                logger.fine("interleavedMerging: recompressing...");
                Bisimulation.mergePartitions(partitionGraph,
                        new TemporalInvariantSet(satisfiedInvariants));
            }

            logStr += "split[" + numSplitSteps + "] " + "new invs satisfied: "
                    + newlySatisfiedInvariants.size();
        }
        logger.fine(logStr + ", new graph size: "
                + partitionGraph.getNodes().size() + ", unsat invs remaining: "
                + unsatisfiedInvariants.size());

        if (Main.dumpIntermediateStages) {
            GraphVizExporter.quickExport(
                    Main.getIntermediateDumpFilename("r", numSplitSteps + 1),
                    partitionGraph);
        }

        return numSplitSteps + 1;

    }

    /**
     * Splits the partitions in {@code partitionGraph} until all
     * synoptic.invariants returned by {@code partitionGraph.getInvariants()}
     * are satisfied.
     * 
     * @param partitionGraph
     *            the partition graph to refine\split
     */
    public static void splitPartitions(PartitionGraph partitionGraph) {
        TimedTask refinement = PerformanceMetrics.createTask("refinement",
                false);

        if (Main.dumpIntermediateStages) {
            GraphVizExporter.quickExport(
                    Main.getIntermediateDumpFilename("r", 0), partitionGraph);
        }

        int numSplitSteps = 0;

        Set<ITemporalInvariant> unsatisfiedInvariants = new LinkedHashSet<ITemporalInvariant>();
        unsatisfiedInvariants.addAll(partitionGraph.getInvariants().getSet());
        Set<ITemporalInvariant> satisfiedInvariants = new LinkedHashSet<ITemporalInvariant>();

        while (unsatisfiedInvariants.size() > 0) {
            numSplitSteps = performOneSplitPartitionsStep(numSplitSteps,
                    partitionGraph, unsatisfiedInvariants, satisfiedInvariants);
        }

        if (Main.dumpIntermediateStages) {
            GraphVizExporter.quickExport(
                    Main.getIntermediateDumpFilename("r", numSplitSteps),
                    partitionGraph);
        }

        PerformanceMetrics.get().record("numOfSplitSteps", numSplitSteps);
        refinement.stop();
    }

    /**
     * Compute possible splits to remove the path relPath from partitionGraph.
     * This is done by following relPath in the original graph and determining
     * the point where partitionGraph allows a transition it should not allow.
     * The original graph is accessed via the Messages (which are nodes in the
     * original graph) in the partitions.
     * 
     * @param counterexampleTrace
     *            - the path to remove
     * @param partitionGraph
     *            - the graph from which the path shall be removed
     * @return a list of partition splits that remove relPath
     */
    private static List<PartitionSplit> getSplits(
            RelationPath<Partition> counterexampleTrace,
            PartitionGraph partitionGraph) {
        /**
         * Holds the return values.
         */
        List<PartitionSplit> candidateSplits = new ArrayList<PartitionSplit>();
        /**
         * The messages (i.e. nodes in the original graph) that are on the
         * counterexampleTrace
         */
        Set<LogEvent> hot = new HashSet<LogEvent>();
        hot.addAll(counterexampleTrace.path.get(0).getMessages());
        Partition prevPartition = null;
        Partition nextPartition = null;
        Partition curPartition = null;
        // logger.fine("" + counterexampleTrace.path);
        // Walk along the path
        for (Partition part : counterexampleTrace.path) {
            if (part == null) {
                throw new InternalSynopticException(
                        "relation path contained null");
            }
            prevPartition = curPartition;
            curPartition = nextPartition;
            nextPartition = part;
            hot.retainAll(part.getMessages());
            // if we cannot follow, we found the partition we have to split.
            if (hot.size() == 0) {
                break;
            }
            // Compute the valid successor messages in the original trace
            Set<LogEvent> successorMessages = new HashSet<LogEvent>();
            for (LogEvent m : hot) {
                successorMessages.addAll(m
                        .getSuccessors(counterexampleTrace.invariant
                                .getRelation()));
            }
            hot = successorMessages;
        }
        ITransition<Partition> outgoingTransition = curPartition.getTransition(
                nextPartition, counterexampleTrace.invariant.getRelation());
        ITransition<Partition> incomingTransition = null;
        if (prevPartition != null) {
            incomingTransition = prevPartition.getTransition(curPartition,
                    counterexampleTrace.invariant.getRelation());
        }
        if (outgoingTransition != null) {
            // logger.fine("outgoingTrans:" + outgoingTransition);
            PartitionSplit newSplit = curPartition
                    .getCandidateDivisionBasedOnOutgoing(outgoingTransition);
            // logger.fine("outgoingSplit:" + newSplit);
            candidateSplits.add(newSplit);

        }
        if (incomingTransition != null && incomingTransitionSplit) {
            // logger.fine("incomingTrans:" + incomingTransition);
            PartitionSplit newSplit = curPartition
                    .getCandidateDivisionBasedOnIncoming(prevPartition,
                            incomingTransition.getRelation());
            // logger.fine("incomingSplit:" + newSplit);
            candidateSplits.add(newSplit);
        }
        return candidateSplits;
    }

    /**
     * Construct a partition graph from {@code graph} (by partitioning by
     * label), call {@code splitPartitions} on it, and return the refined graph.
     * 
     * @param graph
     *            the graph from which should be used as initial graph
     * @return the refined graph
     * @throws InterruptedException
     */
    public static PartitionGraph getSplitGraph(IGraph<LogEvent> graph)
            throws InterruptedException {
        PartitionGraph g = new PartitionGraph(graph, true);
        splitPartitions(g);
        return g;
    }

    /**
     * Construct a partition graph from {@code graph} (by partitioning by
     * label), call {@code mergePartitions} on it, and return the merged graph.
     * 
     * @param graph
     *            the graph from which should be used as initial graph
     * @return the merged graph
     * @throws InterruptedException
     */
    public static PartitionGraph getMergedGraph(IGraph<LogEvent> graph) {
        PartitionGraph g = new PartitionGraph(graph);
        mergePartitions(g);
        return null;
    }

    /**
     * Works as {@code mergePartitions} but fixes synoptic.invariants to
     * {@code partitionGraph.getInvariants()}
     * 
     * @param partitionGraph
     */
    public static void mergePartitions(PartitionGraph partitionGraph) {
        TemporalInvariantSet invariants = partitionGraph.getInvariants();
        mergePartitions(partitionGraph, invariants);
    }

    /**
     * Works as {@code mergePartitions} but fixes k to 0.
     * 
     * @param partitionGraph
     * @param synoptic
     *            .invariants
     */
    public static void mergePartitions(PartitionGraph partitionGraph,
            TemporalInvariantSet invariants) {
        mergePartitions(partitionGraph, invariants, 0);
    }

    /**
     * Merge partitions if they are {@code k} equal, while maintaining
     * {@code synoptic.invariants}
     * 
     * @param partitionGraph
     *            the graph to coarsen
     * @param invariants
     *            the invariants to maintain during merge, can be null
     * @param k
     *            the k parameter for k-equality
     */
    public static void mergePartitions(PartitionGraph partitionGraph,
            TemporalInvariantSet invariants, int k) {
        TimedTask coarsening = PerformanceMetrics.createTask("coarsening",
                false);
        int outerItters = 0;
        int numAttemptedMerges = 0;

        // The blacklist keeps a history of partitions we've attempted to merge
        // and
        // which did not work out because they resulted in invariant violations.
        HashMap<Partition, HashSet<Partition>> blacklist = new HashMap<Partition, HashSet<Partition>>();
        outerWhile:
        while (true) {

            if (Main.dumpIntermediateStages) {
                GraphVizExporter.quickExport(
                        Main.getIntermediateDumpFilename("c", outerItters),
                        partitionGraph);
            }
            outerItters++;

            ArrayList<Partition> partitions = new ArrayList<Partition>();
            partitions.addAll(partitionGraph.getNodes());

            // Attempt to merge all pairs of partitions in the current graph.
            for (Partition p : partitions) {
                for (Partition q : partitions) {
                    // 1. Can't merge a partition with itself
                    if (p == q) {
                        continue;
                    }

                    // 2. Only merge partitions that are k-equivalent
                    if (!KTails.kEquals(p, q, k, false)) {
                        continue;
                    }

                    // 3. Ignore partition pairs that were previously tried (are
                    // in blacklist)
                    if (blacklist.containsKey(p)
                            && blacklist.get(p).contains(q)
                            || blacklist.containsKey(q)
                            && blacklist.get(q).contains(p)) {
                        continue;
                    }

                    logger.fine("Attempting merge: " + p + " + " + q);

                    Set<Partition> parts = new HashSet<Partition>();
                    parts.addAll(partitionGraph.getNodes());
                    IOperation rewindOperation = partitionGraph
                            .apply(new PartitionMerge(p, q));
                    numAttemptedMerges++;

                    RelationPath<Partition> vio = null;

                    if (invariants != null) {
                        vio = invariants.getFirstCounterExample(partitionGraph);
                    }

                    if (vio != null) {
                        // The merge created a violation. Remember this pair of
                        // partitions so that we don't try it again.
                        if (!blacklist.containsKey(p)) {
                            blacklist.put(p, new HashSet<Partition>());
                        }
                        blacklist.get(p).add(q);
                        // Undo the merge.
                        partitionGraph.apply(rewindOperation);

                        if (EXTRA_CHECKS) {
                            partitionGraph.checkSanity();
                        }

                        // We cannot change the partition sets because we are
                        // iterating over the partitions. Therefore, check that
                        // the resulting partition set is the same as the
                        // original partition set.
                        if (!(parts.containsAll(partitionGraph.getNodes()) && partitionGraph
                                .getNodes().containsAll(parts))) {
                            throw new InternalSynopticException(
                                    "partition set changed due to rewind: "
                                            + rewindOperation);
                        }

                    } else {
                        logger.fine("Merge maintains invs, continuing");
                        continue outerWhile;
                    }
                }
            }

            // Unable to find any k-equivalent partitions; we're done.
            break;
        }

        if (Main.dumpIntermediateStages) {
            GraphVizExporter.quickExport(
                    Main.getIntermediateDumpFilename("c", outerItters),
                    partitionGraph);
        }

        PerformanceMetrics.get().record("numOfMergeSteps", numAttemptedMerges);
        coarsening.stop();
    }

    /**
     * Wrapper to mergePartitions without invariant preservation.
     * 
     * @param partitionGraph
     *            the graph to reduce (merge {@code k}-equivalent partitions)
     * @param k
     *            parameter for equality
     */
    public static void kReduce(PartitionGraph partitionGraph, int k) {
        mergePartitions(partitionGraph, null, k);
    }
}