package model.interfaces;


import java.util.Set;

import util.IterableIterator;
import model.Action;
import model.Partition;


public interface INode<NodeType extends INode<NodeType>> {
	String getLabel();
	IterableIterator<? extends ITransition<NodeType>> getTransitionsIterator();
	IterableIterator<? extends ITransition<NodeType>> getTransitionsIterator(Action act);
	ITransition<NodeType> getTransition(NodeType iNode, Action relation);
	Set<? extends ITransition<NodeType>> getTransitions();
	void setParent(Partition parent);
	Partition getParent(); 
	String toStringConcise();
}
