package synoptic.model.state;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import daikonizer.DaikonType;
import daikonizer.DaikonVar;

import synoptic.main.parser.ParseException;

/**
 * Represents a state parsed from a log file. A state is a set of
 * variable-value pairs, defined at a point in time (in an execution trace).
 * A String representation of a state is in the form var=value,...,var=value.
 * 
 * @author rsukkerd
 *
 */
public class State implements Iterable<Map.Entry<DaikonVar, String>> {
    /** An identifier-value pair delimiter */
    private static final String DELIM = "\\s*,\\s*";
    /** An assignment pattern -- the left side is identifier, the right side is value */
    private static final Pattern matchAssign = Pattern
            .compile("(?<ID>[^\\s=]+)\\s*=\\s*(?<VALUE>[^\\s=]+)");
    
    /** A String representation of this state. */
    private final String stateString;
    /** A map from variables to values. */
    private final Map<DaikonVar, String> stateMap;
    
    public State(String stateString) throws ParseException {
        this.stateString = stateString;
        stateMap = new HashMap<DaikonVar, String>();
        buildStateMap();
    }
    
    private void buildStateMap() throws ParseException {
        String[] pairs = stateString.split(DELIM);
        
        for (String pair : pairs) {
            Matcher matcher = matchAssign.matcher(pair);
            if (!matcher.matches()) {
                throw new ParseException("State: " + stateString
                        + " is not in the format id=value,...,id=value");
            }
            String id = matcher.group("ID");
            String value = matcher.group("VALUE");
            if (id == null || value == null) {
                throw new ParseException("State: " + stateString
                        + " is not in the format id=value,...,id=value");
            }
            DaikonType type = determineType(value);
            DaikonVar var = new DaikonVar(id, type);
            stateMap.put(var, value);
        }
    }
    
    private DaikonType determineType(String value) {
        // TODO: figure out type of value
        return null;
    }
    
    public Set<DaikonVar> getVariables() {
        return stateMap.keySet();
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
    public Iterator<Entry<DaikonVar, String>> iterator() {
        return stateMap.entrySet().iterator();
    }
    
    @Override
    public String toString() {
        return stateMap.toString();
    }
}
