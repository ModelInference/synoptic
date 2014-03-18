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

import org.junit.Before;
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

    private Set<EventType> alphabet;
    private EventTypeEncodings encodings;

    private String tPath = ".." + File.separator + "traces" + File.separator;
    private String simpleModelPath = tPath + "abstract" + File.separator
            + "simple-model" + File.separator;

    @Before
    public void fillAlphabet() {
        alphabet = new HashSet<EventType>();
        alphabet.add(a);
        alphabet.add(b);
        encodings = new EventTypeEncodings(alphabet);
    }

    private EncodedAutomaton createAPdfa(EventType x, EventType y) {

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

    private EncodedAutomaton createNFbydfa(EventType x, EventType y) {
        State preX = new State();
        State postX = new State();
        preX.setAccept(true);
        postX.setAccept(true);

        // This adds all transitions in the alphabet except x back to
        // first accepting state
        for (EventType event : alphabet) {
            if (!event.equals(x)) {
                preX.addTransition(new dk.brics.automaton.Transition(encodings
                        .getEncoding(event), preX));

            }
        }

        // adding transition to postX state, after x has been seen
        preX.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(x), postX));

        // after this y cannot be seen, so add all transitions except y
        for (EventType event : alphabet) {
            if (!event.equals(y))
                postX.addTransition(new dk.brics.automaton.Transition(encodings
                        .getEncoding(event), postX));

        }

        return new CustomModel(encodings, preX);
    }

    private EncodedAutomaton createNIFbydfa(EventType x, EventType y) {
        State preX = new State();
        State postX = new State();
        preX.setAccept(true);
        postX.setAccept(true);

        // This adds all transitions in the alphabet except x back to
        // first accepting state
        for (EventType event : alphabet) {
            if (!event.equals(x)) {
                preX.addTransition(new dk.brics.automaton.Transition(encodings
                        .getEncoding(event), preX));

            }
        }

        // adding transition to postX state, after x has been seen
        preX.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(x), postX));

        // if X is picked again, remain at this state (need to check it's not
        // IFby), if y is seen here, reject by lack of transitions
        postX.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(x), postX));

        // if anything not x or y is seen, return to preX
        for (EventType event : alphabet) {
            if (!event.equals(y) && !event.equals(x))
                postX.addTransition(new dk.brics.automaton.Transition(encodings
                        .getEncoding(event), preX));

        }

        return new CustomModel(encodings, preX);
    }

    private EncodedAutomaton createAFbydfa(EventType x, EventType y) {
        State preX = new State();
        State postX = new State();
        preX.setAccept(true);

        // This adds all transitions in the alphabet except x back to
        // first accepting state
        for (EventType event : alphabet) {
            if (!event.equals(x)) {
                preX.addTransition(new dk.brics.automaton.Transition(encodings
                        .getEncoding(event), preX));

            }
        }

        // adding transition to postX state, after x has been seen
        preX.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(x), postX));

        // since x must me followed by y, anything except y will stay in this
        // state
        for (EventType event : alphabet) {
            if (!event.equals(y))
                postX.addTransition(new dk.brics.automaton.Transition(encodings
                        .getEncoding(event), postX));

        }

        // if we do see y, Afby has been satisfied, return to accepting state
        postX.addTransition(new dk.brics.automaton.Transition(encodings
                .getEncoding(y), preX));

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
        sequence.add(a);
        sequence.add(a);
        sequence.add(b);
        sequence.add(a);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(a);
        sequence.add(b);
        sequence.add(b);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(b);
        sequence.add(a);
        sequence.add(b);
        assertFalse(dfa.run(sequence));

        EncodedAutomaton expectedDfa = createAPdfa(a, b);
        System.out.println(dfa.toGraphviz());
        // System.out.println(expectedDfa.toGraphviz());
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
        sequence.add(a);
        sequence.add(b);
        sequence.add(a);
        sequence.add(b);
        assertFalse(dfa.run(sequence));

        sequence.clear();
        sequence.add(a);
        sequence.add(b);
        sequence.add(b);
        assertTrue(dfa.run(sequence));

        sequence.clear();
        sequence.add(b);
        sequence.add(a);
        sequence.add(b);
        assertFalse(dfa.run(sequence));

        EncodedAutomaton expectedDfa = createNIFbydfa(b, a);
        System.out.println(dfa.toGraphviz());
        System.out.println(expectedDfa.toGraphviz());
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

        List<EventType> sequence = new ArrayList<EventType>();
        sequence.add(a);
        sequence.add(b);
        sequence.add(a);
        assertFalse(dfa.run(sequence));

        sequence.clear();
        sequence.add(a);
        sequence.add(b);
        sequence.add(a);
        sequence.add(b);
        assertTrue(dfa.run(sequence));

        //

        EncodedAutomaton expectedDfa = createAFbydfa(a, b);
        System.out.println(dfa.toGraphviz());
        System.out.println(expectedDfa.toGraphviz());
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with NFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestNFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--NFby",
                simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createNFbydfa(b, a);
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AP + AFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAPAFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AP",
                "--AFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAPdfa(a, b);
        expectedDfa.intersectWith(createAFbydfa(a, b));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AP + NFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAPNFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AP",
                "--NFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAPdfa(a, b);
        expectedDfa.intersectWith(createNFbydfa(b, a));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AP + NIFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAPNIFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AP",
                "--NIFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAPdfa(a, b);
        expectedDfa.intersectWith(createNIFbydfa(b, a));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AF + NFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAFbyNFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AFby",
                "--NFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAFbydfa(a, b);
        expectedDfa.intersectWith(createNFbydfa(b, a));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AF + NFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAFbyNIFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AFby",
                "--NIFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAFbydfa(a, b);
        expectedDfa.intersectWith(createNIFbydfa(b, a));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with NF + NIFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestNFbyNIFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--NFby",
                "--NIFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createNFbydfa(b, a);

        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AP + NF + NIFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAPNFbyNIFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AP",
                "--NFby", "--NIFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAPdfa(a, b);
        expectedDfa.intersectWith(createNFbydfa(b, a));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AP + AF + NIFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAPAFbyNIFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AP",
                "--AFby", "--NIFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAPdfa(a, b);
        expectedDfa.intersectWith(createAFbydfa(a, b));
        expectedDfa.intersectWith(createNIFbydfa(b, a));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));
    }

    /**
     * Tests InvariMintPropTypes with AP + AF + NFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAPAFbyNFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AP",
                "--AFby", "--NFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAPdfa(a, b);
        expectedDfa.intersectWith(createAFbydfa(a, b));
        expectedDfa.intersectWith(createNFbydfa(b, a));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AF + NF + NIFby on simple-model example in
     * traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAFNFbyNIFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AFby",
                "--NFby", "--NIFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAFbydfa(a, b);
        expectedDfa.intersectWith(createNFbydfa(b, a));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

    /**
     * Tests InvariMintPropTypes with AP + AFby + NF + NIFby on simple-model
     * example in traces/abstract
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelTestAPAFNFbyNIFby() throws Exception {

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AP",
                "--AFby", "--NFby", "--NIFby", simpleModelPath + "trace.txt" };
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintPropTypes alg = new InvariMintPropTypes(opts);
        EncodedAutomaton dfa = alg.runInvariMint();

        EncodedAutomaton expectedDfa = createAFbydfa(a, b);
        expectedDfa.intersectWith(createAPdfa(a, b));
        expectedDfa.intersectWith(createNFbydfa(b, a));
        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }
}