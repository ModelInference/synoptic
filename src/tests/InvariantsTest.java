package tests;

import java.io.File;
import algorithms.bisim.Bisimulation;

import model.PartitionGraph;
import model.export.GraphVizExporter;

public class InvariantsTest {

	public static void main(String[] args) throws Exception {
		GraphVizExporter exporter = new GraphVizExporter();
		PartitionGraph t2 = StateTest.traceTwoPhaseCommit();
		File file = new File("output/twoc.dot");
		exporter.export(file, t2);
		exporter.exportPng(file);
		// InvariantSet set1 = InvariantSet.computeInvariants(t2);
		// System.out.println(set1);

		//		
		// PartitionGraph pt2 = new PartitionGraph(t2);
		// File file3 = new File("output/twoc-part.dot");
		// exporter.export(file3, pt2);
		// exporter.exportPng(file3);
		// t2.convertTransitionsToStates();
		// GraphLTLChecker c = new GraphLTLChecker();
		// Counterexample ce = c.check(t2, "[](did(a)->[](can(txc)))", null);
		// if (ce != null)
		// System.out.println(c.convertCounterexample(ce));
		// else
		// System.out.println("no counterexample found");
		// InvariantSet set2 = InvariantSet.computeInvariantsOld(t2);
		// System.out.println(set2);
		// if (set1.sameInvariants(set2))
		// System.out.println("sane.");
		// Trace tr = trace.Reader.loadTrace("traces/pingPong.trace");
		// GraphBuilder gb = new GraphBuilder("myGraph");
		// gb.buildGraph(tr, 1);
		// Graph t = gb.getGraph();

		// t.closeUnderTransitivity();
		Bisimulation.refinePartitions(t2);
		// File file2 = new File("output/twoc-min.dot");
		// exporter.export(file2, min);
		// exporter.exportPng(file2);
		// min.convertTransitionsToStates();
		// File file2 = new File("output/twoc-mt.dot");
		// exporter.exportMessagesAsTransitions(file2, min);
		// exporter.exportPng(file2);
		// exporter.exportPng(file);
		// System.out.println("Wrote " + file);
		// InvariantSet set3 = Invariants.computeInvariants(t);
		// System.out.println(set3);
		// InvariantSet set2 = InvariantSet.computeInvariants(t);
		// System.out.println(set2);
		// System.out.println("Done invarinants.");
		// exporter.export(new File("ping-invariants.dot"), invariants);
		// exporter.exportPng(new File("ping-invariants.dot"));
		// System.out.println(invariants.toLTLFormulas_Preceded());
		// System.out.println("Wrote ping-invariants.dot.");
		System.out.println("done.");
	}
}
