package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import util.IterableAdapter;
import util.IterableIterator;

import model.input.VectorTime;
import model.interfaces.INode;
import model.interfaces.ITransition;

/**
 * The event class. This class may need some work.
 * @author Sigurd Schneider
 *
 */
public class MessageEvent implements INode<MessageEvent>, IEvent {
	private int count;
	private Partition parent;
	private Action action;

	List<Relation<MessageEvent>> transitions = new ArrayList<Relation<MessageEvent>>();
	LinkedHashMap<String, List<Relation<MessageEvent>>> transitionsByAction = new LinkedHashMap<String, List<Relation<MessageEvent>>>();
	LinkedHashMap<String, LinkedHashMap<MessageEvent, List<Relation<MessageEvent>>>> transitionsByActionAndTarget = new LinkedHashMap<String, LinkedHashMap<MessageEvent, List<Relation<MessageEvent>>>>();

	public MessageEvent(MessageEvent copyFrom) {
		this.count = copyFrom.count;
		this.parent = copyFrom.parent;
		this.action = copyFrom.action;
	}

	public MessageEvent(Action signature, int count) {
		this.count = count;
		this.action = signature;
		this.parent = null;
	}

	public String getLabel() {
		return action.getLabel();
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

	public void addTransition(MessageEvent dest, String relation) {
		if (dest == null)
			throw new RuntimeException("Dest was null");
		addTransition(new Relation<MessageEvent>(this, dest, relation));
	}

	public void addTransition(MessageEvent dest, String relation,
			double probability) {
		if (dest == null)
			throw new RuntimeException("Dest was null");
		addTransition(new Relation<MessageEvent>(this, dest, relation,
				probability));
	}

	public void addTransition(Relation<MessageEvent> transition) {
		transitions.add(transition);
		String action = transition.getRelation();
		MessageEvent target = transition.getTarget();
		List<Relation<MessageEvent>> ref = transitionsByAction.get(action);
		if (ref == null) {
			ref = new ArrayList<Relation<MessageEvent>>();
			transitionsByAction.put(action, ref);
		}
		ref.add(transition);

		LinkedHashMap<MessageEvent, List<Relation<MessageEvent>>> ref1 = transitionsByActionAndTarget
				.get(action);
		if (ref1 == null) {
			ref1 = new LinkedHashMap<MessageEvent, List<Relation<MessageEvent>>>();
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

			if (transitionsByAction.containsKey(transition.getRelation())) {
				transitionsByAction.get(transition.getRelation()).remove(
						transition);
			}

			if (transitionsByActionAndTarget
					.containsKey(transition.getRelation())
					&& transitionsByActionAndTarget.get(transition.getRelation())
							.containsKey(transition.getTarget())) {
				transitionsByActionAndTarget.get(transition.getRelation()).get(
						transition.getTarget()).remove(transition);
			}
		}

	}

	public final List<Relation<MessageEvent>> getTransitions() {
		//Set<Relation<MessageEvent>> set = new LinkedHashSet<Relation<MessageEvent>>();
		//set.addAll(transitions);
		return transitions;
	}

	public List<Relation<MessageEvent>> getTransitions(String relation) {
		//checkConsistency();
		List<Relation<MessageEvent>> res = transitionsByAction.get(relation);
		if (res == null) {
			return Collections.emptyList();
		}
		return res;
	}

	/**
	 * Check that all transitions are in local cache.
	 */
	public void checkConsistency() {
		for (ITransition<MessageEvent> t : transitions) {
			if (!transitionsByAction.get(t.getRelation()).contains(t))
				throw new RuntimeException(
						"inconsistent transitions in message");
		}
	}

	public List<Relation<MessageEvent>> getTransitions(Partition target,
			String relation) {
		List<Relation<MessageEvent>> forAction = transitionsByAction.get(relation);
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
			String relation) {
		HashMap<MessageEvent, List<Relation<MessageEvent>>> forAction = transitionsByActionAndTarget
				.get(relation);
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
		return "[MessageEvent A: " + getAction() + " ("
				+ hashCode() + ")" + "]";
	}

	// INode
	@Override
	public IterableIterator<Relation<MessageEvent>> getTransitionsIterator() {
		return IterableAdapter.make(getTransitions().iterator());
	}

	@Override
	public IterableIterator<Relation<MessageEvent>> getTransitionsIterator(String
			relation) {
		return IterableAdapter.make(getTransitions(relation).iterator());
	}

	@Override
	public ITransition<MessageEvent> getTransition(MessageEvent target,
			String relation) {
		List<Relation<MessageEvent>> list = getTransitions(target, relation);
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

	public Action getAction() {
		return action;
	}

	//TODO: order
	public Set<String> getRelations() {
		return transitionsByAction.keySet();
	}

	public VectorTime getTime() {
		return action.getTime();
	}

	@Override
	public String getStringArgument(String name) {
		return action.getStringArgument(name);
	}

	@Override
	public void setStringArgument(String name, String value) {
		action.setStringArgument(name, value);
	}

	@Override
	public String getName() {
		return action.getLabel();
	}

	@Override
	public Set<String> getStringArguments() {
		return action.getStringArgumentNames();
	}

	public Set<MessageEvent> getSuccessors(String relation) {
		Set<MessageEvent> successors = new LinkedHashSet<MessageEvent>();
		for (Relation<MessageEvent> e : getTransitionsIterator(relation))
			successors.add(e.getTarget());
		return successors;
	}
	
	@Override
	public boolean isFinal() {
		return transitions.isEmpty();
	}
}
