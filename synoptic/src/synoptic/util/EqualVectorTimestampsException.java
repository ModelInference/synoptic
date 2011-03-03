package synoptic.util;


public class EqualVectorTimestampsException extends RuntimeException {
    /**
     * Unique version uid
     */
    private static final long serialVersionUID = 1L;
    public VectorTime e1;
    public VectorTime e2;

    public EqualVectorTimestampsException(VectorTime e1, VectorTime e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
}
