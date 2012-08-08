package dynoptic.invariants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AlwaysFollowedByTests extends InvTesting {

    @Test
    public void create() {
        AlwaysFollowedBy inv = new AlwaysFollowedBy(e1, e2);
        logger.info(inv.toString());
    }

    @Test
    public void scmBadStatesString() {
        AlwaysFollowedBy inv = new AlwaysFollowedBy(e1, e2);

        inv.setFirstSynthTracers(fSynth1, fSynth2);
        inv.setSecondSynthTracers(sSynth1, sSynth2);

        logger.info(inv.scmBadStateQRe());
    }

    @Test
    public void getFirstSecond() {
        AlwaysFollowedBy inv = new AlwaysFollowedBy(e1, e2);
        inv.getFirst().equals(e1);
        inv.getSecond().equals(e2);
    }

    @Test
    public void equality() {
        AlwaysFollowedBy inv1 = new AlwaysFollowedBy(e1, e2);
        AlwaysFollowedBy inv2 = new AlwaysFollowedBy(e1, e2);
        assertEquals(inv1, inv2);

        AlwaysFollowedBy inv3 = new AlwaysFollowedBy(e0, e2);
        assertTrue(!inv1.equals(inv3));
    }
}
