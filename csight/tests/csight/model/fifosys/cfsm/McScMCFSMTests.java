package csight.model.fifosys.cfsm;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import csight.CSightTest;
import csight.invariants.AlwaysFollowedBy;
import csight.invariants.AlwaysPrecedes;
import csight.invariants.NeverFollowedBy;
import csight.mc.MCResult;
import csight.mc.mcscm.McScM;

public class McScMCFSMTests extends CFSMTesting {

    McScM mcscm;
    String verifyPath;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // NOTE: We assume the tests are run from synoptic/mcscm-bridge/
        verifyPath = CSightTest.getMcPath();
        mcscm = new McScM(verifyPath);
    }

    public MCResult verifyAndPrint() throws IOException,
            InterruptedException {
        String cStr = cfsm.toScmString("test");

        mcscm.verify(cStr, 60);
        logger.info(cStr);

        MCResult result = mcscm.getVerifyResult(cfsm.getChannelIds());
        // logger.info(result.toRawString());
        // logger.info(result.toString());
        return result;
    }

    @Test
    public void verifyAFby() throws Exception {
        AlwaysFollowedBy inv = new AlwaysFollowedBy(p0Sm, p1Rm);
        cfsm.augmentWithInvTracing(inv);
        MCResult result = verifyAndPrint();
        assertTrue(result.getCExample() == null);
    }

    @Test
    public void verifyNFby() throws Exception {
        NeverFollowedBy inv = new NeverFollowedBy(p0Sm, p1Rm);
        cfsm.augmentWithInvTracing(inv);
        MCResult result = verifyAndPrint();
        assertTrue(result.getCExample() != null);
    }

    @Test
    public void verifyAP() throws Exception {
        AlwaysPrecedes inv = new AlwaysPrecedes(p0Sm, p1Rm);
        cfsm.augmentWithInvTracing(inv);
        MCResult result = verifyAndPrint();
        assertTrue(result.getCExample() == null);
    }

}
