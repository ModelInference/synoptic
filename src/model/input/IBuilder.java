package model.input;

import model.Action;

/**
 * Builder interface for graphs.
 * 
 * @author Sigurd Schneider
 * 
 * @param <T>
 *            The node type of the graph to build.
 */
public interface IBuilder<T> {
	/**
	 * Append a new node with act as payload to the graph. If split was not
	 * called, an edge from the previously inserted node to the newly created
	 * node will be created as well.
	 * 
	 * @param act
	 *            the action (payload) for the newly created node
	 * @return the node created
	 */
	T append(Action act);

	/**
	 * Insert a new node with payload act after node (i.e.. creating an edge
	 * from node to the newly created one). If split as called immediatelly
	 * before, this will behave like insert, e.g. no edge will be created.
	 * 
	 * @param node
	 *            The event after which this node should be inserted.
	 * @param relation
	 *            the relation (edge labeling) for the edge from event to the
	 *            new .
	 * @return the newly created event
	 */
	T insertAfter(T node, Action act);

	/**
	 * Suppress edge creation for the next call to append or insertAfter.
	 */
	void split();

	/**
	 * Insert a new event with payload act, and do not created any edges.
	 * 
	 * @param act
	 *            the payload for the new event
	 * @return the newly created event
	 */
	T insert(Action act);

	/**
	 * Tag a node as initial.
	 * 
	 * @param curMessage
	 *            the node to tag as initial
	 * @param relation
	 *            the relation for which it should be considered initial
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
	 * Tag a node as terminal.
	 * 
	 * @param terminalNode
	 *            the node to tag as terminal.
	 */
	void setTerminal(T terminalNode);
}
