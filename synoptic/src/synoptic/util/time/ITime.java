package synoptic.util.time;

/**
 * An interface that all Synoptic time types must implement. It abstract away
 * the type of time used -- vector clocks/integer/float/etc. And exposes the
 * very basic operations on time, such as comparison for ordering.
 */
public interface ITime extends Comparable<ITime> {

    /**
     * Used to compare two time values. Note that (x < y) || (y < x) is only
     * true for totally ordered time instances. It is not necessarily true for
     * partially ordered time, such as vector clocks.
     * 
     * @param t
     *            the other ITime instance
     * @return true if (this < t), otherwise false
     */
    boolean lessThan(ITime t);

    /**
     * Used to compare two time values. Note that (x < y) || (y < x) is only
     * true for totally ordered time instances. It is not necessarily true for
     * partially ordered time, such as vector clocks.
     * 
     * @param t
     *            the other ITime instance
     * @return true if (this < t), otherwise false
     */
    int compareTo(ITime t);

    /**
     * Computes the time difference between this ITime and another ITime
     * instance.
     * 
     * @param other
     *            the other ITime instance
     * @return delta time between this ITime and other ITime instance
     */
    ITime computeDelta(ITime other);

    /**
     * Increments the ITime object by the specified amount, and returns the
     * incremented time as a new object.
     * 
     * @param other
     *            The ITime object with which to increment.
     * @return The incremented ITime object.
     */
    ITime incrBy(ITime other);

    /**
     * Divides the ITime object by the specified divisor.
     * 
     * <pre>
     * NOTE: Cannot divide time by zero or a negative number, since
     *       time cannot be negative.
     * </pre>
     * 
     * @param divisor
     * @return The ITime object after division has occurred.
     */
    ITime divBy(int divisor);

    /**
     * Returns a normalized ITime in respect to relativeTime.
     * 
     * @param relativeTime
     *            the time to use for the normalization, usually the time with
     *            the biggest value of the trace.
     * @return A normalized ITime object.
     */
    ITime normalize(ITime relativeTime);

    /**
     * Creates a new zero-time object
     * 
     * @return New ITime of the same subtype with a time value of zero
     */
    ITime getZeroTime();

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    @Override
    String toString();
}
