package synoptic.algorithms.graph;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;

// TODO: Implement Graph merging

/**
 * Implements merging a Partition Graph into another.
 */
public class GraphMerge implements IOperation {
    PartitionGraph graph;

    /**
     * Construct a graph merge.
     *
     * @param graph
     *            the graph to merge with the graph the operation is later
     *            applied to.
     */
    public GraphMerge(PartitionGraph graph) {
        this.graph = graph;
    }

    @Override
    public IOperation commit(PartitionGraph g) {
        for (Partition p : graph.getNodes()) {
            g.add(p);
        }
        return null;
    }
}
