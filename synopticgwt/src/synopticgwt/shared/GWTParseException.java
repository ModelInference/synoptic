package synopticgwt.shared;

/**
 * A GWT version of synoptic.main.ParseException.
 */
public class GWTParseException extends GWTServerException {

    private static final long serialVersionUID = -4529834915235068303L;

    /**
     * Conflicting regular expression
     */
    private String regex;

    /**
     * Conflicting log entry
     */
    private String logLine;

    // DO NOT REMOVE.
    // Necessary to resolve a SerializationException:
    // ".. was not included in the set of types which can be serialized by this SerializationPolicy or its Class object could not be loaded"
    public GWTParseException() {
        //
    }

    public GWTParseException(String message, Throwable cause,
            String stackTrace, String regex, String logLine) {
        super(message, cause, stackTrace);
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
