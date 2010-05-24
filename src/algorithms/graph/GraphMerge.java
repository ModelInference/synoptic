package algorithms.graph;

import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.IModifiableGraph;

// TODO: Implement Graph merging
/**
 * Implements merging a Partition Graph into another
 */
public class GraphMerge implements Operation {
	PartitionGraph graph;
	
	public GraphMerge(PartitionGraph graph) {
		this.graph = graph;
	}
	
	@Override
	public Operation commit(PartitionGraph g, IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		for (Partition p : graph.getNodes())
			partitionGraph.add(p);
		for (SystemState<Partition> s : graph.getSystemStateGraph().getNodes())
			stateGraph.add(s);
		return null;
	}
}
