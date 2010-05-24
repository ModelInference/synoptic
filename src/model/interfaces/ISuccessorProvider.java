package model.interfaces;

import util.IterableIterator;

import model.Action;
import model.SystemState;


public interface ISuccessorProvider<T extends ITransition<SystemState<T>>> {
	IterableIterator<T> getSuccessorIterator();
	IterableIterator<T> getSuccessorIterator(Action act);
	void setTarget(SystemState<T> s);
}
