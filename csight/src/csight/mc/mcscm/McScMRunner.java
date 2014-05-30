package csight.mc.mcscm;

import csight.invariants.BinaryInvariant;
import csight.mc.MC;
import csight.mc.MCRunner;
import csight.model.fifosys.cfsm.CFSM;

public class McScMRunner extends MCRunner {

    public McScMRunner(String mcPath, int numParallel) {
        super(mcPath, numParallel);
    }

    @Override
    protected MC initMC() {
        return new McScM(mcPath);
    }

    @Override
    protected String prepareMCInputString(CFSM cfsm, BinaryInvariant curInv)
            throws Exception {
        // Augment the CFSM with synthetic states/events to check
        // curInv (only fone for McScM).
        cfsm.augmentWithInvTracing(curInv);

        return cfsm.toScmString("checking_scm_"
                + curInv.getConnectorString());
    }

}
