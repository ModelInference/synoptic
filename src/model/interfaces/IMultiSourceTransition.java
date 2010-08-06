package model.interfaces;

import java.util.Set;

import model.Action;

public interface IMultiSourceTransition<NodeType> extends ITransition<NodeType> {
	public NodeType getTarget();
	public Set<NodeType> getSources();
	public String getRelation();
	public void setTarget(NodeType target);
	public void addSource(NodeType source);
	public void addSources(Set<NodeType> sources);
	public void clearSources();
	public String toStringConcise();
}
