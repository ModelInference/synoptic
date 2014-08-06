package csight.model.fifosys.cfsm;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    public Map<Integer, MCResult> verifyAndPrint(List<BinaryInvariant> invs)
            throws IOException, InterruptedException {
        String cStr = cfsm.toPromelaString(invs, 5);
        logger.info(cStr);
        spin.prepare(cStr, 5);
        for (int i = 0; i < invs.size(); i++) {
            spin.verify(cStr, 5, i);
        }
        Map<Integer, MCResult> result = spin.getVerifyResults(
                cfsm.getChannelIds(), invs.size());
        GraphExporter.exportCFSM("test-output/test.dot", cfsm);
        GraphExporter.generatePngFileFromDotFile("test-output/test.dot");
        return result;
    }

    /**
     * Backwards compatibility with old tests.
     * 
     * @param inv
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public MCResult verifyAndPrint(BinaryInvariant inv) throws IOException,
            InterruptedException {
        List<BinaryInvariant> invs = Util.newList();
        invs.add(inv);
        return verifyAndPrint(invs).get(0);
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
    /**
     * This test case contains a CFSM that starts in the accept state. The expected counterexample is an empty execution.
     */
    public void verifyEventuallyUnsafe2() throws Exception {
        // Constructs a new CFSM with two FSMs. The first FSM has a self loop on
        // the init/accept state.
        // The expected counterexample should have no events in the trace.

        FSMState p0InitAccept = new FSMState(true, true, 0, 0);
        states = Util.newSet();
        states.add(p0InitAccept);
        p0InitAccept.addTransition(p0Le, p0InitAccept);
        f0 = new FSM(0, p0InitAccept, p0InitAccept, states, 1);

        FSMState p1InitAccept = new FSMState(true, true, 1, 0);
        states = Util.newSet();
        states.add(p1InitAccept);
        f1 = new FSM(1, p1InitAccept, p1InitAccept, states, 2);

        channels = Util.newList();
        channels.add(cid);
        cfsm = new CFSM(2, channels);
        cfsm.addFSM(f0);
        cfsm.addFSM(f1);

        EventuallyHappens inv = new EventuallyHappens(p0Le);
        MCResult result = verifyAndPrint(inv);
        assertTrue(!result.modelIsSafe());
        assertTrue(result.getCExample() != null);
        assertTrue(result.getCExample().getEvents().size() == 0);
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

    /**
     * This test checks if we don't accidentally catch a (e NFby e) where both
     * events are the same. The CFSM is a simple one with no loop to accommodate
     * that.
     */
    @Test
    public void verifyNFbySafe2() throws Exception {
        simplifyCFSM();
        NeverFollowedBy inv = new NeverFollowedBy(p0Sm, p0Sm);
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
    public void verifyNFbyUnsafe2() throws Exception {
        NeverFollowedBy inv = new NeverFollowedBy(p1Rm, p0Sm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(!result.modelIsSafe());
        assertTrue(result.getCExample() != null);
    }

    /**
     * This test checks if we do catch a (e NFby e) where both events are the
     * same. The CFSM has a cycle. This ensures that although the CFSM does
     * reach an end state for both processes without cycling, it will consider
     * the case where it does repeat.
     */
    @Test
    public void verifyNFbyUnsafe3() throws Exception {
        NeverFollowedBy inv = new NeverFollowedBy(p0Sm, p0Sm);
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
        AlwaysPrecedes inv = new AlwaysPrecedes(p1Rm, p0Sm);
        MCResult result = verifyAndPrint(inv);
        assertTrue(!result.modelIsSafe());
        assertTrue(result.getCExample() != null);
    }

    @Test
    public void verifyMultiple() throws Exception {
        List<BinaryInvariant> invs = Util.newList();
        invs.add(new EventuallyHappens(p1Rm)); // Safe
        invs.add(new AlwaysFollowedBy(p0Sm, p1Rm)); // Safe
        invs.add(new NeverFollowedBy(p0Sm, p1Rm)); // Unsafe
        invs.add(new NeverFollowedBy(p1Rm, p0Sm)); // Unsafe
        invs.add(new NeverFollowedBy(p0Sm, p0Sm)); // Unsafe
        invs.add(new AlwaysPrecedes(p0Sm, p1Rm)); // Safe
        invs.add(new AlwaysPrecedes(p1Rm, p0Sm)); // Unsafe
        Map<Integer, MCResult> result = verifyAndPrint(invs);
        assertTrue(result.get(0).modelIsSafe());
        assertTrue(result.get(1).modelIsSafe());
        assertTrue(!result.get(2).modelIsSafe());
        assertTrue(!result.get(3).modelIsSafe());
        assertTrue(!result.get(4).modelIsSafe());
        assertTrue(result.get(5).modelIsSafe());
        assertTrue(!result.get(6).modelIsSafe());
    }

}
