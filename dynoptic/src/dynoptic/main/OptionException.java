package dynoptic.main;

/**
 * Represents an issue/early-termination raised by cmd line options processing.
 */
public class OptionException extends RuntimeException {

    private boolean printHelpException;

    public OptionException(String err) {
        super(err);
        printHelpException = false;
    }

    public OptionException() {
        super();
        printHelpException = true;
    }

    public boolean isPrintHelpException() {
        return printHelpException;
    }

    private static final long serialVersionUID = 1L;

}
