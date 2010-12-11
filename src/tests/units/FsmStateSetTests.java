package tests.units;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import invariants.fsmcheck.AFbyInvFsms;
import invariants.fsmcheck.APInvFsms;
import invariants.fsmcheck.NFbyInvFsms;
import invariants.fsmcheck.FsmStateSet;

public class FsmStateSetTests {

	private static BitSet parseBitSet(String str) {
		BitSet result = new BitSet();
		for (int i = str.length() - 1; i >= 0; i--)
			result.set(i, str.charAt(str.length() - i - 1) == '1');
		return result;
	}
	
	private static void transfer(FsmStateSet s, String input) {
		String[] inputs = input.split(" ");
		List<BitSet> bitsets = new ArrayList<BitSet>(inputs.length);
		for (String str : inputs)
			bitsets.add(parseBitSet(str));
		s.transition(bitsets);
	}

	@Test
	public void AFbyInvFsmsTest() {
		FsmStateSet a = new AFbyInvFsms(4);
		FsmStateSet before = a.clone();
		
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
	}
	
	@Test
	public void NFbyInvFsmsTest() {
		//FsmStateSet a = new AFbyInvFsms(4);
		FsmStateSet b = new APInvFsms(4);
		FsmStateSet before;
		
		//assert(a.isFail().equals(new BitSet()));
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
	}
	
	@Test
	public void APInvFsmsTest() {
		FsmStateSet c = new NFbyInvFsms(4);
		
		transfer(c, "0000 0101");
		// assert(b.equals(before));
		transfer(c, "1010 0101");
		//assert(!b.equals(before));
		//assert(a.isFail().equals(new BitSet()));
		transfer(c, "0010 1000");
		//assert(a.isFail().equals(parseBitSet("1000")));
	}
	
}
