package tests;

import model.Action;
import model.Graph;
import model.MessageEvent;
import model.PartitionGraph;
import model.export.GraphVizExporter;

import org.junit.Test;

import algorithms.bisim.Bisimulation;


public class SplitTest {
	@Test
	public void test() throws Exception {
		Action a = new Action("a");
		Action A = new Action("A");
		Action B = new Action("B");
		String t = "t";
		MessageEvent a1 = new MessageEvent(a, 1);
		MessageEvent a2 = new MessageEvent(a, 1);
		MessageEvent a3 = new MessageEvent(a, 1);
		MessageEvent a4 = new MessageEvent(a, 1);
		MessageEvent A1 = new MessageEvent(A, 1);
		MessageEvent B1 = new MessageEvent(B, 1);
		a1.addTransition(A1, t);
		a2.addTransition(A1, t);
		a2.addTransition(B1, t);
		B1.addTransition(a3, t);
		a4.addTransition(B1, t);
		Graph<MessageEvent> g = new Graph<MessageEvent>();
		g.add(a1);
		g.addInitial(a1, t);
		g.add(a2);
		g.addInitial(a2, t);
		g.add(a3);
		g.addInitial(a3, t);
		g.add(a4);
		g.addInitial(a4, t);
		g.add(A1);
		g.add(B1);
		GraphVizExporter e = new GraphVizExporter();
		PartitionGraph pg = new PartitionGraph(g, true);
		e.exportAsDotAndPngFast("output/tests/initial.dot", pg);
		Bisimulation.refinePartitions(pg);
		e.exportAsDotAndPngFast("output/tests/final.dot", pg);
	}
}
