package csight.mc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import csight.invariants.BinaryInvariant;
import csight.model.fifosys.cfsm.CFSM;
import csight.model.fifosys.gfsm.GFSM;
import csight.util.Util;

/**
 * A model-checker process runner class for running  and managing
 * multiple mc processes in parallel
 */
public abstract class MCRunner {
    /** Complete path to the model checker binary (e.g., McScM verify). */
    protected final String mcPath;
    
    /** The number of parallel process to run */
    protected final int numParallel;
    
    /** The list of invariants that were ran in parallel */
    private List<BinaryInvariant> invsRan;
    
    /** The result of the first returned invariant */
    private MCRunnerResult result;
    
    /** The ExecutorService used to run processes in parallel */
    private ExecutorService eService;
    
    public MCRunner(String mcPath, int numParallel) {
        this.mcPath = mcPath;
        this.numParallel = numParallel;
        this.eService = Executors.newFixedThreadPool(numParallel);
    }
    
    /**
     * Runs multiple model checkers in parallel to check the supplied model
     * against the invariants. Times out the process after timeoutSecs
     * 
     * @param pGraph
     *            - The GFSM model that will be checked and refined to satisfy all
     *              of the invariants in invsTocheck
     * @param invs
     *            - CSight invariants to check and satisfy in pGraph
     * @param timeOut
     *            - Seconds to run model checking before timeout
     * @param minimize
     *            - whether to minimize each process of the FSM
     * @return 
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException 
     * @throws ExecutionException 
     */
    public void verify(GFSM pGraph, List<BinaryInvariant> invs, int timeOut,
            boolean minimize) throws IOException, ExecutionException,
            TimeoutException, InterruptedException {
        invsRan = getInvsToRun(invs);
        List<Callable<MCRunnerResult>> callables = getCallablesToRun(pGraph,
                invsRan, minimize);
        assert invsRan.size() == callables.size();
        
        // TODO: add logging at appropriate locations in right format
        result = eService.invokeAny(callables, timeOut, TimeUnit.SECONDS);
    }

    /**
     * Returns a MCResult of the successfully checked invariant
     * @return
     */
    public MCResult getMCResult() {
        return result.getMCResult();
    }
    
    /**
     * Returns the invariant that was successfully checked
     * @return
     */
    public BinaryInvariant getResultInvariant() {
        return result.getInv();
    }
    
    /**
     * Returns the list of invariants that were attempted to run
     */
    public List<BinaryInvariant> getInvariantsRan() {
        return invsRan;
    }
    
    /**
     * Returns the list of invariants that are to be ran in parallel
     * @return
     */
    private List<BinaryInvariant> getInvsToRun(List<BinaryInvariant> invs) {
        List<BinaryInvariant> invsToRun = Util.newList();
        for (int i=0; i < invs.size() && i < numParallel; i++) {
            invsToRun.add(invs.get(i));
        }
        return invsToRun;
    }
    
    /**
     * Creates a new model checker
     * @return
     */
    protected abstract MC initMC();
    
    /**
     * Returns an input string for model checking from a CFSM model and
     * an invariant to check
     * @return
     * @throws Exception 
     */
    protected abstract String prepareMCInputString(CFSM cfsm,
            BinaryInvariant curInv) throws Exception;
    
    /**
     * Returns a list of Callables to run in parallel with ExecutorService
     * given a list of invariants to check
     * @param pGraph
     * @param invsToRun
     * @param minimize
     * @return
     */
    protected abstract List<Callable<MCRunnerResult>> getCallablesToRun(final GFSM pGraph,
            final List<BinaryInvariant> invsToRun, final boolean minimize);
    
    /**
     * A result class that stores the completed invariant
     * and its corresponding MCResult
     */
    protected final class MCRunnerResult {
        
        /** The invariant that was model-checked */
        private final BinaryInvariant inv;
        
        /** The MCResult of the checked invariant */
        private final MCResult mcResult;
        
        public MCRunnerResult(BinaryInvariant inv, MCResult res) {
            this.inv = inv;
            this.mcResult = res;
        }
        
        public BinaryInvariant getInv() {
            return inv;
        }
        
        public MCResult getMCResult() {
            return mcResult;
        }
    }
}
