package synoptic.util.resource;

/**
 * To be comparable, two vector timestamps must have the same length. This
 * exception is thrown when attempting to compare two vector timestamps that
 * have different vector lengths.
 */
public class NotComparableVectorsException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;
    public AbstractResource e1;
    public AbstractResource e2;

    public NotComparableVectorsException(AbstractResource e1, AbstractResource e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}
