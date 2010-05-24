package invariants.ltlcheck;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;

import java.util.HashSet;

public class GeneralGraph extends Graph {
	private final HashSet<Node> initialNodes;

	public GeneralGraph() {
		super();
		initialNodes = new HashSet<Node>();
	}

	public HashSet<Node> getInitialNodes() {
		return initialNodes;
	}
}
