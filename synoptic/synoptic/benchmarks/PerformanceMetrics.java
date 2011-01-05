package benchmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * A class to record performance metrics. It is a key value store, that keeps
 * record of the number of updates to each value. An update to a value (using
 * record) will add the value to the previously recorded one.
 * 
 * This also keeps track of a hierarchy of tasks: createTask adds a task to the
 * task stack, and a call to the task's stop method pops it.
 * 
 * @author Sigurd Schneider
 * 
 */
public class PerformanceMetrics {
	/**
	 * Print all recorded values.
	 */
	private static boolean VERBOSE = false;
	/**
	 * A global performance metric instance to record stats to.
	 */
	private static PerformanceMetrics globalPerformanceMetrics = new PerformanceMetrics();
	/**
	 * The last created timed task. This is used to create hierarchy.
	 */
	private static TimedTask previous = null;
	/**
	 * Holds the values mesured for each metric. Consecutive mesurements will be
	 * accumulated by addition here.
	 */
	HashMap<String, Long> values = new HashMap<String, Long>();
	/**
	 * For each metric, this holds the number of different measurements that
	 * contributed to the measurement. E.g., if add("task", _) has been called
	 * three times, this will hold 3.
	 */
	HashMap<String, Long> numberOfMesurements = new HashMap<String, Long>();

	/**
	 * Record whether the task should be accumulative or not. If a task is
	 * accumulative, it will not be divided by the number of measurements.
	 */
	HashMap<String, Boolean> accumulativity = new HashMap<String, Boolean>();

	/**
	 * Record the task t. This will increment the number of measurements for
	 * that task name, and pop previous from the task stack.
	 * 
	 * @param t
	 *            the task to record
	 */
	public void record(TimedTask t) {
		t.stop();
		record(t.getTask(), t.getTime());
		accumulativity.put(t.getTask(), t.getAccumulativity());
		previous = t.getParent();
	}

	/**
	 * Find out whether a task is accumulative
	 */
	private boolean getAccumulativity(String key) {
		if (!accumulativity.containsKey(key))
			return false;
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
		if (VERBOSE && !getAccumulativity(key)) {
			System.out.println(key + " = " + value);
		}
		if (!values.containsKey(key))
			values.put(key, 0L);
		values.put(key, values.get(key) + value);
		if (!numberOfMesurements.containsKey(key))
			numberOfMesurements.put(key, 0L);
		numberOfMesurements.put(key, numberOfMesurements.get(key) + 1);

	}

	/**
	 * retrieve the global performance metrics object
	 * 
	 * @return
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
	 * @param string
	 *            the task name. Choose something unique.
	 * @param accumulativity
	 *            set this task to be accumulative
	 * @return the timed task created
	 */
	public static TimedTask createTask(String string, boolean accumulativity) {
		previous = new TimedTask(string, previous, globalPerformanceMetrics,
				accumulativity);
		return previous;
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
