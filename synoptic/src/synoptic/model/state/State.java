package synoptic.model.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import daikonizer.DaikonVarType;
import daikonizer.DaikonVar;

import synoptic.main.parser.ParseException;

/**
 * Represents a state parsed from a log file. A state is a set of
 * variable-value pairs, defined at a point in time (in an execution trace).
 * 
 * A String representation of a state (from the log) is in the form
 * id=value,...,id=value. It has no type information. Therefore, State class
 * must determine the types of the variables.
 * 
 * @author rsukkerd
 *
 */
public class State {
    /** An identifier-value pair delimiter */
    private static final String DELIM = "\\s*,\\s*";
    /** An assignment pattern -- the left side is identifier, the right side is value */
    // This Pattern only works in Java 7.
    // private static final Pattern matchAssign = Pattern
    //      .compile("(?<ID>[^\\s=]+)\\s*=\\s*(?<VALUE>.+)");
    private static final Pattern matchAssign = Pattern
            .compile("([^\\s=]+)\\s*=\\s*(.+)");
    
    /** A String representation of this state. */
    private final String stateString;
    /** A map from variables to values. */
    private final Map<DaikonVar, String> stateMap;
    
    public State(String stateString) throws ParseException {
        this.stateString = stateString.trim();
        stateMap = new HashMap<DaikonVar, String>();
        buildStateMap();
    }
    
    /**
     * Builds a map from variables to values.
     * 
     * @throws ParseException
     */
    private void buildStateMap() throws ParseException {
        String[] pairs = stateString.split(DELIM);
        
        for (String pair : pairs) {
            Matcher matcher = matchAssign.matcher(pair);
            if (!matcher.matches()) {
                throw new ParseException("State: " + stateString
                        + " is not in the format id=value,...,id=value");
            }
            // The following 2 lines only work in Java 7.
            // String id = matcher.group("ID");
            // String value = matcher.group("VALUE");
            String id = matcher.group(1);
            String value = matcher.group(2);
            if (id == null || value == null) {
                throw new ParseException("State: " + stateString
                        + " is not in the format id=value,...,id=value");
            }
            DaikonVarType type;
            try {
                type = DaikonVarType.determineType(value);
            } catch (Exception e) {
                throw new ParseException(e.getMessage());
            }
            DaikonVar var = new DaikonVar(id, type);
            stateMap.put(var, value);
        }
    }
    
    public Set<DaikonVar> getVariables() {
        return stateMap.keySet();
    }
    
    public String getValue(DaikonVar variable) {
        return stateMap.get(variable);
    }
    
    @Override
    public int hashCode() {
        return stateMap.hashCode();
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
        State other = (State) obj;
        return stateMap.equals(other.stateMap);
    }

    @Override
    public String toString() {
        return stateMap.toString();
    }
}
