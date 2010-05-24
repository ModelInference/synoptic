package invariants;

import java.util.List;

import model.Action;
import model.interfaces.INode;

public class AlwaysFollowedInvariant extends BinaryInvariant {

	public AlwaysFollowedInvariant(String typeFrist, String typeSecond,
			Action relation) {
		super(typeFrist, typeSecond, relation);
	}

	@Override
	public String toString() {
		return first + " alwaysFollowedBy("+relation+") " + second;
	}

	@Override
	public String getLTLString() {
		return "[](did(" + first + ") -> <" + "> did(" + second + ")))";
	}

	@Override
	public <T extends INode<T>> List<T> shorten(List<T> trace) {
		return trace;
	}

	@Override
	public String getShortName() {
		return "AFby";
	}

}
