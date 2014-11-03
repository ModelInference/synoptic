package synoptic.util.time;

/**
 * A totally ordered time type with a long integer value.
 */
public class LTotalTime implements ITime {
    /** Time value */
    public long time;

    /**
     * Builds a Time object from an long
     * 
     * @param i
     */
    public LTotalTime(long i) {
        time = i;
    }

    @Override
    public boolean lessThan(ITime t) {
        if (!(t instanceof LTotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return time < ((LTotalTime) t).time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) time;
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
        LTotalTime other = (LTotalTime) obj;
        if (time != other.time) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Long.toString(time);
    }

    @Override
    public int compareTo(ITime t) {
        if (!(t instanceof LTotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return Long.valueOf(time).compareTo(((LTotalTime) t).time);
    }

    @Override
    public ITime computeDelta(ITime other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof LTotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new LTotalTime(this.time - ((LTotalTime) other).time);
    }

    @Override
    public ITime incrBy(ITime other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof LTotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new LTotalTime(this.time + ((LTotalTime) other).time);
    }

    @Override
    public ITime divBy(int divisor) {
        if (divisor < 1) {
            throw new IllegalArgumentException();
        }

        return new LTotalTime(this.time / divisor);
    }

    @Override
    public ITime normalize(ITime relativeTime) {
        if (!(relativeTime instanceof LTotalTime)) {
            throw new NonComparableTimesException(this, relativeTime);
        }

        // If the relativeTime is zero, the normalized time should be zero, too
        if (relativeTime.equals(relativeTime.getZeroTime())) {
            return new DTotalTime(0.0);
        }

        return new DTotalTime(1.0 * this.time
                / ((LTotalTime) relativeTime).time);
    }

    @Override
    public ITime getZeroTime() {
        return new LTotalTime(0);
    }
}
