package csight.mc;

import java.io.IOException;
import java.util.List;

import csight.invariants.BinaryInvariant;
import csight.model.fifosys.gfsm.GFSM;

/**
 * A model-checker process runner class for running  and managing
 * multiple mc processes in parallel
 */
public abstract class MCRunner {
    /** Complete path to the model checker binary (e.g., McScM verify). */
    protected String mcPath;
    
    /** The number of parallel process to run */
    protected int numParallel;
    
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
     * @throws IOException
     * @throws InterruptedException
     */
    public void verify(GFSM pGraph, List<BinaryInvariant> invs, int timeOut,
            boolean minimize) throws IOException, InterruptedException {
        // TODO: use Java ExecutorService to run multiple mc process
        // use invokeany
    }
    
    protected abstract void prepareMcInputString();
}