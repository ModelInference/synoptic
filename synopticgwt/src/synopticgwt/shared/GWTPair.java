package synopticgwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

// cut down http://www.ideograph.com/content/generic-pair-java
public class GWTPair<L, R> implements IsSerializable {
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
        String ret = "<";
        if (l != null) {
            ret = ret + l.toString() + ",";
        } else {
            ret = ret + "null,";
        }
        if (r != null) {
            ret = ret + r.toString() + ">";
        } else {
            ret = ret + "null>";
        }
        return ret;
    }
}
