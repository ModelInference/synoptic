package invariants;

import java.util.List;

import model.interfaces.INode;

public class NeverFollowedInvariant extends BinaryInvariant {

	public NeverFollowedInvariant(String typeFrist, String typeSecond,
			String relation) {
		super(typeFrist, typeSecond, relation);
	}

	public String toString() {
		return first + " neverFollowedBy("+relation+") " + second;
	}

	@Override
	public String getLTLString() {
		if (useDIDCAN)
			return "[](did(" + first + ") -> X([] !(did(" + second + "))))";
		else
			return "[](\"" + first + "\" -> X([] !(\"" + second + "\")))";
	}

	/**
	 * Returns a sub-trace of the input violating trace that looks like
	 * ...'first' ... 'second'
	 * where 'first' NFby 'second' is this invariant.
	 * 
	 * It includes the section of the trace that precedes 'first' and ignors the
	 * section of the trace that follows 'second'. If the trace is not a
	 * counter-example trace (and therefore does not contain such a sequence)
	 * then it returns null. 
	 * 
	 * @param <T>
	 * @param first_seen whether or not we've seen 'first' in the trace so far
	 * @param trace_pos the position of where we are in the trace so far
	 * @param trace the trace we are operating on
	 * @return the sub-trace described above
	 */
	private <T extends INode<T>> List<T> shortenImp(boolean first_seen,
			int trace_pos, List<T> trace) {
		if (trace.size() <= trace_pos)
			return null;
		T message = trace.get(trace_pos);
		if (message.getLabel().equals(first) && !first_seen)
			first_seen = true;
		else if (message.getLabel().equals(second) && first_seen)
			return trace.subList(0, trace_pos+1);
		return shortenImp(first_seen, trace_pos + 1, trace);
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
