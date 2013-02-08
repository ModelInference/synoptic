package synoptic.tests.units;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import daikonizer.DaikonType;
import daikonizer.DaikonVar;

import synoptic.main.parser.ParseException;
import synoptic.model.state.State;

public class StateTests {

    /**
     * Boolean values.
     * 
     * @throws ParseException 
     */
    @Test
    public void booleanTest() throws ParseException {
        String stateStr = "p=true, q=false";
        State state = new State(stateStr);
        DaikonVar p = new DaikonVar("p", DaikonType.BOOLEAN);
        DaikonVar q = new DaikonVar("q", DaikonType.BOOLEAN);
        Map<DaikonVar, String> expected = new HashMap<DaikonVar, String>();
        expected.put(p, "true");
        expected.put(q, "false");
        checkState(expected, state);
    }
    
    /**
     * Small int values.
     * 
     * @throws ParseException 
     */
    @Test
    public void smallIntTest() throws ParseException {
        String stateStr = "i=0, j=1, k=-1, m=-42, n=73";
        State state = new State(stateStr);
        DaikonVar i = new DaikonVar("i", DaikonType.INT);
        DaikonVar j = new DaikonVar("j", DaikonType.INT);
        DaikonVar k = new DaikonVar("k", DaikonType.INT);
        DaikonVar m = new DaikonVar("m", DaikonType.INT);
        DaikonVar n = new DaikonVar("n", DaikonType.INT);
        Map<DaikonVar, String> expected = new HashMap<DaikonVar, String>();
        expected.put(i, "0");
        expected.put(j, "1");
        expected.put(k, "-1");
        expected.put(m, "-42");
        expected.put(n, "73");
        checkState(expected, state);
    }
    
    /**
     * Big int values.
     * 
     * @throws ParseException 
     */
    @Test
    public void bigIntTest() throws ParseException {
        String stateStr = "i=15670465892, j=-54121051040";
        State state = new State(stateStr);
        DaikonVar i = new DaikonVar("i", DaikonType.INT);
        DaikonVar j = new DaikonVar("j", DaikonType.INT);
        Map<DaikonVar, String> expected = new HashMap<DaikonVar, String>();
        expected.put(i, "15670465892");
        expected.put(j, "-54121051040");
        checkState(expected, state);
    }
    
    /**
     * Hashcode values.
     * 
     * @throws ParseException 
     */
    @Test
    public void hashcodeTest() throws ParseException {
        String stateStr = "h=0x38E8A, k=0xdeadbeef";
        State state = new State(stateStr);
        DaikonVar h = new DaikonVar("h", DaikonType.HASHCODE);
        DaikonVar k = new DaikonVar("k", DaikonType.HASHCODE);
        Map<DaikonVar, String> expected = new HashMap<DaikonVar, String>();
        expected.put(h, "0x38E8A");
        expected.put(k, "0xdeadbeef");
        checkState(expected, state);
    }
    
    /**
     * Small double values.
     * 
     * @throws ParseException 
     */
    @Test
    public void smallDoubleTest() throws ParseException {
        String stateStr = "i=0.0, j=1.003, k=-0.45, m=-9.999, n=3.14159";
        State state = new State(stateStr);
        DaikonVar i = new DaikonVar("i", DaikonType.DOUBLE);
        DaikonVar j = new DaikonVar("j", DaikonType.DOUBLE);
        DaikonVar k = new DaikonVar("k", DaikonType.DOUBLE);
        DaikonVar m = new DaikonVar("m", DaikonType.DOUBLE);
        DaikonVar n = new DaikonVar("n", DaikonType.DOUBLE);
        Map<DaikonVar, String> expected = new HashMap<DaikonVar, String>();
        expected.put(i, "0.0");
        expected.put(j, "1.003");
        expected.put(k, "-0.45");
        expected.put(m, "-9.999");
        expected.put(n, "3.14159");
        checkState(expected, state);
    }
    
    /**
     * Big double values.
     * 
     * @throws ParseException 
     */
    @Test
    public void bigDoubleTest() throws ParseException {
        String stateStr = "i=5.972E24, j=-0.10848103540060e-009, k=7098701.806456e+100";
        State state = new State(stateStr);
        DaikonVar i = new DaikonVar("i", DaikonType.DOUBLE);
        DaikonVar j = new DaikonVar("j", DaikonType.DOUBLE);
        DaikonVar k = new DaikonVar("k", DaikonType.DOUBLE);
        Map<DaikonVar, String> expected = new HashMap<DaikonVar, String>();
        expected.put(i, "5.972E24");
        expected.put(j, "-0.10848103540060e-009");
        expected.put(k, "7098701.806456e+100");
        checkState(expected, state);
    }
    
    /**
     * String values.
     * 
     * @throws ParseException 
     */
    @Test
    public void stringTest() throws ParseException {
        String stateStr = "str=\"Hello World!\", msg=\"\"";
        State state = new State(stateStr);
        DaikonVar str = new DaikonVar("str", DaikonType.STRING);
        DaikonVar msg = new DaikonVar("msg", DaikonType.STRING);
        Map<DaikonVar, String> expected = new HashMap<DaikonVar, String>();
        expected.put(str, "\"Hello World!\"");
        expected.put(msg, "\"\"");
        checkState(expected, state);
    }
    
    /**
     * Checks that state contains the expected variables and values.
     */
    private void checkState(Map<DaikonVar, String> expected, State actual) {
        Set<DaikonVar> actualVars = actual.getVariables();
        assertTrue (expected.size() == actualVars.size());
        
        for (DaikonVar actualVar : actualVars) {
            assertTrue(expected.containsKey(actualVar));
            String actualVal = actual.getValue(actualVar);
            String expectedVal = expected.get(actualVar);
            assertEquals(expectedVal, actualVal);
        }
    }

}
