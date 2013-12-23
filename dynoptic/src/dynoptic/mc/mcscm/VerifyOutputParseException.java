package dynoptic.mc.mcscm;

/** An exception in parsing McScM verify (model checker) command line output. */
public class VerifyOutputParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public VerifyOutputParseException(String reason) {
        super(reason);
    }
}
