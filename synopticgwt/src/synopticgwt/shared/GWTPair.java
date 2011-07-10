package synopticgwt.shared;

import java.io.Serializable;

// cut down http://www.ideograph.com/content/generic-pair-java
public class GWTPair<L, R> implements Serializable {
    private static final long serialVersionUID = 1L;

    public L l;
    public R r;

    // TODO: Move this to a separate datatype.
    // Currently, this is a placeholder for something
    // a separate GWTInvariant datatype should do.
    private int iD;

    public GWTPair() {
        l = null;
        r = null;
        this.iD = 0;
    }

    public GWTPair(L left, R right, int hash) {
        l = left;
        r = right;
        iD = hash;
    }

    @Override
    public boolean equals(Object other) {
    	if (other instanceof GWTPair<?, ?>) {
    		return (((GWTPair<?, ?>) other).l.equals(this.l) &&
    				((GWTPair<?, ?>) other).r.equals(this.r));
    	} else {
    		return false;
    	}
    }

    public L getLeft() {
        return l;
    }

    public int hashCode() {
    	return iD;
    }

    public R getRight() {
        return r;
    }

    @Override
    public String toString() {
        return new String("<" + l.toString() + "," + r.toString() + ">");
    }
}
