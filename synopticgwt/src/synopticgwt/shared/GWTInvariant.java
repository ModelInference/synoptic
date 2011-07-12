package synopticgwt.shared;

import java.io.Serializable;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.NeverFollowedInvariant;

/**
 * A client-side counterpart to the temporal invariant for communication between
 * the server/client.
 *
 * Currently not all of the data within the server-side temporal invariant
 * classes can be converted to work with GWT.
 */
public class GWTInvariant<S, T> implements Serializable{
	private static final long serialVersionUID = 1L;

	// The source node, the target node,
	// and the transition type between them,
	// i.e. AFby NFby AP
	private S source;
	private T target;
	private String transitionType;

	// This is a copy of the server-side
	// temporal invariant's hashCode.
	private int invID = 0;

	public GWTInvariant() {
		source = null;
		target = null;
		transitionType = null;
	}

	/**
	 * Constructs a new GWTInvariant.
	 * @param source
	 * 	The source node
	 * @param target
	 * 	The target node
	 * @param transitionType
	 * 	The type of transition between the nodes (i.e. AFBy, NFby, AP).
	 */
	public GWTInvariant(S source, T target, String transitionType) {
		this.source = source;
		this.target = target;
		this.transitionType = transitionType;
	}

	/**
	 * Set the ID of the current invariant. Used to identify this invariant
	 * and match it to the server-side invariant of the same type.
	 * @param id
	 * 	The ID number to which the current invariant will be set.
	 */
	public void setID(Integer id) {
		invID = id;
	}

	@Override
	public boolean equals(Object other) {
    	if (other instanceof GWTInvariant<?, ?>) {
    		return (((GWTInvariant<?, ?>) other).getTarget().equals(this.target) &&
    				((GWTInvariant<?, ?>) other).getSource().equals(this.source));
    	} else {
    		return false;
    	}
    }

	public S getSource() {
		return source;
	}

	public T getTarget() {
		return target;
	}

	public String getTransitionType() {
		return transitionType;
	}

	/**
	 * Returns the invariant's ID so it can be compared to a server-side invariant
	 * for equality.
	 */
	// TODO: add a proper way to return a correct hashcode
	public int getID() {
		return invID;
	}

	@Override
	public String toString() {
		return "<" + source.toString() + "," + target.toString() + ">";
	}
}
