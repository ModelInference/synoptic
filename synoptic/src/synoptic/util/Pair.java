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

    @Override
    public int hashCode() {
        int result = l.hashCode();
        result = 31 * result + r.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }

        if (!(other instanceof Pair)) {
            return false;
        }

        Pair<?, ?> otherP = (Pair<?, ?>) other;
        if (!otherP.getLeft().equals(l)) {
            return false;
        }
        if (!otherP.getRight().equals(r)) {
            return false;
        }
        return true;
    }
}
