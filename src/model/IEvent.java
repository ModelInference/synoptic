package model;

import java.util.Set;

import model.input.VectorTime;

public interface IEvent {
	VectorTime getTime();
	String getStringArgument(String name);
	void setStringArgument(String name, String value);
	String getName();
	Set<String> getStringArguments();
}
