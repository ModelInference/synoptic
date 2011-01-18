package synoptic.tests.units;

import org.junit.Test;
import static org.junit.Assert.*;

import synoptic.invariants.fsmcheck.AFbyInvFsms;
import synoptic.invariants.fsmcheck.APInvFsms;
import synoptic.invariants.fsmcheck.FsmStateSet;
import synoptic.invariants.fsmcheck.NFbyInvFsms;
import synoptic.model.Action;
import synoptic.model.MessageEvent;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;


public class FsmStateSetTests {

	public static MessageEvent msg = new MessageEvent(new Action("x"), 0);
	
	private static BitSet parseBitSet(String str) {
		BitSet result = new BitSet();
		for (int i = str.length() - 1; i >= 0; i--)
			result.set(i, str.charAt(str.length() - i - 1) == '1');
		return result;
	}
	
	private static void transfer(FsmStateSet<MessageEvent> s, String input) {
		String[] inputs = input.split(" ");
		for (int i = 0; i < inputs.length; i++)
			s.mappings.get(i).put("x", parseBitSet(inputs[i]));
		s.transition(msg);
	}
	
	public static void setMapping(FsmStateSet<MessageEvent> s) {
		s.mappings = new ArrayList<Map<String, BitSet>>();
		s.mappings.clear();
		s.mappings.add(new HashMap<String,BitSet>());
		s.mappings.add(new HashMap<String,BitSet>());
	}

	@Test
	public void AFbyInvFsmsTest() {
		FsmStateSet<MessageEvent> a = new AFbyInvFsms<MessageEvent>(4);
		setMapping(a);
		a.setInitial(msg);
		FsmStateSet<MessageEvent> before = a.copy();
		
		transfer(a, "0000 0000");
		assertTrue(a.equals(before));
		transfer(a, "0000 0101");
		assertTrue(a.equals(before));
		assertTrue(a.whichFail().equals(new BitSet()));
		transfer(a, "1010 0000");
		assertTrue(a.whichFail().equals(parseBitSet("1010")));
		assertTrue(!a.equals(before));
		transfer(a, "0000 1000");
		assertTrue(!a.equals(before));
		transfer(a, "0000 0010");
		assertTrue(a.equals(before));
	}
	
	@Test
	public void NFbyInvFsmsTest() {
		//FsmStateSet a = new AFbyInvFsms(4);
		FsmStateSet<MessageEvent> b = new APInvFsms<MessageEvent>(4);
		setMapping(b);
		b.setInitial(msg);
		FsmStateSet<MessageEvent> before;
		
		//assertTrue(a.isFail().equals(new BitSet()));
		transfer(b, "0111 1000");
		assertTrue(b.whichFail().equals(parseBitSet("1000")));
		
		before = b.copy();
		transfer(b, "0101 0000");
		transfer(b, "0010 0000");
		assertTrue(b.equals(before));
		transfer(b, "0000 0110");
		assertTrue(!b.equals(before));
		transfer(b, "0100 0000");
		assertTrue(b.whichFail().equals(parseBitSet("1100")));
	}
	
	@Test
	public void APInvFsmsTest() {
		FsmStateSet<MessageEvent> c = new NFbyInvFsms<MessageEvent>(4);
		setMapping(c);
		c.setInitial(msg);
		
		transfer(c, "0000 0101");
		// assertTrue(b.equals(before));
		transfer(c, "1010 0101");
		//assertTrue(!b.equals(before));
		//assertTrue(a.isFail().equals(new BitSet()));
		transfer(c, "0010 1000");
		//assertTrue(a.isFail().equals(parseBitSet("1000")));
	}
	
}
