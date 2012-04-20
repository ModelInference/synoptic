package synoptic.util;

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
