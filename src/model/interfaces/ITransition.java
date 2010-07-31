package model.interfaces;

import model.Action;

/**
 * A generic interface for a transition. This interface provides method to modify the transition.
 * @author sigurd
 *
 * @param <NodeType> the type of the nodes which are connected by this transition
 */
public interface ITransition<NodeType> {
	/**
	 * Get the target node.
	 * @return the target node
	 */
	public NodeType getTarget();
	
	/**
	 * Get the source node.
	 * @return the source node
	 */
	public NodeType getSource();
	
	/**
	 * get the label of the transition (i.e. the relation)
	 * @return
	 */
	public String getAction();
	
	/**
	 * Set the target node.
	 * @param target the new target node
	 */
	public void setTarget(NodeType target);
	
	/**
	 * Set the source node.
	 * @param source the new source node
	 */
	public void setSource(NodeType source);
	
	/**
	 * Get the weight of this transition
	 * @return the weight
	 */
	public int getWeight();
	
	/**
	 * Add a number to the weight of the transition.
	 * @param weightToAdd the number to add to the weight
	 */
	public void addWeight(int weightToAdd);
	
	/**
	 * Get a short description of the transition
	 * @return a short description
	 */
	public String toStringConcise();
}
