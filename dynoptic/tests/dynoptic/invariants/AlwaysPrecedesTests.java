package dynoptic.invariants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AlwaysPrecedesTests extends InvTesting {

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
}
