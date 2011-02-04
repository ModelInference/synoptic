package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import synoptic.model.input.VectorTime;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.IIterableIterator;
import synoptic.util.InternalSynopticException;
import synoptic.util.IterableAdapter;

/**
 * The event class. This class may need some work.
 * 
 * @author Sigurd Schneider
 */
public class LogEvent implements INode<LogEvent>, IEvent, Comparable<LogEvent> {

    /**
     * The partition that contains this message event.
     */
    private Partition parent;
    private final Action action;

    List<Relation<LogEvent>> transitions = new ArrayList<Relation<LogEvent>>();
    LinkedHashMap<String, List<Relation<LogEvent>>> transitionsByAction = new LinkedHashMap<String, List<Relation<LogEvent>>>();
    LinkedHashMap<String, LinkedHashMap<LogEvent, List<Relation<LogEvent>>>> transitionsByActionAndTarget = new LinkedHashMap<String, LinkedHashMap<LogEvent, List<Relation<LogEvent>>>>();

    public LogEvent(LogEvent copyFrom) {
        parent = copyFrom.parent;
        action = copyFrom.action;
    }

    public LogEvent(Action signature) {
        action = signature;
        parent = null;
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

    @Override
    public String toString() {
        return "[" + getAction() + " (" + hashCode() + ")" + "]";
    }

    public void addTransition(LogEvent dest, String relation) {
        if (dest == null) {
            throw new InternalSynopticException("Dest was null");
        }
        addTransition(new Relation<LogEvent>(this, dest, relation));
    }

    public void addTransition(LogEvent dest, String relation, double probability) {
        if (dest == null) {
            throw new InternalSynopticException("Dest was null");
        }
        addTransition(new Relation<LogEvent>(this, dest, relation, probability));
    }

    public void addTransition(Relation<LogEvent> transition) {
        transitions.add(transition);
        String action = transition.getRelation();
        LogEvent target = transition.getTarget();
        List<Relation<LogEvent>> ref = transitionsByAction.get(action);
        if (ref == null) {
            ref = new ArrayList<Relation<LogEvent>>();
            transitionsByAction.put(action, ref);
        }
        ref.add(transition);

        LinkedHashMap<LogEvent, List<Relation<LogEvent>>> ref1 = transitionsByActionAndTarget
                .get(action);
        if (ref1 == null) {
            ref1 = new LinkedHashMap<LogEvent, List<Relation<LogEvent>>>();
            transitionsByActionAndTarget.put(action, ref1);
        }
        List<Relation<LogEvent>> ref2 = ref1.get(target);
        if (ref2 == null) {
            ref2 = new ArrayList<Relation<LogEvent>>();
            ref1.put(target, ref2);
        }
        ref2.add(transition);
    }

    public void removeTransitions(List<Relation<LogEvent>> transitions) {
        this.transitions.removeAll(transitions);
        for (Relation<LogEvent> transition : transitions) {

            if (transitionsByAction.containsKey(transition.getRelation())) {
                transitionsByAction.get(transition.getRelation()).remove(
                        transition);
            }

            if (transitionsByActionAndTarget.containsKey(transition
                    .getRelation())
                    && transitionsByActionAndTarget.get(
                            transition.getRelation()).containsKey(
                            transition.getTarget())) {
                transitionsByActionAndTarget.get(transition.getRelation()).get(
                        transition.getTarget()).remove(transition);
            }
        }

    }

    public final List<Relation<LogEvent>> getTransitions() {
        // Set<Relation<LogEvent>> set = new
        // LinkedHashSet<Relation<LogEvent>>();
        // set.addAll(transitions);
        return transitions;
    }

    public List<Relation<LogEvent>> getTransitions(String relation) {
        // checkConsistency();
        List<Relation<LogEvent>> res = transitionsByAction.get(relation);
        if (res == null) {
            return Collections.emptyList();
        }
        return res;
    }

    /**
     * Check that all transitions are in local cache.
     */
    public void checkConsistency() {
        for (ITransition<LogEvent> t : transitions) {
            if (!transitionsByAction.get(t.getRelation()).contains(t)) {
                throw new InternalSynopticException(
                        "inconsistent transitions in message");
            }
        }
    }

    public List<Relation<LogEvent>> getTransitions(Partition target,
            String relation) {
        List<Relation<LogEvent>> forAction = transitionsByAction.get(relation);
        if (forAction == null) {
            return Collections.emptyList();
        }

        List<Relation<LogEvent>> res = new ArrayList<Relation<LogEvent>>();
        for (Relation<LogEvent> t : forAction) {
            if (t.getTarget().getParent() == target) {
                res.add(t);
            }
        }
        return res;
    }

    public List<Relation<LogEvent>> getTransitions(LogEvent target,
            String relation) {
        HashMap<LogEvent, List<Relation<LogEvent>>> forAction = transitionsByActionAndTarget
                .get(relation);
        if (forAction == null) {
            return Collections.emptyList();
        }
        List<Relation<LogEvent>> res = forAction.get(target);
        if (res == null) {
            return Collections.emptyList();
        }
        return res;
    }

    public void addTransitions(Collection<Relation<LogEvent>> transitions) {
        for (Relation<LogEvent> t : transitions) {
            this.addTransition(t);
        }
    }

    public void setTransitions(ArrayList<Relation<LogEvent>> t) {
        transitions.clear();
        transitions.addAll(t);
    }

    public String toStringFull() {
        return "[LogEvent A: " + getAction() + " (" + hashCode() + ")" + "]";
    }

    // INode
    @Override
    public IIterableIterator<Relation<LogEvent>> getTransitionsIterator() {
        return IterableAdapter.make(getTransitions().iterator());
    }

    @Override
    public IIterableIterator<Relation<LogEvent>> getTransitionsIterator(
            String relation) {
        return IterableAdapter.make(getTransitions(relation).iterator());
    }

    @Override
    public ITransition<LogEvent> getTransition(LogEvent target, String relation) {
        List<Relation<LogEvent>> list = getTransitions(target, relation);
        return list.size() == 0 ? null : list.get(0);
    }

    @Override
    public String toStringConcise() {
        return getAction().getLabel();
    }

    public Action getAction() {
        return action;
    }

    // TODO: order
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

    public Set<LogEvent> getSuccessors(String relation) {
        Set<LogEvent> successors = new LinkedHashSet<LogEvent>();
        for (Relation<LogEvent> e : getTransitionsIterator(relation)) {
            successors.add(e.getTarget());
        }
        return successors;
    }

    @Override
    public boolean isFinal() {
        return transitions.isEmpty();
    }

    @Override
    public int compareTo(LogEvent other) {
        if (this == other) {
            return 0;
        }

        // compare labels of the two message events
        int labelCmp = getLabel().compareTo(other.getLabel());
        if (labelCmp != 0) {
            return labelCmp;
        }

        // compare number of children
        int transitionCntCmp = new Integer(transitions.size())
                .compareTo(other.transitions.size());
        if (transitionCntCmp != 0) {
            return transitionCntCmp;
        }

        // compare children labels
        ArrayList<Relation<LogEvent>> thisSortedTrans = new ArrayList<Relation<LogEvent>>(
                transitions);
        ArrayList<Relation<LogEvent>> otherSortedTrans = new ArrayList<Relation<LogEvent>>(
                other.transitions);
        Collections.sort(thisSortedTrans);
        Collections.sort(otherSortedTrans);
        for (int i = 0; i < thisSortedTrans.size(); i++) {
            int childCmp = thisSortedTrans.get(i).compareTo(
                    otherSortedTrans.get(i));
            if (childCmp != 0) {
                return childCmp;
            }
        }

        return 0;
    }
}
