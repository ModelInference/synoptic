package synoptic.model.scalability;

import java.util.LinkedHashSet;
import java.util.Set;

import synoptic.model.PartitionGraph;
import synoptic.util.InternalSynopticException;

// TODO: It's unclear if this is used for anything.
public class ScalableGraph {
    Set<PartitionGraph> graphs = new LinkedHashSet<PartitionGraph>();

    public void addGraph(PartitionGraph graph) {
        graphs.add(graph);
    }

    public PartitionGraph mergeAll() {
        PartitionGraph mergedGraph = null;
        for (PartitionGraph graph : graphs) {
            if (mergedGraph == null) {
                mergedGraph = graph;
                continue;
            }
            throw new InternalSynopticException(
                    "Caching for graph.Apply(GraphMerge) not implemented.");
            // mergedGraph.apply(new GraphMerge(graph));
        }
        return mergedGraph;
    }

    public PartitionGraph kReduce(int k, boolean subsumption,
            boolean preserveInvariants) {
        // 1. run gk-tail over each graph
        // TODO: implement this with our partition-based kTail instead
        for (@SuppressWarnings("unused")
        PartitionGraph g : graphs) {
            // KTail.kReduce(g, k, subsumption, preserveInvariants);
        }

        // 2. merge all the graphs and run gk-tail again.
        PartitionGraph mergedGraph = mergeAll();
        // KTail.kReduce(mergedGraph, k, subsumption, preserveInvariants);

        return mergedGraph;
    }
}
