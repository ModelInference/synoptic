package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import synoptic.invariants.fsmcheck.AFbyInvFsms;
import synoptic.invariants.fsmcheck.APInvFsms;
import synoptic.invariants.fsmcheck.FsmStateSet;
import synoptic.invariants.fsmcheck.NFbyInvFsms;
import synoptic.model.Action;
import synoptic.model.LogEvent;

public class FsmStateSetTests {

    public static LogEvent msg = new LogEvent(new Action("x"));

    private static BitSet parseBitSet(String str) {
        BitSet result = new BitSet();
        for (int i = str.length() - 1; i >= 0; i--) {
            result.set(i, str.charAt(str.length() - i - 1) == '1');
        }
        return result;
    }

    private static void transfer(FsmStateSet<LogEvent> s, String input) {
        String[] inputs = input.split(" ");
        for (int i = 0; i < inputs.length; i++) {
            s.mappings.get(i).put("x", parseBitSet(inputs[i]));
        }
        s.transition(msg);
    }

    public static void setMapping(FsmStateSet<LogEvent> s) {
        s.mappings = new ArrayList<Map<String, BitSet>>();
        s.mappings.clear();
        s.mappings.add(new HashMap<String, BitSet>());
        s.mappings.add(new HashMap<String, BitSet>());
    }

    @Test
    public void AFbyInvFsmsTest() {
        FsmStateSet<LogEvent> a = new AFbyInvFsms<LogEvent>(4);
        setMapping(a);
        a.setInitial(msg);
        FsmStateSet<LogEvent> before = a.copy();

        transfer(a, "0000 0000");
        System.out.println(a.mappings);
        System.out.println(before.mappings);
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
        // FsmStateSet a = new AFbyInvFsms(4);
        FsmStateSet<LogEvent> b = new APInvFsms<LogEvent>(4);
        setMapping(b);
        b.setInitial(msg);
        FsmStateSet<LogEvent> before;

        // assertTrue(a.isFail().equals(new BitSet()));
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
        FsmStateSet<LogEvent> c = new NFbyInvFsms<LogEvent>(4);
        setMapping(c);
        c.setInitial(msg);

        transfer(c, "0000 0101");
        // assertTrue(b.equals(before));
        transfer(c, "1010 0101");
        // assertTrue(!b.equals(before));
        // assertTrue(a.isFail().equals(new BitSet()));
        transfer(c, "0010 1000");
        // assertTrue(a.isFail().equals(parseBitSet("1000")));
    }

}
