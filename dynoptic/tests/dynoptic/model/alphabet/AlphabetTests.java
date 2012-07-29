package dynoptic.model.alphabet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dynoptic.DynopticTest;

public class AlphabetTests extends DynopticTest {

    @Test
    public void createAlphabet() {
        FSMAlphabet a = new FSMAlphabet();
        EventType e = EventType.LocalEvent("e", 0);
        a.add(e);
        assertEquals(a.size(), 1);
        assertTrue(a.contains(e));
    }
}
