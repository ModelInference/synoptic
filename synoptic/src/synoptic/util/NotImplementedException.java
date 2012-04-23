package synoptic.util;

/**
 * Indicates that the desired functionality is not yet supported, but will be
 * supported in the future.
 */
public class NotImplementedException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;

    public NotImplementedException(String msg) {
        super(msg);
    }

    public NotImplementedException() {
        super();
    }
}
