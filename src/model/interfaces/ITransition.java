package model.interfaces;

import model.Action;

public interface ITransition<NodeType> {
	public NodeType getTarget();
	public NodeType getSource();
	public Action getAction();
	public void setTarget(NodeType target);
	public void setSource(NodeType source);
	public int getCount();
	public void addCount(int count);
	public String toStringConcise();
}
