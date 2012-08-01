package mcscm;

public class VerifyOutputParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public VerifyOutputParseException(String reason) {
        super(reason);
    }
}
