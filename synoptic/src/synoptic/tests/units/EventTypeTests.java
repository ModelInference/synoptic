package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import synoptic.model.DistEventType;
import synoptic.model.StringEventType;
import synoptic.tests.SynopticTest;

/**
 * Unit tests for all EventType classes.
 */
public class EventTypeTests extends SynopticTest {
    /**
     * Tests StringEventTypes equals(), hashCode(), and compareTo() methods
     */
    @Test
    public void basicStringEventTypesTest() {
        // Different
        StringEventType e1 = new StringEventType("a");
        StringEventType e2 = new StringEventType("b");
        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e1));
        assertTrue(e1.compareTo(e2) != 0);
        assertTrue(e2.compareTo(e1) != 0);

        // Identical
        e1 = new StringEventType("a");
        e2 = new StringEventType("a");
        assertTrue(e1.equals(e2));
        assertTrue(e2.equals(e1));
        assertTrue(e1.hashCode() == e2.hashCode());
        assertTrue(e1.compareTo(e2) == 0);
        assertTrue(e2.compareTo(e1) == 0);

        // Initial and non-Initial
        e1 = StringEventType.NewInitialStringEventType();
        e2 = new StringEventType("a");
        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e1));
        assertTrue(e1.compareTo(e2) != 0);
        assertTrue(e2.compareTo(e1) != 0);

        // Terminal and non-Terminal
        e1 = StringEventType.NewTerminalStringEventType();
        e2 = new StringEventType("a");
        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e1));
        assertTrue(e1.compareTo(e2) != 0);
        assertTrue(e2.compareTo(e1) != 0);

        // Terminal and Initial
        e1 = StringEventType.NewTerminalStringEventType();
        e2 = StringEventType.NewInitialStringEventType();
        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e1));
        assertTrue(e1.compareTo(e2) != 0);
        assertTrue(e2.compareTo(e1) != 0);

        // Initial and Initial
        e1 = StringEventType.NewInitialStringEventType();
        e2 = StringEventType.NewInitialStringEventType();
        assertTrue(e1.equals(e2));
        assertTrue(e2.equals(e1));
        assertTrue(e1.hashCode() == e2.hashCode());
        assertTrue(e1.compareTo(e2) == 0);
        assertTrue(e2.compareTo(e1) == 0);

        // Terminal and Terminal
        e1 = StringEventType.NewTerminalStringEventType();
        e2 = StringEventType.NewTerminalStringEventType();
        assertTrue(e1.equals(e2));
        assertTrue(e2.equals(e1));
        assertTrue(e1.hashCode() == e2.hashCode());
        assertTrue(e1.compareTo(e2) == 0);
        assertTrue(e2.compareTo(e1) == 0);
    }

    /**
     * Tests that different EventTypes (for now, just StringEventType and
     * DistEventType) can never be equal
     */
    public void diffTypeEqualityEventTypeTest() {
        StringEventType e1 = StringEventType.NewInitialStringEventType();
        DistEventType e3 = DistEventType.NewInitialDistEventType();
        assertFalse(e1.equals(e3));
        assertFalse(e3.equals(e1));

        e1 = StringEventType.NewTerminalStringEventType();
        e3 = DistEventType.NewTerminalDistEventType();
        assertFalse(e1.equals(e3));
        assertFalse(e3.equals(e1));
    }

    /**
     * Tests StringEventType's exceptional case in compareTo()
     */
    @Test(expected = ClassCastException.class)
    public void compareToExceptionStringEventTypeTest() {
        StringEventType e1 = StringEventType.NewInitialStringEventType();
        DistEventType e3 = DistEventType.NewInitialDistEventType();
        assertTrue(e1.compareTo(e3) != 0);
    }

    /**
     * Tests DistEventType equals(), hashCode(), and compareTo() methods
     */
    @Test
    public void basicDistEventTypeTest() {
        // Different in at least one aspect
        DistEventType e1 = new DistEventType("a", 1);
        DistEventType e2 = new DistEventType("b", 1);
        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e1));
        assertTrue(e1.compareTo(e2) != 0);
        assertTrue(e2.compareTo(e1) != 0);

        e1 = new DistEventType("a", 1);
        e2 = new DistEventType("a", 2);
        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e1));
        assertTrue(e1.compareTo(e2) != 0);
        assertTrue(e2.compareTo(e1) != 0);

        // Identical
        e1 = new DistEventType("a", 1);
        e2 = new DistEventType("a", 1);
        assertTrue(e1.equals(e2));
        assertTrue(e2.equals(e1));
        assertTrue(e1.hashCode() == e2.hashCode());
        assertTrue(e1.compareTo(e2) == 0);
        assertTrue(e2.compareTo(e1) == 0);

        // Initial and non-Initial
        e1 = DistEventType.NewInitialDistEventType();
        e2 = new DistEventType("a", 1);
        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e1));
        assertTrue(e1.compareTo(e2) != 0);
        assertTrue(e2.compareTo(e1) != 0);

        // Terminal and non-Terminal
        e1 = DistEventType.NewTerminalDistEventType();
        e2 = new DistEventType("a", 1);
        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e1));
        assertTrue(e1.compareTo(e2) != 0);
        assertTrue(e2.compareTo(e1) != 0);

        // Terminal and Initial
        e1 = DistEventType.NewTerminalDistEventType();
        e2 = DistEventType.NewInitialDistEventType();
        assertFalse(e1.equals(e2));
        assertFalse(e2.equals(e1));
        assertTrue(e1.compareTo(e2) != 0);
        assertTrue(e2.compareTo(e1) != 0);

        // Initial and Initial
        e1 = DistEventType.NewInitialDistEventType();
        e2 = DistEventType.NewInitialDistEventType();
        assertTrue(e1.equals(e2));
        assertTrue(e2.equals(e1));
        assertTrue(e1.hashCode() == e2.hashCode());
        assertTrue(e1.compareTo(e2) == 0);
        assertTrue(e2.compareTo(e1) == 0);

        // Terminal and Terminal
        e1 = DistEventType.NewTerminalDistEventType();
        e2 = DistEventType.NewTerminalDistEventType();
        assertTrue(e1.equals(e2));
        assertTrue(e2.equals(e1));
        assertTrue(e1.hashCode() == e2.hashCode());
        assertTrue(e1.compareTo(e2) == 0);
        assertTrue(e2.compareTo(e1) == 0);
    }
}
