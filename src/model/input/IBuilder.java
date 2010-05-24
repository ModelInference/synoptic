package model.input;

import model.Action;

public interface IBuilder<T> {
	T append(Action act);
	T insertAfter(T event, Action act);
	void split();
	T insert(Action act);
	void addInitial(T curMessage, Action relation);
	void connect(T first, T second, Action relation);
	void setTerminal(T terminalNode);
}
