package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.invariants.CExamplePath;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.birelational.AFBiRelationInvariant;
import synoptic.invariants.birelational.APBiRelationInvariant;
import synoptic.invariants.birelational.NFBiRelationInvariant;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;

/**
 * 
 * @author Tim
 */
@RunWith(value = Parameterized.class)
public class BiModelCheckerTests extends SynopticTest {
    
    public static String relation = "r";
    
    public static final ITemporalInvariant aAFbyb = 
            new AFBiRelationInvariant("a", "b", relation);
    
    public static final ITemporalInvariant aNFbyb = 
            new NFBiRelationInvariant("a", "b", relation);
    
    public static final ITemporalInvariant aAPb = 
            new APBiRelationInvariant("a", "b", relation);

    /**
     * Generates parameters for this unit test. The first instance of this test
     * (using first set of parameters) will run using the FSM checker, while the
     * second instance (using the second set of parameters) will run using the
     * NASA model checker.
     * 
     * @return The set of parameters to pass to the constructor the unit test.
     */
    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { true }};
        return Arrays.asList(data);
    }

    boolean useFSMChecker;

    public BiModelCheckerTests(boolean useFSMChecker) {
        this.useFSMChecker = useFSMChecker;
    }

    @Before
    public void setUp() throws ParseException {
        super.setUp();
        synoptic.main.SynopticMain.getInstanceWithExistenceCheck().options.useFSMChecker = true;
        synoptic.main.SynopticMain.getInstanceWithExistenceCheck().options.multipleRelations = true;
    }

    /**
     * Test that the graph g generates or not (depending on the value of
     * cExampleExists) a counter-example for invariant inv, which is exactly the
     * expectedPath through the graph g.
     */
    @SuppressWarnings("null")
    private static <T extends INode<T>> void testCExamplePath(IGraph<T> g,
            ITemporalInvariant inv, boolean cExampleExists, List<T> expectedPath)
            throws InternalSynopticException {

        TemporalInvariantSet invs = new TemporalInvariantSet();
        invs.add(inv);

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
        // Create the graph.
        ChainsTraceGraph g = genInitialLinearGraph(events);

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
        parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        parser.addRegex("^(?<TIME>)(?<RELATION>)(?<TYPE>)$");
        parser.addRegex("^(?<TIME>)(?<RELATION*>)cl(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
        
        PartitionGraph pGraph = genInitialPartitionGraph(events, parser,
                new TransitiveClosureInvMiner(), true);

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
    
    @Test
    public void LinearAFNoCExamples() throws InternalSynopticException, ParseException {
        // (a) -t-> (z) -r-> (z) 
        String[] events1 = new String[] { "0 a", "1 z", "2 r z" };
        testLinearGraphCExample(events1, aAFbyb, false, 0);
        
        // (a) -r-> (a) -t-> (z) -t-> (b) -r-> (b)
        String[] events2 = new String[] { "0 a", "1 r a", "2 r z" };
        testLinearGraphCExample(events2, aAFbyb, false, 0);
        
        // (INITIAL) -r-> (x)
        String[] events3 = new String[] { "0 a", "1 r a", "2 r z" };
        testLinearGraphCExample(events3, aAFbyb, false, 0);
    }
    
    @Test
    public void LinearAFCExamplesPresent() throws InternalSynopticException, ParseException {
        // (a) -r-> (a) -t-> (b) -t-> (z) -r-> (z) 
        String[] events1 = new String[] { "0 a", "1 r a", "2 b", "3 z", "4 r z" };
        testLinearGraphCExample(events1, aAFbyb, true, 6);
        
        // (a) -r-> (a) -t-> (b)
        String[] events2 = new String[] { "0 a", "1 r a", "2 b" };
        testLinearGraphCExample(events2, aAFbyb, true, 4);
    }
    
    @Test
    public void LinearNFNoCExamples() throws InternalSynopticException, ParseException {
        // success : (a) -t-> (b) -r-> (b) 
        String[] events1 = new String[] { "0 a", "1 b", "2 r b" };
        testLinearGraphCExample(events1, aNFbyb, false, 0);
        
        // success : (a) -r-> (a) -t-> (b) -t-> (z) -r-> (z) 
        String[] events2 = new String[] { "0 a", "1 r a", "2 b", "3 z", "4 r z" };
        testLinearGraphCExample(events2, aNFbyb, false, 0);
        
        // success : (a) -r-> (a) -t-> (b)
        String[] events3 = new String[] { "0 a", "1 r a", "2 b" };
        testLinearGraphCExample(events3, aNFbyb, false, 0);
    }
    
    @Test
    public void LinearNFCExamplesPresent() throws InternalSynopticException, ParseException {
        // (a) -r-> (a) -t-> (z) -t-> (b) -r-> (b)
        String[] events1 = new String[] { "0 a", "1 r a", "2 z", "3 b", "4 r b" };
        testLinearGraphCExample(events1, aNFbyb, true, 4);
    }
    
    @Test
    public void LinearAPNoCExamples() throws InternalSynopticException, ParseException {
        // (a) -r-> (a) -t-> (z) -t-> (b) -r-> (b)
        String[] events1 = new String[] { "0 a", "1 r a", "2 z", "3 b", "3 r b" };
        testLinearGraphCExample(events1, aAPb, false, 0);
        
        // success : (z) -r-> (z) -t-> (b)
        String[] events2 = new String[] { "0 z", "1 r z", "2 b" };
        testLinearGraphCExample(events2, aAPb, false, 0);
    }
    
    @Test
    public void LinearAPCExamplesPresent() throws InternalSynopticException, ParseException {
        // (a) -t-> (b) -r-> (b)
        String[] events1 = new String[] { "0 a", "1 b", "2 r b" };
        testLinearGraphCExample(events1, aAPb, true, 4);
        
        // (z) -r-> (z) -t-> (a) -t-> (b) -r-> (b)
        String[] events2 = new String[] { "0 z", "1 r z", "2 a", "3 b", "4 r b" };
        testLinearGraphCExample(events2, aAPb, true, 6);
    }
    
    /**
     * Tests that a linear graph with a cycle does _not_ generate an AFby
     * c-example. This demonstrates why we need "<> TERMINAL ->" as the prefix
     * in the AFby LTL formula -- without this prefix this tests fails.
     * 
     * @throws Exception
     */
    @Test
    public void NoAFbyLinearGraphWithCycleTest() throws Exception {
        String[] events = new String[] { "x", "a", "c", "x", "a", "y", "b", "w" };

        testPartitionGraphCExample(events, aAFbyb, false, null);
    }

    @Test
    public void AFbyCELinear() throws Exception {
        String[] events = new String[] { "0 x", "1 r a", "2 b", "3 x" };
        String[] cExampleStringLabels = new String[] { "x", "a", "b", "x" };
        List<EventType> cExampleLabels = 
                stringsToStringEventTypes(cExampleStringLabels);
        
        cExampleLabels.add(StringEventType.newTerminalStringEventType());
        testPartitionGraphCExample(events, aAFbyb, true, cExampleLabels);
    }
    
    @Test
    public void NFbyCELinear() throws Exception {
        String[] events = new String[] { "0 x", "1 r a", "2 b", "4 r x" };
        String[] cExampleStringLabels = new String[] { "x", "a", "b" };
        List<EventType> cExampleLabels =
                stringsToStringEventTypes(cExampleStringLabels);
        
        testPartitionGraphCExample(events, aNFbyb, true, cExampleLabels);
    }
    
    @Test
    public void APbyCELinear() throws Exception {
        String[] events = new String[] { "0 x", "1 a", "2 b", "3 r x" };
        String[] cExampleStringLabels = new String[] { "x", "a", "b" };
        List<EventType> cExampleLabels =
                stringsToStringEventTypes(cExampleStringLabels);
        
        testPartitionGraphCExample(events, aAPb, true, cExampleLabels);
    }
    
    @Test
    public void beforeInEdgeTest() throws Exception {
        String[] events = new String[] { 
                "0 a", "1 x", "3 y", "4 r w", "--",
                "0 a", "1 x", "2 r w" };
        
        String[] CE1StringLabels = 
                new String[] { "a", "x", "y" };
        List<EventType> CE1Labels = stringsToStringEventTypes(CE1StringLabels);
        ITemporalInvariant xAPy = 
                new APBiRelationInvariant("x", "y", relation);
        testPartitionGraphCExample(events, xAPy, true, CE1Labels);
        
        String[] CE2StringLabels = 
                new String[] { "a", "x", "w" };
        List<EventType> CE2Labels = stringsToStringEventTypes(CE2StringLabels);
        CE2Labels.add(StringEventType.newTerminalStringEventType());
        ITemporalInvariant xAFy = 
                new AFBiRelationInvariant("x", "y", relation);
        testPartitionGraphCExample(events, xAFy, true, CE2Labels);
        
        ITemporalInvariant xNFw = 
                new NFBiRelationInvariant("x", "w", relation);
        testPartitionGraphCExample(events, xNFw, true, CE2Labels);
        
        ITemporalInvariant xNFy =
                new NFBiRelationInvariant("x", "y", relation);
        testPartitionGraphCExample(events, xNFy, false, null);
        
        ITemporalInvariant yAFw =
                new AFBiRelationInvariant("y", "w", relation);
        testPartitionGraphCExample(events, yAFw, false, null);
        
        ITemporalInvariant xAFw =
                new AFBiRelationInvariant("x", "w", relation);
        testPartitionGraphCExample(events, xAFw, false, null);
    }
    
    @Test
    public void inOutEdgeTest() throws Exception {
        String[] events = new String[] { 
                "0 a", "1 r x", "2 y", "3 b", "--",
                "0 a", "1 r x", "2 y", "3 r z"};
        
        String[] CE1StringLabels = 
                new String[] { "a", "x", "y" };
        List<EventType> CE1Labels = stringsToStringEventTypes(CE1StringLabels);
        ITemporalInvariant aNFy =
                new NFBiRelationInvariant("a", "y", relation);
        testPartitionGraphCExample(events, aNFy, true, CE1Labels);
        
        String[] CE2StringLabels = 
                new String[] { "a", "x", "y", "z"};
        List<EventType> CE2Labels = stringsToStringEventTypes(CE2StringLabels);
        ITemporalInvariant aNFz =
                new NFBiRelationInvariant("a", "z", relation);
        testPartitionGraphCExample(events, aNFz, true, CE2Labels);
        
        ITemporalInvariant xNFy = 
                new NFBiRelationInvariant("x", "y", relation);
        testPartitionGraphCExample(events, xNFy, true, CE1Labels);
        
        ITemporalInvariant xNFz = 
                new NFBiRelationInvariant("x", "z", relation);
        testPartitionGraphCExample(events, xNFz, true, CE2Labels);
        
        ITemporalInvariant aAPy = 
                new APBiRelationInvariant("a", "y", relation);
        testPartitionGraphCExample(events, aAPy, false, null);
        
        ITemporalInvariant xAPy =
                new APBiRelationInvariant("x", "y", relation);
        testPartitionGraphCExample(events, xAPy, false, null);
        
        ITemporalInvariant aAPz =
                new APBiRelationInvariant("a", "z", relation);
        testPartitionGraphCExample(events, aAPz, false, null);
        
        ITemporalInvariant xAPz = 
                new APBiRelationInvariant("x", "z", relation);
        testPartitionGraphCExample(events, xAPz, false, null);
    }
    
}
