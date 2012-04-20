package synoptic.util;

public class Predicate {
    public static interface IBinary<A, B> {
        public boolean eval(A a, B b);
    }

    public static class BinaryFalse<A, B> implements IBinary<A, B> {
        public boolean eval(A a, B b) {
            return false;
        }
    }

    public static class BinaryTrue<A, B> implements IBinary<A, B> {
        public boolean eval(A a, B b) {
            return true;
        }
    }

    public static class Equals<T> implements IBinary<T, T> {
        public boolean eval(T a, T b) {
            return a.equals(b);
        }
    }

    public static Predicate.Equals<String> stringEquality = new Predicate.Equals<String>();

    public static class ComposedFirst<A, B, C> implements IBinary<A, C> {
        Function.IUnary<A, B> fa;
        IBinary<B, C> fb;

        public ComposedFirst(Function.IUnary<A, B> a, IBinary<B, C> b) {
            fa = a;
            fb = b;
        }

        public boolean eval(A a, C b) {
            return fb.eval(fa.eval(a), b);
        }
    }
}
