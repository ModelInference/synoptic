package daikonizer;

/**
 * Representation types that Daikon recognizes.
 * 
 * @author rsukkerd
 *
 */
public enum DaikonType {
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
    }
    // TODO: add array types
}
