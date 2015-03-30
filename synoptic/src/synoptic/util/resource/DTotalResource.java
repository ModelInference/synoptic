package synoptic.util.resource;

/**
 * A totally ordered resource type with a double value.
 */
public class DTotalResource extends AbstractResource {
    /** Resource value */
    public double value;

    /**
     * Builds a Resource object from a double
     * 
     * @param d
     */
    public DTotalResource(double d) {
        super("");
        value = d;
    }

    /**
     * Builds a Resource object from a double and a resource key
     * 
     * @param d
     * @param key
     */
    public DTotalResource(double d, String key) {
        super(key);
        value = d;
    }

    @Override
    public boolean lessThan(AbstractResource r) {
        if (!(r instanceof DTotalResource)) {
            throw new NonComparableTimesException(this, r);
        }
        return value < ((DTotalResource) r).value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(value);
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
        DTotalResource other = (DTotalResource) obj;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public int compareTo(AbstractResource r) {
        if (!(r instanceof DTotalResource)) {
            throw new NonComparableTimesException(this, r);
        }
        return new Double(value).compareTo(((DTotalResource) r).value);
    }

    @Override
    public AbstractResource computeDelta(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof DTotalResource)) {
            throw new NonComparableTimesException(this, other);
        }
        return new DTotalResource(this.value - ((DTotalResource) other).value);
    }

    @Override
    public AbstractResource incrBy(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof DTotalResource)) {
            throw new NonComparableTimesException(this, other);
        }
        return new DTotalResource(this.value + ((DTotalResource) other).value);
    }

    @Override
    public AbstractResource divBy(int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException();
        }
        return new DTotalResource(this.value / divisor);
    }

    @Override
    public AbstractResource normalize(AbstractResource relativeResource) {
        if (!(relativeResource instanceof DTotalResource)) {
            throw new NonComparableTimesException(this, relativeResource);
        }

        // If the relativeResource is zero, the normalized resource should be
        // zero, too
        if (relativeResource.equals(relativeResource.getZeroResource())) {
            return new DTotalResource(0.0);
        }

        return new DTotalResource(this.value
                / ((DTotalResource) relativeResource).value);
    }

    @Override
    public AbstractResource getZeroResource() {
        return new DTotalResource(0.0);
    }
}
