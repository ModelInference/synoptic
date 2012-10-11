package dynoptic.model.fifosys.cfsm;

import java.util.ArrayList;
import java.util.List;

import mcscm.McScMCExample;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;

public class CFSMExecution {
    private final List<FSMState> fsmPath;
    private final McScMCExample cexample;

    public CFSMExecution(McScMCExample cexample) {
        this.fsmPath = new ArrayList<FSMState>();
        this.cexample = cexample;
    }

    public void addToPath(FSMState state) {
        fsmPath.add(state);
    }

    public List<FSMState> getFSMPath() {
        return fsmPath;
    }

}
