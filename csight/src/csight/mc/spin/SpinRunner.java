package csight.mc.spin;

import java.util.List;
import java.util.concurrent.Callable;

import csight.invariants.BinaryInvariant;
import csight.mc.MC;
import csight.mc.MCRunner;
import csight.model.fifosys.cfsm.CFSM;
import csight.model.fifosys.gfsm.GFSM;
import csight.util.Util;

public class SpinRunner extends MCRunner {

    private final int spinChannelCapacity;
    
    public SpinRunner(String mcPath, int numParallel, int spinCapacity) {
        super(mcPath, numParallel);
        spinChannelCapacity = spinCapacity;
    }
    
    @Override
    protected MC initMC() {
        return new Spin(mcPath);
    }

    @Override
    protected List<Callable<MCRunnerResult>> getCallablesToRun(GFSM pGraph,
            final List<BinaryInvariant> invsToRun, final boolean minimize) {
        final String verifyPath = mcPath;
        final int spinCapacity = spinChannelCapacity;
        
        List<Callable<MCRunnerResult>> callablesToRun = Util.newList();
        
        for (final BinaryInvariant inv : invsToRun) {
            final CFSM cfsm = pGraph.getCFSM(minimize);
            
            Callable<MCRunnerResult> callable = new Callable<MCRunnerResult>() {

                @Override
                public MCRunnerResult call() throws Exception {
                    
                    String mcInputStr = cfsm.toPromelaString(
                            "checking_pml_" + inv.getConnectorString(),
                            spinCapacity);

                    Spin spin = new Spin(verifyPath);
                    spin.verifyParallel(mcInputStr);

                    return new MCRunnerResult(inv, spin, cfsm);
                }
                
            };
            callablesToRun.add(callable);
        }
        return callablesToRun;
    }

}
