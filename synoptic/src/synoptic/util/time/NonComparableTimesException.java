package synoptic.util.time;

public class NonComparableTimesException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;
    public ITime e1;
    public ITime e2;

    public NonComparableTimesException(ITime e1, ITime e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}
