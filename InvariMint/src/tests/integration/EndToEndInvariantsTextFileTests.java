package tests.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;

import main.InvariMintFromTextFile;
import model.CustomModel;
import model.EncodedAutomaton;
import model.EventTypeEncodings;

import org.junit.Test;

import dk.brics.automaton.State;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;

import tests.InvariMintTest;

/**
 * Runs InvariMint end-to-end on a simple invariants file and checks that the
 * final model matches the expected output.
 * 
 */
public class EndToEndInvariantsTextFileTests extends InvariMintTest {
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
        String filename = ".." + File.separator + "traces" + File.separator
                + "abstract" + File.separator + "simple-model" + File.separator
                + "invariants.txt";

        EncodedAutomaton dfa = InvariMintFromTextFile.createDFA(filename);

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
