package dynoptic.invariants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import dynoptic.util.Util;

import synoptic.model.event.DistEventType;

public class AlwaysPrecedesTests extends AbsInvTesting {

    @Test
    public void create() {
        AlwaysPrecedes inv = new AlwaysPrecedes(e1, e2);
        logger.info(inv.toString());
    }

    @Test
    public void scmBadStatesString() {
        AlwaysPrecedes inv = new AlwaysPrecedes(e1, e2);

        inv.setFirstSynthTracers(fSynth1, fSynth2);
        inv.setSecondSynthTracers(sSynth1, sSynth2);

        logger.info(inv.scmBadStateQRe());
    }

    @Test
    public void getFirstSecond() {
        AlwaysPrecedes inv = new AlwaysPrecedes(e1, e2);
        inv.getFirst().equals(e1);
        inv.getSecond().equals(e2);
    }

    @Test
    public void equality() {
        AlwaysPrecedes inv1 = new AlwaysPrecedes(e1, e2);
        AlwaysPrecedes inv2 = new AlwaysPrecedes(e1, e2);
        assertEquals(inv1, inv2);

        AlwaysPrecedes inv3 = new AlwaysPrecedes(e0, e2);
        assertTrue(!inv1.equals(inv3));
    }

    @Test
    public void testSatisfies() {
        AlwaysPrecedes inv1 = new AlwaysPrecedes(e1, e2);
        List<DistEventType> ePath = Util.newList();

        // Empty path always satisfies AP.
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e1);
        // e1
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e0);
        // e1, e0
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e2);
        // e1, e0, e2
        assertTrue(inv1.satisfies(ePath));

        ePath.clear();

        ePath.add(e0);
        // e0
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e2);
        // e0, e2
        assertFalse(inv1.satisfies(ePath));

        ePath.add(e1);
        // e0, e2, e1
        assertFalse(inv1.satisfies(ePath));

        ePath.add(e2);
        // e0, e2, e1, e2
        assertFalse(inv1.satisfies(ePath));
    }
}
