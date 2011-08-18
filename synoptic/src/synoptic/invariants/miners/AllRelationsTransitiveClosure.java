package synoptic.invariants.miners;

import java.util.LinkedHashMap;
import java.util.Set;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

/**
 * This class keeps a set of transitive closures for an IGraph, one transitive
 * closure per relation in the IGraph.
 */
public class AllRelationsTransitiveClosure<NodeType extends INode<NodeType>> {
    private final LinkedHashMap<String, TransitiveClosure<NodeType>> tcs = new LinkedHashMap<String, TransitiveClosure<NodeType>>();

    public AllRelationsTransitiveClosure(IGraph<NodeType> g, boolean useWarshall) {
        for (String relation : g.getRelations()) {
            tcs.put(relation, new TransitiveClosure<NodeType>(g, relation,
                    useWarshall));
        }
    }

    public AllRelationsTransitiveClosure(IGraph<NodeType> g) {
        this(g, true);
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
