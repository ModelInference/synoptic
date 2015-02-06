package synoptic.util.resource;

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
    public AbstractResource e1;
    public AbstractResource e2;

    public EqualVectorTimestampsException(AbstractResource e1, AbstractResource e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}
