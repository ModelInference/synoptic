package dynoptic.model.fifosys.gfsm.observed;

import java.util.List;
import java.util.Set;

import dynoptic.model.fifosys.AbsMultiFSMState;

import synoptic.model.event.DistEventType;

/**
 * Represents the instantaneous observed state across all of the processes in a
 * FIFO system. This does not include the queue state of the processes.
 */
public class ObsMultFSMState extends AbsMultiFSMState<ObsMultFSMState> {

    // Observed FSM states tuple. The list is ordered according to process
    // IDs.
    protected List<ObsFSMState> fsmStates;

    public ObsMultFSMState(List<ObsFSMState> fsmStates) {
        super(fsmStates.size());
        this.fsmStates = fsmStates;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        String ret = "";
        for (ObsFSMState s : fsmStates) {
            ret += ", " + s.toString();
        }
        return "FSMStates[" + ret.substring(2) + "]";
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        return fsmStates.get(pid).isTerminal();
    }

    @Override
    public boolean isInitForPid(int pid) {
        return fsmStates.get(pid).isInitial();
    }

    @Override
    public boolean isInitial() {
        for (ObsFSMState s : fsmStates) {
            if (!s.isInitial()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAccept() {
        for (ObsFSMState s : fsmStates) {
            if (!s.isTerminal()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return fsmStates.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ObsMultFSMState)) {
            return false;
        }
        ObsMultFSMState S = (ObsMultFSMState) o;
        return fsmStates.equals(S.fsmStates);
    }

    @Override
    public Set<DistEventType> getTransitioningEvents() {
        // We do not maintain transitions here because these need to depend on
        // the state of the queue, and queue state is not maintained here.
        assert false : "Transition information for ObsMultFSMState is maintained by ObservedFifoSysState instances.";
        return null;
    }

    @Override
    public Set<ObsMultFSMState> getNextStates(DistEventType event) {
        // We do not maintain transitions here because these need to depend on
        // the state of the queue, and queue state is not maintained here.
        assert false : "Transition information for ObsMultFSMState is maintained by ObservedFifoSysState instances.";
        return null;
    }
    
    public List<ObsFSMState> getFSMStates() {
        return fsmStates;
    }
}
