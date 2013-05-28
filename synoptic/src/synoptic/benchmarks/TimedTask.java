package synoptic.benchmarks;

/**
 * A timed task for recording the duration of a task.
 * 
 * @author Sigurd Schneider
 */
public class TimedTask {
    /**
     * Task start time (as returned by currentTimeMillis)
     */
    private final long timeStart;
    /**
     * Task end time
     */
    private Long timeEnd;
    /**
     * Name of the task
     */
    private final String task;
    /**
     * PerformanceMetrics instance to record things at.
     */
    private PerformanceMetrics recordAt = null;
    /**
     * The timed task this task is a subcomputation of.
     */
    private TimedTask parent = null;
    /**
     * Accumulativity is true, if the recorded number shall not be averaged by
     * PerformanceMetrics.
     */
    boolean accumulativity = false;

    /**
     * Construct a timed task. Construction also records the starting time.
     * 
     * @param task
     *            the name of the task
     */
    public TimedTask(String task) {
        this.task = task;
        timeStart = System.currentTimeMillis();
    }

    /**
     * Create a task. Once stop is called, the task will be recorded at the
     * performance metrics recordAt.
     * 
     * @param task
     *            the task name
     * @param parent
     *            the parent task
     * @param recordAt
     *            where to record this task when stop is called
     * @param accumulativity
     */
    public TimedTask(String task, TimedTask parent,
            PerformanceMetrics recordAt, boolean accumulativity) {
        this(parent == null ? task : parent.getTask() + "/" + task);
        this.parent = parent;
        this.accumulativity = accumulativity;
        this.recordAt = recordAt;
    }

    /**
     * Set the accumulativity of this task.
     */
    public void setAccumulativity(boolean accumulativity) {
        this.accumulativity = accumulativity;
    }

    /**
     * Get the accumulativity of this task.
     */
    public boolean getAccumulativity() {
        return accumulativity;
    }

    /**
     * Get the parent task.
     * 
     * @return the parent task, may be null.
     */
    public TimedTask getParent() {
        return parent;
    }

    /**
     * Stop the timed task. This takes the current system time and saves it.
     * Calling stop on TimedTasks that have been stopped before has no effect.
     * If the task was given a recordAt argument at construction time, it will
     * be recorded there.
     */
    public void stop() {
        assert (timeEnd == null);
        timeEnd = System.currentTimeMillis();
        if (recordAt != null) {
            recordAt.record(this);
        }
    }

    /**
     * Get a string representing this timed task. If the task has not been
     * stopped yet, it will be stopped.
     */
    @Override
    public String toString() {
        if (timeEnd == null) {
            stop();
        }
        return task + " in " + (timeEnd - timeStart) + "ms";
    }

    /**
     * Get the name of the task.
     */
    public String getTask() {
        return task;
    }

    /**
     * Get the duration of the task. Will return null of it has not been stopped
     * yet.
     */
    public Long getTime() {
        if (timeEnd == null) {
            return null;
        }
        return timeEnd - timeStart;
    }
}
