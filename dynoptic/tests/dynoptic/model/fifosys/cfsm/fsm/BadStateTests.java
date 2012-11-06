package dynoptic.model.fifosys.cfsm.fsm;

import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.cfsm.BadState;
import dynoptic.model.fifosys.cfsm.CFSMState;
import dynoptic.util.Util;

public class BadStateTests extends DynopticTest {

    // Initial state for pid 0.
    FSMState i_0;

    // Accepting state at pid 1.
    FSMState a_1;

    @Test
    public void createAndScmString() {
        i_0 = new FSMState(false, true, 0, 0);
        a_1 = new FSMState(true, false, 1, 2);
        List<FSMState> states = Util.newList();
        states.add(i_0);
        states.add(a_1);
        CFSMState c = new CFSMState(states);

        List<String> qReList = Util.newList();
        qReList.add(".*");
        qReList.add(".*");

        BadState b = new BadState(c, qReList);
        logger.info(b.toScmString());
        logger.info(b.toString());
    }

}
