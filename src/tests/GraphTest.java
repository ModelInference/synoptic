package tests;

import java.util.Collections;

import algorithms.graph.GraphMerge;
import algorithms.ktail.InputEquivalence;
import junit.framework.TestCase;
import model.Action;
import model.Graph;
import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.interfaces.IGraph;
import model.scalability.ScalableGraph;

public class GraphTest extends TestCase {
	public void testGraph() {
		SystemState<MessageEvent> s3prime = new SystemState<MessageEvent>("s3");
		SystemState<MessageEvent> s2prime = new SystemState<MessageEvent>("s2");
		SystemState<MessageEvent> s1prime = new SystemState<MessageEvent>("s1");
		s1prime.addSuccessorProvider(GraphBuilder.makeSuccessorProvider(Collections.singleton(new MessageEvent(new Action("foo"), s1prime, s2prime, 1))));
		s2prime.addSuccessorProvider(GraphBuilder.makeSuccessorProvider(Collections.singleton(new MessageEvent(new Action("baz"), s2prime, s3prime, 1))));

		Graph<SystemState<MessageEvent>> g = new Graph<SystemState<MessageEvent>>();
		g.addInitial(s1prime, new Action("t"));
		GraphVizExporter export = new GraphVizExporter();
		String text = export.export(g);
		System.out.println(text);
	}

	public void testIsInputEquivalent() {
		// The simple case
		String[] trace1 = new String[] { "foo", "bar", "baz" };
		String[] trace2 = new String[] { "foo", "bar", "baz" };
		String[][] traces = new String[][] { trace1, trace2 };
		PartitionGraph g = GraphBuilder.buildGraph(traces);

		SystemState<Partition>[] initialStates = g.getSystemStateGraph()
				.getInitialNodes().toArray(new SystemState[] {});
		assertTrue(InputEquivalence.isInputEquivalent(initialStates[0],
				initialStates[1]));

		// The simple negative case
		trace2 = new String[] { "foo", "bar", "NO" };
		traces = new String[][] { trace1, trace2 };
		g = GraphBuilder.buildGraph(traces);

		initialStates = g.getSystemStateGraph().getInitialNodes().toArray(
				new SystemState[] {});
		assertFalse(InputEquivalence.isInputEquivalent(initialStates[0],
				initialStates[1]));

		// The length-negative case
		trace2 = new String[] { "foo", "bar" };
		traces = new String[][] { trace1, trace2 };
		g = GraphBuilder.buildGraph(traces);

		initialStates = g.getSystemStateGraph().getInitialNodes().toArray(
				new SystemState[] {});
		assertFalse(InputEquivalence.isInputEquivalent(initialStates[0],
				initialStates[1]));
	}

	public void testMergeInputEquivalentTrace() {
		// The simple case
		String[] trace1 = new String[] { "foo", "bar", "baz" };
		String[] trace2 = new String[] { "foo", "bar", "baz" };
		String[][] traces = new String[][] { trace1, trace2 };
		PartitionGraph g = GraphBuilder.buildGraph(traces);

		SystemState<Partition>[] initialStates = g.getSystemStateGraph()
				.getInitialNodes().toArray(new SystemState[] {});
		GraphVizExporter.quickExport("output/debug/test.dot", g
				.getSystemStateGraph());
		assertEquals(8, g.getSystemStateGraph().getNodes().size());
		try {
		InputEquivalence.mergeTrace(g, initialStates[0], initialStates[1]);
		} finally {
		GraphVizExporter.quickExport("output/debug/test2.dot", g
				.getSystemStateGraph());
		}

		assertEquals(4, g.getSystemStateGraph().getNodes().size());
	}

	public void testMergeInputEquivalentTraces() {
		// The simple case
		String[] trace1 = new String[] { "foo", "bar", "baz" };
		String[] trace2 = new String[] { "foo", "bar", "baz" };
		String[][] traces = new String[][] { trace1, trace2 };
		PartitionGraph pg = GraphBuilder.buildGraph(traces);
		IGraph<SystemState<Partition>> g = pg.getSystemStateGraph();

		assertEquals(8, g.getNodes().size());
		InputEquivalence.mergeTraces(pg);

		assertEquals(4, g.getNodes().size());
	}

	public void testMergeGraphs() {
		String[] trace1 = new String[] { "foo", "bar", "baz" };
		String[] trace2 = new String[] { "red", "green", "blue" };
		String[][] traces = new String[][] { trace1 };
		PartitionGraph g1 = GraphBuilder.buildGraph(traces);

		String[][] traces2 = new String[][] { trace2 };
		PartitionGraph g2 = GraphBuilder.buildGraph(traces2);

		assertEquals(4, g1.getSystemStateGraph().getNodes().size());
		assertEquals(4, g2.getSystemStateGraph().getNodes().size());

		g1.apply(new GraphMerge(g2));
		assertEquals(8, g1.getSystemStateGraph().getNodes().size());

		GraphVizExporter gve = new GraphVizExporter();
		System.out.println(gve.export(g1.getSystemStateGraph()));

	}

	public void testScalable() {
		String[] trace1 = new String[] { "woo", "foo", "bar", "baz", "bar",
				"baz" };
		String[] trace2 = new String[] { "wee", "foo", "green", "blue",
				"green", "blue", "green", "blue", };
		String[][] traces = new String[][] { trace1 };
		PartitionGraph g1 = GraphBuilder.buildGraph(traces);

		String[][] traces2 = new String[][] { trace2 };
		PartitionGraph g2 = GraphBuilder.buildGraph(traces2);
		ScalableGraph sg = new ScalableGraph();
		sg.addGraph(g1);
		sg.addGraph(g2);
		PartitionGraph sgr = sg.kReduce(1, true, false);

		GraphVizExporter gve = new GraphVizExporter();
		System.out.println(gve.export(sgr.getSystemStateGraph()));

	}
}
