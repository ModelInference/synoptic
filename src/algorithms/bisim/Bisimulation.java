package algorithms.bisim;

import invariants.TemporalInvariantSet;
import invariants.TemporalInvariantSet.RelationPath;

import java.util.*;

import algorithms.graph.Operation;
import algorithms.graph.PartitionMerge;
import algorithms.graph.PartitionSplit;

import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.interfaces.IGraph;
import model.interfaces.ITransition;

/**
 * This class provides some methods to compute the quotient of the LTS w.r.t
 * weak or strong bisimulation (i.e. it computes the smalles weak/strong
 * bisimilar LTS).
 * 
 * It is bases on the code from Clemens Hammacher's implementation. Source:
 * https://ccs.hammacher.name Licence: Eclipse Public * License v1.0.
 */
public abstract class Bisimulation {
	private static boolean DEBUG = false;

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
		int outer = 0;
		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/initial.dot",
					partitionGraph);
			GraphVizExporter.quickExport("output/rounds/initials.dot",
					partitionGraph.getSystemStateGraph());
		}
		boolean noprogress = false;
		while (true) {
			boolean noprogress_this = noprogress;
			noprogress = false;
			TemporalInvariantSet invariants = partitionGraph.getInvariants();
			TemporalInvariantSet unsat = invariants
					.getUnsatisfiedInvariants(partitionGraph);
			if (unsat.size() == 0) {
				System.out.println("Invariants statisfied. Stopping.");
				break;
			} else {
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
						System.out.println("Could not get rid of " + r.path);
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
					System.out.println("Splitting according to " + partTrans
							+ "\n  to satisfy " + r.invariant);
					System.out.println("  path:" + r.path);
					System.out.flush();
					System.out.println("  -- ok");
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

	public static void refinePartitionsSmart(PartitionGraph partitionGraph)
			throws InterruptedException {
		Partition partition;
		int outer = 0;
		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/initial.dot",
					partitionGraph);
			GraphVizExporter.quickExport("output/rounds/initials.dot",
					partitionGraph.getSystemStateGraph());
		}
		boolean noprogress = false;
		while (true) {
			boolean noprogress_this = noprogress;
			noprogress = true;
			TemporalInvariantSet invariants = partitionGraph.getInvariants();
			TemporalInvariantSet unsat = invariants
					.getUnsatisfiedInvariants(partitionGraph);
			if (unsat.size() == 0) {
				System.out.println("Invariants statisfied. Stopping.");
				break;
			} else {
				System.out.println("" + unsat.size()
						+ " unsatisfied invariants: " + unsat);
			}

			List<RelationPath<Partition>> rp = unsat
					.getViolations(partitionGraph);

			for (RelationPath<Partition> relPath : rp) {
				PartitionSplit d = getSplit(relPath, partitionGraph);
				if (d == null || !d.isValid()) {
					 System.out.println("  -- invalid: " + d);
					// continue path;
					continue;
				}

				Operation rewindOperation = partitionGraph.apply(d);
				TemporalInvariantSet unsatAfter = invariants
						.getUnsatisfiedInvariants(partitionGraph);
				/*if (unsatAfter.size() == unsat.size() && !noprogress_this) {
					partitionGraph.apply(rewindOperation);
					// System.out.println(" NO PROGRESS ");
					noprogress = true;
					continue;
				}*/
				System.out.flush();
				System.out.println("  -- ok");
				noprogress = false;
				break;
			}
			if (noprogress_this)
				break;
			outer++;
		}

		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/" + outer
					+ "-final.dot", partitionGraph);
			GraphVizExporter.quickExport("output/rounds/" + outer
					+ "-finals.dot", partitionGraph.getSystemStateGraph());
		}
	}

	private static PartitionSplit getSplit(RelationPath<Partition> relPath, PartitionGraph partitionGraph) {
		Set<MessageEvent> hot = new HashSet<MessageEvent>();
		hot.addAll(partitionGraph.getInitialMessages());
		Partition prevPartition = null; 
		Partition nextPartition = null;
		System.out.println(relPath.path);
		for (Partition part : relPath.path) {
			prevPartition = nextPartition;
			nextPartition = part;
			hot.retainAll(part.getMessages());
			if (hot.size()==0)
				break;
			Set<MessageEvent> successors = new HashSet<MessageEvent>();
			for (MessageEvent m : hot)
				successors.addAll(m.getSuccessors(relPath.invariant.getRelation()));
			hot = successors;
		}
		ITransition<Partition> partTrans = prevPartition.getTransition(nextPartition, relPath.invariant.getRelation());
		System.out.println(partTrans);

		return prevPartition.getCandidateDivision(partTrans);
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

	public static void mergePartitions(PartitionGraph partitionGraph) {
		DEBUG = false;
		int outer = 0;
		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/initial.dot",
					partitionGraph);
			GraphVizExporter.quickExport("output/rounds/initials.dot",
					partitionGraph.getSystemStateGraph());
		}
		while (true) {
			TemporalInvariantSet invariants = partitionGraph.getInvariants();
			if (!invariants.check(partitionGraph)) {
				System.out.println("Invariants broken. Stopping.");
				System.out.println(invariants);
				System.out.println(TemporalInvariantSet
						.computeInvariants(partitionGraph));
				System.out.println("unsat "
						+ invariants.getUnsatisfiedInvariants(partitionGraph));
				break;
			}

			if (DEBUG) {
				GraphVizExporter.quickExport("output/rounds/" + outer + ".dot",
						partitionGraph);
				GraphVizExporter.quickExport(
						"output/rounds/" + outer + "s.dot", partitionGraph
								.getSystemStateGraph());
			}
			boolean progress = false;
			ArrayList<Partition> partitions = new ArrayList<Partition>();
			partitions.addAll(partitionGraph.getPartitions());
			out: for (Partition p : partitions) {
				for (Partition q : partitions) {
					if (p.getAction().equals(q.getAction()) && p != q) {
						System.out.println("merge " + p + " with " + q);
						PartitionSplit split = new PartitionSplit(p);
						for (MessageEvent m : q.getMessages())
							split.addFulfills(m);
						if (DEBUG) {
							GraphVizExporter.quickExport("output/rounds/"
									+ outer + "a.dot", partitionGraph);
							GraphVizExporter.quickExport("output/rounds/"
									+ outer + "as.dot", partitionGraph
									.getSystemStateGraph());
						}
						Operation rewindOperation = partitionGraph
								.apply(new PartitionMerge(p, q));
						if (DEBUG) {
							GraphVizExporter.quickExport("output/rounds/"
									+ outer + "b.dot", partitionGraph);
							GraphVizExporter.quickExport("output/rounds/"
									+ outer + "bs.dot", partitionGraph
									.getSystemStateGraph());
						}
						if (!invariants.check(partitionGraph)) {
							System.out.println("  REWIND");
							partitionGraph.apply(rewindOperation);
							if (DEBUG) {
								GraphVizExporter.quickExport("output/rounds/"
										+ outer + "c.dot", partitionGraph);
								GraphVizExporter.quickExport("output/rounds/"
										+ outer + "cs.dot", partitionGraph
										.getSystemStateGraph());
							}
							if (!invariants.check(partitionGraph)) {
								throw new RuntimeException("could not rewind");
							}
						} else {
							progress = true;
							break out;
						}
					}
				}
			}
			if (!progress)
				break;
			outer++;
			System.out.println();
		}
		if (DEBUG) {
			GraphVizExporter.quickExport("output/rounds/" + outer
					+ "-final.dot", partitionGraph);
			GraphVizExporter.quickExport("output/rounds/" + outer
					+ "-finals.dot", partitionGraph.getSystemStateGraph());
		}
	}
}
