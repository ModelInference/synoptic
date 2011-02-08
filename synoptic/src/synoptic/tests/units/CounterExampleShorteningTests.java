package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.model.LogEvent;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;

public class CounterExampleShorteningTests extends SynopticTest {
    /**
     * Check shortening of NFby counter-examples.
     */
    @Test
    public void ShortenNFbyCExamplesTest() {
        NeverFollowedInvariant inv = new NeverFollowedInvariant("x", "y",
                defRelation);
        List<LogEvent> fullPath, shortPath;

        // x,a -> null (because x,a is not a counter-example path)
        fullPath = SynopticTest.getLogEventPath(new String[] { "x", "a" });
        shortPath = inv.shorten(fullPath);
        assertTrue(shortPath == null);

        // a,x,y,y,x,y -> a,x,y (first x, followed by first y)
        fullPath = SynopticTest.getLogEventPath(new String[] { "a", "x", "y", "y", "x", "y" });
        shortPath = inv.shorten(fullPath);
        assertTrue(shortPath != null);
        assertTrue(shortPath.equals(fullPath.subList(0, 3)));

        // ///////////////////////
        inv = new NeverFollowedInvariant("x", "x", defRelation);

        // x,a -> null (because x,a is not a counter-example path)
        fullPath = SynopticTest.getLogEventPath(new String[] { "x", "a" });
        shortPath = inv.shorten(fullPath);
        assertTrue(shortPath == null);

        // a,x,x,x,x,b -> a,x,x (first two x's)
        fullPath = SynopticTest.getLogEventPath(new String[] { "a", "x", "x", "x", "x", "b" });
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
        fullPath = SynopticTest.getLogEventPath(new String[] { "x", "y" });
        shortPath = inv.shorten(fullPath);
        assertTrue(shortPath == null);

        // y,x,y,a,b -> y (loose everything after the first y)
        fullPath = SynopticTest.getLogEventPath(new String[] { "y", "x", "y", "a", "b" });
        shortPath = inv.shorten(fullPath);
        logger.fine(fullPath.toString());
        logger.fine(shortPath.toString());
        assertTrue(shortPath != null);
        assertTrue(shortPath.equals(fullPath.subList(0, 1)));

        // a,b,y,x,y,a -> y (loose everything after the first y, with some non-y
        // initial events)
        fullPath = SynopticTest.getLogEventPath(new String[] { "a", "b", "y", "x", "y", "a" });
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
