package synoptic.util.time;

/**
 * A totally ordered time type with a float value.
 */
public class FTotalTime implements ITime {
    /** Time value */
    public float time;

    /**
     * Builds a Time object from a float
     * 
     * @param f
     * @throws IllegalArgumentException
     *             when f is negative
     */
    public FTotalTime(float f) {
        if (f < 0) {
            throw new IllegalArgumentException();
        }
        time = f;
    }

    @Override
    public boolean lessThan(ITime t) {
        if (!(t instanceof FTotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return time < ((FTotalTime) t).time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(time);
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
        FTotalTime other = (FTotalTime) obj;
        if (Float.floatToIntBits(time) != Float.floatToIntBits(other.time)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Float.toString(time);
    }

    @Override
    public int compareTo(ITime t) {
        if (!(t instanceof FTotalTime)) {
            throw new NonComparableTimesException(this, t);
        }
        return new Float(time).compareTo(((FTotalTime) t).time);
    }

    @Override
    public ITime computeDelta(ITime other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof FTotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new FTotalTime(this.time - ((FTotalTime) other).time);
    }

    @Override
    public ITime incrBy(ITime other) {
        if (other == null) {
            return this;
        }

        if (!(other instanceof FTotalTime)) {
            throw new NonComparableTimesException(this, other);
        }
        return new FTotalTime(this.time + ((FTotalTime) other).time);
    }

    @Override
    public ITime divBy(int divisor) {
        if (divisor < 1) {
            throw new IllegalArgumentException();
        }
        return new FTotalTime(this.time / divisor);
    }

    @Override
    public ITime normalize(ITime relativeTime) {
        if (!(relativeTime instanceof FTotalTime)) {
            throw new NonComparableTimesException(this, relativeTime);
        }

        // If the relativeTime is zero, the normalized time should be zero, too
        if (relativeTime.equals(relativeTime.getZeroTime())) {
            return new DTotalTime(0.0);
        }

        return new DTotalTime(this.time / ((FTotalTime) relativeTime).time);
    }

    @Override
    public ITime getZeroTime() {
        return new FTotalTime(0.0f);
    }
}
