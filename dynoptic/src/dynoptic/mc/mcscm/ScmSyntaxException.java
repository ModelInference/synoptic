package dynoptic.mc.mcscm;

/**
 * Wraps/represents a command line scm (model checker's input file format)
 * syntax error as a Java exception.
 */
public class ScmSyntaxException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ScmSyntaxException(String reason) {
        super(reason);
    }
}
