package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import main.InvariMintOptions;
import model.EncodedAutomaton;

import org.junit.Test;

import algorithms.InvariMintPropTypes;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;

public class InvMintPropTypesTest {

    protected static final String testOutputDir = "." + File.separator
            + "test-output" + File.separator;

    private EventType initial = StringEventType.newInitialStringEventType();
    private EventType terminal = StringEventType.newTerminalStringEventType();

    private EventType a = new StringEventType("a");
    private EventType b = new StringEventType("b");

    private String tPath = ".." + File.separator + "traces" + File.separator;
    private String simpleModelPath = tPath + "abstract" + File.separator
            + "simple-model" + File.separator;

    /**
     * Tests InvariMintPropTypes with AP on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAP() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AP",
                simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        List<EventType> sequence = new ArrayList<EventType>();
        sequence.add(initial);
        sequence.add(a);
        sequence.add(a);
        sequence.add(b);
        sequence.add(a);
        sequence.add(terminal);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(a);
        sequence.add(b);
        sequence.add(b);
        sequence.add(terminal);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(b);
        sequence.add(a);
        sequence.add(b);
        sequence.add(terminal);
        assertFalse(dfa.run(sequence));

        /*
         * TODO: Find a way to make this work // Create expected model:
         * EventTypeEncodings encodings = dfa.getEventEncodings(); State
         * initialState = new State(); State one = new State(); State two = new
         * State(); State three = new State(); State terminalState = new
         * State(); terminalState.setAccept(true);
         * initialState.addTransition(new
         * dk.brics.automaton.Transition(encodings .getEncoding(initial), one));
         * one.addTransition(new dk.brics.automaton.Transition(encodings
         * .getEncoding(a), two)); two.addTransition(new
         * dk.brics.automaton.Transition(encodings .getEncoding(b), three));
         * three.addTransition(new dk.brics.automaton.Transition(encodings
         * .getEncoding(terminal), terminalState)); EncodedAutomaton expectedDfa
         * = new CustomModel(encodings, initialState);
         * System.out.println(dfa.getEventEncodings().toString());
         * System.out.println(expectedDfa.toString()); //
         * assertTrue(dfa.subsetOf(expectedDfa));
         * assertTrue(expectedDfa.subsetOf(dfa));
         */
    }

    /**
     * Tests InvariMintPropTypes with NIFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestNIFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--NIFby",
                simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        List<EventType> sequence = new ArrayList<EventType>();
        sequence.add(initial);
        sequence.add(a);
        sequence.add(b);
        sequence.add(a);
        sequence.add(b);
        sequence.add(terminal);
        assertFalse(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(a);
        sequence.add(b);
        sequence.add(b);
        sequence.add(terminal);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(initial);
        sequence.add(b);
        sequence.add(a);
        sequence.add(b);
        sequence.add(terminal);
        assertFalse(dfa.run(sequence));

    }
}
