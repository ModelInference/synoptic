package synoptic.util.time;

public interface ITime {

    /**
     * Returns true if (this < t), otherwise returns false
     * 
     * @param t
     *            the other vtime
     * @return
     */
    public abstract boolean lessThan(ITime t);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

}