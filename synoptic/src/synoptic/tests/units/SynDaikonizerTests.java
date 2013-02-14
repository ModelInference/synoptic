package synoptic.tests.units;

import java.util.List;

import org.junit.Test;

import daikon.inv.Invariant;

import synoptic.model.state.State;
import synoptic.model.state.SynDaikonizer;
import synoptic.tests.SynopticTest;

public class SynDaikonizerTests extends SynopticTest {
    
    /**
     * All states have 1 variable: x.
     * 
     * @throws Exception
     */
    @Test
    public void oneVariableTest() throws Exception {
        State s1 = new State("x=-1");
        State s2 = new State("x=0");
        State s3 = new State("x=1");
        detectDaikonInvariants(s1, s2, s3);
    }
    
    /**
     * All states have 2 variables: x and y.
     * 
     * @throws Exception
     */
    @Test
    public void twoVariablesTest() throws Exception {
        State s1 = new State("x=1,y=1");
        State s2 = new State("x=2,y=2");
        State s3 = new State("x=3,y=3");
        detectDaikonInvariants(s1, s2, s3);
    }
    
    /**
     * All states have 3 variables: x, y and z.
     * 
     * @throws Exception
     */
    @Test
    public void threeVariablesTest() throws Exception {
        State s1 = new State("x=1,y=1,z=2");
        State s2 = new State("x=2,y=-1,z=1");
        State s3 = new State("x=3,y=0,z=3");
        detectDaikonInvariants(s1, s2, s3);
    }
    
    /**
     * Detects invariants of the states and prints them.
     */
    private void detectDaikonInvariants(State... states) throws Exception {
        SynDaikonizer daikonizer = new SynDaikonizer();
        for (State state : states) {
            daikonizer.addInstance(state);
        }
        List<Invariant> invariants = daikonizer.getDaikonEnterInvariants();
        System.out.println("Invariants of the states:");
        for (State state : states) {
            System.out.println("\t" + state);
        }
        System.out.println("are:");
        for (Invariant inv : invariants) {
            System.out.println("\t" + inv);
        }
    }
}
