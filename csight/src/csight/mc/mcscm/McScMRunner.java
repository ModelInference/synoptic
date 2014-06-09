package csight.mc.mcscm;

import java.util.List;
import java.util.concurrent.Callable;

import csight.invariants.BinaryInvariant;
import csight.mc.MC;
import csight.mc.MCRunner;
import csight.model.fifosys.cfsm.CFSM;
import csight.model.fifosys.gfsm.GFSM;
import csight.util.Util;

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

    /**
     * Returns a list of Callables to run in parallel with ExecutorService
     * given a list of invariants to run
     * @param pGraph 
     * @param invs
     * @param minimize 
     * @return
     */
    protected List<Callable<MCRunnerResult>> getCallablesToRun(GFSM pGraph,
            final List<BinaryInvariant> invsToRun, final boolean minimize) {
        final String verifyPath = mcPath;
        List<Callable<MCRunnerResult>> callablesToRun = Util.newList();
        
        for (final BinaryInvariant inv : invsToRun) {
            final CFSM cfsm = pGraph.getCFSM(minimize);
            
            Callable<MCRunnerResult> callable = new Callable<MCRunnerResult>() {

                @Override
                public MCRunnerResult call() throws Exception {
                    cfsm.augmentWithInvTracing(inv);
                    
                    String mcInputStr = cfsm.toScmString("checking_scm_"
                            + inv.getConnectorString());
                    System.out.println(mcInputStr);
                    McScM mcscm = new McScM(verifyPath);
                    mcscm.verifyParallel(mcInputStr);

                    return new MCRunnerResult(inv, mcscm, cfsm);
                }
                
            };
            callablesToRun.add(callable);
        }
        return callablesToRun;
    }
}
