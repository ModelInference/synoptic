package csight.mc.spin;

import java.util.List;
import java.util.concurrent.Callable;

import csight.invariants.BinaryInvariant;
import csight.mc.MC;
import csight.mc.MCRunner;
import csight.model.fifosys.cfsm.CFSM;
import csight.model.fifosys.gfsm.GFSM;

public class SpinRunner extends MCRunner {

    @Override
    protected MC initMC() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String prepareMCInputString(CFSM cfsm, BinaryInvariant curInv)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<Callable<MCRunnerResult>> getCallablesToRun(GFSM pGraph,
            List<BinaryInvariant> invsToRun, boolean minimize) {
        // TODO Auto-generated method stub
        return null;
    }

}
