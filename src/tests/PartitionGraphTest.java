package tests;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import model.Action;
import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.interfaces.IGraph;
import model.interfaces.INode;

import org.junit.Before;
import org.junit.Test;

import algorithms.graph.Operation;
import algorithms.graph.PartitionMerge;
import algorithms.graph.PartitionSplit;

public class PartitionGraphTest {
	private GraphVizExporter exporter;
	private PartitionGraph pg;
	private PartitionGraph pgSingle;

	private void print(String name, PartitionGraph pg) {
		try {
			//exporter.debugExportAsDotAndPng("output/tests/" + name + ".dot", pg);
			exporter.exportAsDotAndPngFast("output/tests/" + name + ".dot", pg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Before
	public void setUp() throws Exception {
		exporter = new GraphVizExporter();
		pg = createGraph();
		print("1-PartitionGraphTestInitial", pg);
		
		pgSingle = createSingleGraph();
		print("1-PartitionGraphTestInitialSingle", pgSingle);
	}

	private PartitionGraph createGraph() {
		GraphBuilder gb = new GraphBuilder();
		gb.append(new Action("A"));
		gb.append(new Action("B"));
		gb.split();
		gb.append(new Action("A"));
		gb.append(new Action("B"));
		gb.split();
		gb.append(new Action("C"));
		gb.append(new Action("D"));
		gb.split();
		gb.append(new Action("C"));
		gb.append(new Action("D"));
		
		return gb.getGraph(true);
	}
	
	private PartitionGraph createSingleGraph() {
		GraphBuilder gb = new GraphBuilder();
		MessageEvent m = gb.append(new Action("A"));
		gb.append(new Action("B"));
		gb.split();
		gb.append(new Action("C"));
		gb.append(new Action("D"));
		
		gb.insertAfter(m, new Action("D"));
		
		return gb.getGraph(false);
	}

	@Test
	public void testMergePartitionTop() {
		Iterator<Partition> iter = pg.getInitialNodes().iterator();
		Operation rewind = pg.apply(new PartitionMerge(iter.next(), iter.next()));
		print("mergePartitionTop", pg);
		pg.apply(rewind);
		print("mergePartitionTopRewound", pg);
	}

	@Test
	public void testMergePartitionBottom() {
		Set<Partition> set = new HashSet<Partition>();
		set.addAll(pg.getNodes());
		set.removeAll(pg.getInitialNodes());
		Iterator<Partition> iter = set.iterator();
		Operation rewind = pg.apply(new PartitionMerge(iter.next(), iter.next()));
		print("mergePartitionBottom", pg);
		pg.apply(rewind);
		print("mergePartitionBottomRewound", pg);
	}
	
	@Test
	public void testSplitPartitionTop() {
		Partition splitNode = pg.getInitialNodes().iterator().next();
		PartitionSplit split = new PartitionSplit(splitNode);
		Iterator<MessageEvent> m = splitNode.getMessages().iterator();
		split.addFulfills(m.next());
		//split.addFulfillsNot(m.next());
		Operation rewind = pg.apply(split);
		print("splitPartitionTop", pg);
		pg.apply(rewind);
		print("splitPartitionTopRewound", pg);
	}
	
	@Test
	public void testSplitPartitionBottom() {
		Set<Partition> set = new HashSet<Partition>();
		set.addAll(pg.getNodes());
		set.removeAll(pg.getInitialNodes());
		Partition splitNode = set.iterator().next();
		Iterator<MessageEvent> m = splitNode.getMessages().iterator();
		PartitionSplit split = new PartitionSplit(splitNode);
		split.addFulfills(m.next());
		//split.addFulfillsNot(m.next());
		Operation rewind = pg.apply(split);
		print("splitPartitionBottom", pg);
		pg.apply(rewind);
		print("splitPartitionBottomRewound", pg);
	}
	
	private <T extends INode<T>> T getNodeByName(IGraph<T> g, String nodeName) {
		for (T node : g.getNodes()) {
			if (node.getLabel().equals(nodeName))
				return node;
		}
		return null;
	}
}

