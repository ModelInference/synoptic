package synoptic.util.resource;

/**
 * A totally ordered resource type with a float value.
 */
public class FTotalResource extends AbstractResource {
    /** Resource value */
    public float value;

    /**
     * Builds a Resource object from a float
     * 
     * @param f
     */
    public FTotalResource(float f) {
        super("");
        value = f;
    }

    /**
     * Builds a Resource object from a float and a resource key
     * 
     * @param f
     * @param key
     */
    public FTotalResource(float f, String key) {
        super(key);
        value = f;
    }

    @Override
    public boolean lessThan(AbstractResource r) {
        if (!isComparable(r)) {
            throw new NonComparableResourceException(this, r);
        }
        return value < ((FTotalResource) r).value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(value) + key.hashCode();
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
        FTotalResource other = (FTotalResource) obj;
        if (!key.equals(other.key)) {
            return false;
        }
        if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Float.toString(value);
    }

    @Override
    public int compareTo(AbstractResource r) {
        if (!isComparable(r)) {
            throw new NonComparableResourceException(this, r);
        }
        return new Float(value).compareTo(((FTotalResource) r).value);
    }

    @Override
    public AbstractResource computeDelta(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!isComparable(other)) {
            throw new NonComparableResourceException(this, other);
        }
        return new FTotalResource(this.value - ((FTotalResource) other).value);
    }

    @Override
    public AbstractResource incrBy(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!isComparable(other)) {
            throw new NonComparableResourceException(this, other);
        }
        return new FTotalResource(this.value + ((FTotalResource) other).value);
    }

    @Override
    public AbstractResource divBy(int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException();
        }
        return new FTotalResource(this.value / divisor, key);
    }

    @Override
    public AbstractResource normalize(AbstractResource relativeResource) {
        if (!isComparable(relativeResource)) {
            throw new NonComparableResourceException(this, relativeResource);
        }

        // If the relativeTime is zero, the normalized time should be zero, too
        if (relativeResource.equals(relativeResource.getZeroResource())) {
            return new DTotalResource(0.0, key);
        }

        return new DTotalResource(this.value
                / ((FTotalResource) relativeResource).value, key);
    }

    @Override
    public AbstractResource getZeroResource() {
        return new FTotalResource(0.0f, key);
    }
}
