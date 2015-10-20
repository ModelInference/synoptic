package synoptic.util.resource;

import java.math.BigDecimal;

/**
 * A totally ordered resource type with a double value
 */
public class DTotalResource extends AbstractResource {
    /** Resource value */
    private BigDecimal value;

    /**
     * Builds a double resource from an int
     */
    public DTotalResource(int value) {
        this(value, "");
    }

    /**
     * Builds a double resource from a double's String representation
     * 
     * @throws NumberFormatException
     */
    public DTotalResource(String value) {
        this(value, "");
    }

    /**
     * Builds a double resource from an int and a resource key
     */
    public DTotalResource(int value, String key) {
        this(new BigDecimal(value), key);
    }

    /**
     * Builds a double resource from a long int and a resource key
     */
    public DTotalResource(long value, String key) {
        this(new BigDecimal(value), key);
    }

    /**
     * Builds a double resource from a double's String representation and a resource key
     * 
     * @throws NumberFormatException
     */
    public DTotalResource(String value, String key) {
        this(new BigDecimal(value), key);
    }

    /**
     * Builds a Resource object from a BigDecimal and a resource key
     */
    public DTotalResource(BigDecimal value, String key) {
        super(key);
        this.value = value;
    }

    /**
     * @return The resource's double value
     */
    public double getValue() {
        return value.doubleValue();
    }

    @Override
    public boolean lessThan(AbstractResource r) {
        if (!isComparable(r)) {
            throw new NonComparableResourceException(this, r);
        }
        DTotalResource other = (DTotalResource) r;
        return value.compareTo(other.value) < 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(getValue());
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        DTotalResource other = (DTotalResource) obj;
        if (!key.equals(other.key)) {
            return false;
        }
        return value.compareTo(other.value) == 0;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int compareTo(AbstractResource r) {
        if (!isComparable(r)) {
            throw new NonComparableResourceException(this, r);
        }
        DTotalResource other = (DTotalResource) r;
        return value.compareTo(other.value);
    }

    @Override
    public AbstractResource computeDelta(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!isComparable(other)) {
            throw new NonComparableResourceException(this, other);
        }
        DTotalResource otherD = (DTotalResource) other;
        return new DTotalResource(value.subtract(otherD.value), key);
    }

    @Override
    public AbstractResource incrBy(AbstractResource other) {
        if (other == null) {
            return this;
        }

        if (!isComparable(other)) {
            throw new NonComparableResourceException(this, other);
        }
        DTotalResource otherD = (DTotalResource) other;
        return new DTotalResource(value.add(otherD.value), key);
    }

    @Override
    public AbstractResource divBy(int divisor) {
        return divBy((long) divisor);
    }

    /**
     * Extra division method only for double resources, since they can handle larger (long int)
     * divisors
     */
    public AbstractResource divBy(long divisor) {
        if (divisor == 0L) {
            throw new IllegalArgumentException();
        }
        return new DTotalResource(value.divide(new BigDecimal(divisor)), key);
    }

    @Override
    public AbstractResource normalize(AbstractResource relativeResource) {
        if (!isComparable(relativeResource)) {
            throw new NonComparableResourceException(this, relativeResource);
        }

        // If the relativeResource is zero, the normalized resource should be
        // zero, too
        if (relativeResource.equals(relativeResource.getZeroResource())) {
            return getZeroResource();
        }

        DTotalResource relative = (DTotalResource) relativeResource;
        return new DTotalResource(value.divide(relative.value), key);
    }

    @Override
    public AbstractResource getZeroResource() {
        return new DTotalResource(BigDecimal.ZERO, key);
    }
}
