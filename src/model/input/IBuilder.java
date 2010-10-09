package model.input;

import model.Action;

/**
 * Builder interface for graphs.
 * 
 * @author Sigurd Schneider
 * 
 * @param <T>
 *            The node type to build.
 */
public interface IBuilder<T> {
	/**
	 * Append a new event with act as payload to the graph. If split was not
	 * called, an edge from the previously inserted event to this event will be
	 * created as well.
	 * 
	 * @param act
	 *            the action (payload) to append
	 * @return the node created
	 */
	T append(Action act);

	/**
	 * Insert a new node with payload event after event. Split will suppress the
	 * creation of the edge as well.
	 * 
	 * @param event
	 *            The event after which this node should be inserted.
	 * @param relation
	 *            the relation (edge labeling) for the edge from event to the
	 *            new .
	 * @return the newly created event
	 */
	T insertAfter(T event, Action relation);

	/**
	 * Suppress edge creation for the next call to append or insertAfter.
	 */
	void split();

	/**
	 * Insert a new event with payload act.
	 * 
	 * @param act
	 *            the payload for the new event
	 * @return the newly created event
	 */
	T insert(Action act);

	/**
	 * Add a node and tag it as initial.
	 * 
	 * @param curMessage
	 * @param relation
	 */
	void addInitial(T curMessage, String relation);

	/**
	 * Add a new edge between first and last.
	 * 
	 * @param first
	 *            source node
	 * @param second
	 *            target node
	 * @param relation
	 *            edge label
	 */
	void connect(T first, T second, String relation);

	/**
	 * Set a node as terminal.
	 * 
	 * @param terminalNode
	 *            the node to set as terminal.
	 */
	void setTerminal(T terminalNode);
}
