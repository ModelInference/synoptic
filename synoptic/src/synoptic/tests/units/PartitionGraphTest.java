package synoptic.tests.units;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.graph.IOperation;
import synoptic.algorithms.graph.PartitionMerge;
import synoptic.algorithms.graph.PartitionSplit;
import synoptic.main.ParseException;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;

public class PartitionGraphTest extends SynopticTest {
    private GraphVizExporter exporter;
    private PartitionGraph pg;
    private PartitionGraph pgSingle;

    private void print(String name, PartitionGraph pg) {
        try {
            // exporter.debugExportAsDotAndPng("output/synoptic.tests/" + name +
            // ".dot", pg);
            exporter.exportAsDotAndPngFast("output/synoptic.tests/" + name
                    + ".dot", pg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUp() throws ParseException {
        super.setUp();
        exporter = new GraphVizExporter();
        pg = createGraph();
        print("1-PartitionGraphTestInitial", pg);

        pgSingle = createSingleGraph();
        print("1-PartitionGraphTestInitialSingle", pgSingle);
    }

    private PartitionGraph createGraph() throws InternalSynopticException,
            ParseException {
        String[] events = new String[] { "A", "B", "--", "A", "B", "--", "C",
                "D", "--", "C", "D" };
        Graph<LogEvent> graph = TemporalInvariantSetTests
                .genInitialGraph(events);
        return new PartitionGraph(graph, true);
    }

    private PartitionGraph createSingleGraph()
            throws InternalSynopticException, ParseException {
        String[] events = new String[] { "A", "B", "--", "C", "D", };
        // TODO: insertAfter("A", new Action("D"));
        Graph<LogEvent> graph = TemporalInvariantSetTests
                .genInitialGraph(events);
        return new PartitionGraph(graph, false);
    }

    @Test
    public void testMergePartitionTop() {
        Iterator<Partition> iter = pg.getInitialNodes().iterator();
        IOperation rewind = pg.apply(new PartitionMerge(iter.next(), iter
                .next()));
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
        IOperation rewind = pg.apply(new PartitionMerge(iter.next(), iter
                .next()));
        print("mergePartitionBottom", pg);
        pg.apply(rewind);
        print("mergePartitionBottomRewound", pg);
    }

    @Test
    public void testSplitPartitionTop() {
        Partition splitNode = pg.getInitialNodes().iterator().next();
        PartitionSplit split = new PartitionSplit(splitNode);
        Iterator<LogEvent> m = splitNode.getMessages().iterator();
        split.addEventToSplit(m.next());
        // split.addFulfillsNot(m.next());
        IOperation rewind = pg.apply(split);
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
        Iterator<LogEvent> m = splitNode.getMessages().iterator();
        PartitionSplit split = new PartitionSplit(splitNode);
        split.addEventToSplit(m.next());
        // split.addFulfillsNot(m.next());
        IOperation rewind = pg.apply(split);
        print("splitPartitionBottom", pg);
        pg.apply(rewind);
        print("splitPartitionBottomRewound", pg);
    }

    private <T extends INode<T>> T getNodeByName(IGraph<T> g, String nodeName) {
        for (T node : g.getNodes()) {
            if (node.getLabel().equals(nodeName)) {
                return node;
            }
        }
        return null;
    }
}
