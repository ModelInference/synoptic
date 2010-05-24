package algorithms.ktail;

import invariants.TemporalInvariantSet;

import java.util.HashSet;
import java.util.Set;

import algorithms.graph.CascadingStateMerge;
import algorithms.graph.Operation;

import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.export.GraphVizExporter;
import model.interfaces.IGraph;

public class KTail {

	/**
	 * Preforms GK-Tail reduction on the graph
	 * 
	 * @param k
	 * @param subsumption
	 * @param preserveInvariants
	 */
	public static void kReduce(PartitionGraph g, int k, boolean subsumption,
			boolean preserveInvariants) {
		boolean result = true;
		TemporalInvariantSet invariants = preserveInvariants ? TemporalInvariantSet
				.computeInvariants(g) : null;

		while (result) {
			result = kReduceHelper(g, k, subsumption, invariants);
			// System.out.println(export.exportMessagesAsTransitions(this));
		}
	}

	/**
	 * The actual body of the GK-Tail implementation.
	 * 
	 * @param k
	 * @param subsumption
	 * @param invariants
	 * @return
	 */
	private static boolean kReduceHelper(PartitionGraph g, int k,
			boolean subsumption, TemporalInvariantSet invariants) {
		IGraph<SystemState<Partition>> gs = g.getSystemStateGraph();
		
		int merges = 0;
		Set<SystemState<Partition>> states = new HashSet<SystemState<Partition>>(gs.getNodes());
		for (SystemState<Partition> s1 : states) {
			for (SystemState<Partition> s2 : states) {
				// no self merges
				if (s1 == s2)
					continue;

				// If states are not k-equivalent then look for a pair that is.
				if (!StateUtil.kEquals(s1, s2, k, subsumption)) {
				//	System.out.println("not kEqual[" + k + "]" + s1 + " and "
				//			+ s2);
					continue;
				}
				//System.out.println("trying to merge " + s1 + " and " + s2);

				Operation operation = new CascadingStateMerge(s1, s2);
				Operation rewindOperation = g.apply(operation);
				
				// if we have invariants and broke them, undo the merge and try
				// again
				if (invariants != null) {
					if (!invariants.check(g)) {
						GraphVizExporter.quickExport("output/debug/"+merges+"-pre.dot", g);
						g.apply(rewindOperation);
						GraphVizExporter.quickExport("output/debug/"+merges+"-post.dot", g);
						++merges;
						// rewind operations insert the exact same state again, so we can go on.
						continue;
					}
				}
				// we should try to find another merge
				System.out.println("--SUCCESS");
				return true;
			}
		}
		// we could not find a possible merge
		return false;
	}
}
