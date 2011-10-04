package synopticgwt.shared;

import java.io.Serializable;

/**
 * A client-side counterpart to the temporal invariant for communication between
 * the server/client. Currently not all of the data within the server-side
 * temporal invariant classes can be converted to work with GWT.
 */
public class GWTInvariant implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The first event label type for a binary invariant. */
    private String eFirst;

    /** The second event label type for a binary invariant. */
    private String eSecond;

    /** The invariant type, i.e. "AFby", "NFby", or "AP" */
    private String transitionType;

    // This is a copy of the server-side
    // temporal invariant's hashCode.
    private int invID = 0;

    public GWTInvariant() {
        eFirst = null;
        eSecond = null;
        transitionType = null;
    }

    /**
     * Constructs a new GWTInvariant.
     * 
     * @param source
     *            The source node
     * @param target
     *            The target node
     * @param transitionType
     *            The type of transition between the nodes (i.e. AFBy, NFby,
     *            AP).
     */
    public GWTInvariant(String source, String target, String transitionType) {
        this.eFirst = source;
        this.eSecond = target;
        this.transitionType = transitionType;
    }

    /**
     * Set the ID of the current invariant. Used to identify this invariant and
     * match it to the server-side invariant of the same type.
     * 
     * @param id
     *            The ID number to which the current invariant will be set.
     */
    public void setID(Integer id) {
        invID = id;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GWTInvariant) {
            return (((GWTInvariant) other).getTarget().equals(this.eSecond) && ((GWTInvariant) other)
                    .getSource().equals(this.eFirst));
        }
        return false;
    }

    public String getSource() {
        return eFirst;
    }

    public String getTarget() {
        return eSecond;
    }

    public String getTransitionType() {
        return transitionType;
    }

    /**
     * Returns the invariant's ID so it can be compared to a server-side
     * invariant for equality.
     */
    // TODO: add a proper way to return a correct hashcode
    public int getID() {
        return invID;
    }

    @Override
    public String toString() {
        return "<" + eFirst.toString() + "," + eSecond.toString() + ">";
    }

}
