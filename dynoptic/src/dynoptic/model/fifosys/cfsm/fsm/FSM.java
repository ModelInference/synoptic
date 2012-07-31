package dynoptic.model.fifosys.cfsm.fsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dynoptic.model.AbsFSM;
import dynoptic.model.fifosys.channel.ChannelId;

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

    public FSM(int pid, FSMState initState, FSMState acceptState,
            Set<FSMState> states) {
        this(pid, Collections.singleton(initState), Collections
                .singleton(acceptState), states);
    }

    public FSM(int pid, Set<FSMState> initStates, Set<FSMState> acceptStates,
            Collection<FSMState> states) {
        super();
        assert states != null;
        assert states.containsAll(initStates);
        assert states.containsAll(acceptStates);

        for (FSMState s : initStates) {
            assert s.isInitial();
        }
        for (FSMState s : acceptStates) {
            assert s.isAccept();
        }

        for (FSMState s : states) {
            assert s.getPid() == pid;
        }

        this.pid = pid;
        this.states.addAll(states);
        this.initStates.addAll(initStates);
        this.acceptStates.addAll(acceptStates);

        // Construct the alphabet from the events associated with each state.
        this.recomputeAlphabet();
    }

    // //////////////////////////////////////////////////////////////////

    public int getPid() {
        return this.pid;
    }

    /**
     * Generate SCM representation of this FSM, using a specific channelIds
     * ordering.
     */
    public String toScmString(Map<ChannelId, Integer> cIdsToInt) {
        String ret;

        // Build a map from [0...states.size()-1] to FSMState.
        List<FSMState> orderedStates = new ArrayList<FSMState>();
        Map<FSMState, Integer> statesToInt = new LinkedHashMap<FSMState, Integer>();
        int i = 0;
        for (FSMState s : states) {
            orderedStates.add(s);
            statesToInt.put(s, i);
            i++;
        }

        ret = null;
        for (FSMState s : initStates) {
            if (ret == null) {
                ret += "initial : " + statesToInt.get(s);
            } else {
                ret += " , " + statesToInt.get(s);
            }
        }
        ret += "\n";

        for (i = 0; i < orderedStates.size(); i++) {
            FSMState s = orderedStates.get(i);
            ret += "state " + Integer.toString(i) + " : \n";
            ret += s.toScmString(statesToInt, cIdsToInt);
            ret += "\n\n";
        }

        return ret;
    }
}
