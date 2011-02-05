package synoptic.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class InternalSynopticException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;

    /**
     * The human readable message to display, in the case that we are not
     * wrapping a java exception.
     */
    String errMessage = null;

    /**
     * The stack trace for this exception -- initialized in the constructors.
     */
    String stackTrace = "";

    /**
     * Create an exception based on some internal error.
     * 
     * @param errMsg
     *            Error message to display to the user
     */
    public InternalSynopticException(String errMsg) {
        errMessage = errMsg;
        StringWriter sw = new StringWriter();
        super.printStackTrace(new PrintWriter(sw));
        stackTrace = sw.toString();
    }

    /**
     * Create an internal exception based on a Java exception
     * 
     * @param e
     *            Some Java exception
     */
    private InternalSynopticException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        stackTrace = sw.toString();
    }

    /**
     * Create an internal exception wrapper around a Java exception. Unless the
     * exception itself is an internal exception.
     * 
     * @param e
     *            Some Java exception
     * @return a InternalSynopticException wrapper for the Java exception
     */
    public static InternalSynopticException Wrap(Exception e) {
        if (e instanceof InternalSynopticException) {
            return (InternalSynopticException) e;
        }
        return new InternalSynopticException(e);
    }

    @Override
    public String toString() {
        String ret = new String("Internal error, notify developers.\n");

        if (errMessage != null) {
            ret += "Error: " + errMessage + "\n";
        }
        ret += "Error traceback:\n";
        ret += stackTrace;
        return ret;
    }

}
