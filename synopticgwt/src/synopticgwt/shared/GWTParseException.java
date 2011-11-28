package synopticgwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A GWT version of synoptic.main.ParseException.
 */
public class GWTParseException extends Exception implements IsSerializable {

    private static final long serialVersionUID = -4529834915235068303L;

    /**
     * Conflicting regular expression
     */
    private String regex;

    /**
     * Conflicting log entry
     */
    private String logLine;

    public GWTParseException(String message, Throwable cause, String regex,
            String logLine) {
        super(message, cause);
        this.setRegex(regex);
        this.setLogLine(logLine);
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
