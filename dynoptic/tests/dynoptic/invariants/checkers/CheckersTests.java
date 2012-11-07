package dynoptic.invariants.checkers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.invariants.EventuallyHappens;
import dynoptic.invariants.NeverFollowedBy;
import dynoptic.invariants.checkers.BinChecker.Validity;

import synoptic.model.event.DistEventType;

public class CheckersTests {

    @Test
    public void checkAFby() {
        Validity v;
        DistEventType x = DistEventType.LocalEvent("x", 0);
        DistEventType y = DistEventType.LocalEvent("y", 1);
        DistEventType z = DistEventType.LocalEvent("z", 1);

        BinaryInvariant inv = new AlwaysFollowedBy(x, y);
        BinChecker<?> invCh = inv.newChecker();
        assertFalse(invCh.isFail());

        // x
        v = invCh.transition(x);
        assertEquals(v, Validity.TEMP_FAIL);
        assertTrue(invCh.isFail());

        // x - z
        v = invCh.transition(z);
        assertEquals(v, Validity.TEMP_FAIL);
        assertTrue(invCh.isFail());

        // x - z - y
        v = invCh.transition(y);
        assertEquals(v, Validity.TEMP_SUCCESS);
        assertFalse(invCh.isFail());

        // x - z - y - z
        v = invCh.transition(z);
        assertEquals(v, Validity.TEMP_SUCCESS);
        assertFalse(invCh.isFail());

        // x - z - y - z - x
        v = invCh.transition(x);
        assertEquals(v, Validity.TEMP_FAIL);
        assertTrue(invCh.isFail());

        // x - z - y - z - x - x
        v = invCh.transition(x);
        assertEquals(v, Validity.TEMP_FAIL);
        assertTrue(invCh.isFail());
    }

    @Test
    public void checkNFby() {
        Validity v;
        DistEventType x = DistEventType.LocalEvent("x", 0);
        DistEventType y = DistEventType.LocalEvent("y", 1);
        DistEventType z = DistEventType.LocalEvent("z", 1);

        BinaryInvariant inv = new NeverFollowedBy(x, y);
        BinChecker<?> invCh = inv.newChecker();
        assertFalse(invCh.isFail());

        // y
        // y before x is okay.
        v = invCh.transition(y);
        assertEquals(v, Validity.TEMP_SUCCESS);
        assertFalse(invCh.isFail());

        // y - x
        v = invCh.transition(x);
        assertEquals(v, Validity.TEMP_SUCCESS);
        assertFalse(invCh.isFail());

        // y - x - z
        v = invCh.transition(z);
        assertEquals(v, Validity.TEMP_SUCCESS);
        assertFalse(invCh.isFail());

        // y - x - z - y
        // After the y, we are in permanent failure state.
        v = invCh.transition(y);
        assertEquals(v, Validity.PERM_FAIL);
        assertTrue(invCh.isFail());

        // y - x - z - y - z
        v = invCh.transition(z);
        assertEquals(v, Validity.PERM_FAIL);
        assertTrue(invCh.isFail());

        // y - x - z - y - z - x
        v = invCh.transition(x);
        assertEquals(v, Validity.PERM_FAIL);
        assertTrue(invCh.isFail());

        // y - x - z - y - z - x - x
        v = invCh.transition(x);
        assertEquals(v, Validity.PERM_FAIL);
        assertTrue(invCh.isFail());
    }

    @Test
    public void checkAP() {
        Validity v;
        DistEventType x = DistEventType.LocalEvent("x", 0);
        DistEventType y = DistEventType.LocalEvent("y", 1);
        DistEventType z = DistEventType.LocalEvent("z", 1);

        BinaryInvariant inv = new AlwaysPrecedes(x, y);
        BinChecker<?> invCh = inv.newChecker();
        assertFalse(invCh.isFail());

        // z
        v = invCh.transition(z);
        assertEquals(v, Validity.TEMP_SUCCESS);
        assertFalse(invCh.isFail());

        // z - x
        // Once we see an x, we are in permanent success state.
        v = invCh.transition(x);
        assertEquals(v, Validity.PERM_SUCCESS);
        assertFalse(invCh.isFail());

        // z - x - y
        v = invCh.transition(y);
        assertEquals(v, Validity.PERM_SUCCESS);
        assertFalse(invCh.isFail());

        // z - x - y - x
        v = invCh.transition(x);
        assertEquals(v, Validity.PERM_SUCCESS);
        assertFalse(invCh.isFail());

        // /////////////////////////////

        invCh = inv.newChecker();

        // z
        v = invCh.transition(z);
        assertEquals(v, Validity.TEMP_SUCCESS);
        assertFalse(invCh.isFail());

        // z - y
        // Once we see a y, we are in permanent failed state.
        v = invCh.transition(y);
        assertEquals(v, Validity.PERM_FAIL);
        assertTrue(invCh.isFail());

        // z - y - x
        v = invCh.transition(x);
        assertEquals(v, Validity.PERM_FAIL);
        assertTrue(invCh.isFail());
    }

    @Test
    public void checkEventually() {
        Validity v;
        DistEventType x = DistEventType.LocalEvent("x", 0);
        DistEventType y = DistEventType.LocalEvent("y", 1);

        BinaryInvariant inv = new EventuallyHappens(x);
        BinChecker<?> invCh = inv.newChecker();
        assertTrue(invCh.isFail());

        // y
        v = invCh.transition(y);
        assertEquals(v, Validity.TEMP_FAIL);
        assertTrue(invCh.isFail());

        // y - y
        v = invCh.transition(y);
        assertEquals(v, Validity.TEMP_FAIL);
        assertTrue(invCh.isFail());

        // y - y - x
        v = invCh.transition(x);
        assertEquals(v, Validity.PERM_SUCCESS);
        assertFalse(invCh.isFail());

        // y - y - x - y
        v = invCh.transition(y);
        assertEquals(v, Validity.PERM_SUCCESS);
        assertFalse(invCh.isFail());
    }

}
