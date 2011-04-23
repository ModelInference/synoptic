package synoptic.model;

import synoptic.main.Main;
import synoptic.util.time.ITime;

/**
 * Represents an event parsed from a log file. Each event needs at least a name,
 * called a label. Optionally, a time and data fields can set. If data fields
 * will be used, {@code useDatafields} must be set before compilation.
 * 
 * @author Sigurd Schneider
 */
public class Event {
    /**
     * The event's label.
     */
    private final String label;

    /**
     * The time this event occurred.
     */
    private ITime time;

    /**
     * The complete log line corresponding to this event.
     */
    private final String logLine;

    /**
     * The filename from where the label for this event was parsed.
     */
    private final String fileName;

    /**
     * The line number from where the label for this event was parsed.
     */
    private final int lineNum;

    /**
     * The host identifier -- set when vector time is used.
     * 
     * <pre>
     * TODO: modify constructors to set this appropriately.
     * </pre>
     */
    private final int hostId = 0;

    /**
     * Create an event with a label. Does _not_ check for collisions with
     * internally used labels (e.g., INITIAL).
     * 
     * @param label
     *            the label for the event
     * @param isSpecialLabel
     * @param logLine
     * @param fileName
     * @param lineNum
     */
    public Event(String label, boolean isSpecialLabel, String logLine,
            String fileName, int lineNum) {
        this.label = label;
        this.logLine = logLine;
        this.fileName = fileName;
        this.lineNum = lineNum;
        if (!isSpecialLabel) {
            // TODO: translate labels so that collisions such as this do not
            // occur.
            if (label.equals(Main.initialNodeLabel)
                    || label.equals(Main.terminalNodeLabel)) {
                throw new IllegalArgumentException(
                        "Cannot create a node with label '"
                                + label
                                + "' because it conflicts with internal INITIAL/TERMINAL Synoptic labels.");
            }
        }
    }

    /**
     * Create an event with a label.
     */
    public Event(String label, String logLine, String fileName, int lineNum) {
        this(label, false, logLine, fileName, lineNum);

    }

    public Event(String label) {
        this(label, null, null, 0);
    }

    /**
     * Returns the special INITIAL event.
     */
    public static Event newInitialEvent() {
        return new Event(Main.initialNodeLabel, true, null, null, 0);
    }

    /**
     * Returns the special terminal event.
     */
    public static Event newTerminalEvent() {
        return new Event(Main.terminalNodeLabel, true, null, null, 0);
    }

    @Override
    public String toString() {
        return label + "-" + time.toString();
    }

    /**
     * Get the label of the event.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + lineNum;
        result = prime * result + ((logLine == null) ? 0 : logLine.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Event other = (Event) obj;
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
            return false;
        }
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!label.equals(other.label)) {
            return false;
        }
        if (lineNum != other.lineNum) {
            return false;
        }
        if (logLine == null) {
            if (other.logLine != null) {
                return false;
            }
        } else if (!logLine.equals(other.logLine)) {
            return false;
        }
        if (time == null) {
            if (other.time != null) {
                return false;
            }
        } else if (!time.equals(other.time)) {
            return false;
        }
        return true;
    }

    /**
     * Set the time when this event occurred.
     * 
     * @param t
     *            the time
     */
    public void setTime(ITime t) {
        time = t;
    }

    /**
     * Get the time of this event.
     * 
     * @return the time when this event occurred.
     */
    public ITime getTime() {
        return time;
    }

    public String getLine() {
        return logLine;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNum() {
        return lineNum;
    }
}
