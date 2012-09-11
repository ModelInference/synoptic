package dynoptic.invariants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
}
