package synoptic.model.resource;

public class NonComparableResourceException extends RuntimeException {

    /**
     * Unique version uid
     */
    private static final long serialVersionUID = -4739857547879349149L;

    public final IResource<?> r1;
    public final IResource<?> r2;

    public NonComparableResourceException(IResource<?> r1, IResource<?> r2) {
        this.r1 = r1;
        this.r2 = r2;
    }
}
