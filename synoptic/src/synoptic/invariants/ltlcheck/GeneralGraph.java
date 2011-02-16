package synoptic.invariants.ltlcheck;


import java.util.LinkedHashSet;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;

public class GeneralGraph extends Graph {
    private final LinkedHashSet<Node> initialNodes;

    public GeneralGraph() {
        super();
        initialNodes = new LinkedHashSet<Node>();
    }

    public LinkedHashSet<Node> getInitialNodes() {
        return initialNodes;
    }
}
