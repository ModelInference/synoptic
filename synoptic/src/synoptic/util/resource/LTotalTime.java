package synoptic.util.resource;

/**
 * A totally ordered time type with a long integer value.
 */
public class LTotalTime extends AbstractResource {
    /** Time value */
    public long time;

    /**
     * Builds a Time object from an long
     * 
     * @param i
     */
    public LTotalTime(long i) {
        super("");
        time = i;
    }

    /**
     * Builds a Resource object form a long and a resource key
     * 
     * @param i
     * @param key
     */
    public LTotalTime(long i, String key) {
        super(key);
        time = i;
    }

    @Override
    public boolean lessThan(AbstractResource t) {
        if (!(t instanceof LTotalTime)) {
            throw new NonComparableResourceException(this, t);
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
    public int compareTo(AbstractResource t) {
        if (!(t instanceof LTotalTime)) {
            throw new NonComparableResourceException(this, t);
        }
        return Long.valueOf(time).compareTo(((LTotalTime) t).time);
    }

    @Override
    public AbstractResource computeDelta(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof LTotalTime)) {
            throw new NonComparableResourceException(this, other);
        }
        return new LTotalTime(this.time - ((LTotalTime) other).time);
    }

    @Override
    public AbstractResource incrBy(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof LTotalTime)) {
            throw new NonComparableResourceException(this, other);
        }
        return new LTotalTime(this.time + ((LTotalTime) other).time);
    }

    @Override
    public AbstractResource divBy(int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException();
        }

        return new LTotalTime(this.time / divisor);
    }

    @Override
    public AbstractResource normalize(AbstractResource relativeTime) {
        if (!(relativeTime instanceof LTotalTime)) {
            throw new NonComparableResourceException(this, relativeTime);
        }

        // If the relativeTime is zero, the normalized time should be zero, too
        if (relativeTime.equals(relativeTime.getZeroResource())) {
            return new DTotalResource(0.0);
        }

        return new DTotalResource(1.0 * this.time
                / ((LTotalTime) relativeTime).time);
    }

    @Override
    public AbstractResource getZeroResource() {
        return new LTotalTime(0);
    }
}
