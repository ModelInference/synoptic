package synoptic.util;

public class Predicate {
	public static interface IBinary<A, B> {
		public boolean eval(A a, B b);
	}
	public static class BinaryFalse<A, B> implements IBinary<A, B> {
		public boolean eval(A a, B b) { return false; }
	}
	public static class BinaryTrue<A, B> implements IBinary<A, B> {
		public boolean eval(A a, B b) { return true; }
	}
	public static class Equals<T> implements IBinary<T, T> {
		public boolean eval(T a, T b) { return a.equals(b); }
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
	
	/*
	public static <A, B> BitSet fromPredicate(Predicate.IBinary<A, B> func, A val, List<B> arr) {
		BitSet result = new BitSet();
		for (int i = 0; i < arr.size(); i++) {
			result.set(i, func.eval(val, arr.get(i)));
		}
		return result;
	}
	
	public static class EqualsA implements Function.IUnary<BinaryInvariant, String> {
		public String eval(BinaryInvariant inv) { return inv.getFirst(); }
	}

	public static class EqualsB implements Function.IUnary<BinaryInvariant, String> {
		public String eval(BinaryInvariant inv) { return inv.getSecond(); }
	}
	
	public static class NameMatch extends Predicate.ComposedFirst<BinaryInvariant, String, String> {
		NameMatch(Function.IUnary<BinaryInvariant, String> f) { super(f, Predicate.stringEquality); }
	}
	
	public static NameMatch equalsA = new NameMatch(new EqualsA());
	public static NameMatch equalsB = new NameMatch(new EqualsB());
	*/
}
