package synoptic.main;

import java.io.Serializable;

public class ParseException extends Exception implements Serializable {

    /**
     *  Conflicting regular expression
     */
	private String regex;
	
    /**
     *  Conflicting log entry
     */
	private String logLine;
	
	/**
	 * Exception version id
	 */
	private static final long serialVersionUID = -4455111019098315998L;

	public ParseException() {
		super();
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @return Nullable string describing conflicting regular expression
	 */
	public String getRegex() {
		return regex;
	}
	
	public void setRegex(String regex) {
		this.regex = regex;
	}
	
	public boolean hasRegex() {
		return regex != null;
	}
	
	public String getLogLine() {
		return logLine;
	}
	
	public void setLogLine(String logLine) {
		this.logLine = logLine;
	}
	
	public boolean hasLogLine() {
		return logLine != null;
	}
	
}