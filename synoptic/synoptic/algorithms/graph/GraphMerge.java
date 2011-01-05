package algorithms.graph;

import model.Partition;
import model.PartitionGraph;
import model.interfaces.IModifiableGraph;

// TODO: Implement Graph merging
/**
 * Implements merging a Partition Graph into another.
 */
public class GraphMerge implements Operation {
	PartitionGraph graph;
	
	/**
	 * Construct a graph merge.
	 * @param graph the graph to merge with the graph the operation is later applied to.
	 */
	public GraphMerge(PartitionGraph graph) {
		this.graph = graph;
	}
	
	@Override
	public Operation commit(PartitionGraph g, IModifiableGraph<Partition> partitionGraph) {
		for (Partition p : graph.getNodes())
			partitionGraph.add(p);
		return null;
	}
}
