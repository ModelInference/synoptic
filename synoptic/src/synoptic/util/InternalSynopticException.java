package synoptic.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class InternalSynopticException extends Exception {
	/**
	 * Unique version uid 
	 */
	private static final long serialVersionUID = 1L;
	
	public String toString() {
		String ret = new String("Internal error, notify developers. Error traceback:\n");
		StringWriter sw = new StringWriter();
		this.printStackTrace(new PrintWriter(sw));
		ret += sw.toString();
		return ret;
  	}

}
