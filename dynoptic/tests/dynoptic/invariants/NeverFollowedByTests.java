package dynoptic.invariants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import dynoptic.util.Util;

import synoptic.model.event.DistEventType;

public class NeverFollowedByTests extends AbsInvTesting {

    @Test
    public void create() {
        NeverFollowedBy inv = new NeverFollowedBy(e1, e2);
        logger.info(inv.toString());
    }

    @Test
    public void scmBadStatesString() {
        NeverFollowedBy inv = new NeverFollowedBy(e1, e2);

        inv.setFirstSynthTracers(fSynth1, fSynth2);
        inv.setSecondSynthTracers(sSynth1, sSynth2);

        logger.info(inv.scmBadStateQRe());
    }

    @Test
    public void getFirstSecond() {
        NeverFollowedBy inv = new NeverFollowedBy(e1, e2);
        inv.getFirst().equals(e1);
        inv.getSecond().equals(e2);
    }

    @Test
    public void equality() {
        NeverFollowedBy inv1 = new NeverFollowedBy(e1, e2);
        NeverFollowedBy inv2 = new NeverFollowedBy(e1, e2);
        assertEquals(inv1, inv2);

        NeverFollowedBy inv3 = new NeverFollowedBy(e0, e2);
        assertTrue(!inv1.equals(inv3));
    }

    @Test
    public void testSatisfies() {
        List<DistEventType> ePath = Util.newList();

        // //////////////////////////////////////
        // Test x NFby y

        NeverFollowedBy inv1 = new NeverFollowedBy(e1, e2);

        // Empty path always satisfies NFby.
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e2);
        // e2
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e0);
        // e2, e0
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e1);
        // e2, e0, e1
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e1);
        // e2, e0, e1, e1
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e2);
        // e2, e0, e1, e1, e2
        assertFalse(inv1.satisfies(ePath));

        ePath.add(e1);
        // e2, e0, e1, e1, e2, e1
        assertFalse(inv1.satisfies(ePath));

        // //////////////////////////////////////
        // Test x NFby x

        NeverFollowedBy inv2 = new NeverFollowedBy(e1, e1);
        ePath.clear();

        ePath.add(e0);
        // e0
        assertTrue(inv2.satisfies(ePath));

        ePath.add(e1);
        // e0, e1
        assertTrue(inv2.satisfies(ePath));

        ePath.add(e2);
        // e0, e1, e2
        assertTrue(inv2.satisfies(ePath));

        ePath.add(e1);
        // e0, e1, e2,e1
        assertFalse(inv2.satisfies(ePath));

        ePath.add(e0);
        // e0, e1, e2,e1, e0
        assertFalse(inv2.satisfies(ePath));

        ePath.add(e1);
        // e0, e1, e2,e1, e0, e1
        assertFalse(inv2.satisfies(ePath));
    }
}
