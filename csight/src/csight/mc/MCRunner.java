package csight.mc;

import java.io.IOException;
import java.util.List;

import csight.invariants.BinaryInvariant;

/**
 * A model-checker process runner class for running multiple
 * mc processes in parallel
 */
public class MCRunner {
    // List of Invariants to check
    private List<BinaryInvariant> invs;
    
    public MCRunner(List<BinaryInvariant> invs) {
        this.invs = invs;
    }
    
    // TODO: use Java Executor service to run multiple mc process
    public void verify() throws IOException, InterruptedException {
        //
    }
    
    /**
     * Stops all MC process and use the first result that is returned
     * @param mc the finished mc process
     */
    public synchronized void onResult(MC mc) {
        // TODO: stop all mc process when one terminates
    }
}