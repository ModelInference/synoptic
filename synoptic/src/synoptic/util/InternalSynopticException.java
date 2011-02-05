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

    public InternalSynopticException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        stackTrace = sw.toString();
    }

    public InternalSynopticException(String errMsg) {
        errMessage = errMsg;
        StringWriter sw = new StringWriter();
        super.printStackTrace(new PrintWriter(sw));
        stackTrace = sw.toString();
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
