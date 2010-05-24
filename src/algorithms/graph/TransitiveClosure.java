package algorithms.graph;

import java.util.HashMap;
import java.util.Iterator;

import model.Action;
import model.interfaces.IGraph;
import model.interfaces.INode;
import model.interfaces.ITransition;

/**
 * Code taken from
 * http://www.cs.princeton.edu/courses/archive/fall05/cos226/lectures/digraph.pdf
 * @author Sigurd
 *
 */
public class TransitiveClosure<NodeType extends INode<NodeType>> {
	private HashMap<NodeType, HashMap<NodeType, Boolean>> tc = new HashMap<NodeType, HashMap<NodeType, Boolean>>();
	private Action act;

	public TransitiveClosure(IGraph<NodeType> g, Action act) {
		this.act = act;
		for (NodeType m : g.getNodes())
			for (Iterator<? extends ITransition<NodeType>> i =  m.getTransitionsIterator(act); i.hasNext();) {
				ITransition<NodeType> t = i.next();
				dfs(m, t.getTarget());
			}
	}

	// reachability from s, made it to v
	private void dfs(NodeType m, NodeType n) {
		if (!tc.containsKey(m))
			tc.put(m, new HashMap<NodeType, Boolean>());
		tc.get(m).put(n, true);
		for (Iterator<? extends ITransition<NodeType>> i = n.getTransitionsIterator(act); i.hasNext();) {
			ITransition<NodeType> t = i.next();
			Boolean r = tc.get(m).get(t.getTarget());
			if (r == null || r == false)
				dfs(m, t.getTarget());
		}
	}

	public boolean isReachable(NodeType m, NodeType n) {
		if (!tc.containsKey(m))
			return false;
		Boolean r = tc.get(m).get(n);
		return r != null && r == true;
	}

	public boolean isEqual(TransitiveClosure<NodeType> o) {
		for (NodeType u : tc.keySet()) {
			for (NodeType v : tc.get(u).keySet()) {
				if (isReachable(u,v) != o.isReachable(u, v))
					return false;
			}
		}
		return true;
	}
	

}
