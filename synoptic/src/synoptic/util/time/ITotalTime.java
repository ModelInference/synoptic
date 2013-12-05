package synoptic.util.time;

/**
 * A totally ordered time type with an integer value.
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

    @Override
    public int compareTo(ITime t) {
        if (!(t instanceof ITotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return Integer.valueOf(time).compareTo(((ITotalTime) t).time);
    }

    @Override
    public ITime computeDelta(ITime other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof ITotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new ITotalTime(this.time - ((ITotalTime) other).time);
    }

    @Override
    public ITime incrBy(ITime other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof ITotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new ITotalTime(this.time + ((ITotalTime) other).time);
    }

    @Override
    public ITime divBy(int divisor) {
        if (divisor < 1) {
            throw new IllegalArgumentException();
        }

        return new ITotalTime(this.time / divisor);
    }

    @Override
    public ITime normalize(ITime relativeTime) {
        if (!(relativeTime instanceof ITotalTime)) {
            throw new NonComparableTimesException(this, relativeTime);
        }

        // If the relativeTime is zero, the normalized time should be zero, too
        if (relativeTime.equals(relativeTime.getZeroTime())) {
            return new DTotalTime(0.0);
        }

        return new DTotalTime(1.0 * this.time
                / ((ITotalTime) relativeTime).time);
    }

    @Override
    public ITime getZeroTime() {
        return new ITotalTime(0);
    }
}
