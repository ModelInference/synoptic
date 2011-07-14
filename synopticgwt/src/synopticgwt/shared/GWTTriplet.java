package synopticgwt.shared;

import java.io.Serializable;

// cut down http://www.ideograph.com/content/generic-pair-java
public class GWTTriplet<L, M, R> implements Serializable {
    private static final long serialVersionUID = 1L;

    public L l;
    public M m;
    public R r;

    public GWTTriplet() {
        l = null;
        r = null;
        m = null;
    }

    public GWTTriplet(L left, M middle, R right) {
        l = left;
        m = middle;
        r = right;
    }

    public L getLeft() {
        return l;
    }

    public M getMiddle() {
        return m;
    }

    public R getRight() {
        return r;
    }

    @Override
    public String toString() {
        return new String("<" + l.toString() + "," + m.toString() + ","
                + r.toString() + ">");
    }
}
