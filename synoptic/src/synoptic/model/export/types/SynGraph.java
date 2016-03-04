package synoptic.model.export.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SynGraph<T> {
    protected Map<SynNode<T>, Double> initialNodesProb;
    protected List<SynNode<T>> nodes;
    protected Map<SynNode<T>, List<SynEdge<T>>> edgesByNode;
    protected Map<T, SynEdge<T>> edgeByElem;

    public SynGraph() {
        initialNodesProb = new LinkedHashMap<>();
        nodes = new LinkedList<>();
        edgesByNode = new HashMap<>();
        edgeByElem = new HashMap<>();
    }

    public Map<SynNode<T>, Double> getInitialNodesAndProb() {
        return initialNodesProb;
    }

    public List<SynNode<T>> getNodes() {
        return nodes;
    }

    public SynNode<T> addNode(Collection<T> elems) {
        SynNode<T> newNode = new SynNode<>(elems);
        nodes.add(newNode);
        edgesByNode.put(newNode, new LinkedList<SynEdge<T>>());
        return newNode;
    }

    public void setNodeAsInitial(SynNode<T> node, double prob) {
        initialNodesProb.put(node, prob);
    }

    public void addEdge(SynNode<T> srcNode, SynNode<T> destNode,
            Collection<SynSubEdge<T>> subEdges, double prob) {
        //
        if (!nodes.contains(srcNode) || !nodes.contains(destNode)) {
            throw new IllegalArgumentException(
                    "Src or dest nodes of the edge do not exist in the graph");
        }
        SynEdge<T> newEdge = new SynEdge<>(srcNode, destNode, subEdges, prob);

        //
        edgesByNode.get(srcNode).add(newEdge);
        for (SynSubEdge<T> subEdge : subEdges) {
            edgeByElem.put(subEdge.srcElem, newEdge);
        }
    }

    @Override
    public String toString() {
        // StringBuilder sb = new StringBuilder();
        //
        // for (int id : nodes.keySet()) {
        // SynNode n = nodes.get(id);
        // sb.append(id).append(": ").append(n.label).append('\n');
        // for (Edge e : n.outEdges) {
        // sb.append(" ").append(e.src.id).append(" -> ").append(e.dest.id).append(" / ")
        // .append(e.prob).append('\n');
        // }
        // }
        //
        // return sb.toString();
        return null;
    }
}
