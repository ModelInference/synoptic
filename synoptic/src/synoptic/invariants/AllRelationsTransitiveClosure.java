package synoptic.invariants;

import java.util.LinkedHashMap;
import java.util.Set;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

/**
 * Code taken from:
 * http://www.cs.princeton.edu/courses/archive/fall05/cos226/lectures
 * /digraph.pdf
 * 
 * @author Sigurd
 */
public class AllRelationsTransitiveClosure<NodeType extends INode<NodeType>> {
    private final LinkedHashMap<String, TransitiveClosure<NodeType>> tcs = new LinkedHashMap<String, TransitiveClosure<NodeType>>();

    public AllRelationsTransitiveClosure(IGraph<NodeType> g) {
        for (String relation : g.getRelations()) {
            tcs.put(relation, new TransitiveClosure<NodeType>(g, relation));
        }
    }

    public boolean isReachable(NodeType m, NodeType n, String relation) {
        if (!tcs.containsKey(relation)) {
            return false;
        }
        return tcs.get(relation).isReachable(m, n);
    }

    public TransitiveClosure<NodeType> get(String relation) {
        return tcs.get(relation);
    }

    public Set<String> getRelations() {
        return tcs.keySet();
    }
}
