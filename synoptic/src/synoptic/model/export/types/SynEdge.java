package synoptic.model.export.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SynEdge<T> {
    protected SynNode<T> srcNode;
    protected SynNode<T> destNode;
    protected Map<T, T> subEdges;
    protected double prob;

    public SynEdge(SynNode<T> srcNode, SynNode<T> destNode,
            Collection<SynSubEdge<T>> subEdges, double prob) {
        this.srcNode = srcNode;
        this.destNode = destNode;

        // Populate sub-edge map
        this.subEdges = new HashMap<>();
        for (SynSubEdge<T> subEdge : subEdges) {
            // Ensure sub-edge is legal
            if (!srcNode.contains(subEdge.srcElem)
                    || !destNode.contains(subEdge.destElem)) {
                throw new IllegalArgumentException(
                        "Src of sub-edge is not in src node, or dest of "
                                + "sub-edge is not in dest node");
            }
            this.subEdges.put(subEdge.srcElem, subEdge.destElem);
        }

        this.prob = prob;
    }

    public SynNode<T> getSrcNode() {
        return srcNode;
    }

    public SynNode<T> getDestNode() {
        return destNode;
    }

    public T getDestElem(T srcElem) {
        return subEdges.get(srcElem);
    }

    public double getProb() {
        return prob;
    }
}
