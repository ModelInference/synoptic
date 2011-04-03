package synoptic.util.time;

public interface ITime {

    /**
     * @param t
     *            the other vtime
     * @return true if (this < t), otherwise returns false
     */
    public abstract boolean lessThan(ITime t);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

}