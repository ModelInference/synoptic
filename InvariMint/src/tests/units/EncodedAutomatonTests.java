package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;

import org.junit.Before;
import org.junit.Test;

import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.model.EventType;

/**
 * Basic tests for the EncodedAutomaton class.
 * 
 * @author Jenny
 */
public class EncodedAutomatonTests {

    // Encodings for a, b, c, d events.
    private EventTypeEncodings encodings;

    // Valid a AFby b sequence.
    private List<EventType> validAFbySequence;

    // Invalid a AFby b sequence.
    private List<EventType> invalidAFbySequence;

    @Before
    public void setUp() {
        encodings = EncodingTests.getBasicEncodings();

        // Construct sequences
        validAFbySequence = new ArrayList<EventType>();
        validAFbySequence.add(EncodingTests.aEvent);
        validAFbySequence.add(EncodingTests.bEvent);
        validAFbySequence.add(EncodingTests.bEvent);

        invalidAFbySequence = new ArrayList<EventType>();
        invalidAFbySequence.add(EncodingTests.aEvent);
        invalidAFbySequence.add(EncodingTests.bEvent);
        invalidAFbySequence.add(EncodingTests.bEvent);
        invalidAFbySequence.add(EncodingTests.aEvent);
    }

    /**
     * Tests that an EncodedAutomaton initially accepts any sequence of
     * EventTypes encoded in encodings (and no others)
     */
    @Test
    public void testConstructor() {
        EncodedAutomaton automaton = new InvsModel(encodings);
        List<EventType> sequence = new ArrayList<EventType>();

        // 'c' and 'b' encoded by encodings, so any sequence of bs and cs whould
        // be accepted.
        sequence.add(EncodingTests.bEvent);
        assertTrue(automaton.run(sequence));
        sequence.add(EncodingTests.cEvent);
        assertTrue(automaton.run(sequence));
        sequence.add(EncodingTests.cEvent);
        assertTrue(automaton.run(sequence));

        // 'z' not encoded by encodings, so should not be accepted.
        sequence.add(EncodingTests.zEvent);
        assertFalse(automaton.run(sequence));
        sequence.add(EncodingTests.zEvent);
        assertFalse(automaton.run(sequence));
    }

    @Test
    public void testSubsetOf() {

        InvModel aAFbyb = new InvModel(new AlwaysFollowedInvariant("a", "b",
                "t"), encodings);

        InvModel aAPb = new InvModel(
                new AlwaysPrecedesInvariant("a", "b", "t"), encodings);

        InvsModel first = new InvsModel(encodings);
        first.intersectWith(aAFbyb);

        InvsModel second = new InvsModel(encodings);
        second.intersectWith(aAFbyb);
        second.intersectWith(aAPb);

        // An automaton will always be a subset of itself.
        assertTrue(first.subsetOf(first));
        assertTrue(second.subsetOf(second));

        // Second is more restrictive than first and so will be a subset of
        // first but first should NOT be a subset of second.
        assertTrue(second.subsetOf(first));
        assertFalse(first.subsetOf(second));

    }

    /**
     * Some basic sanity tests for checking that run is behaving as expected.
     * More creative ideas needed!
     */
    @Test
    public void testRun() {

        InvModel model = new InvModel(
                new AlwaysFollowedInvariant("a", "b", "t"), encodings);

        List<EventType> sequence = new ArrayList<EventType>();

        sequence.add(EncodingTests.aEvent);
        assertFalse(model.run(sequence));

        sequence.add(EncodingTests.bEvent);
        assertTrue(model.run(sequence));

        sequence.add(EncodingTests.aEvent);
        assertFalse(model.run(sequence));

        sequence.remove(1);
        sequence.add(EncodingTests.bEvent);
        assertTrue(model.run(sequence));

        sequence.add(EncodingTests.zEvent);
        assertFalse(model.run(sequence));
    }

    @Test
    public void testMinimize() {
        // We'll manually construct a minimize-able DFA, then check if
        // minimizing keeps the language the same.
        EncodedAutomaton model = new InvsModel(encodings);

        // Build dfa.
        State initial = new State();
        State second = new State();

        char aEncoding = encodings.getEncoding(EncodingTests.aEvent);
        char bEncoding = encodings.getEncoding(EncodingTests.bEvent);

        initial.addTransition(new Transition(aEncoding, second));
        initial.addTransition(new Transition(aEncoding, second));

        State third = new State();
        second.addTransition(new Transition(bEncoding, third));
        second.addTransition(new Transition(bEncoding, third));

        State fourth = new State();
        third.addTransition(new Transition(bEncoding, fourth));
        third.addTransition(new Transition(bEncoding, fourth));
        third.addTransition(new Transition(bEncoding, fourth));

        fourth.setAccept(true);
        model.setInitialState(initial);

        // The valid and invalid AFby sequences happen to work here.

        // Check language is as expected.
        assertTrue(model.run(validAFbySequence));
        assertFalse(model.run(invalidAFbySequence));

        model.minimize();

        // Check language still as expected.
        assertTrue(model.run(validAFbySequence));
        assertFalse(model.run(invalidAFbySequence));
    }

    @Test
    public void testIntersectionWithRE() {

        EncodedAutomaton model = new InvsModel(encodings);

        // Initially the model should accept all sequences of valid encodings.
        assertTrue(model.run(validAFbySequence));
        assertTrue(model.run(invalidAFbySequence));

        // Intersect with the regex for a AFby b
        ITemporalInvariant alwaysFollowed = new AlwaysFollowedInvariant("a",
                "b", "t");
        model.intersectWithRE(alwaysFollowed.getRegex(
                encodings.getEncoding(EncodingTests.aEvent),
                encodings.getEncoding(EncodingTests.bEvent)));

        // Now the model should only accepted valid AFby sequences.
        assertTrue(model.run(validAFbySequence));
        assertFalse(model.run(invalidAFbySequence));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntersectionWithMismatchedEncodings() {
        EncodedAutomaton model = new InvsModel(encodings);
        EncodedAutomaton other = new InvsModel(new EventTypeEncodings(
                new HashSet<EventType>()));
        model.intersectWith(other);
    }

    @Test
    public void testIntersectionWith() {
        EncodedAutomaton model = new InvsModel(encodings);
        EncodedAutomaton inv = new InvModel(new AlwaysFollowedInvariant("a",
                "b", "t"), encodings);

        // Initially the model should accept all sequences of valid encodings.
        assertTrue(model.run(validAFbySequence));
        assertTrue(model.run(invalidAFbySequence));

        model.intersectWith(inv);

        // Now the model should only accept valid AFby sequences.
        assertTrue(model.run(validAFbySequence));
        assertFalse(model.run(invalidAFbySequence));
    }

    @Test
    public void testToGraphviz() {
        EncodedAutomaton model = new InvsModel(encodings);
        String graph = model.toGraphviz();

        // Asserts that two known events from encodings are found in the
        // Graphviz output.
        assertTrue(graph.indexOf("label=\"a\"") > 0);
        assertTrue(graph.indexOf("label=\"d\"") > 0);
    }
}
