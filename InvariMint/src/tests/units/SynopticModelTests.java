package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.SynopticModel;

import org.junit.Test;

import synoptic.invariants.miners.ImmediateInvariantMiner;
import synoptic.main.SynopticMain;
import synoptic.model.PartitionGraph;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;

import tests.InvariMintTest;

/**
 * Basic tests for the SynopticModel class - checks that SynopticModels are
 * correctly translated. Models are considered correctly translated if the
 * languages accepted are equivalent.
 * 
 * @author Jenny
 */
public class SynopticModelTests extends InvariMintTest {

    @Test
    public void testConversion() throws Exception {
        // Testing correctness for a known model.
        String[] args = new String[] {
                "--dumpInitialPartitionGraph=false",
                "--dumpTraceGraphDotFile=false",
                "--dumpTraceGraphPngFile=false",
                "-o",
                testOutputDir + "syn-model-osx-login-test.png",
                "-r",
                "(?<TYPE>.+)",
                "-s",
                "--",
                exampleTracesDir + "abstract" + File.separator
                        + "osx-login-example" + File.separator + "trace.txt" };

        // Set up Synoptic.
        SynopticMain synMain = SynopticMain.processArgs(args);
        PartitionGraph pGraph = synMain.createInitialPartitionGraph();
        synMain.runSynoptic(pGraph);

        // Set up InvariMint
        ImmediateInvariantMiner miner = new ImmediateInvariantMiner(
                pGraph.getTraceGraph());
        Set<EventType> allEvents = miner.getEventTypes();
        EventTypeEncodings encodings = new EventTypeEncodings(allEvents);
        EncodedAutomaton convertedDfa = new SynopticModel(pGraph, encodings);

        List<EventType> validSequence = new ArrayList<EventType>();
        validSequence.add(new StringEventType("INITIAL"));
        validSequence.add(new StringEventType("login attempt"));
        validSequence.add(new StringEventType("guest login"));
        validSequence.add(new StringEventType("authorized"));
        validSequence.add(new StringEventType("TERMINAL"));
        assertTrue(convertedDfa.run(validSequence));

        List<EventType> invalidSequence = new ArrayList<EventType>();
        invalidSequence.add(new StringEventType("INITIAL"));
        invalidSequence.add(new StringEventType("login attempt"));
        invalidSequence.add(new StringEventType("auth failed"));
        invalidSequence.add(new StringEventType("TERMINAL"));
        assertFalse(convertedDfa.run(invalidSequence));

        validSequence = new ArrayList<EventType>();
        validSequence.add(new StringEventType("INITIAL"));
        validSequence.add(new StringEventType("login attempt"));
        validSequence.add(new StringEventType("auth failed"));
        validSequence.add(new StringEventType("login attempt"));
        validSequence.add(new StringEventType("auth failed"));
        validSequence.add(new StringEventType("login attempt"));
        validSequence.add(new StringEventType("auth failed"));
        validSequence.add(new StringEventType("authorized"));
        validSequence.add(new StringEventType("TERMINAL"));
        assertTrue(convertedDfa.run(validSequence));
    }
}
