package csight.mc.parallelizer;

import csight.mc.MCResult;

/**
 * Parallelizer results sent by Parallelizer to CSightMain through the results
 * channel.
 */
public class ParallelizerResult {

    private final InvariantTimeoutPair invTimeoutPair;
    private final MCResult mcResult;
    private final boolean isTimeout;
    private final boolean isException;
    private final Exception e;
    private final int refinementCounter;

    /**
     * Builds a timeout ParallelizerResult for invariant
     * 
     * @param invTimeoutPair
     * @param refinementCounter
     * @return
     */
    protected static ParallelizerResult timeOutResult(
            InvariantTimeoutPair invTimeoutPair, int refinementCounter) {
        return new ParallelizerResult(invTimeoutPair, null, true, false, null,
                refinementCounter);
    }

    /**
     * Builds a verification result representing safe/unsafe invariant
     * 
     * @param invTimeoutPair
     * @param mcResult
     * @param refinementCounter
     * @return
     */
    protected static ParallelizerResult verificationResult(
            InvariantTimeoutPair invTimeoutPair, MCResult mcResult,
            int refinementCounter) {
        return new ParallelizerResult(invTimeoutPair, mcResult, false, false,
                null, refinementCounter);
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
        return new ParallelizerResult(null, null, false, true, e,
                refinementCounter);
    }

    /**
     * Builds an exceptional result with no refinement counter
     * 
     * @param e
     * @return
     */
    protected static ParallelizerResult exceptionResult(Exception e) {
        return new ParallelizerResult(null, null, false, true, e, -1);
    }

    /**
     * Constructs a ParallelizerResult
     * 
     * @param invTimeoutPair
     * @param mcResult
     * @param isTimeout
     * @param isException
     * @param e
     * @param refinementCounter
     */
    private ParallelizerResult(InvariantTimeoutPair invTimeoutPair,
            MCResult mcResult, boolean isTimeout, boolean isException,
            Exception e, int refinementCounter) {
        this.invTimeoutPair = invTimeoutPair;
        this.mcResult = mcResult;
        this.isTimeout = isTimeout;
        this.isException = isException;
        this.e = e;
        this.refinementCounter = refinementCounter;
    }

    public InvariantTimeoutPair getInvTimeoutPair() {
        return invTimeoutPair;
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