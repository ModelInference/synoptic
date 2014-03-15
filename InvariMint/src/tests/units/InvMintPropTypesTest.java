package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.InvariMintOptions;
import model.CustomModel;
import model.EncodedAutomaton;
import model.EventTypeEncodings;

import org.junit.Test;

import algorithms.InvariMintPropTypes;
import dk.brics.automaton.State;

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

    private EncodedAutomaton createAPdfa(EventType x, EventType y,
            Set<EventType> alphabet) {
        EventTypeEncodings encodings = new EventTypeEncodings(alphabet);
        State preX = new State();
        State postX = new State();
        preX.setAccept(true);
        postX.setAccept(true);

        // This adds all transitions in the alphabet except x and y back to
        // first accepting state
        for (EventType event : alphabet) {
            if (!event.equals(x) && !event.equals(y)) {
                preX.addTransition(new dk.brics.automaton.Transition(encodings
                        .getEncoding(event), preX));

            }
        }

        // adding transition to postX state, after x has been seen
        preX.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(x), postX));

        // This adds all transitions in the alphabet to final accepting state
        for (EventType event : alphabet) {
            postX.addTransition(new dk.brics.automaton.Transition(encodings
                    .getEncoding(event), postX));

        }

        return new CustomModel(encodings, preX);
    }

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
         * // TODO: see why this is appearing like this?? EventTypeEncodings
         * encodings = dfa.getEventEncodings(); State initialState = new
         * State(); State one = new State(); one.setAccept(true);
         * initialState.setAccept(true); initialState.addTransition(new
         * dk.brics.automaton.Transition(encodings .getEncoding(initial),
         * initialState)); initialState.addTransition(new
         * dk.brics.automaton.Transition(encodings .getEncoding(terminal),
         * initialState)); initialState.addTransition(new
         * dk.brics.automaton.Transition(encodings .getEncoding(a), one));
         * one.addTransition(new dk.brics.automaton.Transition(encodings
         * .getEncoding(initial), one)); one.addTransition(new
         * dk.brics.automaton.Transition(encodings .getEncoding(terminal),
         * one)); one.addTransition(new dk.brics.automaton.Transition(encodings
         * .getEncoding(a), one)); one.addTransition(new
         * dk.brics.automaton.Transition(encodings .getEncoding(b), one));
         * 
         * EncodedAutomaton expectedDfa = new CustomModel(encodings,
         * initialState); // System.out.println(dfa.toGraphviz()); //
         * System.out.println(expectedDfa.toGraphviz());
         */
        Set<EventType> alphabet = new HashSet<EventType>();
        alphabet.add(a);
        alphabet.add(b);
        alphabet.add(initial);
        alphabet.add(terminal);
        EncodedAutomaton expectedDfa = createAPdfa(a, b, alphabet);
        System.out.println(dfa.toGraphviz());
        System.out.println(expectedDfa.toGraphviz());
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

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

        // create dfa
        EventTypeEncodings encodings = dfa.getEventEncodings();
        State initialState = new State();
        State inita = new State();
        State gota = new State();
        State gotb = new State();
        State term = new State();

        initialState.setAccept(true);
        inita.setAccept(true);
        gota.setAccept(true);
        gotb.setAccept(true);
        term.setAccept(true);

        initialState.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(initial), inita));
        initialState.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(a), gota));
        initialState.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(b), gotb));
        initialState.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(terminal), term));
        inita.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(a), gota));
        gota.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(a), gota));
        gota.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(b), gotb));
        gotb.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(b), gotb));
        gotb.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(terminal), term));

        EncodedAutomaton expectedDfa = new CustomModel(encodings, initialState);
        // System.out.println(dfa.toGraphviz());
        // System.out.println(expectedDfa.toGraphviz());
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AFby",
                simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        // create dfa
        EventTypeEncodings encodings = dfa.getEventEncodings();
        State initialState = new State();
        State inita = new State();
        State gota = new State();
        State gotb = new State();
        State term = new State();

        initialState.setAccept(true);
        inita.setAccept(true);
        gota.setAccept(true);
        gotb.setAccept(true);
        term.setAccept(true);

        initialState.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(initial), inita));
        initialState.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(a), gota));
        initialState.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(b), gotb));
        initialState.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(terminal), term));
        inita.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(a), gota));
        gota.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(a), gota));
        gota.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(b), gotb));
        gotb.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(b), gotb));
        gotb.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(terminal), term));

        EncodedAutomaton expectedDfa = new CustomModel(encodings, initialState);
        System.out.println(dfa.toGraphviz());
        System.out.println(expectedDfa.toGraphviz());
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));
    }
}
