package synoptic.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * The default brief log formatter used by Synoptic for logging to stdout.
 */
public class BriefLogFormatter extends Formatter {
    private static final String lineSep = System.getProperty("line.separator");

    /**
     * Shortened from: http://www.javalobby.org/java/forums/t18515.html
     */
    @Override
    public String format(LogRecord record) {
        StringBuilder output = new StringBuilder().append(record.getLevel())
                .append(": ").append(record.getMessage()).append(' ')
                .append(lineSep);
        return output.toString();
    }
}
