package synopticgwt.shared;

import java.io.Serializable;

/**
 * A representation of a graph node for GWT. Overall, this is a representation
 * of a partition node which acts as a bridge between Synoptic's server and the
 * front end.
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
    private String eventType = null;

    // The hashCode of the corresponding pNode.
    private int pNodeHash;

    public GWTNode() {
        // Default constructor to avoid serialization errors.
    }

    /**
     * Constructs a GWTNode object, which identifies itself via its event type.
     * The event type is the String representation of the event type to which
     * this node must correspond.
     * 
     * @param eType
     *            The String of the eType of the corresponding partition node.
     * @param hashCode
     *            The hashCode of the corresponding partition node.
     */
    public GWTNode(String eType, int hashCode) {
        assert eType != null;
        this.eventType = eType;
        this.pNodeHash = hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof GWTNode))
            return false;
        GWTNode o = (GWTNode) other;
        return o.toString().equals(this.toString())
                && o.pNodeHash == this.pNodeHash;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + eventType.hashCode();
        result = 31 * result + pNodeHash;
        return result;
    }

    public String getEventType() {
        assert this.eventType != null;
        return eventType;
    }

    @Override
    public String toString() {
        return getEventType();
    }

    /**
     * @return The hash code for the Partition Node object that this object
     *         represents.
     */
    public int getPartitionNodeHashCode() {
        return pNodeHash;
    }

    /**
     * @return The hash code for the Partition Node object that this object
     *         represents.
     */
    public String getPartitionNodeHashCodeStr() {
        return ((Integer) pNodeHash).toString();
    }

}
