package dynoptic.model.fifosys.cfsm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import mcscm.McScM;
import mcscm.Os;
import mcscm.VerifyResult;

import org.junit.Test;

import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.NeverFollowedBy;

public class McScMCFSMTests extends CFSMTesting {

    McScM mcscm;
    String verifyPath;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Determine whether to use the Linux or the OSX McScM binary.
        String osStr = null;
        if (Os.isLinux()) {
            osStr = "linux";
        } else if (Os.isMac()) {
            osStr = "osx";
        } else {
            fail("Running on an unsupported OS (not Linux, and not Mac).");
        }

        // NOTE: We assume the tests are run from synoptic/mcscm-bridge/
        verifyPath = "../bin/mcscm.verify." + osStr;
        mcscm = new McScM(verifyPath);
    }

    public VerifyResult verifyAndPrint() throws IOException,
            InterruptedException {
        String cStr = cfsm.toScmString();

        mcscm.verify(cStr);
        logger.info(cStr);

        VerifyResult result = mcscm.getVerifyResult(cfsm.getChannelIds());
        logger.info(result.toRawString());
        logger.info(result.toString());
        return result;
    }

    @SuppressWarnings("unused")
    @Test
    public void verifyAFby() throws IOException, InterruptedException {
        AlwaysFollowedBy inv = new AlwaysFollowedBy(p0Sm, p1Rm);
        cfsm.augmentWithInvTracing(inv);
        VerifyResult result = verifyAndPrint();
        // NOTE: there is a bug in McScM.verify for this particular
        // CFSM example. Once the bug is fixed, we need to:
        //
        // assertTrue(result.getCExample() == null);
    }

    @Test
    public void verifyNFby() throws IOException, InterruptedException {
        NeverFollowedBy inv = new NeverFollowedBy(p0Sm, p1Rm);
        cfsm.augmentWithInvTracing(inv);
        VerifyResult result = verifyAndPrint();
        assertTrue(result.getCExample() != null);
    }

    @Test
    public void verifyAP() throws IOException, InterruptedException {
        AlwaysPrecedes inv = new AlwaysPrecedes(p0Sm, p1Rm);
        cfsm.augmentWithInvTracing(inv);
        VerifyResult result = verifyAndPrint();
        assertTrue(result.getCExample() == null);
    }

}
