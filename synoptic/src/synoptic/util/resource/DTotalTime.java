package synoptic.util.resource;

/**
 * A totally ordered time type with a double value.
 */
public class DTotalTime extends AbstractResource {
    /** Time value */
    public double time;

    /**
     * Builds a Time object from a double
     * 
     * @param d
     */
    public DTotalTime(double d) {
        super("");
        time = d;
    }

    /**
     * Builds a Resource object from a double and a resource key
     * 
     * @param d
     * @param key
     */
    public DTotalTime(double d, String key) {
        super(key);
        time = d;
    }

    @Override
    public boolean lessThan(AbstractResource t) {
        if (!(t instanceof DTotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return time < ((DTotalTime) t).time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(time);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        DTotalTime other = (DTotalTime) obj;
        if (Double.doubleToLongBits(time) != Double
                .doubleToLongBits(other.time)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Double.toString(time);
    }

    @Override
    public int compareTo(AbstractResource t) {
        if (!(t instanceof DTotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return new Double(time).compareTo(((DTotalTime) t).time);
    }

    @Override
    public AbstractResource computeDelta(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof DTotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new DTotalTime(this.time - ((DTotalTime) other).time);
    }

    @Override
    public AbstractResource incrBy(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof DTotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new DTotalTime(this.time + ((DTotalTime) other).time);
    }

    @Override
    public AbstractResource divBy(int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException();
        }
        return new DTotalTime(this.time / divisor);
    }

    @Override
    public AbstractResource normalize(AbstractResource relativeTime) {
        if (!(relativeTime instanceof DTotalTime)) {
            throw new NonComparableTimesException(this, relativeTime);
        }

        // If the relativeTime is zero, the normalized time should be zero, too
        if (relativeTime.equals(relativeTime.getZeroResource())) {
            return new DTotalTime(0.0);
        }

        return new DTotalTime(this.time / ((DTotalTime) relativeTime).time);
    }

    @Override
    public AbstractResource getZeroResource() {
        return new DTotalTime(0.0);
    }
}
