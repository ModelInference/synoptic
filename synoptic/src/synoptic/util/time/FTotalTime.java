package synoptic.util.time;

/**
 * A wrapper for a totally ordered time type with a float value.
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
}
