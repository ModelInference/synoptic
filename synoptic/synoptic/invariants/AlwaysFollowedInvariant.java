package invariants;

import java.util.List;

import model.interfaces.INode;

/**
 * 
 * @author Sigurd Schneider
 *
 */
public class AlwaysFollowedInvariant extends BinaryInvariant {

	public AlwaysFollowedInvariant(String typeFrist, String typeSecond,
			String relation) {
		super(typeFrist, typeSecond, relation);
	}

	@Override
	public String toString() {
		return first + " alwaysFollowedBy("+relation+") " + second;
	}

	@Override
	public String getLTLString() {
		if (useDIDCAN)
			return "[](did(" + first + ") -> <" + "> did(" + second + ")))";
		else
			return "[](\""+first+"\" -> (<>\""+second+"\"))";
	}

	/**
	 * TODO: why does this invariant type not need violating trace shortening
	 * like the other types? 
	 */
	@Override
	public <T extends INode<T>> List<T> shorten(List<T> trace) {
		return trace;
	}

	@Override
	public String getShortName() {
		return "AFby";
	}

}
