package synoptic.tests.units;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.main.Main;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.MessageEvent;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphVizExporter;

public class SplitTest {
	
	@Before
	public void setUp() {
		int randomSeed = 0;
		Main.random = new Random(randomSeed);
	}
	
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
		PartitionGraph pg = new PartitionGraph(g, true);
		Bisimulation.splitPartitions(pg);
		// TODO: test the resulting graph
		fail("TODO");
	}
}
