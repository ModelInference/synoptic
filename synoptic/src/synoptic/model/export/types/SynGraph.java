package synoptic.model.export.types;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SynGraph<T> {
    List<SynNode<T>> nodes = null;

    public SynGraph() {
        nodes = new LinkedList<>();
    }

    public List<SynNode<T>> getNodes() {
        return nodes;
    }

    public void addNode(Collection<T> elems) {
        SynNode<T> newNode = new SynNode<>(elems);
        nodes.add(newNode);
    }

    public void addEdge(SynNode srcNode, T srcElem, T destElem) {
        // SynNode srcNode = nodes.get(src);
        // SynNode destNode = nodes.get(dest);
        // srcNode.addOutEdge(destNode, prob);
    }

    public void addEdge(int src, int dest, String query) {
        // SynNode srcNode = nodes.get(src);
        // SynNode destNode = nodes.get(dest);
        // srcNode.addOutEdge(destNode, query);
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
