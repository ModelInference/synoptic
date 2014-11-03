package csight.mc;

/**
 * Wraps/represents a command line scm/promela (model checker's input file
 * format) syntax error as a Java exception.
 */
public class MCSyntaxException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MCSyntaxException(String reason) {
        super(reason);
    }
}
