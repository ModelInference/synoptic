package tests;

import invariants.TemporalInvariantSet;
import model.Graph;
import model.MessageEvent;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.input.StenningReader;
import algorithms.bisim.Bisimulation;

public class StenningDataLinkTest {
	public static void main(String[] args) throws Exception {
		GraphBuilder b = new GraphBuilder();
		StenningReader<MessageEvent> r = new StenningReader<MessageEvent>(b);
		GraphVizExporter e = new GraphVizExporter();
		// r.readGraphSet("traces/PetersonLeaderElection/generated_traces/5node1seed_withid.trace",
		// 5);
		//r.readGraphSet("traces/PetersonLeaderElection/generated_traces/peterson_trace-more-n5-1-s?.txt", 5);
		r.readGraphSet("traces/StenningDataLink/generated_traces/t-10-0.5-0-s?.txt", 5);
		Graph<MessageEvent> g = b.getRawGraph();
		e.exportAsDotAndPng("output/stenning/initial.dot", g);
		TemporalInvariantSet s = TemporalInvariantSet.computeInvariants(g);
		e.exportAsDotAndPng("output/stenning/invariants.dot", s
				.getInvariantGraph(null));
		e.exportAsDotAndPng("output/stenning/invariants-AP.dot", s
				.getInvariantGraph("AP"));
		e.exportAsDotAndPng("output/stenning/invariants-AFby.dot", s
				.getInvariantGraph("AFby"));
		e.exportAsDotAndPng("output/stenning/invariants-NFby.dot", s
				.getInvariantGraph("NFby"));
		System.out.println(s);
		PartitionGraph pg = new PartitionGraph(g, true);
		e.exportAsDotAndPng("output/stenning/initial-pg.dot", pg);
		Bisimulation.refinePartitions(pg);
		e.exportAsDotAndPng("output/stenning/output-pg.dot", pg);
		Bisimulation.mergePartitions(pg);
		System.out.println("Merge done.");
		e.exportAsDotAndPng("output/stenning/output-pg-merged.dot", pg);
	}
}
