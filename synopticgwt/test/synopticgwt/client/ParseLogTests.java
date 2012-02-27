package synopticgwt.client;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTNode;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.GWTParseException;
import synopticgwt.shared.GWTSynOpts;

/**
 * Tests the parseLog RPC.
 */
public class ParseLogTests extends SynopticGWTTestCase {

    /**
     * A test to parse a simple log without any exceptions.
     */
    @Test
    public void testSimpleSuccessParseLog() {
        String logLines = "event";
        LinkedList<String> regExps = new LinkedList<String>();
        regExps.add("(?<TYPE>)");
        String partitionRegExp = "";
        String separatorRegExp = "";

        GWTSynOpts synOpts = new GWTSynOpts(logLines, regExps, partitionRegExp,
                separatorRegExp, false, false, false);
        service.parseLog(synOpts,
                new AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        fail(caught.toString());
                    }

                    @SuppressWarnings("synthetic-access")
                    @Override
                    public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> ret) {
                        // 1. Check the invariants.
                        GWTInvariantSet invs = ret.getLeft();

                        Set<String> itypes = invs.getInvTypes();
                        assertEquals(itypes.size(), 2);
                        assertTrue(itypes.contains(new String("AFby")));
                        assertTrue(itypes.contains(new String("NFby")));

                        List<GWTInvariant> afby = invs.getInvs("AFby");
                        assertEquals(afby.size(), 1);
                        assertEquals(afby.get(0).getSource(), "INITIAL");
                        assertEquals(afby.get(0).getTarget(), "event");

                        // 2. Check the initial model.
                        GWTGraph graph = ret.getRight();

                        HashSet<GWTNode> nodes = graph.getNodes();
                        assertEquals(nodes.size(), 3);
                        GWTNode initial, event, terminal;
                        initial = event = terminal = null;
                        for (GWTNode node : nodes) {
                            if (node.getEventType().equals("INITIAL")) {
                                initial = node;
                            }
                            if (node.getEventType().equals("event")) {
                                event = node;
                            }
                            if (node.getEventType().equals("TERMINAL")) {
                                terminal = node;
                            }
                        }
                        assertTrue(initial != null);
                        assertTrue(event != null);
                        assertTrue(terminal != null);

                        List<GWTEdge> edges = graph.getEdges();
                        GWTEdge e1 = new GWTEdge(initial, event, 1, 1);
                        GWTEdge e2 = new GWTEdge(event, terminal, 1, 1);
                        assertEquals(edges.size(), 2);
                        assertTrue(edges.contains(e1));
                        assertTrue(edges.contains(e2));

                        // Declare the test as complete.
                        finishTest();
                    }
                });

        delayTestFinish(testFinishDelay);
    }

    /**
     * A test to parse a simple log with an expected failure.
     */
    @Test
    public void testSimpleFailParseLog() {
        String logLines = "event";
        LinkedList<String> regExps = new LinkedList<String>();
        final String failingRegExp = "blank-on-purpose";
        regExps.add(failingRegExp);
        String partitionRegExp = "";
        String separatorRegExp = "";
        GWTSynOpts synOpts = new GWTSynOpts(logLines, regExps, partitionRegExp,
                separatorRegExp, false, false, false);
        service.parseLog(synOpts,
                new AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>>() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public void onFailure(Throwable caught) {
                        if (!(caught instanceof GWTParseException)) {
                            fail("Unexpected exception type.");
                        }
                        GWTParseException e = (GWTParseException) caught;
                        assertEquals(e.getRegex(), failingRegExp);
                        assertEquals(e.getLogLine(), null);

                        // Declare the test as complete.
                        finishTest();
                    }

                    @Override
                    public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> ret) {
                        fail("Unexpected RPC success.");
                    }
                });

        delayTestFinish(testFinishDelay);
    }
}
