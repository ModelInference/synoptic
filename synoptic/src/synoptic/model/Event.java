package synoptic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import synoptic.util.time.ITime;

/**
 * Represents an event parsed from a log file. Each event needs at least a name,
 * called a label. Optionally, a time and data fields can set. If data fields
 * will be used, {@code useDatafields} must be set before compilation.
 * 
 * @author Sigurd Schneider
 */
public class Event {
	
	public final static String defaultRelation = "t";
	
    /**
     * The event's label.
     */
    private final EventType eType;

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
    
    private List<String> relations;

    /**
     * Create an event of a particular type, with corresponding log line,
     * filename and line number where the event originated.
     * 
     * @param eType
     *            the label for the event
     * @param isSpecialLabel
     * @param logLine
     * @param fileName
     * @param lineNum
     */
    public Event(EventType eType, String logLine, String fileName, int lineNum) {
        this.eType = eType;
        this.logLine = logLine;
        this.fileName = fileName;
        this.lineNum = lineNum;
        this.relations = new ArrayList<String>();
    }

    /**
     * Create an event with a string label.
     */
    public Event(String label) {
        this(new StringEventType(label), null, null, 0);
    }

    /**
     * Create an event with a label of EventType.
     */
    public Event(EventType label) {
        this(label, null, null, 0);
    }

    /**
     * Create an event with DistEvent type, without needing to know the hostId.
     */
    // public static Event newDistEvent(String label, String logLine,
    // String fileName, int lineNum) {
    // return new Event(new DistEventType(label), logLine, fileName, lineNum);
    // }

    /**
     * Returns the special INITIAL event of String type.
     */
    public static Event newInitialStringEvent() {
        return new Event(StringEventType.newInitialStringEventType(), null,
                null, 0);
    }

    /**
     * Returns the special terminal event of String type.
     */
    public static Event newTerminalStringEvent() {
        return new Event(StringEventType.newTerminalStringEventType(), null,
                null, 0);
    }

    /**
     * Returns the special INITIAL event of DistEvent type.
     */
    public static Event newInitialDistEvent() {
        return new Event(DistEventType.newInitialDistEventType(), null, null, 0);
    }

    /**
     * Returns the special terminal event of DistEvent type.
     */
    public static Event newTerminalDistEvent() {
        return new Event(DistEventType.newTerminalDistEventType(), null, null,
                0);
    }

    @Override
    public String toString() {
        return eType.toString() + "-" + time.toString();
    }

    /**
     * Get the label of the event.
     * 
     * @return the label
     */
    public EventType getEType() {
        return eType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((eType == null) ? 0 : eType.hashCode());
        result = prime * result + lineNum;
        result = prime * result + ((logLine == null) ? 0 : logLine.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        for (String relation : relations) {
        	result = prime * result + relation.hashCode();
        }
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
        if (eType == null) {
            if (other.eType != null) {
                return false;
            }
        } else if (!eType.equals(other.eType)) {
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
        
        for (String relation : other.getRelations()) {
        	if (!containsRelation(relation)) {
        		return false;
        	}
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
    	addRelation(defaultRelation);
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
    
    public boolean containsRelation(String s) {
    	return relations.contains(s);
    }
    
    public void addRelation(String s) {
    	assert(!containsRelation(s));
    	relations.add(s);
    }
    
    public List<String> getRelations() {
    	return Collections.unmodifiableList(relations);
    }
}
