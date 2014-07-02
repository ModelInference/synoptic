package csight.mc;

/**
 * An exception in parsing McScM verify (model checker) or Spin trail command
 * line output.
 */
public class VerifyOutputParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public VerifyOutputParseException(String reason) {
        super(reason);
    }
}
