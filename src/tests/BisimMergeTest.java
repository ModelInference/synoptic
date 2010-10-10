package tests;

import java.io.File;

import benchmarks.TimedTask;


import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.interfaces.IGraph;
import algorithms.bisim.Bisimulation;

public class BisimMergeTest {
	private static final int LOOPS = 20;

	public static void main(String[] args) throws Exception {
		GraphVizExporter exporter = new GraphVizExporter();
		GraphBuilder gb = new GraphBuilder();
		String[] trace1 = new String[] { "p", "p", "c", "c", "txc", "txc", };
		String[] trace2 = new String[] { "p", "p", "c", "a", "txa", "txa", };
		String[] trace3 = new String[] { "p", "p", "a", "c", "txa", "txa", };
		String[] trace4 = new String[] { "p", "p", "a", "a", "txa", "txa", };
		// the graph will contain each trace as separate component
		gb.buildGraphLocal(new String[][] { trace1, trace2, trace3, trace4 });
		exporter.exportAsDotAndPng("output/twoc/dot", gb.getRawGraph());
		TimedTask benchmark = new TimedTask("time");
		PartitionGraph pg = null;
		for (int i = 0; i < LOOPS; ++i) {
			pg = new PartitionGraph(gb.getRawGraph(), true);
			Bisimulation.refinePartitions(pg);
			// This is explicit for mergePartitions(pg)
			Bisimulation.mergePartitions(pg, pg.getInvariants(), 0);
		}
		benchmark.stop();
		exporter.exportAsDotAndPng("foo.dot", pg);
		System.out.println("done.");
		System.out.println(pg.getNodes().size());
		System.out.println(benchmark.getTime()/LOOPS);
	}
}
