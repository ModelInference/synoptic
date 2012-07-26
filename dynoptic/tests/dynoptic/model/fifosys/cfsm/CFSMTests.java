package dynoptic.model.fifosys.cfsm;

import org.junit.Test;

import dynoptic.DynopticTest;

public class CFSMTests extends DynopticTest {

    @Test
    public void createCFSM() {
        CFSM c = new CFSM(2, this.getAllToAllChannelIds(2));
    }
}
