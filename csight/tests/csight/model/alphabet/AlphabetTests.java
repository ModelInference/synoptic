package csight.model.alphabet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import csight.CSightTest;
import csight.model.alphabet.FSMAlphabet;

import synoptic.model.event.DistEventType;

public class AlphabetTests extends CSightTest {

    @Test
    public void createAlphabet() {
        FSMAlphabet<DistEventType> a = new FSMAlphabet<DistEventType>();
        DistEventType e = DistEventType.LocalEvent("e", 0);
        a.add(e);
        assertEquals(a.size(), 1);
        assertTrue(a.contains(e));

        assertFalse(a.equals(null));
        assertFalse(a.equals(""));
        assertTrue(a.equals(a));
    }
}
