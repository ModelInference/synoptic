package dynoptic.model.fifosys.cfsm;

import java.util.List;

import mcscm.McScMCExample;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.util.Util;

public class CFSMExecution {
    private final List<FSMState> fsmPath;
    private final McScMCExample cexample;

    public CFSMExecution(McScMCExample cexample) {
        this.fsmPath = Util.newList();
        this.cexample = cexample;
    }

    public void addToPath(FSMState state) {
        fsmPath.add(state);
    }

    public List<FSMState> getFSMPath() {
        return fsmPath;
    }

}
