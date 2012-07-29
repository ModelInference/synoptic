package dynoptic.model.fifosys.gfsm.trace;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;

public class ObservedEventTests extends DynopticTest {

    @Test
    public void create() {
        ObservedEvent e = new ObservedEvent(EventType.LocalEvent("e", 1));
    }

}
