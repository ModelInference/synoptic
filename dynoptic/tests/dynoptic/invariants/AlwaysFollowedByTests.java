package dynoptic.invariants;

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
        logger.info(inv.scmBadStateQRe(alphabet));
    }
}
