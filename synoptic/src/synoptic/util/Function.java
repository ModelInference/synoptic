package synoptic.util;

public class Function {
    public interface IUnary<A, B> {
        public B eval(A a);
    }

    public interface IBinary<A, B, C> {
        public C eval(A a, B b);
    }
}
