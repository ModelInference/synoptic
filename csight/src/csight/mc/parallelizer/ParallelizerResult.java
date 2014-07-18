package csight.mc.parallelizer;

import csight.invariants.BinaryInvariant;

/**
 * Parallelizer results sent by Parallelizer to CSightMain.
 */
public class ParallelizerResult {

    private final BinaryInvariant inv;
    private final boolean safe;
    private final boolean isTimeout;
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
            int refinementCounter) {
        return new ParallelizerResult(inv, false, true, false, null,
                refinementCounter);
    }

    /**
     * Builds a verification result representing safe/unsafe invariant
     * 
     * @param inv
     * @param safe
     * @param refinementCounter
     * @return
     */
    protected static ParallelizerResult verificationResult(BinaryInvariant inv,
            boolean safe, int refinementCounter) {
        return new ParallelizerResult(inv, safe, false, false, null,
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
        return new ParallelizerResult(null, false, false, true, e,
                refinementCounter);
    }

    /**
     * Builds an exceptional result with no refinement counter
     * 
     * @param e
     * @return
     */
    protected static ParallelizerResult exceptionResult(Exception e) {
        return new ParallelizerResult(null, false, false, true, e, -1);
    }

    /**
     * Constructs a ParallelizerResult
     * 
     * @param inv
     * @param safe
     * @param isTimeout
     * @param isException
     * @param e
     * @param refinementCounter
     */
    private ParallelizerResult(BinaryInvariant inv, boolean safe,
            boolean isTimeout, boolean isException, Exception e,
            int refinementCounter) {
        this.inv = inv;
        this.safe = safe;
        this.isTimeout = isTimeout;
        this.isException = isException;
        this.e = e;
        this.refinementCounter = refinementCounter;
    }

    public BinaryInvariant getInvariant() {
        return inv;
    }

    public boolean isSafe() {
        return safe;
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