package synoptic.util.time;

/**
 * Time operations are constrained to operate on the same types of time. This
 * exception is thrown if two types of time are different, or incomparable.
 */
public class NonComparableTimesException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;
    public ITime e1;
    public ITime e2;

    public NonComparableTimesException(ITime e1, ITime e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}
