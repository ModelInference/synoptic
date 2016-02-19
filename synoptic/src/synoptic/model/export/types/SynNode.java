package synoptic.model.export.types;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SynNode<T> {
    List<T> elements = null;
    List<SynEdge<T>> outEdges = null;

    public SynNode(Collection<T> elems) {
        elements = new LinkedList<>(elems);
        outEdges = new LinkedList<>();
    }

    public List<T> getElements() {
        return elements;
    }

    public List<SynEdge<T>> getOutEdges() {
        return outEdges;
    }

    public void addOutEdge(double prob, T destElem) {
        SynEdge<T> newEdge = new SynEdge<T>(prob, destElem);
        outEdges.add(newEdge);
    }
}
