package synoptic.model;

import synoptic.main.Main;
import synoptic.util.time.ITime;

/**
 * The action class abstracts an event. Each event needs at least a name, called
 * a label. Optionally, a vector time and data fields can set. If data fields
 * will be used, {@code useDatafields} must be set before compilation.
 * 
 * @author Sigurd Schneider
 */
public class Action {
    /**
     * The action's label.
     */
    String label;

    /**
     * The time this action occurred.
     */
    private ITime time;

    /**
     * The complete log line corresponding to this action.
     */
    String logLine;

    /**
     * The filename from where the label for this action was parsed.
     */
    String fileName;

    /**
     * The line number from where the label for this action was parsed.
     */
    int lineNum;

    /**
     * Create an action with a label. Do not check for collisions with
     * internally used labels.
     * 
     * @param label
     *            the label for the action
     * @param dummy
     *            unused
     */
    public Action(String label, boolean isSpecialLabel, String logLine,
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
     * Create an action with a label.
     * 
     * @param label
     *            the label for the action
     */
    public Action(String label, String logLine, String fileName, int lineNum) {
        this(label, false, logLine, fileName, lineNum);

    }

    public Action(String label) {
        this(label, null, null, 0);
    }

    /**
     * Returns the special initial action.
     */
    public static Action NewInitialAction() {
        return new Action(Main.initialNodeLabel, true, null, null, 0);
    }

    /**
     * Returns the special terminal action.
     */
    public static Action NewTerminalAction() {
        return new Action(Main.terminalNodeLabel, true, null, null, 0);
    }

    @Override
    public String toString() {
        return label + "-" + time.toString();
    }

    /**
     * Get the label of the action.
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
        Action other = (Action) obj;
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
     * Set the time when this action occurred.
     * 
     * @param vectorTime
     *            the time
     */
    public void setTime(ITime vectorTime) {
        time = vectorTime;
    }

    /**
     * Get the vector time of this action.
     * 
     * @return the vector time when this action occurred.
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
