package csight.model.fifosys.cfsm;

import java.util.List;

import csight.mc.MCcExample;
import csight.model.fifosys.cfsm.fsm.FSMState;
import csight.util.Util;

public class CFSMExecution {
    private final List<FSMState> fsmPath;
    private final MCcExample cexample;

    public CFSMExecution(MCcExample cexample) {
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
