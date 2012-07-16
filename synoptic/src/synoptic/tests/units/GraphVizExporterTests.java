package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.model.export.GraphExporter;
import synoptic.tests.SynopticTest;

public class GraphVizExporterTests extends SynopticTest {

    @Override
    public void setUp() throws ParseException {
        super.setUp();
    }

    /**
     * Returns a canonical dot-format string representation of a graph that
     * encodes a path of events.
     * 
     * @param events
     *            The input event sequence
     * @return Dot-formatted representation of the event sequence graph.
     */
    public String getExportedPathGraph(String[] events) {
        List<EventNode> path = getLogEventPath(events);
        ChainsTraceGraph g = new ChainsTraceGraph();

        // Randomize the order in which we add events to the graph
        List<EventNode> pathCopy = new ArrayList<EventNode>();
        pathCopy.addAll(path);
        Collections.shuffle(pathCopy, SynopticMain.getInstanceWithExistenceCheck().random);
        for (EventNode event : pathCopy) {
            g.add(event);
        }

        g.tagInitial(path.get(0), Event.defTimeRelationStr);

        for (int i = 0; i < path.size() - 1; i++) {
            EventNode event = path.get(i);
            event.addTransition(path.get(i + 1),
                    Event.defTimeRelationStr);
        }

        StringWriter writer = new StringWriter();
        try {
            GraphExporter.exportGraph(writer, g, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    /**
     * Make sure that canonical exporting results in same output for graphs
     * constructed from different nodes and in different orders.
     */
    @Test
    public void canonicalExportTest() {
        String gStr1 = getExportedPathGraph(new String[] { "a", "b", "c" });
        String gStr2 = getExportedPathGraph(new String[] { "a", "b", "c" });
        logger.fine(gStr1);
        logger.fine(gStr2);
        assertTrue(gStr1.equals(gStr2));

        // TODO: expand this to more complex graph topologies.
    }
}
