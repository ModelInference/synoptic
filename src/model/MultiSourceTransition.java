package model;

import java.util.LinkedHashSet;
import java.util.Set;

import model.interfaces.IMultiSourceTransition;

public class MultiSourceTransition<NodeType> implements IMultiSourceTransition<NodeType> {
	private Set<NodeType> sources = new LinkedHashSet<NodeType>();
	private NodeType target;
	private Action action;
	
	public MultiSourceTransition(Set<NodeType> sources, NodeType target, Action action) {
		this.sources.addAll(sources);
		this.target = target;
		this.action = action;
	}

	@Override
	public void addSource(NodeType source) {
		sources.add(source);
	}

	@Override
	public void addSources(Set<NodeType> sources) {
		this.sources.addAll(sources);
	}

	@Override
	public void clearSources() {
		sources.clear();
	}

	@Override
	public String getAction() {
		return action.getLabel();
	}

	@Override
	public Set<NodeType> getSources() {
		return sources;
	}

	@Override
	public NodeType getTarget() {
		return target;
	}

	@Override
	public void setTarget(NodeType target) {
		this.target = target;
	}

	@Override
	public String toStringConcise() {
		return "multitransition";
	}

	@Override
	public void addWeight(int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getWeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public NodeType getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSource(NodeType source) {
		// TODO Auto-generated method stub
		
	}
	
}
