package synoptic.benchmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import synoptic.main.SynopticMain;

/**
 * A class to record performance metrics. It is a key value store, that keeps
 * record of the number of updates to each value. An update to a value (using
 * record) will add the value to the previously recorded one. This also keeps
 * track of a hierarchy of tasks: createTask adds a task to the task stack, and
 * a call to the task's stop method pops it.
 * 
 * @author Sigurd Schneider
 */
public class PerformanceMetrics {
    private static Logger logger = Logger.getLogger("Performance Metrics");
    /**
     * A global performance metric instance to record statistics.
     */
    private static PerformanceMetrics globalPerformanceMetrics = new PerformanceMetrics();
    /**
     * The last created timed task. This is used to create a hierarchy of tasks.
     */
    private static TimedTask previousTask = null;
    /**
     * Holds the values measured for each metric. Consecutive measurements will
     * be accumulated by addition here.
     */
    LinkedHashMap<String, Long> values = new LinkedHashMap<String, Long>();
    /**
     * For each metric, this holds the number of different measurements that
     * contributed to the measurement. E.g., if add("task", _) has been called
     * three times, this will hold 3.
     */
    LinkedHashMap<String, Long> numberOfMesurements = new LinkedHashMap<String, Long>();

    /**
     * Record whether the task should be accumulative or not. If a task is
     * accumulative, it will not be divided by the number of measurements.
     */
    LinkedHashMap<String, Boolean> accumulativity = new LinkedHashMap<String, Boolean>();

    /**
     * Record the task t. This will increment the number of measurements for
     * that task name, and pop previousTask from the task stack.
     * 
     * @param t
     *            the task to record
     */
    public void record(TimedTask t) {
        // t.stop();
        record(t.getTask(), t.getTime());
        accumulativity.put(t.getTask(), t.getAccumulativity());
        previousTask = t.getParent();
    }

    /**
     * Find out whether a task is accumulative
     */
    private boolean getAccumulativity(String key) {
        if (!accumulativity.containsKey(key)) {
            return false;
        }
        return accumulativity.get(key);
    }

    /**
     * Record a value. This will increment the number of measurements for the
     * given key, and add the value to the value saved for that key. If no
     * previous value has been recorded, the previous value is set to 0.
     * 
     * @param key
     *            name of this measurement
     * @param value
     *            the measured value
     */
    public void record(String key, long value) {
        if (!getAccumulativity(key)) {
            // Print all recorded values.
            if (SynopticMain.getInstanceWithExistenceCheck().options.doBenchmarking) {
                logger.fine(key + " = " + value);
            }
        }
        if (!values.containsKey(key)) {
            values.put(key, 0L);
        }
        values.put(key, values.get(key) + value);
        if (!numberOfMesurements.containsKey(key)) {
            numberOfMesurements.put(key, 0L);
        }
        numberOfMesurements.put(key, numberOfMesurements.get(key) + 1);

    }

    /**
     * Returns the global performance metrics object
     */
    static public PerformanceMetrics get() {
        return globalPerformanceMetrics;
    }

    /**
     * Create a new task.
     * 
     * @param string
     *            the task name. Choose something unique.
     * @return the timed task created
     */
    public static TimedTask createTask(String string) {
        return createTask(string, false);
    }

    /**
     * Create a new task.
     * 
     * @param taskName
     *            the task name. Choose something unique.
     * @param accumulativity
     *            set this task to be accumulative
     * @return the created timed task
     */
    public static TimedTask createTask(String taskName, boolean accumulativity) {
        previousTask = new TimedTask(taskName, previousTask,
                globalPerformanceMetrics, accumulativity);
        return previousTask;
    }

    /**
     * Get the data averaged over recording numbers. That means every
     * measurements is divided by the number of measurements for that key.
     * 
     * @return the data in alphabetic order
     */
    public String getDataRelative() {
        ArrayList<String> keys = new ArrayList<String>(values.keySet());
        Collections.sort(keys);
        StringBuilder str = new StringBuilder();
        for (String key : keys) {
            str.append(values.get(key) / numberOfMesurements.get(key) + " ");
        }
        return str.toString();
    }

    /**
     * Get the data associated with the keys in alphabetic order.
     * 
     * @param divisor
     *            the number to divide each measurement through (use for avg)
     * @return data in alphabetic order
     */
    public String getDataDividedBy(int divisor) {
        ArrayList<String> keys = new ArrayList<String>(values.keySet());
        Collections.sort(keys);
        StringBuilder str = new StringBuilder();
        for (String key : keys) {
            str.append(values.get(key) / divisor + " ");
        }
        return str.toString();
    }

    /**
     * Get the keys in alphabetic order.
     * 
     * @return keys in alphabetic order
     */
    public String getHeader() {
        ArrayList<String> keys = new ArrayList<String>(values.keySet());
        Collections.sort(keys);
        StringBuilder str = new StringBuilder();
        for (String key : keys) {
            str.append(key + " ");
        }
        return str.toString();
    }

    /**
     * Resets the global performance metrics object.
     */
    public static void clear() {
        globalPerformanceMetrics = new PerformanceMetrics();
    }
}
