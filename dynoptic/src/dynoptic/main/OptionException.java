package dynoptic.main;

/**
 * Represents an issue/early-termination raised by command line options
 * processing.
 */
public class OptionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // The error string corresponding to this exception.
    private final String err;

    public OptionException(String err) {
        this.err = err;
    }

    @Override
    public String toString() {
        return err;
    }

}
