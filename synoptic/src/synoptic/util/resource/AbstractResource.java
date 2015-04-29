package synoptic.util.resource;

/**
 * An interface that all Synoptic resource types must implement. It abstracts
 * away the type of resource used -- vector clocks/integer/float/etc -- and
 * exposes the basic operations on resources, such as comparison for ordering. A
 * resource contains a key identifying the resource, and a comparable value.
 */
public abstract class AbstractResource implements Comparable<AbstractResource> {

    /**
     * The key identifying the resource. If the key is the empty string then the
     * resource is anonymous. That is, there was no explicit information
     * regarding it's type given to us by the user/log.
     */
    protected final String key;

    public AbstractResource(String key) {
        this.key = key;
    }

    /**
     * Determines if two resources are comparable. Two resources are comparable
     * only if they are of the same type and have identical keys.
     * 
     * @return
     */
    public boolean isComparable(AbstractResource resource) {
        if (!this.key.equals(resource.key)) {
            return false;
        }
        if (!this.getClass().equals(resource.getClass())) {
            return false;
        }
        return true;
    }

    /**
     * Used to compare two resource values. Note that (x < y) || (y < x) is only
     * true for totally ordered resource instances. It is not necessarily true
     * for partially ordered resource, such as vector clocks. Two resources can
     * only be compared if they have the same key identifying the resource.
     * 
     * @param t
     *            the other AbstractResource instance
     * @return true if (this < t), otherwise false
     */
    public abstract boolean lessThan(AbstractResource t);

    /**
     * Used to compare two resource values. Note that (x < y) || (y < x) is only
     * true for totally ordered resource instances. It is not necessarily true
     * for partially ordered resource, such as vector clocks. Two resources can
     * only be compared if they have the same key identifying the resource.
     * 
     * @param t
     *            the other AbstractResource instance
     * @return true if (this < t), otherwise false
     */
    public abstract int compareTo(AbstractResource t);

    /**
     * Computes the difference between this AbstractResource and another
     * AbstractResource instance. The difference between two resources can only
     * be compared if they have the same key identifying the resource.
     * 
     * @param other
     *            the other AbstractResource instance
     * @return Delta time between this AbstractResource and other
     *         AbstractResource instance. (same type as the callee object)
     */
    public abstract AbstractResource computeDelta(AbstractResource other);

    /**
     * Increments the AbstractResource object by the specified amount, and
     * returns the incremented resource as a new object.
     * 
     * @param other
     *            The AbstractResource object with which to increment.
     * @return The incremented AbstractResource object. (same type as the callee
     *         object)
     */
    public abstract AbstractResource incrBy(AbstractResource other);

    /**
     * Divides the AbstractResource instance by the specified divisor.
     * 
     * <pre>
     * NOTE: Cannot divide by zero.
     * </pre>
     * 
     * @param divisor
     * @return The AbstractResource object after division has occurred. (same
     *         type as the callee object)
     */
    public abstract AbstractResource divBy(int divisor);

    /**
     * Returns a normalized AbstractResource in respect to relativeResource.
     * 
     * @param relativeResource
     *            the resource value to use for the normalization, usually the
     *            resource with the biggest value in the trace.
     * @return A normalized AbstractResource object. (same type as the callee
     *         object)
     */
    public abstract AbstractResource normalize(AbstractResource relativeResource);

    /**
     * Creates a new zero-valued resource object with an empty string as key.
     * 
     * @return New AbstractResource with a resource value of zero. (same type as
     *         the callee object)
     */
    public abstract AbstractResource getZeroResource();

    /**
     * @return the key identifying this resource
     */
    public String getKey() {
        return key;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

}
