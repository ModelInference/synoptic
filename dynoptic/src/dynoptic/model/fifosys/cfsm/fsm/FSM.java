package dynoptic.model.fifosys.cfsm.fsm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import dynoptic.main.DynopticMain;
import dynoptic.model.AbsFSM;
import dynoptic.model.fifosys.channel.channelid.LocalEventsChannelId;

import synoptic.model.event.DistEventType;

/**
 * This class models FSMs that make up a CFSM. A few key characteristics:
 * 
 * <pre>
 * 1. It does not maintain channel state. This is done by FifoState/FifoSysExecution instances.
 * 2. Almost all fields are immutable. Pre-construct all FSMState instances prior to constructing this FSM.
 * 3. It does not maintain inter-state transitions. These are managed by FSMState instances.
 * </pre>
 */
public class FSM extends AbsFSM<FSMState> {
    // The process id of this FSM in the CFSM.
    final int pid;

    // This keeps track of the scmId to be used by FSMState instances associated
    // with this FSM.
    private int nextScmFSMStateId = 0;

    public FSM(int pid, FSMState initState, FSMState acceptState,
            Set<FSMState> states, int nextScmFSMStateId) {
        this(pid, Collections.singleton(initState), Collections
                .singleton(acceptState), states, nextScmFSMStateId);
    }

    public FSM(int pid, Set<FSMState> initStates, Set<FSMState> acceptStates,
            Collection<FSMState> states, int nextScmFSMStateId) {
        super();

        assert states != null;
        assert states.containsAll(initStates);
        assert states.containsAll(acceptStates);
        assert nextScmFSMStateId >= 0;

        if (DynopticMain.assertsOn) {
            // Check that:
            // 1. all states have unique scm ids.
            // 2. all states transition only to states in the states collection
            // 3. all states have a pid that matches the pid of this FSM
            // 4. all init/accept states are in fact init/accept
            Set<Integer> scmIds = new LinkedHashSet<Integer>();
            for (FSMState s : states) {
                assert !scmIds.contains(s.getScmId());
                scmIds.add(s.getScmId());
                assert (states.containsAll(s.getNextStates()));
                assert s.getPid() == pid;
                assert nextScmFSMStateId > s.getScmId();
            }

            for (FSMState s : initStates) {
                assert s.isInitial();
            }
            for (FSMState s : acceptStates) {
                assert s.isAccept();
            }
        }

        this.pid = pid;
        this.nextScmFSMStateId = nextScmFSMStateId;
        this.states.addAll(states);
        this.initStates.addAll(initStates);
        this.acceptStates.addAll(acceptStates);

        // Construct the alphabet from the events associated with each state.
        this.recomputeAlphabet();
    }

    // //////////////////////////////////////////////////////////////////

    /** Adds a new synthetic state for tracking events for invariants checking. */
    public void addSyntheticState(FSMState parent, FSMState child,
            DistEventType eToTrace, DistEventType eTracer1,
            DistEventType eTracer2) {
        assert this.states.contains(parent);
        assert this.states.contains(child);

        parent.rmTransition(eToTrace, child);

        FSMState synthState1 = new FSMState(parent.isAccept(),
                parent.isInitial(), pid, nextScmFSMStateId);
        nextScmFSMStateId++;

        FSMState synthState2 = new FSMState(child.isAccept(),
                child.isInitial(), pid, nextScmFSMStateId);
        nextScmFSMStateId++;

        // The tracer events t1 and t2 flank the event to trace so that a queue
        // sequence of 't1t2' would indicate exactly when the event to trace
        // occurred.
        parent.addSynthTransition(eTracer1, synthState1);
        synthState1.addTransition(eToTrace, synthState2);
        synthState2.addSynthTransition(eTracer2, child);

        this.states.add(synthState1);
        this.states.add(synthState2);
    }

    public int getPid() {
        return this.pid;
    }

    /**
     * Generate SCM representation of this FSM, using a specific channelIds
     * ordering.
     */
    public String toScmString(LocalEventsChannelId localEventsChId) {
        String ret;

        ret = null;
        for (FSMState s : initStates) {
            if (ret == null) {
                ret = "initial : " + s.getScmId();
            } else {
                ret += " , " + s.getScmId();
            }
        }
        ret += "\n";

        for (FSMState s : states) {
            ret += s.toScmString(localEventsChId);
            ret += "\n\n";
        }

        return ret;
    }
}
