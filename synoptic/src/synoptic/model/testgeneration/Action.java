package synoptic.model.testgeneration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import synoptic.model.Partition;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

/**
 * Action is a single step in an abstract test case.
 * Action is an INode that can have at most 1 outgoing transition.
 * 
 *
 */
public class Action implements INode<Action> {
	private final EventType eventType;
	private final List<ITransition<Action>> transitions;
	
	public Action(EventType eventType) {
		this.eventType = eventType;
		transitions = new ArrayList<ITransition<Action>>();
	}
	
	/**
	 * Adds a transition to a target action.
	 * NOTE: The transition may or may not have Daikon invariants associated
	 * with it.
	 */
	public void addTransition(ITransition<Action> transition) {
		// Action can have at most 1 outgoing transition.
		assert transitions.isEmpty();
		transitions.add(transition);
	}

	@Override
	public EventType getEType() {
		return eventType;
	}

	@Override
	public Set<Action> getAllSuccessors() {
		Set<Action> successors = new LinkedHashSet<Action>();
		for (ITransition<Action> trans : transitions) {
			successors.add(trans.getTarget());
		}
		return successors;
	}

	@Override
	public List<? extends ITransition<Action>> getAllTransitions() {
		return transitions;
	}

	@Override
	public List<? extends ITransition<Action>> getTransitionsWithExactRelations(
			Set<String> relations) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<? extends ITransition<Action>> getTransitionsWithSubsetRelations(
			Set<String> relations) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<? extends ITransition<Action>> getTransitionsWithIntersectingRelations(
			Set<String> relations) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<? extends ITransition<Action>> getWeightedTransitions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setParent(Partition parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Partition getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTerminal() {
		return eventType.isTerminalEventType();
	}

	@Override
	public boolean isInitial() {
		return eventType.isInitialEventType();
	}

	@Override
	public int compareTo(Action arg0) {
		return eventType.compareTo(arg0.eventType);
	}

}
