package synoptic.invariants.miners;

import java.util.LinkedHashMap;
import java.util.Set;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.model.EventNode;
import synoptic.model.TraceGraph;

/**
 * This class keeps a set of transitive closures for an IGraph, one transitive
 * closure per relation in the IGraph.
 */
public class AllRelationsTransitiveClosure {
    private final LinkedHashMap<String, TransitiveClosure> tcs = new LinkedHashMap<String, TransitiveClosure>();

    public AllRelationsTransitiveClosure(TraceGraph g, boolean useWarshall) {
        for (String relation : g.getRelations()) {
            tcs.put(relation, new TransitiveClosure(g, relation, useWarshall));
        }
    }

    public AllRelationsTransitiveClosure(TraceGraph g) {
        this(g, true);
    }

    public boolean isReachable(EventNode m, EventNode n, String relation) {
        if (!tcs.containsKey(relation)) {
            return false;
        }
        return tcs.get(relation).isReachable(m, n);
    }

    public TransitiveClosure get(String relation) {
        return tcs.get(relation);
    }

    public Set<String> getRelations() {
        return tcs.keySet();
    }
}
