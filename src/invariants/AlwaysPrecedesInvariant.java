package invariants;

import java.util.List;

import model.interfaces.INode;

public class AlwaysPrecedesInvariant extends BinaryInvariant {

	public AlwaysPrecedesInvariant(String typeFrist, String typeSecond,
			String relation) {
		super(typeFrist, typeSecond, relation);
	}

	public String toString() {
		return first + " AlwaysPrecedes("+relation+") " + second;
	}

	@Override
	public String getLTLString() {
		return "!(did(" + second + ")) W (did(" + first + "))";
	}

	private <T extends INode<T>> List<T> shortenImp(boolean seen, int len,
			List<T> trace) {
		if (trace.size() <= len)
			return null;
		T message = trace.get(len);
		if (message.getLabel().equals(first))
			seen = true;
		if (message.getLabel().equals(second) && !seen)
			return trace.subList(0, len+1);
		return shortenImp(seen, len+1, trace);
	}

	@Override
	public <T extends INode<T>> List<T> shorten(List<T> trace) {
		return shortenImp(false, 0, trace);
	}
	
	@Override
	public String getShortName() {
		return "AP";
	}
}
