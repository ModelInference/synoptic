package synoptic.util.time;

/**
 * Typically, vector timestamps are considered to be unique -- there is one per
 * event in the entire system. This exception is thrown when we have found two
 * vector timestamps to be equivalent.
 */
public class EqualVectorTimestampsException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;
    public ITime e1;
    public ITime e2;

    public EqualVectorTimestampsException(ITime e1, ITime e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}
