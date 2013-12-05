package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import synoptic.main.SynopticMain;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Transition;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.tests.PynopticTest;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITime;

public class TraceNormalizationTests extends PynopticTest {

    /**
     * Verifies that two traces with integer times are normalized correctly,
     * both the event and transition times
     */
    @Test
    public void integerNormalizationTest() throws Exception {

        String[] iEvents = { "a 2", "b 3", "c 6", "--", "d 10", "e 11", "f 12" };

        // Generate trace graph
        ChainsTraceGraph traceGraph = (ChainsTraceGraph) genChainsTraceGraph(
                iEvents, genITimeParser());

        // Normalize trace graph
        SynopticMain.normalizeTraceGraph(traceGraph);

        // Get all events in trace graph
        Set<EventNode> events = traceGraph.getNodes();

        // Reference event types
        StringEventType a = new StringEventType("a");
        StringEventType b = new StringEventType("b");
        StringEventType c = new StringEventType("c");
        StringEventType d = new StringEventType("d");
        StringEventType e = new StringEventType("e");
        StringEventType f = new StringEventType("f");

        // To store whether each event time is found and correct
        boolean correctA = false;
        boolean correctB = false;
        boolean correctC = false;
        boolean correctD = false;
        boolean correctE = false;
        boolean correctF = false;

        // Reference times for testing normalized event times
        DTotalTime zero = new DTotalTime(0.0);
        DTotalTime quarter = new DTotalTime(0.25);
        DTotalTime half = new DTotalTime(0.5);
        DTotalTime threeQuarter = new DTotalTime(0.75);
        DTotalTime one = new DTotalTime(1.0);

        // Check if all event times are normalized correctly
        for (EventNode event : events) {
            EventType eType = event.getEType();
            ITime eTime = event.getTime();

            // 'a' == 0.0
            if (eType.equals(a) && eTime.equals(zero)) {
                correctA = true;
            }
            // 'b' == 0.25
            else if (eType.equals(b) && eTime.equals(quarter)) {
                correctB = true;
            }
            // 'c' == 1.0
            else if (eType.equals(c) && eTime.equals(one)) {
                correctC = true;
            }
            // 'd' == 0.0
            else if (eType.equals(d) && eTime.equals(zero)) {
                correctD = true;
            }
            // 'e' == 0.5
            else if (eType.equals(e) && eTime.equals(half)) {
                correctE = true;
            }
            // 'f' == 1.0
            else if (eType.equals(f) && eTime.equals(one)) {
                correctF = true;
            }
        }

        // Verify that all event times were correct
        assertTrue(correctA);
        assertTrue(correctB);
        assertTrue(correctC);
        assertTrue(correctD);
        assertTrue(correctE);
        assertTrue(correctF);

        // Get all transitions between events
        List<Transition<EventNode>> transitions = new ArrayList<Transition<EventNode>>();
        for (EventNode event : events) {
            transitions.addAll(event.getAllTransitions());
        }

        // To store whether each transition time is found and correct
        boolean correctAB = false;
        boolean correctBC = false;
        boolean correctDE = false;
        boolean correctEF = false;

        // Check if all transition time deltas are normalized correctly
        for (Transition<EventNode> trans : transitions) {
            EventType eType = trans.getSource().getEType();
            ITime transTime = trans.getTimeDelta();

            // 'a->b' == 0.25
            if (eType.equals(a) && transTime.equals(quarter)) {
                correctAB = true;
            }
            // 'b->c' == 0.75
            else if (eType.equals(b) && transTime.equals(threeQuarter)) {
                correctBC = true;
            }
            // 'd->e' == 0.5
            else if (eType.equals(d) && transTime.equals(half)) {
                correctDE = true;
            }
            // 'e->f' == 0.5
            else if (eType.equals(e) && transTime.equals(half)) {
                correctEF = true;
            }
        }

        // Verify that all transition times were correct
        assertTrue(correctAB);
        assertTrue(correctBC);
        assertTrue(correctDE);
        assertTrue(correctEF);
    }

    /**
     * Common method for verifying floating-point trace-wise normalization
     */
    public void floatNormalizationTestCommon(TraceParser parser)
            throws Exception {

        String[] rawEvents = { "a 3.1", "b 7.1", "c 8.1" };

        // Generate trace graph
        ChainsTraceGraph traceGraph = (ChainsTraceGraph) genChainsTraceGraph(
                rawEvents, parser);

        // Normalize trace graph
        SynopticMain.normalizeTraceGraph(traceGraph);

        // Get all events in trace graph
        Set<EventNode> events = traceGraph.getNodes();

        // Reference event types
        StringEventType a = new StringEventType("a");
        StringEventType b = new StringEventType("b");
        StringEventType c = new StringEventType("c");

        // To store whether each event time is found and correct
        boolean correctA = false;
        boolean correctB = false;
        boolean correctC = false;

        // Check if all event times are normalized correctly
        for (EventNode event : events) {
            EventType eType = event.getEType();

            // Get event time, and round to one decimal place to mitigate
            // floating-point error
            double eTime = 0.0;
            if (!eType.isSpecialEventType()) {
                eTime = ((DTotalTime) event.getTime()).time;
                eTime = Math.round(eTime * 10) / 10.0;
            }

            // 'a' == 0.0
            if (eType.equals(a) && eTime == 0.0) {
                correctA = true;
            }
            // 'b' == 0.8
            else if (eType.equals(b) && eTime == 0.8) {
                correctB = true;
            }
            // 'c' == 1.0
            else if (eType.equals(c) && eTime == 1.0) {
                correctC = true;
            }
        }

        // Verify that all event times were correct
        assertTrue(correctA);
        assertTrue(correctB);
        assertTrue(correctC);

        // Get all transitions between events
        List<Transition<EventNode>> transitions = new ArrayList<Transition<EventNode>>();
        for (EventNode event : events) {
            transitions.addAll(event.getAllTransitions());
        }

        // To store whether each transition time is found and correct
        boolean correctAB = false;
        boolean correctBC = false;

        // Check if all transition time deltas are normalized correctly
        for (Transition<EventNode> trans : transitions) {
            EventType eType = trans.getSource().getEType();

            // Get transition time, and round to one decimal place to mitigate
            // floating-point error
            double transTime = 0.0;
            if (!eType.isSpecialEventType() && trans.getTimeDelta() != null) {
                transTime = ((DTotalTime) trans.getTimeDelta()).time;
                transTime = Math.round(transTime * 10) / 10.0;
            }

            // 'a->b' == 0.8
            if (eType.equals(a) && transTime == 0.8) {
                correctAB = true;
            }
            // 'b->c' == 0.2
            else if (eType.equals(b) && transTime == 0.2) {
                correctBC = true;
            }
        }

        // Verify that all transition times were correct
        assertTrue(correctAB);
        assertTrue(correctBC);
    }

    /**
     * Verifies that a trace with float times is normalized correctly, both the
     * event and transition times
     */
    @Test
    public void floatNormalizationTest() throws Exception {
        floatNormalizationTestCommon(genFTimeParser());
    }

    /**
     * Verifies that a trace with double times is normalized correctly, both the
     * event and transition times
     */
    @Test
    public void doubleNormalizationTest() throws Exception {
        floatNormalizationTestCommon(genDTimeParser());
    }
}
