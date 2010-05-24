package invariants;

import java.util.List;

import model.Action;
import model.interfaces.INode;

public class NeverFollowedInvariant extends BinaryInvariant {

	public NeverFollowedInvariant(String typeFrist, String typeSecond,
			Action relation) {
		super(typeFrist, typeSecond, relation);
	}

	public String toString() {
		return first + " neverFollowedBy("+relation+") " + second;
	}

	@Override
	public String getLTLString() {
		return "[](did(" + first + ") -> X([] !did(" + second + ")))";
	}

	private <T extends INode<T>> List<T> shortenImp(boolean seen, int len, List<T> trace) {
		if (trace.size() <= len)
			return null;
		T message = trace.get(len);
		if (message.getLabel().equals(first) && !seen)
			seen = true;
		else if (message.getLabel().equals(second) && seen)
			return trace.subList(0, len+1);
		return shortenImp(seen, len + 1, trace);
	}

	@Override
	public <T extends INode<T>> List<T> shorten(List<T> trace) {
		return shortenImp(false, 0, trace);
	}
	
	@Override
	public String getShortName() {
		return "NFby";
	}
}
