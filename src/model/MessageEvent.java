package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.IterableAdapter;
import util.IterableIterator;

import model.input.VectorTime;
import model.interfaces.INode;
import model.interfaces.ITransition;

public class MessageEvent implements ITransition<SystemState<MessageEvent>>,
		INode<MessageEvent>, IEvent {
	private int count;
	private Partition parent;
	private Transition<SystemState<MessageEvent>> transition;

	List<Relation<MessageEvent>> transitions = new ArrayList<Relation<MessageEvent>>();
	HashMap<Action, List<Relation<MessageEvent>>> transitionsByAction = new HashMap<Action, List<Relation<MessageEvent>>>();
	HashMap<Action, HashMap<MessageEvent, List<Relation<MessageEvent>>>> transitionsByActionAndTarget = new HashMap<Action, HashMap<MessageEvent, List<Relation<MessageEvent>>>>();

	public MessageEvent(MessageEvent target) {
		transition = new Transition<SystemState<MessageEvent>>(target
				.getSource(), target.getTarget(), target.getAction());
		this.count = target.count;
	}

	public MessageEvent(Action signature, SystemState<MessageEvent> prevState,
			SystemState<MessageEvent> nextState, int count) {
		transition = new Transition<SystemState<MessageEvent>>(prevState,
				nextState, signature);
		this.count = count;
	}

	public String getLabel() {
		return getAction().getLabel();
	}

	public Partition getParent() {
		return parent;
	}

	public void setParent(Partition parent) {
		this.parent = parent;
	}

	public String toString() {
		return "[" + getAction() + " (" + hashCode() + ")" + "]";
	}

	public void addTransition(MessageEvent dest, Action action) {
		if (dest == null)
			throw new RuntimeException("Dest was null");
		addTransition(new Relation<MessageEvent>(this, dest, action));
	}

	public void addTransition(MessageEvent dest, Action action,
			double probability) {
		if (dest == null)
			throw new RuntimeException("Dest was null");
		addTransition(new Relation<MessageEvent>(this, dest, action,
				probability));
	}

	public void addTransition(Relation<MessageEvent> transition) {
		transitions.add(transition);
		Action action = transition.getAction();
		MessageEvent target = transition.getTarget();
		List<Relation<MessageEvent>> ref = transitionsByAction.get(action);
		if (ref == null) {
			ref = new ArrayList<Relation<MessageEvent>>();
			transitionsByAction.put(action, ref);
		}
		ref.add(transition);

		HashMap<MessageEvent, List<Relation<MessageEvent>>> ref1 = transitionsByActionAndTarget
				.get(action);
		if (ref1 == null) {
			ref1 = new HashMap<MessageEvent, List<Relation<MessageEvent>>>();
			transitionsByActionAndTarget.put(action, ref1);
		}
		List<Relation<MessageEvent>> ref2 = ref1.get(target);
		if (ref2 == null) {
			ref2 = new ArrayList<Relation<MessageEvent>>();
			ref1.put(target, ref2);
		}
		ref2.add(transition);
	}

	public void removeTransitions(List<Relation<MessageEvent>> transitions) {
		this.transitions.removeAll(transitions);
		for (Relation<MessageEvent> transition : transitions) {

			if (transitionsByAction.containsKey(transition.getAction())) {
				transitionsByAction.get(transition.getAction()).remove(
						transition);
			}

			if (transitionsByActionAndTarget
					.containsKey(transition.getAction())
					&& transitionsByActionAndTarget.get(transition.getAction())
							.containsKey(transition.getTarget())) {
				transitionsByActionAndTarget.get(transition.getAction()).get(
						transition.getTarget()).remove(transition);
			}
		}

	}

	public Set<Relation<MessageEvent>> getTransitions() {
		Set<Relation<MessageEvent>> set = new HashSet<Relation<MessageEvent>>();
		set.addAll(transitions);
		return set;
	}

	public List<Relation<MessageEvent>> getTransitions(Action action) {
		//checkConsistency();
		List<Relation<MessageEvent>> res = transitionsByAction.get(action);
		if (res == null) {
			return Collections.emptyList();
		}
		return res;
	}

	private void checkConsistency() {
		for (ITransition<MessageEvent> t : transitions) {
			if (!transitionsByAction.get(t.getAction()).contains(t))
				throw new RuntimeException(
						"inconsistent transitions in message");
		}
	}

	public List<Relation<MessageEvent>> getTransitions(Partition target,
			Action action) {
		List<Relation<MessageEvent>> forAction = transitionsByAction.get(action);
		if (forAction == null)
			return Collections.emptyList();
		
		List<Relation<MessageEvent>> res = new ArrayList<Relation<MessageEvent>>();
		for (Relation<MessageEvent> t : forAction) {
			if (t.getTarget().getParent() == target) {
				res.add(t);
			}
		}
		return res;
	}

	public List<Relation<MessageEvent>> getTransitions(MessageEvent target,
			Action action) {
		HashMap<MessageEvent, List<Relation<MessageEvent>>> forAction = transitionsByActionAndTarget
				.get(action);
		if (forAction == null)
			return Collections.emptyList();
		List<Relation<MessageEvent>> res = forAction.get(target);
		if (res == null)
			return Collections.emptyList();
		return res;
	}

	public void addTransitions(Collection<Relation<MessageEvent>> transitions) {
		for (Relation<MessageEvent> t : transitions) {
			this.addTransition(t);
		}
	}

	public void setTransitions(ArrayList<Relation<MessageEvent>> t) {
		transitions.clear();
		transitions.addAll(t);
	}

	public String toStringFull() {
		return "[{" + getSource().hashCode() + "} -> {"
				+ getTarget().hashCode() + "} A: " + getAction() + " ("
				+ hashCode() + ")" + "]";
	}

	// INode
	@Override
	public IterableIterator<Relation<MessageEvent>> getTransitionsIterator() {
		return IterableAdapter.make(getTransitions().iterator());
	}

	@Override
	public IterableIterator<Relation<MessageEvent>> getTransitionsIterator(
			Action act) {
		return IterableAdapter.make(getTransitions(act).iterator());
	}

	@Override
	public ITransition<MessageEvent> getTransition(MessageEvent target,
			Action action) {
		List<Relation<MessageEvent>> list = getTransitions(target, action);
		return list.size() == 0 ? null : list.get(0);
	}

	public int getWeight() {
		return count;
	}

	public void addWeight(int count) {
		this.count += count;
	}

	@Override
	public String toStringConcise() {
		return getAction().getLabel();
	}

	@Override
	public Action getAction() {
		return transition.getAction();
	}

	@Override
	public SystemState<MessageEvent> getSource() {
		return transition.getSource();
	}

	@Override
	public SystemState<MessageEvent> getTarget() {
		return transition.getTarget();
	}

	@Override
	public void setSource(SystemState<MessageEvent> source) {
		transition.setSource(source);
	}

	@Override
	public void setTarget(SystemState<MessageEvent> target) {
		transition.setTarget(target);
	}

	public Set<Action> getRelations() {
		return transitionsByAction.keySet();
	}

	public VectorTime getTime() {
		return transition.getAction().getTime();
	}

	@Override
	public String getStringArgument(String name) {
		return transition.getAction().getStringArgument(name);
	}

	@Override
	public void setStringArgument(String name, String value) {
		transition.getAction().setStringArgument(name, value);
	}

	@Override
	public String getName() {
		return transition.getAction().getLabel();
	}

	@Override
	public Set<String> getStringArguments() {
		return transition.getAction().getStringArgumentNames();
	}

	public Set<MessageEvent> getSuccessors(Action action) {
		Set<MessageEvent> successors = new HashSet<MessageEvent>();
		for (Relation<MessageEvent> e : getTransitionsIterator(action))
			successors.add(e.getTarget());
		return successors;
	}

}
