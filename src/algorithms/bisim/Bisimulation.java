package algorithms.bisim;

import invariants.TemporalInvariantSet;
import invariants.TemporalInvariantSet.RelationPath;

import java.util.*;
import java.util.Map.Entry;

import algorithms.graph.Operation;
import algorithms.graph.PartitionMerge;
import algorithms.graph.PartitionSplit;
import algorithms.ktail.StateUtil;

import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.interfaces.IGraph;
import model.interfaces.ITransition;

/**
 * This class provides some methods to compute the quotient of the LTS w.r.t
 * weak or strong bisimulation (i.e. it computes the smalles weak/strong
 * bisimilar LTS. The main functionality is provided by {@code
 * refinePartitionsSmart}.
 * 
 * It is bases on the code from Clemens Hammacher's implementation. Source:
 * https://ccs.hammacher.name Licence: Eclipse Public * License v1.0.
 */
public abstract class Bisimulation {
	/**
	 * Essential status output
	 */
	private static final boolean ESSENTIAL = true;
	private static boolean VERBOSE = false;
	/**
	 * Export a graph in every round
	 */
	private static boolean DEBUG = false;
	/**
	 * Consider incomming edges for splitting
	 */
	private static boolean incommingEdgeSplit = false;
	/**
	 * Coarsen the representation after each successful split (i.e. each split
	 * that caused a previously unsatisfied invariant to become satisfied. This
	 * may or may not speed up operation.
	 */
	private static boolean interleavedMerging = false;
	/**
	 * Number of steps done in refinePartitionsSmart
	 */
	public static int steps;
	/**
	 * Number of merges done in coarsenPartitions
	 */
	public static int merge;

	private Bisimulation() {
		// forbid instantiation
	}

	private static PartitionGraph computePartitions(IGraph<MessageEvent> graph)
			throws InterruptedException {
		PartitionGraph g = new PartitionGraph(graph, true);
		refinePartitions(g);
		return g;
	}

	public static void refinePartitions(PartitionGraph partitionGraph)
			throws InterruptedException {
		Partition partition;
		int outer = 1;
		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/0.dot", partitionGraph);
			GraphVizExporter.quickExport("output/rounds/0s.dot", partitionGraph
					.getSystemStateGraph());
		}
		boolean noprogress = false;
		while (true) {
			boolean noprogress_this = noprogress;
			noprogress = false;
			TemporalInvariantSet invariants = partitionGraph.getInvariants();
			TemporalInvariantSet unsat = invariants
					.getUnsatisfiedInvariants(partitionGraph);
			if (unsat.size() == 0) {
				if (VERBOSE)
					System.out.println("Invariants statisfied. Stopping.");
				break;
			} else {
				if (VERBOSE)
					System.out.println("" + unsat.size()
							+ " unsatisfied invariants: " + unsat);
			}

			List<RelationPath<Partition>> rp = unsat
					.getViolations(partitionGraph);
			int maxSize = 0;
			for (RelationPath<Partition> r : rp) {
				Collections.reverse(r.path);
				maxSize = Math.max(maxSize, r.path.size());
			}

			path: for (int i = 1; i < maxSize; ++i) {
				for (RelationPath<Partition> r : rp) {
					if (r.path.size() <= i) {
						continue;
					}
					// System.out.println("Shortes violation: " + r.path);
					if (DEBUG) {
						GraphVizExporter.quickExport("output/rounds/" + outer
								+ "-" + i + ".dot", partitionGraph);
						GraphVizExporter.quickExport("output/rounds/" + outer
								+ "-" + i + "s.dot", partitionGraph
								.getSystemStateGraph());
					}
					partition = r.path.get(i - 1);
					Partition prevPartition = r.path.get(i);
					ITransition<Partition> partTrans = prevPartition
							.getTransition(partition, r.invariant.getRelation());
					if (prevPartition.getMessages().size() < 2) {
						if (VERBOSE)
							System.out
									.println("Could not get rid of " + r.path);
						continue;
					}

					PartitionSplit d = prevPartition
							.getCandidateDivision(partTrans);
					if (d == null || !d.isValid()) {
						// System.out.println("  -- invalid");
						// continue path;
						continue;
					}

					Operation rewindOperation = partitionGraph.apply(d);
					TemporalInvariantSet unsatAfter = invariants
							.getUnsatisfiedInvariants(partitionGraph);
					if (unsatAfter.size() == unsat.size() && !noprogress_this) {
						partitionGraph.apply(rewindOperation);
						// System.out.println(" NO PROGRESS ");
						noprogress = true;
						continue;
					}
					if (VERBOSE) {
						System.out.println("Splitting according to "
								+ partTrans + "\n  to satisfy " + r.invariant);
						System.out.println("  path:" + r.path);
						System.out.flush();
						System.out.println("  -- ok");
					}
					noprogress = false;
					break path;
				}
			}
			// if (noprogress)
			// break;
			outer++;
		}

		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/" + outer
					+ "-final.dot", partitionGraph);
			GraphVizExporter.quickExport("output/rounds/" + outer
					+ "-finals.dot", partitionGraph.getSystemStateGraph());
		}
	}

	/**
	 * Refines the partitions in {@code partitionGraph} until all invariants
	 * returned by {@code partitionGraph.getInvariants()} are satisfied.
	 * 
	 * @param partitionGraph
	 *            - the partition graph to refine
	 */
	public static void refinePartitionsSmart(PartitionGraph partitionGraph) {
		int outer = 1;
		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/0.dot", partitionGraph);
		}
		steps = 0;
		/**
		 * noprogress is true if all splits tried did cause any unsatisfied
		 * invariant to become satisfied
		 */
		boolean noprogress = false;
		// This keeps track of the splitting done; currently it is not used
		// anymore.
		HashMap<Partition, PartitionSplit> partDepsOn = new HashMap<Partition, PartitionSplit>();
		HashSet<PartitionSplit> effectiveSplits = new HashSet<PartitionSplit>();
		HashMap<PartitionSplit, Operation> rewinds = new HashMap<PartitionSplit, Operation>();
		int lastUnsatSize = 0;
		// These invariants will be satisfied
		TemporalInvariantSet invariants = partitionGraph.getInvariants();
		List<RelationPath<Partition>> rp = invariants
				.getViolations(partitionGraph);

		while (true) {
			boolean noprogress_this = noprogress;
			noprogress = true;
			boolean lastWasSuccessful = false;
			if (rp == null || rp.size() == 0) {
				if (VERBOSE)
					System.out.println("Invariants statisfied. Stopping.");
				break;
			} else if (ESSENTIAL) {
				System.out.println("" + rp.size() + " unsatisfied invariants: "
						+ rp);
			}

			off: for (RelationPath<Partition> relPath : rp) {
				List<PartitionSplit> dl = getSplits(relPath, partitionGraph);
				for (PartitionSplit d : dl) {
					if (d == null || !d.isValid()) {
						if (VERBOSE)
							System.out.println("  -- invalid: " + d);
						// continue path;
						noprogress = true;
						continue;
					}
					Operation rewindOperation = partitionGraph.apply(d);
					steps++;
					if (DEBUG) {
						GraphVizExporter.quickExport("output/rounds/" + outer
								+ ".dot", partitionGraph);
					}
					// Now we must check if we changed something
					List<RelationPath<Partition>> unsatAfter = invariants
							.getViolations(partitionGraph);
					// if the unsatAfter size is the same, and we are still
					// determining
					// if progress is possible, then rewind here.
					// Note that since refining may never introduce invariant
					// violations for our three types of invariants,
					// a size check is enough here
					if (unsatAfter != null && unsatAfter.size() == rp.size()
							&& !noprogress_this) {
						partitionGraph.apply(rewindOperation);
						if (VERBOSE)
							System.out
									.println(" -- rewind (no progress): " + d);
						noprogress = true;
						continue;
					}
					System.out.flush();
					// This is just bookkeeping that is not used for anything
					// atm.
					PartitionMerge m = (PartitionMerge) rewindOperation;
					partDepsOn.put(m.getRemoved(), d);
					rewinds.put(d, rewindOperation);
					if (unsatAfter == null || unsatAfter.size() < rp.size()) {
						if (VERBOSE)
							System.out.println("  -- ok");
						effectiveSplits.add(d);
						lastWasSuccessful = true;
					} else if (VERBOSE)
						System.out.println("  -- forced");
					// if we are here, we did a successful split. Either because
					// we removed a violation,
					// or because we did any split because noprogress was true.
					noprogress = false;
					rp = unsatAfter;
					break off;
				}
			}
			// handle interleaved merging
			if ((lastWasSuccessful || lastUnsatSize > rp.size())
					&& interleavedMerging) {
				System.out.println("recompressing...");
				Bisimulation.mergePartitions(partitionGraph,
						TemporalInvariantSet.computeInvariants(partitionGraph));
			}
			if (ESSENTIAL)
				System.out.println(partitionGraph.getNodes().size() + " "
						+ (rp != null ? rp.size() : 0));
			outer++;
			if (rp != null)
				lastUnsatSize = rp.size();
		}

		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/" + outer
					+ "-final.dot", partitionGraph);
		}

		// HashSet<Operation> needed = new HashSet<Operation>();
		// for (PartitionSplit s : effectiveSplits) {
		// needed.add(s);
		// while (partDepsOn.containsKey(s.getPartition())) {
		// s = partDepsOn.get(s.getPartition());
		// needed.add(s);
		// }
		// }
		// int rewound = 0;
		// for (Entry<PartitionSplit, Operation> e : rewinds.entrySet()) {
		// if (needed.contains(e.getKey()))
		// continue;
		// partitionGraph.apply(e.getValue());
		// rewound++;
		// }
		// System.out.println("rewound " + rewound + " of " + rewinds.size());

	}

	/**
	 * Comupte possible splits to remove the path relPath from partitionGraph.
	 * This is done by following relPath in the original graph and determining
	 * the point where partitionGraph allows a transition it should not allow.
	 * The original graph is accessed via the Messages (which are nodes in the
	 * original graph) in the partitions.
	 * 
	 * @param relPath
	 *            - the path to remove
	 * @param partitionGraph
	 *            - the graph from which the path shall be removed
	 * @return a list of partition splits that remove relPath
	 */
	private static List<PartitionSplit> getSplits(
			RelationPath<Partition> relPath, PartitionGraph partitionGraph) {
		List<PartitionSplit> ret = new ArrayList<PartitionSplit>();
		Set<MessageEvent> hot = new HashSet<MessageEvent>();
		hot.addAll(relPath.path.get(0).getMessages());
		Partition prevPartition = null;
		Partition nextPartition = null;
		Partition curPartition = null;
		if (VERBOSE)
			System.out.println(relPath.path);
		for (Partition part : relPath.path) {
			prevPartition = curPartition;
			curPartition = nextPartition;
			nextPartition = part;
			hot.retainAll(part.getMessages());
			if (hot.size() == 0)
				break;
			Set<MessageEvent> successors = new HashSet<MessageEvent>();
			for (MessageEvent m : hot)
				successors.addAll(m.getSuccessors(relPath.invariant
						.getRelation()));
			hot = successors;
		}
		ITransition<Partition> partTrans = curPartition.getTransition(
				nextPartition, relPath.invariant.getRelation());
		ITransition<Partition> partTrans2 = null;
		if (prevPartition != null)
			partTrans2 = prevPartition.getTransition(curPartition,
					relPath.invariant.getRelation());
		if (partTrans != null) {
			if (VERBOSE)
				System.out.println(partTrans);
			ret.add(curPartition.getCandidateDivision(partTrans));
		}
		if (partTrans2 != null && incommingEdgeSplit) {
			if (VERBOSE)
				System.out.println(partTrans2);
			ret.add(curPartition.getCandidateDivisionReach(prevPartition,
					partTrans2));
		}
		return ret;
	}

	public static PartitionGraph getRefinedSystem(IGraph<MessageEvent> system)
			throws InterruptedException {
		PartitionGraph g = Bisimulation.computePartitions(system);
		return g;
	}

	public static PartitionGraph getMergedSystem(IGraph<MessageEvent> graph) {
		PartitionGraph g = new PartitionGraph(graph);
		mergePartitions(g);
		return null;
	}

	/**
	 * Works as {@code mergePartitions} but fixes invariants to {@code partitionGraph.getInvariants()}
	 * @param partitionGraph
	 */
	public static void mergePartitions(PartitionGraph partitionGraph) {
		TemporalInvariantSet invariants = partitionGraph.getInvariants();
		mergePartitions(partitionGraph, invariants);
	}

	/**
	 * Works as {@code mergePartitions} but fixes k to 0.
	 * @param partitionGraph
	 * @param invariants
	 */
	public static void mergePartitions(PartitionGraph partitionGraph,
			TemporalInvariantSet invariants) {
		mergePartitions(partitionGraph, invariants, 0);
	}

	/**
	 * Merge partitions if they are {@code k} equal, while maintaining {@code invariants}
	 * @param partitionGraph the graph to coarsen
	 * @param invariants - the invariants to maintain, can be null
	 * @param k - the k parameter for k-equality
	 */
	public static void mergePartitions(PartitionGraph partitionGraph,
			TemporalInvariantSet invariants, int k) {
		int outer = 0;
		merge = 0;
		HashMap<Partition, HashSet<Partition>> blacklist = new HashMap<Partition, HashSet<Partition>>();
		out: while (true) {
			if (ESSENTIAL)
				System.out.println("m " + partitionGraph.getNodes().size());
			if (DEBUG) {
				GraphVizExporter.quickExport(
						"output/rounds/m" + outer + ".dot", partitionGraph);
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
						if (VERBOSE)
							System.out.println("merge " + p + " with " + q);
						Set<Partition> parts = new HashSet<Partition>();
						parts.addAll(partitionGraph.getNodes());
						Operation rewindOperation = partitionGraph
								.apply(new PartitionMerge(p, q));
						merge++;
						List<RelationPath<Partition>> vio = null;
						// partitionGraph.checkSanity();
						// if (true)
						// continue out;
						if (invariants != null)
							vio = invariants.getViolations(partitionGraph);

						if (invariants != null && vio != null && vio.size() > 0) {

							if (VERBOSE)
								System.out.println("  REWIND");
							if (!blacklist.containsKey(p))
								blacklist.put(p, new HashSet<Partition>());
							blacklist.get(p).add(q);
							partitionGraph.apply(rewindOperation);
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
		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/m" + outer
					+ "-final.dot", partitionGraph);
		}
	}

	public static void kReduce(PartitionGraph partitionGraph, int k) {
		mergePartitions(partitionGraph, null, k);
	}
}
