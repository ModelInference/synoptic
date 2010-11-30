/** Implementation of the main algorithms */

package algorithms.bisim;

import invariants.TemporalInvariant;
import invariants.TemporalInvariantSet;
import invariants.TemporalInvariantSet.RelationPath;

import java.util.*;
import java.util.logging.Logger;

import benchmarks.PerformanceMetrics;
import benchmarks.TimedTask;

import main.Main;

import algorithms.graph.Operation;
import algorithms.graph.PartitionMerge;
import algorithms.graph.PartitionMultiSplit;
import algorithms.graph.PartitionSplit;
import algorithms.ktail.StateUtil;

import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.interfaces.IGraph;
import model.interfaces.ITransition;

/**
 * This class implements the algorithm BisimH ({@code
 * Bisimulation.refinePartitions}), and a modified version of the algorithm
 * kTail ({@code Bisimulation.mergePartitions}) that considers state labels
 * instead of state transitions.
 * 
 * It is based on the code from Clemens Hammacher's implementation of a
 * partition refinement algorithm for bisimulation minimization. Source:
 * https://ccs.hammacher.name Licence: Eclipse Public License v1.0.
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
	 * Refines the partitions in {@code partitionGraph} until all invariants
	 * returned by {@code partitionGraph.getInvariants()} are satisfied.
	 * 
	 * @param partitionGraph
	 *            - the partition graph to refine
	 */
	public static void refinePartitions(PartitionGraph partitionGraph) {
		TimedTask refinement = PerformanceMetrics.createTask("refinement", false);
		
		if (Main.dumpIntermediateStages) {
			GraphVizExporter.quickExport(Main.GetIntermediateDumpFilename("r", 0), partitionGraph);
		}
		
		int numSplitSteps = 0;
		// These invariants will be satisfied
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
			 * Stores all invariants for which we have a split that satisfies
			 * it.
			 */
			ArrayList<TemporalInvariant> newlySatisfiedInvariants = new ArrayList<TemporalInvariant>();

			// retrieve the counterexamples.
			counterexampleTraces = new TemporalInvariantSet(
					unsatisfiedInvariants).getViolations(partitionGraph);

			// if we have no counterexamples, we are done
			if (counterexampleTraces == null
					|| counterexampleTraces.size() == 0) {
				logger.fine("Invariants statisfied. Stopping.");
				break;
			}
			logger.info("" + counterexampleTraces.size()
					+ " unsatisfied invariants: " + counterexampleTraces);

			// Update the sets with satisfied and unsatisfied invariants.
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
						GraphVizExporter.quickExport(Main.GetIntermediateDumpFilename("r", numSplitSteps + 1),
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
							GraphVizExporter.quickExport(Main.GetIntermediateDumpFilename("r", numSplitSteps + 1),
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
					System.out.println("no valid split available, recomputing.");
					continue;
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
				// update invariants
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
			GraphVizExporter.quickExport(Main.GetIntermediateDumpFilename("r", numSplitSteps),
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
			// Compute the sucessor messages
			Set<MessageEvent> successorMessages = new HashSet<MessageEvent>();
			for (MessageEvent m : hot)
				successorMessages.addAll(m
						.getSuccessors(counterexampleTrace.invariant
								.getRelation()));
			hot = successorMessages;
		}
		ITransition<Partition> outgoingTransition = curPartition.getTransition(
				nextPartition, counterexampleTrace.invariant.getRelation());
		ITransition<Partition> incommingTransition = null;
		if (prevPartition != null)
			incommingTransition = prevPartition.getTransition(curPartition,
					counterexampleTrace.invariant.getRelation());
		if (outgoingTransition != null) {
			logger.fine("" + outgoingTransition);
			candidateSplits.add(curPartition
					.getCandidateDivision(outgoingTransition));
		}
		if (incommingTransition != null && incomingTransitionSplit) {
			logger.fine("" + incommingTransition);
			candidateSplits.add(curPartition
					.getCandidateDivisionBasedOnIncoming(prevPartition,
							incommingTransition.getRelation()));
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
	 * Works as {@code mergePartitions} but fixes invariants to {@code
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
	 * @param invariants
	 */
	public static void mergePartitions(PartitionGraph partitionGraph,
			TemporalInvariantSet invariants) {
		mergePartitions(partitionGraph, invariants, 0);
	}

	/**
	 * Merge partitions if they are {@code k} equal, while maintaining {@code
	 * invariants}
	 * 
	 * @param partitionGraph
	 *            the graph to coarsen
	 * @param invariants
	 *            - the invariants to maintain, can be null
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
			logger.info("m " + partitionGraph.getNodes().size());
			
			if (Main.dumpIntermediateStages) {
				GraphVizExporter.quickExport(Main.GetIntermediateDumpFilename("c", outer),
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
							// if (!invariants.check(partitionGraph)) {
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
				GraphVizExporter.quickExport(Main.GetIntermediateDumpFilename("c", outer),
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
