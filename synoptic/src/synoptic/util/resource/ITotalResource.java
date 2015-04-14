package synoptic.util.resource;

/**
 * A totally ordered resource type with an integer value.
 */
public class ITotalResource extends AbstractResource {
    /** Resource value */
    public int value;
    String stuff;

    /**
     * Builds a Resource object from an int
     * 
     * @param i
     */
    public ITotalResource(int i) {
        super("");
        value = i;
    }

    /**
     * Builds a Resource object from an int and a resource key
     * 
     * @param i
     * @param key
     */
    public ITotalResource(int i, String key) {
        super(key);
        value = i;
    }

    @Override
    public boolean lessThan(AbstractResource r) {
        if (!isComparable(r)) {
            throw new NonComparableResourceException(this, r);
        }
        return value < ((ITotalResource) r).value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        result = prime * result + key.hashCode();
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
        ITotalResource other = (ITotalResource) obj;
        if (!key.equals(other.key)) {
            return false;
        }
        if (value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int compareTo(AbstractResource r) {
        if (!isComparable(r)) {
            throw new NonComparableResourceException(this, r);
        }
        return Integer.valueOf(value).compareTo(((ITotalResource) r).value);
    }

    @Override
    public AbstractResource computeDelta(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!isComparable(other)) {
            throw new NonComparableResourceException(this, other);
        }
        return new ITotalResource(this.value - ((ITotalResource) other).value,
                key);
    }

    @Override
    public AbstractResource incrBy(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!isComparable(other)) {
            throw new NonComparableResourceException(this, other);
        }
        return new ITotalResource(this.value + ((ITotalResource) other).value,
                key);
    }

    @Override
    public AbstractResource divBy(int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException();
        }

        return new ITotalResource(this.value / divisor, key);
    }

    @Override
    public AbstractResource normalize(AbstractResource relativeTime) {
        if (!isComparable(relativeTime)) {
            throw new NonComparableResourceException(this, relativeTime);
        }

        // If the relativeTime is zero, the normalized time should be zero, too
        if (relativeTime.equals(relativeTime.getZeroResource())) {
            return new DTotalResource(0.0, key);
        }

        return new DTotalResource(1.0 * this.value
                / ((ITotalResource) relativeTime).value, key);
    }

    @Override
    public AbstractResource getZeroResource() {
        return new ITotalResource(0, key);
    }
}
