package synoptic.util.resource;

/**
 * Thrown when the expected time type does not match what was supplied.
 */
public class WrongTimeTypeException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;
    public AbstractResource e1;
    public AbstractResource e2;
}
