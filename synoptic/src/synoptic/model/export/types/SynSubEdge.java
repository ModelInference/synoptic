package synoptic.model.export.types;

import java.io.Serializable;

public class SynSubEdge<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public T srcElem;
    public T destElem;

    public SynSubEdge() {
    }

    public SynSubEdge(T srcElem, T destElem) {
        this.srcElem = srcElem;
        this.destElem = destElem;
    }
}
