package synopticgwt.shared;

import java.io.Serializable;

/**
 * A representation of a graph node for GWT.  Overall, this is a
 * representation of a partition node which acts as a bridge between
 * Synoptic's server and the front end.
 * 
 * @author a.w.davies.vio
 */
public class GWTNode implements Serializable {
    
    // TODO: Create a JSNI field that holds a reference
    // to a Dracula Graph node so that updates can be sent
    // to the node via this object rather than in JS
    // file.
    
    private static final long serialVersionUID = 1L;

    // The event type of the partition.
    private String eType = null;

    public GWTNode() {
        // Default constructor to avoid serialization errors.
    }

    /**
     * Constructs a GWTNode object, which identifies itself via its event type.
     * The event type is the String representation of the event type to which
     * this node must correspond.
     */
    public GWTNode(String eType) {
        assert eType != null;
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
