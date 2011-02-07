package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.model.Action;
import synoptic.model.LogEvent;
import synoptic.util.InternalSynopticException;

public class CounterExampleShorteningTests extends SynopticUnitTest {
    // Default relation to use throughout this test.
    String defRelation = "t";

    /**
     * Given an array of strings, create a list of corresponding LogEvent
     * instances.
     * 
     * @param strEvents
     *            A string sequence of events.
     * @return A LogEvent sequence of events.
     */
    public List<LogEvent> getLogEventPath(String[] strEvents) {
        ArrayList<LogEvent> ret = new ArrayList<LogEvent>();
        LogEvent prevEvent = null;
        for (String strEvent : strEvents) {
            Action act = new Action(strEvent);
            LogEvent logEvent = new LogEvent(act);
            ret.add(logEvent);
            if (prevEvent != null) {
                prevEvent.addTransition(logEvent, defRelation);
            }
        }
        return ret;
    }

    /**
     * Check shortening of NFby counter-examples.
     */
    @Test
    public void ShortenNFbyCExamplesTest() {
        NeverFollowedInvariant inv = new NeverFollowedInvariant("x", "y",
                defRelation);
        List<LogEvent> fullPath, shortPath;

        // x,a -> null (because x,a is not a counter-example path)
        fullPath = getLogEventPath(new String[] { "x", "a" });
        shortPath = inv.shorten(fullPath);
        assertTrue(shortPath == null);

        // a,x,y,y,x,y -> a,x,y (first x, followed by first y)
        fullPath = getLogEventPath(new String[] { "a", "x", "y", "y", "x", "y" });
        shortPath = inv.shorten(fullPath);
        assertTrue(shortPath != null);
        assertTrue(shortPath.equals(fullPath.subList(0, 3)));

        // ///////////////////////
        inv = new NeverFollowedInvariant("x", "x", defRelation);

        // x,a -> null (because x,a is not a counter-example path)
        fullPath = getLogEventPath(new String[] { "x", "a" });
        shortPath = inv.shorten(fullPath);
        assertTrue(shortPath == null);

        // a,x,x,x,x,b -> a,x,x (first two x's)
        fullPath = getLogEventPath(new String[] { "a", "x", "x", "x", "x", "b" });
        shortPath = inv.shorten(fullPath);
        assertTrue(shortPath != null);
        assertTrue(shortPath.equals(fullPath.subList(0, 3)));
    }

    /**
     * Check shortening of AP counter-examples.
     */
    @Test
    public void ShortenAPCExamplesTest() {
        AlwaysPrecedesInvariant inv = new AlwaysPrecedesInvariant("x", "y",
                defRelation);
        List<LogEvent> fullPath, shortPath;

        // x,y -> null (because x,y is not a counter-example path)
        fullPath = getLogEventPath(new String[] { "x", "y" });
        shortPath = inv.shorten(fullPath);
        assertTrue(shortPath == null);

        // y,x,y,a,b -> y (loose everything after the first y)
        fullPath = getLogEventPath(new String[] { "y", "x", "y", "a", "b" });
        shortPath = inv.shorten(fullPath);
        logger.fine(fullPath.toString());
        logger.fine(shortPath.toString());
        assertTrue(shortPath != null);
        assertTrue(shortPath.equals(fullPath.subList(0, 1)));

        // a,b,y,x,y,a -> y (loose everything after the first y, with some non-y
        // initial events)
        fullPath = getLogEventPath(new String[] { "a", "b", "y", "x", "y", "a" });
        shortPath = inv.shorten(fullPath);
        logger.fine(fullPath.toString());
        logger.fine(shortPath.toString());
        assertTrue(shortPath != null);
        assertTrue(shortPath.equals(fullPath.subList(0, 3)));
    }

    /**
     * Check that we cannot create an "x AP x" invariant.
     */
    @Test(expected = InternalSynopticException.class)
    public void xAPxInvariantTest() {
        @SuppressWarnings("unused")
        AlwaysPrecedesInvariant inv = new AlwaysPrecedesInvariant("x", "x",
                defRelation);
    }
}
