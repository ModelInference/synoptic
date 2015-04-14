package synoptic.util.resource;

/**
 * Thrown when the expected resource type identified by key does not match the
 * resource that was supplied.
 */
public class WrongResourceTypeException extends RuntimeException {

    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;
    public String expectedKey;
    public AbstractResource e;

    public WrongResourceTypeException(String key, AbstractResource delta) {
        expectedKey = key;
        e = delta;
    }
}
