package trace.twitter;

import java.io.File;
import java.io.FileInputStream;

import algorithms.bisim.Bisimulation;
import algorithms.ktail.KTail;

import trace.MessageTrace.TraceSet;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;

public class CreateTwitGraph {
	public static void main(String[] args) throws Exception {
		GraphVizExporter exporter = new GraphVizExporter();
		TraceSet tr = TraceSet.parseFrom(new FileInputStream(
				"src/trace/twitter/TwitTrace2.trace"));
		PartitionGraph org = GraphBuilder.buildGraph(tr);
		File file = new File("output/TwitTrace2.dot");
		exporter.export(file, org);
		exporter.exportPng(file);

		Bisimulation.refinePartitions(org);

		File file2 = new File("output/TwitTrace2-bisim.dot");
		exporter.export(file2, org);
		exporter.exportPng(file2);

		org = GraphBuilder.buildGraph(tr);
		KTail.kReduce(org, 1, true, true);

		File file3 = new File("output/TwitTrace2-gktail.dot");
		exporter.export(file3, org);
		exporter.exportPng(file3);
	}

}
