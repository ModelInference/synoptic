package synoptic.model.export.types;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SynGraph<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    protected SynNode<T> initialNode;
    protected List<SynNode<T>> nodes;
    protected Map<SynNode<T>, List<SynEdge<T>>> edgesByNode;
    protected Map<T, SynEdge<T>> edgeByElem;

    public SynGraph() {
        initialNode = new SynNode<>();
        nodes = new LinkedList<>();
        edgesByNode = new HashMap<>();
        edgeByElem = new HashMap<>();
    }

    public SynNode<T> getInitialNode() {
        return initialNode;
    }

    public List<SynNode<T>> getNodes() {
        return nodes;
    }

    public List<SynEdge<T>> getEdgesOutOfNode(SynNode<T> node) {
        return edgesByNode.get(node);
    }

    public SynEdge<T> getEdgeOutOfElem(T elem) {
        return edgeByElem.get(elem);
    }

    public SynNode<T> addNode(Collection<T> elems) {
        SynNode<T> newNode = new SynNode<>(elems);
        nodes.add(newNode);
        edgesByNode.put(newNode, new LinkedList<SynEdge<T>>());
        return newNode;
    }

    public SynNode<T> addInitialNode(Collection<T> elems) {
        initialNode = new SynNode<>(elems);
        edgesByNode.put(initialNode, new LinkedList<SynEdge<T>>());
        return initialNode;
    }

    public void addEdge(SynNode<T> srcNode, SynNode<T> destNode,
            Collection<SynSubEdge<T>> subEdges, double prob) {
        //
        if ((!nodes.contains(srcNode) && srcNode != initialNode)
                || !nodes.contains(destNode)) {
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
        StringBuilder sb = new StringBuilder();

        appendNode(sb, initialNode);

        for (SynNode<T> node : nodes) {
            appendNode(sb, node);
        }

        return sb.toString();
    }

    private void appendNode(StringBuilder sb, SynNode<T> node) {
        if (node == initialNode) {
            sb.append("INITIAL\n");
        } else {
            sb.append(node.hashCode())
                    .append(node.isTerminal ? " (TERMINAL)" : "").append("\n  ")
                    .append(node.elements).append('\n');
        }
        for (SynEdge<T> edge : edgesByNode.get(node)) {
            sb.append("  -> ").append(edge.destNode.hashCode()).append(" / ")
                    .append(edge.prob).append('\n');
        }
    }
}
