package model.interfaces;

import util.IterableIterator;

import model.SystemState;

/**
 * 
 * @author sigurd
 *
 * @param <T>
 */
public interface ISuccessorProvider<T extends ITransition<SystemState<T>>> {
	IterableIterator<T> getSuccessorIterator();
	IterableIterator<T> getSuccessorIterator(String relation);
	void setTarget(SystemState<T> s);
}
