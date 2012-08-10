package dynoptic.model.fifosys.gfsm.trace;

import java.util.List;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.AbsMultiFSMState;

/**
 * 
 */
public class ObsMultFSMState extends AbsMultiFSMState<ObsMultFSMState> {

    // Observed FSM states tuple. The list is ordered according to process
    // IDs.
    private List<ObservedFSMState> fsmStates;

    public ObsMultFSMState(List<ObservedFSMState> fsmStates) {
        super(fsmStates.size());
        this.fsmStates = fsmStates;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        String ret = "[";
        for (ObservedFSMState s : fsmStates) {
            ret = ret + "," + s.toString();
        }
        return ret + "]";
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
        for (ObservedFSMState s : fsmStates) {
            if (!s.isInitial()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAccept() {
        for (ObservedFSMState s : fsmStates) {
            if (!s.isTerminal()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<? extends EventType> getTransitioningEvents() {
        assert false : "Transition information for ObsMultFSMState is maintained by ObservedFifoSysState instances.";
        return null;
    }

    @Override
    public Set<ObsMultFSMState> getNextStates(EventType event) {
        assert false : "Transition information for ObsMultFSMState is maintained by ObservedFifoSysState instances.";
        return null;
    }

    @Override
    public int hashCode() {
        return fsmStates.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return fsmStates.equals(o);
    }

}
