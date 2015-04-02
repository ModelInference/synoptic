package synoptic.util.resource;

/**
 * Resource operations are constrained to operate on the same types of resource
 * with the same key. This exception is thrown if two types of resource are
 * different, or incomparable.
 */
public class NonComparableResourceException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;
    public AbstractResource e1;
    public AbstractResource e2;

    public NonComparableResourceException(AbstractResource e1,
            AbstractResource e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}
