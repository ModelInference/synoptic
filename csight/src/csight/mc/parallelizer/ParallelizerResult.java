package csight.mc.parallelizer;

import csight.invariants.BinaryInvariant;
import csight.mc.MCResult;

/**
 * Parallelizer results sent by Parallelizer to CSightMain through the results
 * channel.
 */
public class ParallelizerResult {

    private final BinaryInvariant inv;
    private final MCResult mcResult;
    private final boolean isTimeout;
    private final Integer timeout;
    private final boolean isException;
    private final Exception e;
    private final int refinementCounter;

    /**
     * Builds a timeout ParallelizerResult for invariant
     * 
     * @param inv
     * @param refinementCounter
     * @return
     */
    protected static ParallelizerResult timeOutResult(BinaryInvariant inv,
            int timeout, int refinementCounter) {
        return new ParallelizerResult(inv, null, true, timeout, false, null,
                refinementCounter);
    }

    /**
     * Builds a verification result representing safe/unsafe invariant
     * 
     * @param inv
     * @param mcResult
     * @param refinementCounter
     * @return
     */
    protected static ParallelizerResult verificationResult(BinaryInvariant inv,
            MCResult mcResult, int refinementCounter) {
        return new ParallelizerResult(inv, mcResult, false, null, false, null,
                refinementCounter);
    }

    /**
     * Builds an exceptional result with refinement counter
     * 
     * @param e
     * @param refinementCounter
     * @return
     */
    protected static ParallelizerResult exceptionResult(Exception e,
            int refinementCounter) {
        return new ParallelizerResult(null, null, false, null, true, e,
                refinementCounter);
    }

    /**
     * Builds an exceptional result with no refinement counter
     * 
     * @param e
     * @return
     */
    protected static ParallelizerResult exceptionResult(Exception e) {
        return new ParallelizerResult(null, null, false, null, true, e, -1);
    }

    /**
     * Constructs a ParallelizerResult
     * 
     * @param inv
     * @param mcResult
     * @param isTimeout
     * @param isException
     * @param e
     * @param refinementCounter
     */
    private ParallelizerResult(BinaryInvariant inv, MCResult mcResult,
            boolean isTimeout, Integer timeout, boolean isException,
            Exception e, int refinementCounter) {
        this.inv = inv;
        this.mcResult = mcResult;
        this.isTimeout = isTimeout;
        this.timeout = timeout;
        this.isException = isException;
        this.e = e;
        this.refinementCounter = refinementCounter;
    }

    public BinaryInvariant getInvariant() {
        return inv;
    }

    public MCResult getMCResult() {
        return mcResult;
    }

    public boolean isVerifyResult() {
        return !isTimeout && !isException;
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public boolean isException() {
        return isException;
    }

    public Exception getException() {
        return e;
    }

    public int getRefinementCounter() {
        return refinementCounter;
    }
}