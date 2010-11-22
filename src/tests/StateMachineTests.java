package tests;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import invariants.fsmcheck.AlwaysFollowedSet;
import invariants.fsmcheck.AlwaysPrecedesSet;
import invariants.fsmcheck.NeverFollowedSet;
import invariants.fsmcheck.StateSet;

public class StateMachineTests {
	public static void main(String[] args) {
		StateSet a = new AlwaysFollowedSet(4), before = a.clone();
		transfer(a, "0000 0000");
		assert(a.equals(before));
		transfer(a, "0000 0101");
		assert(a.equals(before));
		assert(a.isFail().equals(new BitSet()));
		transfer(a, "1010 0000");
		assert(a.isFail().equals(parseBitSet("1010")));
		assert(!a.equals(before));
		transfer(a, "0000 1000");
		assert(!a.equals(before));
		transfer(a, "0000 0010");
		assert(a.equals(before));
		
		StateSet b = new AlwaysPrecedesSet(4);
		assert(a.isFail().equals(new BitSet()));
		transfer(b, "0111 1000");
		assert(b.isFail().equals(parseBitSet("1000")));
		before = b.clone();
		transfer(b, "0101 0000");
		transfer(b, "0010 0000");
		assert(b.equals(before));
		transfer(b, "0000 0110");
		assert(!b.equals(before));
		transfer(b, "0100 0000");
		assert(b.isFail().equals(parseBitSet("1100")));
		
		StateSet c = new NeverFollowedSet(4);
		before = b.clone();
		transfer(c, "0000 0101");
		assert(b.equals(before));
		transfer(c, "1010 0101");
		assert(!b.equals(before));
		assert(a.isFail().equals(new BitSet()));
		transfer(c, "0010 1000");
		assert(a.isFail().equals(parseBitSet("1000")));
	}
	
	public static BitSet parseBitSet(String str) {
		BitSet result = new BitSet();
		for (int i = str.length() - 1; i >= 0; i--)
			result.set(i, str.charAt(str.length() - i - 1) == '1');
		return result;
	}
	
	public static void transfer(StateSet s, String input) {
		String[] inputs = input.split(" ");
		List<BitSet> bitsets = new ArrayList<BitSet>(inputs.length);
		for (String str : inputs)
			bitsets.add(parseBitSet(str));
		s.transition(bitsets);
	}
}
