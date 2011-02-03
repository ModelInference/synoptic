package synoptic.model.scalability;

import java.util.HashSet;
import java.util.Set;

import synoptic.algorithms.graph.GraphMerge;
import synoptic.model.PartitionGraph;

public class ScalableGraph {
    Set<PartitionGraph> graphs = new HashSet<PartitionGraph>();

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
            mergedGraph.apply(new GraphMerge(graph));
        }
        return mergedGraph;
    }

    public PartitionGraph kReduce(int k, boolean subsumption,
            boolean preserveInvariants) {
        // 1. run gk-tail over each graph
        // TODO: implement this with our partition-based kTail instead
        for (PartitionGraph g : graphs) {
            // KTail.kReduce(g, k, subsumption, preserveInvariants);
        }

        // 2. merge all the graphs and run gk-tail again.
        PartitionGraph mergedGraph = mergeAll();
        // KTail.kReduce(mergedGraph, k, subsumption, preserveInvariants);

        return mergedGraph;
    }
}
