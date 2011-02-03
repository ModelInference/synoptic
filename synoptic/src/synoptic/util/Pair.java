package synoptic.util;

// cut down http://www.ideograph.com/content/generic-pair-java
public class Pair<L, R> {
    private final L l;
    private final R r;

    public Pair(L left, R right) {
        l = left;
        r = right;
    }

    public L getLeft() {
        return l;
    }

    public R getRight() {
        return r;
    }
}
