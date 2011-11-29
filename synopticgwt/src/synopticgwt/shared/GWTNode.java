package synopticgwt.shared;

import java.io.Serializable;

public class GWTNode implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eType = null;

    public GWTNode() {
        // Default constructor to avoid serialization errors.
    }

    /**
     * Constructs a GWTNode object, which identifies itself via its event type.
     */
    public GWTNode(String eType) {
        assert this.eType != null;
        this.eType = eType;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof GWTNode))
            return false;
        GWTNode o = (GWTNode) other;
        return o.toString().equals(this.toString());
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + eType.hashCode();
        return result;
    }

    public String toString() {
        assert this.eType != null;
        return eType;
    }
}
