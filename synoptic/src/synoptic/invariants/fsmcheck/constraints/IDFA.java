package synoptic.invariants.fsmcheck.constraints;

import synoptic.model.interfaces.INode;
import synoptic.util.time.ITime;

/**
 * Interface for constrained AFby and AP DFAs.
 */
public interface IDFA<Node extends INode<Node>> {
	/**
	 * @return current state of DFA
	 */
	public State getState();
	
	/**
	 * Transition to a target node.
	 * @param target node to transition to
	 * @param delta the delta time to transition to target node
	 */
	public void transition(Node target, ITime delta);
	
}
