package dynoptic.invariants;

import org.junit.Test;

public class NeverFollowedByTests extends InvTesting {

    @Test
    public void create() {
        NeverFollowedBy inv = new NeverFollowedBy(e1, e2);
        logger.info(inv.toString());
    }

    @Test
    public void scmBadStatesString() {
        NeverFollowedBy inv = new NeverFollowedBy(e1, e2);

        inv.setFirstSynthTracer(fSynth);
        inv.setSecondSynthTracer(sSynth);

        logger.info(inv.scmBadStateQRe(alphabet));
    }
}
