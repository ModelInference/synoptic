package synoptic.util.resource;

/**
 * A totally ordered time type with an integer value.
 */
public class ITotalTime extends AbstractResource {
    /** Time value */
    public int time;

    /**
     * Builds a Time object from an int
     * 
     * @param i
     */
    public ITotalTime(int i) {
        super("");
        time = i;
    }

    /**
     * Builds a Resource object from an int and a resource key
     * 
     * @param i
     * @param key
     */
    public ITotalTime(int i, String key) {
        super(key);
        time = i;
    }

    @Override
    public boolean lessThan(AbstractResource t) {
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
    public int compareTo(AbstractResource t) {
        if (!(t instanceof ITotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return Integer.valueOf(time).compareTo(((ITotalTime) t).time);
    }

    @Override
    public AbstractResource computeDelta(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof ITotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new ITotalTime(this.time - ((ITotalTime) other).time);
    }

    @Override
    public AbstractResource incrBy(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof ITotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new ITotalTime(this.time + ((ITotalTime) other).time);
    }

    @Override
    public AbstractResource divBy(int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException();
        }

        return new ITotalTime(this.time / divisor);
    }

    @Override
    public AbstractResource normalize(AbstractResource relativeTime) {
        if (!(relativeTime instanceof ITotalTime)) {
            throw new NonComparableTimesException(this, relativeTime);
        }

        // If the relativeTime is zero, the normalized time should be zero, too
        if (relativeTime.equals(relativeTime.getZeroResource())) {
            return new DTotalResource(0.0);
        }

        return new DTotalResource(1.0 * this.time
                / ((ITotalTime) relativeTime).time);
    }

    @Override
    public AbstractResource getZeroResource() {
        return new ITotalTime(0);
    }
}
