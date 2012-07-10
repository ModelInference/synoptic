package mcscm;

public class ScmSyntaxException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ScmSyntaxException(String reason) {
        super(reason);
    }

}
