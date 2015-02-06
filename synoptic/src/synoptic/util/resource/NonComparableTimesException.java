package synoptic.util.resource;

/**
 * Time operations are constrained to operate on the same types of time. This
 * exception is thrown if two types of time are different, or incomparable.
 */
public class NonComparableTimesException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;
    public AbstractResource e1;
    public AbstractResource e2;

    public NonComparableTimesException(AbstractResource e1, AbstractResource e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}
