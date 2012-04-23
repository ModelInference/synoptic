package synoptic.util.time;

/**
 * A totally ordered time type with a double value.
 */
public class DTotalTime implements ITime {
    /** Time value */
    public double time;

    /**
     * Builds a Time object from a double
     * 
     * @param d
     * @throws IllegalArgumentException
     *             when d is negative
     */
    public DTotalTime(double d) {
        if (d < 0) {
            throw new IllegalArgumentException();
        }
        time = d;
    }

    @Override
    public boolean lessThan(ITime t) {
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
    public int compareTo(ITime t) {
        if (!(t instanceof DTotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return new Double(time).compareTo(((DTotalTime) t).time);
    }

    @Override
    public ITime computeDelta(ITime other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof DTotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new DTotalTime(this.time - ((DTotalTime) other).time);
    }

    @Override
    public ITime incrBy(ITime other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof DTotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new DTotalTime(this.time + ((DTotalTime) other).time);
    }

    @Override
    public ITime divBy(int divisor) {
        if (divisor < 1) {
            throw new IllegalArgumentException();
        }
        return new DTotalTime(this.time / divisor);
    }
}
