package csight.model.fifosys.cfsm;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import csight.CSightTest;
import csight.invariants.AlwaysFollowedBy;
import csight.invariants.AlwaysPrecedes;
import csight.invariants.BinaryInvariant;
import csight.invariants.EventuallyHappens;
import csight.invariants.NeverFollowedBy;
import csight.mc.MCResult;
import csight.mc.spin.Spin;
import csight.model.export.GraphExporter;
import csight.model.fifosys.cfsm.fsm.FSM;
import csight.model.fifosys.cfsm.fsm.FSMState;
import csight.util.Util;

public class SpinCFSMTests extends CFSMTesting {

    Spin spin;
    String verifyPath;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        verifyPath = CSightTest.getMcPath("spin");
        spin = new Spin(verifyPath);
    }

    /**
     * Removes the transitions from accept states to init states in the CFSM.
     * This removes cycles from the testing CFSM. This is to make sure our
     * invariants work for a basic model.
     */
    public void simplifyCFSM() {
        p0Accept.rmTransition(p0Le, p0Init);
        p1Accept.rmTransition(p1Lf, p1Init);
    }

    public MCResult verifyAndPrint(BinaryInvariant inv) throws IOException,
            InterruptedException {
        String cStr = cfsm.toPromelaString("test", 5);
        cStr += inv.promelaNeverClaim();
        spin.verify(cStr, 60);
        logger.info(cStr);

        MCResult result = spin.getVerifyResult(cfsm.getChannelIds());
        GraphExporter.exportCFSM("test-output/test.dot", cfsm);
        GraphExporter.generatePngFileFromDotFile("test-output/test.dot");
        logger.info(result.toRawString());
        logger.info(result.toString());
        return result;
    }

    @Test
    public void verifyEventuallySafe() throws Exception {
        EventuallyHappens inv = new EventuallyHappens(p1Rm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(result.modelIsSafe());
        assertTrue(result.getCExample() == null);
    }

    @Test
    public void verifyEventuallyUnSafe() throws Exception {
        simplifyCFSM();
        EventuallyHappens inv = new EventuallyHappens(p0Le);
        MCResult result = verifyAndPrint(inv);
        assertTrue(!result.modelIsSafe());
        assertTrue(result.getCExample() != null);
    }

    @Test
    // This one starts in the accept state.
    public void verifyEventuallyUnsafe2() throws Exception {
        // Constructs a new CFSM with one FSM that has a self loop on the
        // init/accept state.
        // The expected counterexample should have no events in the trace.
        FSMState p0InitAccept = new FSMState(true, true, 0, 0);
        states = Util.newSet();
        states.add(p0InitAccept);
        p0InitAccept.addTransition(p0Le, p0InitAccept);
        f0 = new FSM(0, p0InitAccept, p0InitAccept, states, 1);
        channels = Util.newList();
        channels.add(cid);
        cfsm = new CFSM(1, channels);
        cfsm.addFSM(f0);

        EventuallyHappens inv = new EventuallyHappens(p0Le);
        MCResult result = verifyAndPrint(inv);
        assertTrue(!result.modelIsSafe());
        assertTrue(result.getCExample() != null);
    }

    @Test
    public void verifyAFbySimpleSafe() throws Exception {
        simplifyCFSM();
        AlwaysFollowedBy inv = new AlwaysFollowedBy(p0Sm, p1Rm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(result.modelIsSafe());
        assertTrue(result.getCExample() == null);
    }

    @Test
    public void verifyAFbySafe() throws Exception {
        AlwaysFollowedBy inv = new AlwaysFollowedBy(p0Sm, p1Rm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(result.modelIsSafe());
        assertTrue(result.getCExample() == null);
    }

    @Test
    public void verifyAFbyUnsafe() throws Exception {
        simplifyCFSM();
        AlwaysFollowedBy inv = new AlwaysFollowedBy(p1Rm, p0Sm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(!result.modelIsSafe());
        assertTrue(result.getCExample() != null);
    }

    @Test
    public void verifyNFbySafe() throws Exception {
        simplifyCFSM();
        NeverFollowedBy inv = new NeverFollowedBy(p1Rm, p0Sm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(result.modelIsSafe());
        assertTrue(result.getCExample() == null);
    }

    @Test
    public void verifyNFbyUnsafe() throws Exception {
        NeverFollowedBy inv = new NeverFollowedBy(p0Sm, p1Rm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(!result.modelIsSafe());
        assertTrue(result.getCExample() != null);
    }

    @Test
    public void verifyAPSafe() throws Exception {
        AlwaysPrecedes inv = new AlwaysPrecedes(p0Sm, p1Rm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(result.modelIsSafe());
        assertTrue(result.getCExample() == null);
    }

    @Test
    public void verifyAPUnsafe() throws Exception {
        simplifyCFSM();
        AlwaysPrecedes inv = new AlwaysPrecedes(p1Rm, p0Sm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(!result.modelIsSafe());
        assertTrue(result.getCExample() != null);
    }

}
