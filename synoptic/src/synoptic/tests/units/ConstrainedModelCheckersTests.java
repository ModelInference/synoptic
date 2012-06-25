package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.constraints.ConstrainedTemporalInvariantSet;
import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.options.SynopticOptions;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;
import synoptic.util.time.ITotalTime;

/**
 * Tests for constrained model checker.
 */
public class ConstrainedModelCheckersTests extends SynopticTest {
	/**
     * Test that the graph g generates or not (depending on the value of
     * cExampleExists) a counter-example for invariant inv, which is exactly the
     * expectedPath through the graph g.
     */
    @SuppressWarnings("null")
    private static <T extends INode<T>> void testCExamplePath(IGraph<T> g,
            ITemporalInvariant inv, boolean cExampleExists, List<T> expectedPath)
            throws InternalSynopticException {

        ConstrainedTemporalInvariantSet invs = new ConstrainedTemporalInvariantSet();
        invs.add(inv);
        System.out.println(g.getNodes());
        
        List<CExamplePath<T>> cexamples = invs.getAllCounterExamples(g);

        if (cexamples != null) {
            logger.info("model-checker counter-example:"
                    + cexamples.get(0).path);
        }

        if (!cExampleExists) {
            assertTrue(cexamples == null);
            return;
        }

        // Else, there should be just one counter-example
        assertTrue(cexamples != null);
        assertTrue(cexamples.size() == 1);
        List<T> cexamplePath = cexamples.get(0).path;

        // logger.info("model-checker counter-example:" + cexamplePath);
        logger.info("correct counter-example:" + expectedPath);

        // Check that the counter-example is of the right length.
        assertTrue(cexamplePath.size() == expectedPath.size());

        // Check that cexamplePath is exactly the expectedPath
        for (int i = 0; i < cexamplePath.size(); i++) {
            assertTrue(cexamplePath.get(i) == expectedPath.get(i));
        }
        return;
    }
    
    /**
     * Test that the list of events representing a linear graph generates or not
     * (depending on value of cExampleExists) a single counter-example for
     * invariant inv that includes the prefix of linear graph of length up to
     * cExampleIndex (which starts counting at 0 = INITIAL, and may index
     * TERMINAL).
     */
    private void testLinearGraphCExample(String[] events,
            ITemporalInvariant inv, boolean cExampleExists,
            int lastCExampleIndex) throws InternalSynopticException,
            ParseException {
    	
    	TraceParser parser = new TraceParser();
    	parser.addRegex("^(?<TYPE>)(?<TIME>)$");
     	parser.addPartitionsSeparator("^--$");
    	
        // Create the graph.
        ChainsTraceGraph g = (ChainsTraceGraph) genChainsTraceGraph(events, parser);
        
        if (!cExampleExists) {
            // Don't bother constructing the counter-example path.
            testCExamplePath(g, inv, cExampleExists, null);
            return;
        }

        // Build the expectedPath by traversing the entire graph.
        LinkedList<EventNode> expectedPath = new LinkedList<EventNode>();
        EventNode nextNode = g.getDummyInitialNode();
        expectedPath.add(nextNode);
        for (int i = 1; i <= lastCExampleIndex; i++) {
            nextNode = nextNode.getAllTransitions().get(0).getTarget();
            expectedPath.add(nextNode);
        }
        testCExamplePath(g, inv, cExampleExists, expectedPath);
    }
    
    /**
     * The list of partially ordered events is condensed into a partition graph
     * (the most compressed model). This graph is then checked for existence or
     * not (depending on value of cExampleExists) of a counter-example for
     * invariant inv specified by cExampleLabels. The format for each event
     * string in the events array (?<TYPE>) with "^--$" as the partitions
     * separator; the format for each element in the counter-example path is
     * (?<TYPE>). <br />
     * <br/>
     * NOTE: We get away with just TYPE for specifying the counter-example
     * because we will deal with the initial partition graph -- where there is
     * exactly one node for each event type. <br />
     * <br />
     * NOTE: INITIAL is always included, therefore cExampleLabels should not
     * include it. However, if TERMINAL is to be included, it should be
     * specified in cExampleLabels.
     * 
     * @throws Exception
     */
    private static void testPartitionGraphCExample(String[] events,
            ITemporalInvariant inv, boolean cExampleExists,
            List<EventType> cExampleLabels) throws Exception {

        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<TYPE>)(?<TIME>)$");
        parser.addPartitionsSeparator("^--$");
        PartitionGraph pGraph = genInitialPartitionGraph(events, parser,
                new TransitiveClosureInvMiner(), false);

        exportTestGraph(pGraph, 1);

        if (!cExampleExists) {
            // If there no cExample then there's no reason to build a path.
            testCExamplePath(pGraph, inv, cExampleExists, null);
            return;
        }

        LinkedList<Partition> expectedPath = new LinkedList<Partition>();
        Partition nextNode = pGraph.getDummyInitialNode();

        // Build the expectedPath by traversing the graph, starting from the
        // initial node by finding the appropriate partition at each hop by
        // matching on the label of each partition.
        expectedPath.add(nextNode);
        nextCExampleHop:
        for (int i = 0; i < cExampleLabels.size(); i++) {
            EventType nextLabel = cExampleLabels.get(i);
            for (ITransition<Partition> transition : nextNode
                    .getAllTransitions()) {
                for (EventNode event : transition.getTarget().getEventNodes()) {
                    if (event.getEType().equals(nextLabel)) {
                        nextNode = transition.getTarget();
                        expectedPath.add(nextNode);
                        continue nextCExampleHop;
                    }
                }
            }
            Assert.fail("Unable to locate transition from "
                    + nextNode.toString() + " to a partition with label "
                    + nextLabel.toString());
        }
        testCExamplePath(pGraph, inv, cExampleExists, expectedPath);
    }
    
    // //////////////////////////// AFby:
    
    /**
     * Tests that a linear graph with a cycle does _not_ generate an AFby
     * c-example. This demonstrates why we need "<> TERMINAL ->" as the prefix
     * in the AFby LTL formula -- without this prefix this tests fails.
     * 
     * @throws Exception
     */
    @Test
    public void NoAFbyLinearGraphWithCycleTest() throws Exception {
    	SynopticOptions.enablePerfDebugging = true;
    	
        String[] events = new String[] { "x 1", "a 2", "c 3", "x 5", "a 7", "y 8", "b 10", "w 12" };

        AlwaysFollowedInvariant inv = new AlwaysFollowedInvariant(
                new StringEventType("a"), new StringEventType("b"),
                Event.defTimeRelationStr);
        
        IThresholdConstraint threshold = new UpperBoundConstraint(new ITotalTime(30));
        
        ITemporalInvariant constrInv = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
        		inv, threshold);

        testPartitionGraphCExample(events, constrInv, false, null);
    }
    
    /**
     * Tests that a linear graph with a cycle does generate an AFby c-example.
     * This tests the LTL formula that includes an "eventually TERMINAL" clause
     * to permit only those counter-examples that reach the TERMINAL node.
     * 
     * @throws Exception
     */
    @Test
    public void AFbyLinearGraphWithCycleTest() throws Exception {
    	SynopticOptions.enablePerfDebugging = true;
    	
        String[] events = new String[] { "x 1", "a 2", "b 3", "x 4", "a 5", "y 6", "w 7",
                "--", "x 10", "a 11", "y 13", "w 14" };

        AlwaysFollowedInvariant inv = new AlwaysFollowedInvariant(
                new StringEventType("a"), new StringEventType("b"),
                Event.defTimeRelationStr);
        
        IThresholdConstraint threshold = new UpperBoundConstraint(new ITotalTime(30));

        ITemporalInvariant constrInv = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
        		inv, threshold);
        
        List<EventType> cExampleLabels = stringsToStringEventTypes(new String[] {
                "x", "a", "y", "w" });
        
        cExampleLabels.add(StringEventType.newTerminalStringEventType());
        testPartitionGraphCExample(events, constrInv, true, cExampleLabels);
    }
    
    /**
     * Tests that a linear graph does not generate an AFby c-example.
     * 
     * @throws InternalSynopticException
     * @throws ParseException
     */
    @Test
    public void NoAFbyLinearGraphTest() throws InternalSynopticException,
            ParseException {
    	SynopticOptions.enablePerfDebugging = true;
    	
        // logger.info("Using the FSMChecker: " + Main.useFSMChecker);
        String[] events = new String[] { "a 1", "x 2", "y 3", "b 4" };
        
        AlwaysFollowedInvariant inv = new AlwaysFollowedInvariant(
                new StringEventType("a"), new StringEventType("b"),
                Event.defTimeRelationStr);
        
        IThresholdConstraint threshold = new LowerBoundConstraint(new ITotalTime(2));
        
        ITemporalInvariant constrInv = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
        		inv, threshold);

        testLinearGraphCExample(events, constrInv, false, 0);
    }
    
    /**
     * Tests that a linear graph does generate an AFby c-example.
     * 
     * @throws InternalSynopticException
     * @throws ParseException
     */
    @Test
    public void AFbyLinearGraphTest() throws InternalSynopticException,
            ParseException {
    	SynopticOptions.enablePerfDebugging = true;
    	
        // logger.info("Using the FSMChecker: " + Main.useFSMChecker);
        String[] events = new String[] { "a 1", "x 2", "y 3", "z 4" };
        
        AlwaysFollowedInvariant inv = new AlwaysFollowedInvariant(
                new StringEventType("a"), new StringEventType("b"),
                Event.defTimeRelationStr);
        
        IThresholdConstraint threshold = new LowerBoundConstraint(new ITotalTime(2));
        
        ITemporalInvariant constrInv = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
        		inv, threshold);
        
        testLinearGraphCExample(events, constrInv, true, 5);
    }
    
    // //////////////////////////// AP:

    /**
     * Tests that a linear graph with a cycle does not generate an AP c-example.
     * 
     * @throws Exception
     */
    @Test
    public void NoAPLinearGraphWithCycleTest() throws Exception {
    	SynopticOptions.enablePerfDebugging = true;
    	
        String[] events = new String[] { "a 1", "c 2", "a 3", "b 4" };

        AlwaysPrecedesInvariant inv = new AlwaysPrecedesInvariant(
                new StringEventType("a"), new StringEventType("b"),
                Event.defTimeRelationStr);
        
        IThresholdConstraint threshold = new UpperBoundConstraint(new ITotalTime(10));
        
        ITemporalInvariant constrInv = new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
        		inv, threshold);

        testPartitionGraphCExample(events, constrInv, false, null);
    }

    /**
     * Tests that a linear graph with a cycle does generate an AP c-example.
     * 
     * @throws Exception
     */
    @Test
    public void APLinearGraphWithCycleTest() throws Exception {
    	SynopticOptions.enablePerfDebugging = true;

        String[] events = new String[] { "z 1", "x 2", "z 3", "b 4" };

        AlwaysPrecedesInvariant inv = new AlwaysPrecedesInvariant(
                new StringEventType("a"), new StringEventType("b"),
                Event.defTimeRelationStr);
        
        IThresholdConstraint threshold = new UpperBoundConstraint(new ITotalTime(10));
        
        ITemporalInvariant constrInv = new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
        		inv, threshold);
        
        List<EventType> cExampleLabels = stringsToStringEventTypes(new String[] {
                "z", "b" });
        testPartitionGraphCExample(events, constrInv, true, cExampleLabels);
    }

    /**
     * Tests that a linear graph does not generate an AP c-example.
     * 
     * @throws InternalSynopticException
     * @throws ParseException
     */
    @Test
    public void NoAPLinearGraphTest() throws InternalSynopticException,
            ParseException {
    	SynopticOptions.enablePerfDebugging = true;

        // logger.info("Using the FSMChecker: " + Main.useFSMChecker);
        String[] events = new String[] { "x 1", "a 2", "x 3", "y 4", "b 5" };
        
        AlwaysPrecedesInvariant inv = new AlwaysPrecedesInvariant(
                new StringEventType("a"), new StringEventType("b"),
                Event.defTimeRelationStr);
        
        IThresholdConstraint threshold = new UpperBoundConstraint(new ITotalTime(10));
        
        ITemporalInvariant constrInv = new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
        		inv, threshold);
       
        testLinearGraphCExample(events, constrInv, false, 0);
    }

    /**
     * Tests that a linear graph does generate an AP c-example.
     * 
     * @throws InternalSynopticException
     * @throws ParseException
     */
    @Test
    public void APLinearGraphTest() throws InternalSynopticException,
            ParseException {
    	SynopticOptions.enablePerfDebugging = true;

        // logger.info("Using the FSMChecker: " + Main.useFSMChecker);
        String[] events = new String[] { "x 1", "y 2", "z 3", "b 4", "a 5" };
        
        AlwaysPrecedesInvariant inv = new AlwaysPrecedesInvariant(
                new StringEventType("a"), new StringEventType("b"),
                Event.defTimeRelationStr);
        
        IThresholdConstraint threshold = new LowerBoundConstraint(new ITotalTime(10));
        
        ITemporalInvariant constrInv = new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
        		inv, threshold);

        testLinearGraphCExample(events, constrInv, true, 4);
    }
}
