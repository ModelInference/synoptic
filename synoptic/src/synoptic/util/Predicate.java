package synoptic.util;

/**
 * Defines a few useful binary predicates.
 */
public class Predicate {
    public static interface IBoolBinary<A, B> {
        public boolean eval(A a, B b);
    }

    /** Evaluates two objects and always returns false. */
    public static class BinaryFalse<A, B> implements IBoolBinary<A, B> {
        public boolean eval(A a, B b) {
            return false;
        }
    }

    /** Evaluates two objects and always returns true. */
    public static class BinaryTrue<A, B> implements IBoolBinary<A, B> {
        public boolean eval(A a, B b) {
            return true;
        }
    }
}
