package invariants.ltlcheck;

import java.util.*;
import gov.nasa.ltl.graph.*;

public class PersistenceChecker
{
	private Counterexample counterexample = null;
	
	private Set<Node> r = new HashSet<Node>(); // set of visited states in the outer DFS
	private Set<Node> ir;                      // set of unvisited initial states
	private Stack<Node> u = new Stack<Node>(); // stack for the outer DFS
	private Set<Node> t = new HashSet<Node>(); // set of visited states in the inner DFS
	private Stack<Node> v = new Stack<Node>(); // stack for the inner DFS
	boolean cycleFound = false;
	
	public PersistenceChecker(GeneralGraph g)
	{
		super();
		this.ir = g.getInitialNodes();
	}
	
	public void run()
	{
		while(!ir.isEmpty() && !cycleFound)
		{
			reachableCycle(ir.iterator().next()); // explore the reachable fragment with outer DFS
		}
		if(!cycleFound) counterexample = null; // YES
		else 
		{	// NO; save counterexample (reverse(V.U))
			
			// Get prefix
			List<Edge> prefix = new ArrayList<Edge>(u.size() + 1);
			for(int i = 0; i < u.size() - 1; ++i)
			{
				for(Edge e: u.get(i).getOutgoingEdges())
				{
					if(e.getNext().equals(u.get(i + 1)))
					{
						prefix.add(e);
						break;
					}
				}
			}

			// Add action to get from last state of prefix to first state of cycle
			if(!u.empty() && !v.empty())
			{
				for(Edge e: u.get(u.size() - 1).getOutgoingEdges())
				{
					if(e.getNext().equals(v.get(0)))
					{
						prefix.add(e);
						break;
					}
				}
			}

			// Get cycle
			List<Edge> cycle = new ArrayList<Edge>(v.size());
			for(int i = 0; i < v.size() - 1; ++i)
			{
				for(Edge e: v.get(i).getOutgoingEdges())
				{
					if(e.getNext().equals(v.get(i + 1)))
					{
						cycle.add(e);
						break;
					}
				}
			}
			
			// If a suffix of the prefix is part (a "suffix", in fact) of the cycle we found, remove that suffix
			// foundSuffix <=> last transition of cycle is last transition of prefix as well
			boolean foundSuffix = prefix.size() > 0 && cycle.get(cycle.size() - 1).equals(prefix.get(prefix.size() - 1));
			while(foundSuffix)
			{
				Edge e = cycle.get(cycle.size() - 1);
				cycle.remove(cycle.size() - 1);
				cycle.add(0, e);
				prefix.remove(prefix.size() - 1);
				
				foundSuffix = prefix.size() > 0 && cycle.get(cycle.size() - 1).equals(prefix.get(prefix.size() - 1));
			}

			// Set counterexample
			counterexample = new Counterexample(prefix, cycle);
		}
	}
	
	private void reachableCycle(Node n)
	{
		u.push(n);
		r.add(n);
		ir.remove(n);
		
		do
		{
			Node nn = u.peek();
			
			// Find an unvisited successor of nn
			Node unvisitedSuccessor = null;
			for(Edge e: nn.getOutgoingEdges())
			{
				if(!r.contains(e.getNext()))
				{
					unvisitedSuccessor = e.getNext();
					break;
				}
			}
			
			if(unvisitedSuccessor != null)
			{ // successor found, explore
				u.push(unvisitedSuccessor); // push the unvisited successor on u
				r.add(unvisitedSuccessor);  // and mark it visited
				ir.remove(r);
			}
			else
			{ // outer DFS is finished for nn
				u.pop(); 
				if(nn.getBooleanAttribute("accepting"))
				{ // proceed with the inner DFS in nn
					cycleFound = cycleCheck(nn);
				}
			}
		}
		while(!u.isEmpty() && !cycleFound);
	}
	
	private boolean cycleCheck(Node n)
	{
		v.push(n);
		t.add(n);
		
		do
		{
			Node nn = v.peek();

			// Check whether we already found a cycle
			for(Edge e: nn.getOutgoingEdges())
			{
				if(e.getNext().equals(n))
				{ // if n in Post(nn), a cycle is found
					v.push(n);
					return true;
				}
			}
			
			// No cycle found yet
			// Find an unvisited successor of nn
			Node unvisitedSuccessor = null;
			for(Edge e: nn.getOutgoingEdges())
			{
				if(!t.contains(e.getNext()))
				{
					unvisitedSuccessor = e.getNext();
					break;
				}
			}	
			
			if(unvisitedSuccessor != null)
			{ // successor found, explore
				v.push(unvisitedSuccessor); // push the unvisited successor on u
				t.add(unvisitedSuccessor);  // and mark it visited
			}
			else
			{ // Cycle search unsuccessful for nn
				v.pop();				
			}			
		}
		while(!v.isEmpty());
		
		return false;
	}

	public Counterexample getCounterexample()
	{
		return counterexample;
	}
}
