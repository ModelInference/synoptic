package synopticgwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SerializableParseException extends Exception implements IsSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4529834915235068303L;
	

	private String regex;
	private String logLine;
	private String errMsg;
	
	public SerializableParseException() {
		super();
	}
	
	public SerializableParseException(String errMsg) {
		super(errMsg);
	}
	
	public SerializableParseException(String errMsg, Throwable cause) {
		super(errMsg, cause);
	}
	
	public SerializableParseException(Throwable cause) {
		super(cause);
	}
	
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
