package synopticgwt.shared;

import java.io.Serializable;

public class GWTGraphDelta implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private GWTGraph graph;
	private int refinedNode;
	
	public GWTGraphDelta () {}
	
	public GWTGraphDelta (GWTGraph g, int refinedNode) {
		this.graph = g;
		this.refinedNode = refinedNode;
	}
	
	public GWTGraph getGraph() {
		return graph;
	}
	
	public int getRefinedNode(){
		return refinedNode;
	}
}
