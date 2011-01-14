/** Implementation of the synoptic.main algorithms */

package synoptic.algorithms.bisim;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.algorithms.graph.Operation;
import synoptic.algorithms.graph.PartitionMerge;
import synoptic.algorithms.graph.PartitionMultiSplit;
import synoptic.algorithms.graph.PartitionSplit;
import synoptic.algorithms.ktail.StateUtil;
import synoptic.benchmarks.PerformanceMetrics;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.TemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.TemporalInvariantSet.RelationPath;
import synoptic.main.Main;
import synoptic.model.MessageEvent;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;





/**
 * This class implements the algorithm BisimH ({@code
 * Bisimulation.refinePartitions}), and a modified version of the algorithm
 * kTail ({@code Bisimulation.mergePartitions}) that considers state labels
 * instead of state transitions.
 * 
 * It is based on the code from Clemens Hammacher's implementation of a
 * partition refinement algorithm for bisimulation minimization. Source:
 * https://ccs.hammacher.name License: Eclipse Public License v1.0.
 */
public abstract class Bisimulation {
	public static Logger logger = Logger.getLogger("Bisimulation");
	/**
	 * If true, a partition may be split on incoming/outgoing edges at once, if
	 * both splits resolve the invariant (when considered separately).
	 */
	private static final boolean fourWaySplit = true;
	/**
	 * Set to combine all candidate splits for each partition into a multi split.
	 */
	private static final boolean combineCandidates = false;
	/**
	 * Perform extra correctness checks.
	 */
	private static boolean EXTRA_CHECKS = true;
	/**
	 * Consider incoming transitions for splitting
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

	/**
	 * Refines the partitions in {@code partitionGraph} until all synoptic.invariants
	 * returned by {@code partitionGraph.getInvariants()} are satisfied.
	 * 
	 * @param partitionGraph
	 *            - the partition graph to refine
	 */
	public static void refinePartitions(PartitionGraph partitionGraph) {
		TimedTask refinement = PerformanceMetrics.createTask("refinement", false);
		
		if (Main.dumpIntermediateStages) {
			GraphVizExporter.quickExport(Main.getIntermediateDumpFilename("r", 0), partitionGraph);
		}
		
		int numSplitSteps = 0;
		// These synoptic.invariants will be satisfied
		TemporalInvariantSet invariants = partitionGraph.getInvariants();

		Set<TemporalInvariant> unsatisfiedInvariants = new LinkedHashSet<TemporalInvariant>();
		unsatisfiedInvariants.addAll(partitionGraph.getInvariants().getSet());
		Set<TemporalInvariant> satisfiedInvariants = new LinkedHashSet<TemporalInvariant>();

		List<RelationPath<Partition>> counterexampleTraces = null;
		while (unsatisfiedInvariants.size() > 0) {
			/**
			 * Stores all splits that cause an invariant to be satisfied,
			 * indexed by partition to which they are applied.
			 */
			LinkedHashMap<Partition, PartitionMultiSplit> splitsToDoByPartition = new LinkedHashMap<Partition, PartitionMultiSplit>();
			/**
			 * Stores all synoptic.invariants for which we have a split that satisfies
			 * it.
			 */
			ArrayList<TemporalInvariant> newlySatisfiedInvariants = new ArrayList<TemporalInvariant>();

			// retrieve the counterexamples.
			counterexampleTraces = new TemporalInvariantSet(
					unsatisfiedInvariants).getViolations(partitionGraph);

			List<RelationPath<Partition>> newExamples = new ArrayList<RelationPath<Partition>>();
			
			// if we have no counterexamples, we are done
			if (counterexampleTraces == null
					|| counterexampleTraces.size() == 0) {
				logger.fine("Invariants statisfied. Stopping.");
				break;
			}
			logger.info("" + counterexampleTraces.size()
					+ " unsatisfied synoptic.invariants: " + counterexampleTraces);

			// Update the sets with satisfied and unsatisfied synoptic.invariants.
			unsatisfiedInvariants.clear();
			for (RelationPath<Partition> relPath : counterexampleTraces) {
				unsatisfiedInvariants.add(relPath.invariant);
			}
			satisfiedInvariants.clear();
			satisfiedInvariants.addAll(partitionGraph.getInvariants().getSet());
			satisfiedInvariants.removeAll(unsatisfiedInvariants);

			/**
			 * Stores the first valid split. This split will be performed if no
			 * other split (that would resolve an invariant) is available.
			 */
			Operation arbitrarySplit = null;
			for (RelationPath<Partition> counterexampleTrace : counterexampleTraces) {
				// Get the possible splits to resolve this invariant.
				List<PartitionSplit> candidateSplits = getSplits(
						counterexampleTrace, partitionGraph);
				PartitionMultiSplit combinedSplit = null;

				if (combineCandidates) {
					for (PartitionSplit candidateSplit : candidateSplits) {
						if (candidateSplit == null || !candidateSplit.isValid()) {
							System.out.println("Skipping invalid source: " + candidateSplit);
							continue;
						}
						if (combinedSplit == null)
							combinedSplit = new PartitionMultiSplit(
									candidateSplit);
						else 
							combinedSplit.incorporate(candidateSplit);
					}
					if (combinedSplit == null) {
						System.out.println("No valid sources available.");
						continue;
					}
					if (!combinedSplit.isValid()) {
						System.out.println("Combined split is invalid.");
						continue;
					}
					if (arbitrarySplit == null)
						arbitrarySplit = combinedSplit;

					Operation rewindOperation = partitionGraph
							.apply(combinedSplit);
					if (Main.dumpIntermediateStages) {
						GraphVizExporter.quickExport(Main.getIntermediateDumpFilename("r", numSplitSteps + 1),
									partitionGraph);
					}

					// see if splitting resolved the violation
					// XXX
					RelationPath<Partition> violation = invariants
							.getViolation(counterexampleTrace.invariant,
									partitionGraph);

					// rewind for now
					partitionGraph.apply(rewindOperation);

					// if the invariant has no further violation, we made
					// progress, so the split is marked as useful.
					if (violation == null) {
						// If we already have a split for that partition, we
						// incorporate the new split into it
						if (splitsToDoByPartition.containsKey(combinedSplit
								.getPartition())) {
							System.out.println("recombining");
							splitsToDoByPartition.get(
									combinedSplit.getPartition()).incorporate(
									combinedSplit);
						} else
							splitsToDoByPartition.put(combinedSplit
									.getPartition(), combinedSplit);
						// Remember that we resolved this invariant
						// violation
						newlySatisfiedInvariants
								.add(counterexampleTrace.invariant);
					}
				} else {
					for (PartitionSplit candidateSplit : candidateSplits) {
						// See if the split is valid
						if (candidateSplit == null || !candidateSplit.isValid()) {
							logger.fine("  -- invalid: "
										+ candidateSplit);
							continue;
						}
						// store away an arbitrary split
						if (arbitrarySplit == null)
							arbitrarySplit = candidateSplit;
						// Perform the split
						Operation rewindOperation = partitionGraph
								.apply(candidateSplit);
						
						if (Main.dumpIntermediateStages) {
							GraphVizExporter.quickExport(Main.getIntermediateDumpFilename("r", numSplitSteps + 1),
									partitionGraph);
						}

						// see if splitting resolved the violation
						RelationPath<Partition> violation = invariants
								.getViolation(counterexampleTrace.invariant,
										partitionGraph);

						// rewind for now
						partitionGraph.apply(rewindOperation);

						// if the invariant has no further violation, we made
						// progress, so the split as marked as useful.
						if (violation == null) {
							// If we already have a split for that partition, we
							// incorporate the new split into it
							if (splitsToDoByPartition
									.containsKey(candidateSplit.getPartition())) {
								splitsToDoByPartition.get(
										candidateSplit.getPartition())
										.incorporate(candidateSplit);
							} else
								splitsToDoByPartition
										.put(candidateSplit.getPartition(),
												new PartitionMultiSplit(
														candidateSplit));
							// Remember that we resolved this invariant
							// violation
							newlySatisfiedInvariants
									.add(counterexampleTrace.invariant);
							if (!fourWaySplit) {
								continue;
							}
						}
					}
				}
			}
			
			if (splitsToDoByPartition.size() == 0) {
				// we have no splits that make progress. See if we have an
				// arbitrary split.
				if (arbitrarySplit == null) {
					/*
					System.out.println("no valid split available, recomputing.");
					continue; */
					System.out.println("no valid split available, exiting.");
					break;
				}
				partitionGraph.apply(arbitrarySplit);
				logger.info("1 arbitrary split, "
							+ partitionGraph.getNodes().size()
							+ " nodes in graph. Split is: " + arbitrarySplit);
			} else {
				// we have splits to do, perform them
				for (PartitionMultiSplit currentSplit : splitsToDoByPartition
						.values()) {
					partitionGraph.apply(currentSplit);
				}
				// update synoptic.invariants
				satisfiedInvariants.addAll(newlySatisfiedInvariants);
				unsatisfiedInvariants.removeAll(newlySatisfiedInvariants);

				// handle interleaved merging
				if (interleavedMerging) {
					logger.fine("recompressing...");
					Bisimulation.mergePartitions(partitionGraph,
							new TemporalInvariantSet(satisfiedInvariants));
				}
				logger.info(newlySatisfiedInvariants.size()
						+ " split(s) done, "
						+ partitionGraph.getNodes().size()
						+ " nodes in graph, "
						+ unsatisfiedInvariants.size()
						+ " unsatisfiedInvariants left.");
			}
			numSplitSteps++;
		}
		
		if (Main.dumpIntermediateStages) {
			GraphVizExporter.quickExport(Main.getIntermediateDumpFilename("r", numSplitSteps),
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
		Set<MessageEvent> hot = new HashSet<MessageEvent>();
		hot.addAll(counterexampleTrace.path.get(0).getMessages());
		Partition prevPartition = null;
		Partition nextPartition = null;
		Partition curPartition = null;
		logger.fine("" + counterexampleTrace.path);
		// Walk along the path
		for (Partition part : counterexampleTrace.path) {
			if (part == null)
				throw new RuntimeException("relation path contained null");
			prevPartition = curPartition;
			curPartition = nextPartition;
			nextPartition = part;
			hot.retainAll(part.getMessages());
			// if we cannot follow, we found the partition we have to split.
			if (hot.size() == 0)
				break;
			// Compute the valid successor messages in the original trace
			Set<MessageEvent> successorMessages = new HashSet<MessageEvent>();
			for (MessageEvent m : hot)
				successorMessages.addAll(m
						.getSuccessors(counterexampleTrace.invariant
								.getRelation()));
			hot = successorMessages;
		}
		ITransition<Partition> outgoingTransition = curPartition.getTransition(
				nextPartition, counterexampleTrace.invariant.getRelation());
		ITransition<Partition> incomingTransition = null;
		if (prevPartition != null)
			incomingTransition = prevPartition.getTransition(curPartition,
					counterexampleTrace.invariant.getRelation());
		if (outgoingTransition != null) {
			logger.fine("" + outgoingTransition);
			candidateSplits.add(curPartition
					.getCandidateDivision(outgoingTransition));
		}
		if (incomingTransition != null && incomingTransitionSplit) {
			logger.fine("" + incomingTransition);
			candidateSplits.add(curPartition
					.getCandidateDivisionBasedOnIncoming(prevPartition,
							incomingTransition.getRelation()));
		}
		return candidateSplits;
	}
	
	/**
	 * Construct a partition graph from {@code graph} (by partitioning by
	 * label), call {@code refinePartitions} on it, and return the refined
	 * graph.
	 * 
	 * @param graph
	 *            the graph from which should be used as initial graph
	 * @return the refined graph
	 * @throws InterruptedException
	 */
	public static PartitionGraph getRefinedGraph(IGraph<MessageEvent> graph)
			throws InterruptedException {
		PartitionGraph g = new PartitionGraph(graph, true);
		refinePartitions(g);
		return g;
	}

	/**
	 * Construct a partition graph from {@code graph} (by partitioning by
	 * label), call {@code mergePartitions} on it, and return the refined graph.
	 * 
	 * @param graph
	 *            the graph from which should be used as initial graph
	 * @return the merged graph
	 * @throws InterruptedException
	 */
	public static PartitionGraph getMergedGraph(IGraph<MessageEvent> graph) {
		PartitionGraph g = new PartitionGraph(graph);
		mergePartitions(g);
		return null;
	}

	/**
	 * Works as {@code mergePartitions} but fixes synoptic.invariants to {@code
	 * partitionGraph.getInvariants()}
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
	 * @param synoptic.invariants
	 */
	public static void mergePartitions(PartitionGraph partitionGraph,
			TemporalInvariantSet invariants) {
		mergePartitions(partitionGraph, invariants, 0);
	}

	/**
	 * Merge partitions if they are {@code k} equal, while maintaining {@code
	 * synoptic.invariants}
	 * 
	 * @param partitionGraph
	 *            the graph to coarsen
	 * @param synoptic.invariants
	 *            - the synoptic.invariants to maintain, can be null
	 * @param k
	 *            - the k parameter for k-equality
	 */
	public static void mergePartitions(PartitionGraph partitionGraph,
			TemporalInvariantSet invariants, int k) {
		TimedTask coarsening = PerformanceMetrics.createTask("coarsening", false);
		int outer = 0;
		int numMergeSteps = 0;
		HashMap<Partition, HashSet<Partition>> blacklist = new HashMap<Partition, HashSet<Partition>>();
		out: while (true) {
			logger.fine("Number of nodes in final graph: " + partitionGraph.getNodes().size());
			
			if (Main.dumpIntermediateStages) {
				GraphVizExporter.quickExport(Main.getIntermediateDumpFilename("c", outer),
						partitionGraph);
			}
			
			boolean progress = false;
			ArrayList<Partition> partitions = new ArrayList<Partition>();
			partitions.addAll(partitionGraph.getPartitions());
			for (Partition p : partitions) {
				for (Partition q : partitions) {
					if (p != q && StateUtil.kEquals(p, q, k, false)) {
						if ((blacklist.containsKey(p) && blacklist.get(p)
								.contains(q))
								|| (blacklist.containsKey(q) && blacklist
										.get(q).contains(p)))
							continue;
						// if (partitionGraph.getInitialNodes().contains(p) !=
						// partitionGraph
						// .getInitialNodes().contains(q))
						// continue;
						logger.fine("merge " + p + " with " + q);
						Set<Partition> parts = new HashSet<Partition>();
						parts.addAll(partitionGraph.getNodes());
						Operation rewindOperation = partitionGraph
								.apply(new PartitionMerge(p, q));
						numMergeSteps++;
						RelationPath<Partition> vio = null;
						// partitionGraph.checkSanity();
						// if (true)
						// continue out;
						if (invariants != null)
							vio = invariants.getFirstViolation(partitionGraph);

						if (invariants != null && vio != null) {

							logger.fine("  REWIND");
							if (!blacklist.containsKey(p))
								blacklist.put(p, new HashSet<Partition>());
							blacklist.get(p).add(q);
							partitionGraph.apply(rewindOperation);
							
							if (EXTRA_CHECKS)
								partitionGraph.checkSanity();
							
							if (!parts.containsAll(partitionGraph.getNodes())
									|| !partitionGraph.getNodes().containsAll(
											parts))
								throw new RuntimeException(
										"partition set changed due to rewind: "
												+ rewindOperation);
							// if (!synoptic.invariants.check(partitionGraph)) {
							// throw new RuntimeException("could not rewind");
							// }
						} else {
							progress = true;
							continue out;
						}
					}
				}
			}
			if (!progress)
				break;
			outer++;
		}

		if (Main.dumpIntermediateStages) {
				GraphVizExporter.quickExport(Main.getIntermediateDumpFilename("c", outer),
						partitionGraph);
		}

		PerformanceMetrics.get().record("numOfMergeSteps", numMergeSteps);
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
