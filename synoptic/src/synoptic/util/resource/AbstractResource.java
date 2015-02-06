package synoptic.util.resource;

/**
 * An interface that all Synoptic resource types must implement. It abstract
 * away the type of resource used -- vector clocks/integer/float/etc. And
 * exposes the very basic operations on resource, such as comparison for
 * ordering. A resource contains a key identifying the resource, and a
 * comparable value.
 */
public abstract class AbstractResource implements Comparable<AbstractResource> {

    /**
     * Used to compare two time values. Note that (x < y) || (y < x) is only
     * true for totally ordered time instances. It is not necessarily true for
     * partially ordered time, such as vector clocks.
     * 
     * @param t
     *            the other ITime instance
     * @return true if (this < t), otherwise false
     */
    public abstract boolean lessThan(AbstractResource t);

    /**
     * Used to compare two time values. Note that (x < y) || (y < x) is only
     * true for totally ordered time instances. It is not necessarily true for
     * partially ordered time, such as vector clocks.
     * 
     * @param t
     *            the other ITime instance
     * @return true if (this < t), otherwise false
     */
    public abstract int compareTo(AbstractResource t);

    /**
     * Computes the time difference between this ITime and another ITime
     * instance.
     * 
     * @param other
     *            the other ITime instance
     * @return Delta time between this ITime and other ITime instance. (same
     *         type as the callee object)
     */
    public abstract AbstractResource computeDelta(AbstractResource other);

    /**
     * Increments the ITime object by the specified amount, and returns the
     * incremented time as a new object.
     * 
     * @param other
     *            The ITime object with which to increment.
     * @return The incremented ITime object. (same type as the callee object)
     */
    public abstract AbstractResource incrBy(AbstractResource other);

    /**
     * Divides the ITime object by the specified divisor.
     * 
     * <pre>
     * NOTE: Cannot divide time by zero or a negative number, since
     *       time cannot be negative.
     * </pre>
     * 
     * @param divisor
     * @return The ITime object after division has occurred. (same type as the
     *         callee object)
     */
    public abstract AbstractResource divBy(int divisor);

    /**
     * Returns a normalized ITime in respect to relativeTime.
     * 
     * @param relativeTime
     *            the time to use for the normalization, usually the time with
     *            the biggest value of the trace.
     * @return A normalized ITime object. (same type as the callee object)
     */
    public abstract AbstractResource normalize(AbstractResource relativeTime);

    /**
     * Creates a new zero-time object
     * 
     * @return New ITime with a time value of zero. (same type as the callee
     *         object)
     */
    public abstract AbstractResource getZeroTime();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
