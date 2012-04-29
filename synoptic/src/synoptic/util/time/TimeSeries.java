package synoptic.util.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates a list of time instances that extend ITime.
 */
public class TimeSeries<TimeType extends ITime> implements
        Comparable<TimeSeries<ITime>> {
    private List<TimeType> times;
    boolean isSorted;

    public TimeSeries() {
        times = new ArrayList<TimeType>();
        isSorted = true;
    }

    /**
     * If the times list is not sorted, then sorts the list in-place.
     */
    private void sort() {
        if (!isSorted) {
            Collections.sort(times);
            isSorted = true;
        }
    }

    /**
     * @return mode delta time for transition, null if transition has zero delta
     *         times.
     */
    public TimeType computeMode() {

        if (this.times.isEmpty()) {
            return null;
        }

        Map<TimeType, Integer> counts = new HashMap<TimeType, Integer>();
        TimeType mostCommon = null;
        int max = 0;
        for (TimeType delta : times) {
            Integer count = counts.get(delta);

            if (count == null) {
                count = 1;
            } else {
                count++;
            }

            if (count > max) {
                mostCommon = delta;
                max = count;
            }

            counts.put(delta, count);
        }

        return mostCommon;
    }

    /**
     * @return median delta time for transition, null if transition has zero
     *         delta times.
     */
    public TimeType computeMedian() {
        if (this.times.isEmpty()) {
            return null;
        }

        // Sort the list.
        sort();

        int middle = times.size() / 2;
        if (times.size() % 2 == 1) {
            // Odd length.
            return times.get(middle);
        }
        // Event length.
        return (TimeType) times.get(middle - 1).incrBy(times.get(middle))
                .divBy(2);
    }

    /**
     * @return mean delta time for transition, null if transition has zero delta
     *         times.
     */
    @SuppressWarnings("unchecked")
    public TimeType computeMean() {
        if (times.isEmpty()) {
            return null;
        }

        // Create a zero valued starting point.
        // TODO create some sort of method to construct a zero valued time
        // as this is rather confusing.
        TimeType initial = this.times.get(0);
        initial = (TimeType) initial.computeDelta(initial);

        for (TimeType t : times) {
            initial = (TimeType) initial.incrBy(t);
        }

        return (TimeType) initial.divBy(times.size());
    }

    /**
     * Adds a time for the transition between the source and target nodes.
     * 
     * @param delta
     *            The time between nodes.
     */
    public void addDelta(TimeType t) {
        assert t != null;
        times.add(t);
        isSorted = false;
    }

    /**
     * Adds a collection of times for transition between source and target
     * nodes.
     * 
     * @param deltas
     */
    public void addAllDeltas(Collection<TimeType> deltas) {
        assert deltas != null;
        times.addAll(deltas);
    }

    @Override
    public int compareTo(TimeSeries<ITime> o) {
        int cmp;

        sort();
        cmp = ((Integer) times.size()).compareTo(o.times.size());
        if (cmp != 0) {
            return cmp;
        }

        int i = 0;
        for (ITime t : times) {
            cmp = t.compareTo(o.times.get(i));
            if (cmp != 0) {
                return cmp;
            }
            i += 1;
        }
        return 0;
    }
}
