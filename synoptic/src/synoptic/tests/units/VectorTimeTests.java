package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.tests.SynopticTest;
import synoptic.util.resource.AbstractResource;
import synoptic.util.resource.DTotalResource;
import synoptic.util.resource.NonComparableResourceException;
import synoptic.util.resource.NotComparableVectorsException;
import synoptic.util.resource.VectorTime;
import synoptic.util.resource.WrongTimeTypeException;

/**
 * Tests for synoptic.model.input.VectorTime class.
 */
public class VectorTimeTests extends SynopticTest {

    TraceParser parser;

    @Override
    public void setUp() throws ParseException {
        super.setUp();
        parser = new TraceParser();
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
    }

    /**
     * Make sure we can create vector time objects without errors.
     */
    @Test
    public void constructorNoExceptionsTest() {
        @SuppressWarnings("unused")
        VectorTime v = new VectorTime("1,2,3");
        v = new VectorTime(Arrays.asList(new Integer[] { 1, 2, 3 }));
    }

    /**
     * Test various equality permutations.
     */
    @Test
    public void equalityTest() {
        AbstractResource v1, v2, v3;

        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime(Arrays.asList(new Integer[] { 1, 2, 3 }));
        assertTrue(v1.equals(v1));
        assertTrue(v1.equals(v2));

        assertFalse(v1.equals("1"));
        assertFalse(v1.equals(null));

        v3 = new VectorTime(Arrays.asList(new Integer[] { 1, 2, 4 }));
        assertFalse(v1.equals(v3));
        assertFalse(v3.equals(v1));
    }

    /**
     * Vectors of different length cannot be compared -- this throws an
     * exception.
     */
    @Test(expected = NotComparableVectorsException.class)
    public void equalityDiffLengthVectorsTest() {
        AbstractResource v1, v2;

        v1 = new VectorTime("1,2,3");

        v2 = new VectorTime(Arrays.asList(new Integer[] { 1, 2, 3, 5 }));
        v1.equals(v2);
    }

    /**
     * Test the step() method.
     */
    @Test
    public void stepTest() {
        VectorTime v1, v2;
        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime("1,2,4");

        assertFalse(v1.equals(v2));
        v1 = v1.step(2);
        assertTrue(v1.equals(v2));
    }

    /**
     * Test the isSingular() method.
     */
    @Test
    public void isSingularTest() {
        VectorTime v1, v2;
        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime("1");
        assertFalse(v1.isSingular());
        assertTrue(v2.isSingular());
    }

    /**
     * Test the isUnitVector() method.
     */
    @Test
    public void isUnitVectorTest() {
        VectorTime v1;
        v1 = new VectorTime("0");
        assertFalse(v1.isUnitVector());
        v1 = new VectorTime("0,1,0");
        assertTrue(v1.isUnitVector());
        v1 = new VectorTime("1");
        assertTrue(v1.isUnitVector());
        v1 = new VectorTime("0,1,1");
        assertFalse(v1.isUnitVector());
        v1 = new VectorTime("2");
        assertFalse(v1.isUnitVector());
        v1 = new VectorTime("0,0,2");
        assertFalse(v1.isUnitVector());
        v1 = new VectorTime("1,2,3");
        assertFalse(v1.isUnitVector());
    }

    /**
     * Test the lessThan() method.
     */
    @Test
    public void lessThanTest() {
        VectorTime v1, v2;
        // Vectors of same lengths.
        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime("1,2,4");
        assertTrue(v1.lessThan(v2));
        assertFalse(v2.lessThan(v1));

        v2 = new VectorTime("1,2,3");
        assertFalse(v1.lessThan(v2));
        assertFalse(v2.lessThan(v1));
    }

    /**
     * Vectors of different length cannot be compared -- this throws an
     * exception.
     */
    @Test(expected = NotComparableVectorsException.class)
    public void lessThanDiffLengthVectorsTest() {
        VectorTime v1, v2;
        v1 = new VectorTime("1,2,3,0");
        v2 = new VectorTime("1,2,4");
        v1.lessThan(v2);
    }

    /**
     * Times of different types cannot be compared -- this throws an exception.
     */
    @Test(expected = NonComparableResourceException.class)
    public void lessThanDiffTimeTypesTest() {
        AbstractResource v, d;
        v = new VectorTime("1,2,3");
        d = new DTotalResource(1);
        v.lessThan(d);
    }

    /**
     * Test the compareTo() method.
     */
    @Test
    public void compareToTest() {
        VectorTime v1, v2;
        // Vectors of same lengths.
        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime("1,2,4");
        assertTrue(v1.compareTo(v1) == 0);
        assertTrue(v2.compareTo(v2) == 0);

        assertTrue(v1.compareTo(v2) == -1);
        assertTrue(v2.compareTo(v1) == 0);

        v2 = new VectorTime("1,0,4");
        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v2.compareTo(v1) == 0);
    }

    /**
     * Vectors of different length cannot be compared -- this throws an
     * exception.
     */
    @Test(expected = NotComparableVectorsException.class)
    public void compareToDiffLengthVectorsTest() {
        VectorTime v1, v2;
        v1 = new VectorTime("1,2,3,0");
        v2 = new VectorTime("1,2,4");
        v1.compareTo(v2);
    }

    /**
     * Times of different types cannot be compared -- this throws an exception.
     */
    @Test(expected = NonComparableResourceException.class)
    public void compareToDiffTimeTypesTest() {
        AbstractResource v, d;
        v = new VectorTime("1,2,3");
        d = new DTotalResource(1);
        v.compareTo(d);
    }

    /**
     * Test the toString() method.
     */
    @Test
    public void toStringTest() {
        AbstractResource v1;
        v1 = new VectorTime("1,2,3,1");
        String s = v1.toString();
        assertEquals("[1, 2, 3, 1]", s);
    }

    /**
     * Test the hashCode() method.
     */
    @Test
    public void hashCodeTest() {
        AbstractResource v1, v2;
        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime("1,2,4");

        assertTrue(v1.hashCode() == v1.hashCode());
        assertTrue(v1.hashCode() != v2.hashCode());
    }

    /**
     * Test the determineIthEvent() method.
     * 
     * @throws ParseException
     */
    @Test
    public void determineIthEventTest() throws ParseException {
        String[] events = new String[] { "1,0 a1", "2,0 b1", "3,0 c1",
                "0,1 a2", "0,2 b2", "0,3 c3" };
        List<EventNode> parsedEvents = parseLogEvents(events, parser);

        for (int i = 0; i < 3; i++) {
            assertTrue(VectorTime.determineIthEvent(0, parsedEvents, i + 1) == parsedEvents
                    .get(i));
        }
        for (int i = 0; i < 3; i++) {
            assertTrue(VectorTime.determineIthEvent(1, parsedEvents, i + 1) == parsedEvents
                    .get(3 + i));
        }
    }

    /**
     * Test that determineIthEvent() generates an exception when the list of
     * events has an event that has a non-vector time.
     */
    @Test(expected = WrongTimeTypeException.class)
    public void determineIthEventNonVTimeTest() {
        AbstractResource vtime, dtime;
        vtime = new VectorTime("1,2,3");
        dtime = new DTotalResource(1);
        List<EventNode> eventNodes = new LinkedList<EventNode>();
        eventNodes.add(new EventNode(new Event("a")));
        eventNodes.add(new EventNode(new Event("b")));
        eventNodes.get(0).getEvent().setTime(vtime);
        eventNodes.get(1).getEvent().setTime(dtime);

        VectorTime.determineIthEvent(0, eventNodes, 0);
    }

    /**
     * Test the mapLogEventsToNodes() method with two nodes that communicate
     * their clocks every once in a while.
     * 
     * @throws ParseException
     */
    @Test
    public void mapLogEventsToNodesTest() throws ParseException {
        String[] events = new String[] { "1,0 a", "2,0 b", "3,0 c", "4,3 d",
                "5,3 e", "0,1 a'", "2,2 b'", "2,3 c'", "2,4 d'" };

        List<EventNode> parsedEvents = parseLogEvents(events, parser);
        List<List<EventNode>> map = VectorTime
                .mapLogEventsToNodes(parsedEvents);

        assertTrue(map.size() == 2);
        for (int i = 0; i < 5; i++) {
            assertTrue(VectorTime.determineIthEvent(0, parsedEvents, i + 1) == parsedEvents
                    .get(i));
        }
        for (int i = 0; i < 4; i++) {
            assertTrue(VectorTime.determineIthEvent(1, parsedEvents, i + 1) == parsedEvents
                    .get(5 + i));
        }
    }
}
