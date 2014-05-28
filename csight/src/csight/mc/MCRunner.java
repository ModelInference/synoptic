package csight.mc;

import java.io.IOException;
import java.util.List;

import csight.invariants.BinaryInvariant;
import csight.model.fifosys.cfsm.CFSM;
import csight.model.fifosys.gfsm.GFSM;

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
    protected List<BinaryInvariant> invsRan;
    
    /** The result of the first returned invariant */
    private MCRunnerResult result;
    
    public MCRunner(String mcPath, int numParallel) {
        this.mcPath = mcPath;
        this.numParallel = numParallel;
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
     */
    public void verify(GFSM pGraph, List<BinaryInvariant> invs, int timeOut,
            boolean minimize) throws IOException, InterruptedException {
        // TODO: use Java ExecutorService to run multiple mc process
        // use invokeany
    }
    
    /**
     * Returns a MCResult of the successfully checked invariant
     * @return
     */
    public MCResult getMCResult() {
        return result.mcResult;
    }
    
    /**
     * Returns the invariant that was successfully checked
     * @return
     */
    public BinaryInvariant getResultInvariant() {
        return result.inv;
    }
    
    /**
     * Returns the list of invariants that were attempted to run
     */
    public List<BinaryInvariant> getInvariantsRan() {
        return invsRan;
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
     * A result class that stores the completed invariant
     * and its corresponding MCResult
     */
    private final class MCRunnerResult {
        
        /** The invariant that was model-checked */
        BinaryInvariant inv;
        
        /** The MCResult of the checked invariant */
        MCResult mcResult;
        
        private MCRunnerResult(BinaryInvariant inv, MCResult res) {
            this.inv = inv;
            this.mcResult = res;
        }
        
    }
}
