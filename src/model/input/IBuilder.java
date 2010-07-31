package model.input;

import model.Action;

public interface IBuilder<T> {
	T append(Action act);
	T insertAfter(T event, Action relation);
	void split();
	T insert(Action act);
	void addInitial(T curMessage, String relation);
	void connect(T first, T second, String relation);
	void setTerminal(T terminalNode);
}
