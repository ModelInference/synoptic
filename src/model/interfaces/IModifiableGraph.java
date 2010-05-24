package model.interfaces;

import model.Action;

public interface IModifiableGraph<NodeType extends INode<NodeType>> extends IGraph<NodeType> {
	public void add(NodeType node);
	public void remove(NodeType node);
	public void addInitial(NodeType initialNode, Action relation);
}
