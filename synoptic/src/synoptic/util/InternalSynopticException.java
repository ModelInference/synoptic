package synoptic.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class InternalSynopticException extends Exception {
	/**
	 * Unique version uid 
	 */
	private static final long serialVersionUID = 1L;
	
	Exception javaException = null;
	
	public InternalSynopticException(Exception e) {
		this.javaException = e;
	}

	public String toString() {
		String ret = new String("Internal error, notify developers. Error traceback:\n");
		Exception exceptToPrint = this;
		if (this.javaException != null) {
			exceptToPrint = this.javaException;
		}
		StringWriter sw = new StringWriter();
		exceptToPrint.printStackTrace(new PrintWriter(sw));
		ret += sw.toString();
		return ret;
  	}

}
