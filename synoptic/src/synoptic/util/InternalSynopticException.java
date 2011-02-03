package synoptic.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class InternalSynopticException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;

    /**
     * The java exception that this is wrapping.
     */
    Exception javaException = null;

    /**
     * The human readable message to display, in the case that we are not
     * wrapping a java exception.
     */
    String errMessage = null;

    public InternalSynopticException(Exception e) {
        javaException = e;
    }

    public InternalSynopticException(String errMsg) {
        errMessage = errMsg;
    }

    @Override
    public String toString() {
        String ret = new String("Internal error, notify developers.\n");

        if (errMessage != null) {
            ret += "Error: " + errMessage;
        }

        ret += "Error traceback:\n";
        Exception exceptToPrint = this;
        if (javaException != null) {
            exceptToPrint = javaException;
        }
        StringWriter sw = new StringWriter();
        exceptToPrint.printStackTrace(new PrintWriter(sw));
        ret += sw.toString();

        return ret;
    }

}
