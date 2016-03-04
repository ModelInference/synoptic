package synoptic.model.export.types;

public class SynSubEdge<T> {
    public T srcElem;
    public T destElem;

    public SynSubEdge() {
    }

    public SynSubEdge(T srcElem, T destElem) {
        this.srcElem = srcElem;
        this.destElem = destElem;
    }
}
