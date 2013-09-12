package dynoptic.invariants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import dynoptic.util.Util;

import synoptic.model.event.DistEventType;

public class EventuallyHappensTests extends AbsInvTesting {

    @Test
    public void create() {
        EventuallyHappens inv = new EventuallyHappens(e1);
        logger.info(inv.toString());
    }

    @Test
    public void scmBadStatesString() {
        EventuallyHappens inv = new EventuallyHappens(e1);

        inv.setFirstSynthTracers(fSynth1, fSynth2);
        inv.setSecondSynthTracers(sSynth1, sSynth2);

        logger.info(inv.scmBadStateQRe());
    }

    @Test
    public void getFirstSecond() {
        EventuallyHappens inv = new EventuallyHappens(e1);
        inv.getFirst().equals(DistEventType.INITIALEventType);
        inv.getSecond().equals(e1);
        inv.getEvent().equals(e1);
    }

    @Test
    public void equality() {
        EventuallyHappens inv1 = new EventuallyHappens(e1);
        EventuallyHappens inv2 = new EventuallyHappens(e1);
        assertEquals(inv1, inv2);

        EventuallyHappens inv3 = new EventuallyHappens(e2);
        assertTrue(!inv1.equals(inv3));
    }

    @Test
    public void testSatisfies() {
        EventuallyHappens inv1 = new EventuallyHappens(e1);
        List<DistEventType> ePath = Util.newList();

        // Empty path always fails Eventually.
        assertFalse(inv1.satisfies(ePath));

        ePath.add(e0);
        // e0
        assertFalse(inv1.satisfies(ePath));

        ePath.add(e2);
        // e0, e2
        assertFalse(inv1.satisfies(ePath));

        ePath.add(e1);
        // e0, e2, e1
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e0);
        // e0, e2, e1, e0
        assertTrue(inv1.satisfies(ePath));

        ePath.add(e1);
        // e0, e2, e1, e0, e1
        assertTrue(inv1.satisfies(ePath));
    }

}
