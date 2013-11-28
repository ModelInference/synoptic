package synoptic.tests.units;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import daikon.inv.Invariant;
import daikon.inv.unary.scalar.OneOfScalar;
import daikon.inv.binary.twoScalar.IntEqual;
import daikon.inv.binary.twoScalar.LinearBinary;
import daikonizer.DaikonInvariants;

import synoptic.main.parser.ParseException;
import synoptic.model.state.State;
import synoptic.model.state.SynDaikonizer;
import synoptic.tests.SynopticTest;

/**
 * Checks that SynDaikonizer returns expected invariants of some kinds.
 * These unit tests are not intended to check every invariant that SynDaikonizer
 * returns; they only confirm that it returns some expected invariants.
 * 
 *
 */
public class SynDaikonizerTests extends SynopticTest {
    
    /**
     * Test OneOfScalar invariant.
     * 
     * @throws ParseException
     */
    @Test
    public void oneOfScalarTest() throws ParseException {
        State s1 = new State("x=-1");
        State s2 = new State("x=0");
        State s3 = new State("x=1");
        long[] expectedElems = new long[] { -1, 0, 1 };
        DaikonInvariants invariants = getDaikonInvariants(s1, s2, s3);
        for (Invariant invariant : invariants) {
            if (invariant instanceof OneOfScalar) {
                OneOfScalar oneOfInv = (OneOfScalar) invariant;
                long[] actualElems = oneOfInv.getElts();
                assertEquals(expectedElems.length, actualElems.length);
                
                for (long expectedElem : expectedElems) {
                    int index = Arrays.binarySearch(actualElems, expectedElem);
                    assertTrue(index >= 0);
                }
                return;
            }
        }
        fail("There is no OneOfScalar invariant: x one of {-1, 0, 1}.");
    }
    
    /**
     * Test IntEqual invariant.
     * 
     * @throws ParseException 
     */
    @Test
    public void intEqualTest() throws ParseException {
        State s1 = new State("x=1,y=1");
        State s2 = new State("x=2,y=2");
        State s3 = new State("x=3,y=3");
        DaikonInvariants invariants = getDaikonInvariants(s1, s2, s3);
        for (Invariant invariant : invariants) {
            if (invariant instanceof IntEqual) {
                IntEqual equalInv = (IntEqual) invariant;
                String var1 = equalInv.var1().str_name();
                String var2 = equalInv.var2().str_name();
                // This is to handle invariants like x == x and y == y.
                // TODO: Filter out such invariants.
                if ((var1.equals("x") && var2.equals("y"))
                        || (var1.equals("y") && var2.equals("x"))) {
                    return;
                }
            }
        }
        fail("There is no IntEqual invariant: x == y.");
    }
    
    /**
     * Test LinearBinary invariant.
     * 
     * @throws ParseException
     */
    @Test
    public void linearBinaryTest() throws ParseException {
        SynDaikonizer daikonizer = new SynDaikonizer();
        for (int i = 0; i < 100; i++) {
            String stateStr = "foo=" + i + ",bar=" + (i + 1);
            State state = new State(stateStr);
            daikonizer.addInstance(state);
        }
        DaikonInvariants invariants = daikonizer.getDaikonEnterInvariants();
        for (Invariant invariant : invariants) {
            if (invariant instanceof LinearBinary) {
                LinearBinary linearInv = (LinearBinary) invariant;
                String var1 = linearInv.var1().str_name();
                assertEquals("foo", var1);
                String var2 = linearInv.var2().str_name();
                assertEquals("bar", var2);
                double a = linearInv.core.a;
                assertEquals(1, a, 0.0001);
                double b = linearInv.core.b;
                assertEquals(-1, b, 0.0001);
                double c = linearInv.core.c;
                assertEquals(1, c, 0.0001);
            }
            return;
        }
        fail("There is no LinearBinary invariant: foo - bar + 1 = 0");
    }
    
    private DaikonInvariants getDaikonInvariants(State... states) {
        SynDaikonizer daikonizer = new SynDaikonizer();
        for (State state : states) {
            daikonizer.addInstance(state);
        }
        DaikonInvariants invariants = daikonizer.getDaikonEnterInvariants();
        return invariants;
    }
}
