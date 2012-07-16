package tests.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;

import main.InvariMintMain;
import main.InvariMintOptions;
import model.CustomModel;
import model.EncodedAutomaton;
import model.EventTypeEncodings;

import org.junit.Test;

import dk.brics.automaton.State;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;

import tests.InvariMintTest;

/**
 * Runs KTails InvariMint end-to-end on a simple log file and checks that the
 * final model matches the expected output.
 * 
 * @author jennyabrahamson
 */
public class EndToEndKTailsMainTest extends InvariMintTest {
    private EventType initial = StringEventType.newInitialStringEventType();
    private EventType terminal = StringEventType.newTerminalStringEventType();

    private EventType a = new StringEventType("a");
    private EventType b = new StringEventType("b");

    /**
     * Tests InvariMint on simple-model example in traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTest() throws Exception {
        String tPath = ".." + File.separator + "traces" + File.separator;
        String simpleModelPath = tPath + "abstract" + File.separator
                + "simple-model" + File.separator;

        String[] args = new String[] { "--performKTails=true", "--kTailLength",
                "2", "-r", "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m",
                "\\k<nodename>", "-o",
                testOutputDir + "ktail-simple-model-example",
                "-runSynoptic=false", simpleModelPath + "trace.txt" };

        InvariMintOptions opts = new InvariMintOptions(args);
        EncodedAutomaton dfa = InvariMintMain.runInvariMint(opts);

        // Create expected model:
        EventTypeEncodings encodings = dfa.getEventEncodings();
        State initialState = new State();
        State one = new State();
        State two = new State();
        State three = new State();
        State four = new State();
        State five = new State();
        State terminalState = new State();
        terminalState.setAccept(true);
        initialState.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(initial), one));
        one.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(a), two));
        two.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(b), five));
        two.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(a), three));
        three.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(b), four));
        four.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(b), five));
        five.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(terminal), terminalState));
        EncodedAutomaton expectedDfa = new CustomModel(encodings, initialState);

        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));
    }
}
