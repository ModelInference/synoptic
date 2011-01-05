package invariants.ltlcheck;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.ltl.graph.*;

public final class GraphTransformations {
	
	public static void removeDeadlock(Graph g)
	{
		Node deadlockNode = null;
		
		// Find sinks in the graph
		for(Node n: g.getNodes())
		{
			if(n.getOutgoingEdgeCount() == 0)
			{	// and link them to the deadlock node
				if(deadlockNode == null) deadlockNode = createDeadlockNode(g);
				new Edge(n, deadlockNode, "__deadlock");
			}
		}
	}
	
	private static Node createDeadlockNode(Graph g)
	{
		// Create deadlock node and self-loop
		Node dln = new Node(g);
		dln.setBooleanAttribute("deadlock", true);
		new Edge(dln, dln, "__deadlock");
		
		// Include "deadlock" as a state predicate
		List<AtomicProposition> label = new ArrayList<AtomicProposition>(1);
		label.add(new AtomicProposition("__deadlock", AtomicProposition.PropositionType.Unknown));
		dln.setAttribute("label", label);
		
		return dln;
	}
}
