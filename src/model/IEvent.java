package model;

import java.util.Set;

import model.input.VectorTime;

/**
 * An interface all events should implement. Currently this is somewhat inane.
 * 
 * @author Sigurd Schneider
 * 
 */
public interface IEvent {
	/**
	 * Get the time the event occured.
	 * 
	 * @return vector time the event occured
	 */
	VectorTime getTime();

	/**
	 * Get the string argument with name {@code name}.
	 * 
	 * @param name
	 *            name of the property
	 * @return value of the property
	 */
	String getStringArgument(String name);

	/**
	 * Set the string argument with name {@code name}
	 * 
	 * @param name
	 *            the name of the propery
	 * @param value
	 *            the value of the propery
	 */
	void setStringArgument(String name, String value);

	/**
	 * Return the name (i.e. label) of the event.
	 * 
	 * @return the name of the event
	 */
	String getName();

	/**
	 * Return the set of strings that are valid string arguments (i.e. have a
	 * value set).
	 * 
	 * @return set of strings s.th. getStringArgument returns no-null value each
	 *         of them
	 */
	Set<String> getStringArguments();
}
