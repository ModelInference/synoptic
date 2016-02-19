package synoptic.model.export.types;

public class SynEdge<T> {
    private double prob;
    private T destElem;

    public SynEdge(double prob, T destElem) {
        this.prob = prob;
        this.destElem = destElem;
    }

    public double getProb() {
        return prob;
    }

    public T getDestElem() {
        return destElem;
    }
}
