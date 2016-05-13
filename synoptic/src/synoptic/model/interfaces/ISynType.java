package synoptic.model.interfaces;

public interface ISynType<T extends ISynType<T>> {
    /**
     * Whether this type and another type are considered similar enough to
     * co-exist within the same Synoptic graph node, or partition
     */
    public boolean typeEquals(T other);
}
