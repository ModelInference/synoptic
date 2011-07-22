package synoptic.util.time;

/**
 * A wrapper for a totally ordered time type with an integer value.
 */
public class ITotalTime implements ITime {
    /** Time value */
    public int time;

    /**
     * Builds a Time object from an int
     * 
     * @param i
     * @throws IllegalArgumentException
     *             when i is negative
     */
    public ITotalTime(int i) {
        if (i < 0) {
            throw new IllegalArgumentException();
        }
        time = i;
    }

    @Override
    public boolean lessThan(ITime t) {
        if (!(t instanceof ITotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return time < ((ITotalTime) t).time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + time;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ITotalTime other = (ITotalTime) obj;
        if (time != other.time) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Integer.toString(time);
    }
}
