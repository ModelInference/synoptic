package synopticgwt.shared;

import java.io.Serializable;

// cut down http://www.ideograph.com/content/generic-pair-java
public class GWTPair<L, R> implements Serializable {
    private static final long serialVersionUID = 1L;

    public L l;
    public R r;

    public GWTPair() {
        l = null;
        r = null;
    }

    public GWTPair(L left, R right) {
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
    public String toString() {
        return new String("<" +
        		l.toString() +
        		"," + r.toString() + ">");
    }
}
