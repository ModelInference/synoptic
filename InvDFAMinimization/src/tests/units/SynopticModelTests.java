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

import synoptic.main.Main;
import synoptic.model.EventType;
import synoptic.model.PartitionGraph;
import synoptic.model.StringEventType;

import tests.InvDFAMinimizationTest;

/**
 * Basic tests for the SynopticModel class - checks that SynopticModels are
 * correctly translated. Models are considered correctly translated if the
 * languages accepted are equivalent.
 * 
 * @author Jenny
 */
public class SynopticModelTests extends InvDFAMinimizationTest {

    @Test
    public void testConversion() throws Exception {
        // Testing correctness for a known model.
        String[] args = new String[] {
                "--dumpInitialPartitionGraph=false",
                "--dumpInitialGraphDotFile=false",
                "--dumpInitialGraphPngFile=false",
                "-o",
                testOutputDir + "syn-model-osx-login-test.png",
                "-r",
                "(?<TYPE>.+)",
                "-s",
                "--",
                exampleTracesDir + "abstract" + File.separator
                        + "osx-login-example" + File.separator + "trace.txt" };

        // Set up Synoptic.
        Main synMain = Main.processArgs(args);
        PartitionGraph pGraph = synMain.createInitialPartitionGraph();
        Set<EventType> allEvents = pGraph.getEventTypes();
        EventTypeEncodings encodings = new EventTypeEncodings(allEvents);
        synMain.runSynoptic(pGraph);
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
