package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import synoptic.benchmarks.TimedTask;
import synoptic.tests.SynopticTest;

/**
 * Tests for synoptic.benchmarks.TimedTask class
 * 
 * @author ivan
 */
public class TimedTaskTests extends SynopticTest {
    /**
     * Make sure we can create a task, and check that it records time correctly.
     * 
     * @throws InterruptedException
     */
    @Test
    public void runTaskTest() throws InterruptedException {
        String taskName = "task1";
        TimedTask t = new TimedTask(taskName);
        assertTrue(t.getTask().equals(taskName));
        assertTrue(t.getTime() == null);
        assertTrue(t.getAccumulativity() == false);
        t.setAccumulativity(true);
        assertTrue(t.getAccumulativity() == true);
        assertTrue(t.getParent() == null);

        t = new TimedTask(taskName);
        Thread.sleep(10);
        t.stop();
        logger.fine(t.toString());
        // Assumption: the sleep(10ms) statement took 10-30ms to run
        assertTrue(t.getTime() >= 10);
        assertTrue(t.getTime() <= 30);
    }
}
