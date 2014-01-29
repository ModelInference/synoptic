package daikonizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation types that Daikon recognizes.
 * 
 *
 */
public enum DaikonVarType {
    BOOLEAN {
        @Override
        public String toString() {
            return "boolean";
        }
    },
    INT {
        @Override
        public String toString() {
            return "int";
        }
    },
    HASHCODE {
        @Override
        public String toString() {
            return "hashcode";
        }
    },
    DOUBLE {
        @Override
        public String toString() {
            return "double";
        }
    },
    STRING {
        @Override
        public String toString() {
            return "java.lang.String";
        }
    };
    // TODO: add array types
    
    /** Type patterns -- boolean, int, hashcode, double and String **/
    private static final Pattern matchBoolean = Pattern
            .compile("true|false");
    private static final Pattern matchInt = Pattern
            .compile("[+-]?(0|[1-9])[0-9]*");
    private static final Pattern matchHashcode = Pattern
            .compile("0x([0-9]|[a-fA-F])+");
    private static final Pattern matchDouble = Pattern
            .compile("[+-]?(0|[1-9][0-9]*)(.[0-9]+)?([eE][+-]?[0-9]+)?");
    private static final Pattern matchString = Pattern
            .compile("\"[^\"]*\"");
    
    /**
     * Determines the type of this value.
     * 
     * @throws Exception
     */
    public static DaikonVarType determineType(String value) throws Exception {
        Matcher matcher = matchBoolean.matcher(value);
        if (matcher.matches()) {
            return DaikonVarType.BOOLEAN;
        }
        matcher = matchInt.matcher(value);
        if (matcher.matches()) {
            return DaikonVarType.INT;
        }
        matcher = matchHashcode.matcher(value);
        if (matcher.matches()) {
            return DaikonVarType.HASHCODE;
        }
        matcher = matchDouble.matcher(value);
        if (matcher.matches()) {
            return DaikonVarType.DOUBLE;
        }
        matcher = matchString.matcher(value);
        if (matcher.matches()) {
            return DaikonVarType.STRING;
        }
        throw new Exception(value + " is not a recognizable type");
    }
}
