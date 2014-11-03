package synopticgwt.shared;

/**
 * A wrapper for all server-generated exceptions.
 */
public class GWTServerException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Stack trace of the server exception -- we have to store it explicitly, as
     * the client will not have access to it.
     */
    public String serverStackTrace;

    // DO NOT REMOVE.
    // Necessary to resolve a SerializationException:
    // ".. was not included in the set of types which can be serialized by this SerializationPolicy or its Class object could not be loaded"
    public GWTServerException() {
        //
    }

    public GWTServerException(String message, Throwable cause, String stackTrace) {
        super(message, cause);
        this.serverStackTrace = stackTrace;
    }
}
