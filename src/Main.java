import invariants.TemporalInvariantSet;

import java.io.File;

import algorithms.bisim.Bisimulation;

import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;

import trace.ProtoTrace.Trace;

public class Main {
	static public void main(String[] args) throws Exception {
		Trace t = trace.Reader.loadTrace("traces/ping_pong.trace");
		GraphBuilder gb = new GraphBuilder();
		gb.buildGraph(t, 1);
		PartitionGraph g = gb.getGraph(false);
		TemporalInvariantSet set = TemporalInvariantSet.computeInvariants(g);
		GraphVizExporter ge = new GraphVizExporter();
		File exportFile = new File("output/myGraph.dot");
		ge.export(exportFile, g);
		ge.exportPng(exportFile);
		Bisimulation.refinePartitions(g);
		File minFile = new File("output/myGraphMin.dot");

		ge.export(minFile, g);
		ge.exportPng(minFile);

		System.out.println(set);
		System.out.println("Check on minimized " + set.check(g) + ": "
				+ set.getUnsatisfiedInvariants(g));
	}
}
