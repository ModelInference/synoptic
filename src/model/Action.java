package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import model.input.VectorTime;

public class Action {
	String label;
	private int cachedHashCode;
	private VectorTime vectorTime;

	// Arguments
	Map<String, Integer> integerArguments = new HashMap<String, Integer>();
	Map<String, Boolean> booleanArguments = new HashMap<String, Boolean>();
	Map<String, String> stringArguments = new HashMap<String, String>();

	public Action(String label) {
		this.label = label;
		computeHashCode();
	}

	public void setStringArgument(String name, String value) {
		stringArguments.put(name, value);
	}

	public String getStringArgument(String name) {
		return stringArguments.get(name);
	}

	public void mergeFromAction(Action action) {
		integerArguments.putAll(action.integerArguments);
		booleanArguments.putAll(action.booleanArguments);
		stringArguments.putAll(action.stringArguments);
	}

	public String toString() {
		return label;
	}

	public int computeHashCode() {
		final int prime = 31;
		cachedHashCode = prime + ((label == null) ? 0 : label.hashCode());
		return cachedHashCode;
	}

	public int hashCode() {
		return cachedHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Action other = (Action) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	public String getLabel() {
		return label;
	}

	public void setTime(VectorTime vectorTime) {
		this.vectorTime = vectorTime;
	}

	public VectorTime getTime() {
		return vectorTime;
	}
	
	public Map<String, String> getStringArguments() {
		return stringArguments;
	}

	public Set<String> getStringArgumentNames() {
		return stringArguments.keySet();
	}
}
