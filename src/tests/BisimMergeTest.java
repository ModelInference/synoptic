package tests;

import java.io.File;

import model.export.GraphVizExporter;
import model.interfaces.IGraph;
import algorithms.bisim.Bisimulation;

public class BisimMergeTest {
	public static void main(String[] args) throws Exception {
		GraphVizExporter exporter = new GraphVizExporter();
		IGraph t2 = StateTest.traceTwoPhaseCommit();
		File file = new File("output/twoc.dot");
		exporter.export(file, t2);
		exporter.exportPng(file);
		IGraph min = Bisimulation.getMergedSystem(t2);
		System.out.println("done.");
	}
}
