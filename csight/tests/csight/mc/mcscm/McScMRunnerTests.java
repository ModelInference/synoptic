package csight.mc.mcscm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import csight.CSightTest;
import csight.invariants.AlwaysFollowedBy;
import csight.invariants.AlwaysPrecedes;
import csight.invariants.BinaryInvariant;
import csight.invariants.EventuallyHappens;
import csight.mc.MCResult;
import csight.model.fifosys.cfsm.CFSM;
import csight.model.fifosys.gfsm.GFSM;
import csight.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class McScMRunnerTests extends CSightTest {
    // TODO: implement tests for running in parallel
    private McScMRunner mcRunner;
    private McScM mcscm;
    private String verifyPath;
    private GFSM pGraph;
    private DistEventType eSend;
    private DistEventType eRecv;
    private List<BinaryInvariant> invariants;
    private int timeOut = 60;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // NOTE: hard-coded assumption about where the tests are run
        verifyPath = CSightTest.getMcPath();
        mcscm = new McScM(verifyPath);
        
        pGraph = createNonSingletonGFSM();
        ChannelId cid0 = new ChannelId(0, 1, 0);
        eSend = DistEventType.SendEvent("e", cid0);
        eRecv = DistEventType.RecvEvent("e", cid0);

        BinaryInvariant inv0 = new AlwaysPrecedes(eSend, eRecv);
        BinaryInvariant inv1 = new AlwaysFollowedBy(eSend, eRecv);
        invariants = Util.newList();
        invariants.add(inv0);
        invariants.add(inv1);
    }
    
    /**
     * Test running one invariant in parallel with only one invariant to check
     * 
     * @throws Exception
     */
    @Test
    public void testRunOneWithOneInv() throws Exception {
        List<BinaryInvariant> invs = Util.newList();
        invs.add(invariants.get(0));
        
        mcRunner = new McScMRunner(verifyPath, 1);
        mcRunner.verify(pGraph, invs, timeOut, false);
        
        MCResult runnerResult = mcRunner.getMCResult();
        BinaryInvariant resultInvariant = mcRunner.getResultInvariant();
        
        assertEquals(1, mcRunner.getInvariantsRan().size());
        assertTrue(invs.containsAll(mcRunner.getInvariantsRan()));
        assertTrue(invs.contains(resultInvariant));
        assertRunnerResult(pGraph, resultInvariant, false, runnerResult);
    }
    
    /**
     * Test running one invariant in parallel with many invariants to check
     * 
     * @throws Exception
     */
    @Test
    public void testRunOneWithManyInvs() throws Exception {
        List<BinaryInvariant> invs = Util.newList();
        invs.add(invariants.get(0));
        invs.add(invariants.get(1));
        invs.add(new EventuallyHappens(eSend));
        
        mcRunner = new McScMRunner(verifyPath, 1);
        mcRunner.verify(pGraph, invs, timeOut, false);
        
        MCResult runnerResult = mcRunner.getMCResult();
        BinaryInvariant resultInvariant = mcRunner.getResultInvariant();
        
        assertEquals(1, mcRunner.getInvariantsRan().size());
        assertTrue(invs.containsAll(mcRunner.getInvariantsRan()));
        assertTrue(invs.contains(resultInvariant));
        assertRunnerResult(pGraph, resultInvariant, false, runnerResult);
    }
    
    /**
     * Test running multiple invariants in parallel with only one invariant to check
     * 
     * @throws Exception
     */
    @Test
    public void testRunManyWithOneInvs() throws Exception {
        List<BinaryInvariant> invs = Util.newList();
        invs.add(invariants.get(0));
        
        mcRunner = new McScMRunner(verifyPath, 2);
        mcRunner.verify(pGraph, invs, timeOut, false);
        
        MCResult runnerResult = mcRunner.getMCResult();
        BinaryInvariant resultInvariant = mcRunner.getResultInvariant();
        
        assertEquals(1, mcRunner.getInvariantsRan().size());
        assertTrue(invs.containsAll(mcRunner.getInvariantsRan()));
        assertTrue(invs.contains(resultInvariant));
        assertRunnerResult(pGraph, resultInvariant, false, runnerResult);
    }
    
    /**
     * Test running multiple invariants in parallel with equal number of
     * invariants to check
     * 
     * @throws Exception
     */
    @Test
    public void testRunManyWithEqualInvs() throws Exception {
        List<BinaryInvariant> invs = Util.newList();
        invs.add(invariants.get(0));
        invs.add(invariants.get(1));
        
        mcRunner = new McScMRunner(verifyPath, 2);
        mcRunner.verify(pGraph, invs, timeOut, false);
        
        MCResult runnerResult = mcRunner.getMCResult();
        BinaryInvariant resultInvariant = mcRunner.getResultInvariant();
        
        assertEquals(2, mcRunner.getInvariantsRan().size());
        assertTrue(invs.containsAll(mcRunner.getInvariantsRan()));
        assertTrue(invs.contains(resultInvariant));
        assertRunnerResult(pGraph, resultInvariant, false, runnerResult);
    }
    
    /**
     * Test running multiple invariants with more invariants to check
     * than number of invariants to run parallel
     * 
     * @throws Exception
     */
    @Test
    public void testRunManyWithManyInvs() throws Exception {
        List<BinaryInvariant> invs = Util.newList();
        invs.add(invariants.get(0));
        invs.add(invariants.get(1));
        invs.add(new EventuallyHappens(eSend));
        
        mcRunner = new McScMRunner(verifyPath, 2);
        mcRunner.verify(pGraph, invs, timeOut, false);
        
        MCResult runnerResult = mcRunner.getMCResult();
        BinaryInvariant resultInvariant = mcRunner.getResultInvariant();
        
        assertEquals(2, mcRunner.getInvariantsRan().size());
        assertTrue(invs.containsAll(mcRunner.getInvariantsRan()));
        assertTrue(invs.contains(resultInvariant));
        assertRunnerResult(pGraph, resultInvariant, false, runnerResult);
    }
    
    private void assertRunnerResult(GFSM gfsm, BinaryInvariant inv,
            boolean minimize, MCResult runnerResult) throws Exception {
        CFSM cfsm = gfsm.getCFSM(minimize);
        cfsm.augmentWithInvTracing(inv);

        String mcInputStr = cfsm.toScmString("checking_scm_"
                + inv.getConnectorString());
        
        mcscm.verify(mcInputStr, timeOut);
        MCResult result = mcscm.getVerifyResult(cfsm.getChannelIds());
        
        assertEquals(result.modelIsSafe(), runnerResult.modelIsSafe());
        assertEquals(result.getCExample(), runnerResult.getCExample());
        assertEquals(result.toRawString(), runnerResult.toRawString());
    }
}