package dynoptic.model.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class GraphExporterTests extends DynopticTest {

    int pid0 = 0;
    int pid1 = 1;

    // Initial and accept state for pid 0
    FSMState s_0;
    // Initial state for pid 0
    FSMState s0;
    // Accept state for pid 0
    FSMState s1;
    // Initial state for pid 1
    FSMState t0;
    // Accept state for pid 1
    FSMState t1;

    // Channels used in CFSM
    List<ChannelId> channels;

    @Override
    public void setUp() {
        s_0 = new FSMState(true, true, pid0, 0);
        s0 = new FSMState(false, true, pid0, 0);
        s1 = new FSMState(true, false, pid0, 1);
        t0 = new FSMState(false, true, pid1, 0);
        t1 = new FSMState(true, false, pid1, 1);

        // Channel connecting pid 0 and pid 1
        ChannelId chId = new ChannelId(pid0, pid1, 0);
        channels = Util.newList(1);
        channels.add(chId);
    }

    @Test
    public void oneFSMOneStateTest() throws IOException {
        Set<FSMState> states = Util.newSet(1);
        states.add(s_0);

        FSM f0 = new FSM(pid0, s_0, s_0, states, 2);

        CFSM cfsm = new CFSM(1, Collections.<ChannelId> emptyList());
        cfsm.addFSM(f0);

        generateOutput(cfsm);
    }

    @Test
    public void oneFSMTwoStatesTest() throws IOException {
        Set<FSMState> states = Util.newSet(2);
        states.add(s0);
        states.add(s1);

        DistEventType e = DistEventType.LocalEvent("e", pid0);

        s0.addTransition(e, s1);

        FSM f0 = new FSM(pid0, s0, s1, states, 2);

        CFSM cfsm = new CFSM(1, Collections.<ChannelId> emptyList());
        cfsm.addFSM(f0);

        generateOutput(cfsm);
    }

    @Test
    public void twoFSMsTwoStatesEachLocalEventsTest() throws IOException {
        Set<FSMState> p0States = Util.newSet(2);
        p0States.add(s0);
        p0States.add(s1);

        DistEventType e0 = DistEventType.LocalEvent("e0", pid0);

        s0.addTransition(e0, s1);

        FSM f0 = new FSM(pid0, s0, s1, p0States, 2);

        Set<FSMState> p1States = Util.newSet(2);
        p1States.add(t0);
        p1States.add(t1);

        DistEventType e1 = DistEventType.LocalEvent("e1", pid1);

        t0.addTransition(e1, t1);

        FSM f1 = new FSM(pid1, t0, t1, p1States, 2);

        CFSM cfsm = new CFSM(2, channels);
        cfsm.addFSM(f0);
        cfsm.addFSM(f1);

        generateOutput(cfsm);
    }

    @Test
    public void twoFSMsTwoStatesEachCommEventsTest() throws IOException {
        Set<FSMState> p0States = Util.newSet(2);
        p0States.add(s0);
        p0States.add(s1);

        DistEventType sendM = DistEventType.SendEvent("m", channels.get(0));

        s0.addTransition(sendM, s1);

        FSM f0 = new FSM(pid0, s0, s1, p0States, 2);

        Set<FSMState> p1States = Util.newSet(2);
        p1States.add(t0);
        p1States.add(t1);

        DistEventType recvM = DistEventType.RecvEvent("m", channels.get(0));

        t0.addTransition(recvM, t1);

        FSM f1 = new FSM(pid1, t0, t1, p1States, 2);

        CFSM cfsm = new CFSM(2, channels);
        cfsm.addFSM(f0);
        cfsm.addFSM(f1);

        generateOutput(cfsm);
    }

    /**
     * Generates test.dot for cfsm and test.dot*.png for each fsm in cfsm.
     */
    private void generateOutput(CFSM cfsm) throws IOException {
        cleanDotOutputs();
        GraphExporter.exportCFSM(DOT_OUTPUT_FILENAME, cfsm);
        assertTrue(new File(DOT_OUTPUT_FILENAME).exists());

        GraphExporter.generatePngFileFromDotFile(DOT_OUTPUT_FILENAME);
        int numFSMs = cfsm.getFSMs().size();
        int numDotPngFiles = getNumDotPngFiles();
        assertEquals(numFSMs, numDotPngFiles);
    }

}
