package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.main.ParseException;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.EqualVectorTimestampsException;
import synoptic.util.IIterableIterator;
import synoptic.util.InternalSynopticException;
import synoptic.util.IterableAdapter;
import synoptic.util.VectorTime;

/**
 * The event class. This class may need some work.
 * 
 * @author Sigurd Schneider
 */
public class LogEvent implements INode<LogEvent>, IEvent, Comparable<LogEvent> {
    private static Logger logger = Logger.getLogger("LogEvent Logger");

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

    @Override
    public String getLabel() {
        return action.getLabel();
    }

    @Override
    public Partition getParent() {
        return parent;
    }

    @Override
    public void setParent(Partition parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "[" + getAction().getLabel() + "]";
    }

    @Override
    public void addTransition(LogEvent dest, String relation) {
        if (dest == null) {
            throw new InternalSynopticException("Dest was null");
        }
        addTransition(new Relation<LogEvent>(this, dest, relation));
    }

    /**
     * Computes the set of direct successors of event e1 from a list of events.
     * For totally ordered traces this set will contain at most a single event.
     * In partially ordered traces this set may be larger than 1.
     * 
     * @param e1
     *            the event for which to find direct successors in group
     * @param group
     *            the group of events of potentially direct successors of e1,
     *            this may contain the LogEvent e1.
     * @return
     * @throws ParseException
     *             when we detect that some two events in group have the same
     *             timestamp or if they have different length vector timestamps
     *             (comparison error).
     */
    public static Set<LogEvent> getDirectSuccessors(LogEvent e1,
            List<LogEvent> group) {
        LinkedHashSet<LogEvent> e1DirectSuccessors = new LinkedHashSet<LogEvent>();

        if (e1.getTime().isSingular()) {
            // All events in group are totally ordered. Therefore,
            // the problem of finding direct successors has been reduced
            // to finding _one_ element in group with the smallest
            // time-stamp that exceeds e1's timestamp. We can do this with a
            // single scan = O(n) time.

            LogEvent directSuccessor = null;
            for (LogEvent e2 : group) {
                if (e1 == e2) {
                    continue;
                }
                if (e1.getTime().equals(e2.getTime())) {
                    throw new EqualVectorTimestampsException(e1.getTime(),
                            e2.getTime());
                }

                if (e1.getTime().lessThan(e2.getTime())) {
                    if (directSuccessor == null) {
                        directSuccessor = e2;
                    } else if (e2.getTime().lessThan(directSuccessor.getTime())) {
                        directSuccessor = e2;
                    }
                }
            }
            if (directSuccessor != null) {
                e1DirectSuccessors.add(directSuccessor);
            }

        } else {
            // Events in group are partially ordered. We have to do more
            // work in this case.

            // The first loop runs in O(n) and the two loops below have a
            // worst cast behavior O(m^2) where m is the length of
            // e1AllSuccessors list. So the worst case run time is:
            // O(n) + O(n^2) = O(n^2)

            // First find all all events that succeed e1, store this set in
            // e1AllSuccessors.
            LinkedHashSet<LogEvent> e1AllSuccessors = new LinkedHashSet<LogEvent>();
            for (LogEvent e2 : group) {
                if (e1 == e2) {
                    continue;
                }

                if (e1.getTime().lessThan(e2.getTime())) {
                    e1AllSuccessors.add(e2);
                } else if (e1.getTime().equals(e2.getTime())) {
                    throw new EqualVectorTimestampsException(e1.getTime(),
                            e2.getTime());
                }
            }

            // Now out of all successors find all direct successors of e1.
            for (LogEvent e1Succ1 : e1AllSuccessors) {
                boolean directSuccessor = true; // whether or not e1Succ1 is
                                                // a direct successor of e2
                for (LogEvent e1Succ2 : e1AllSuccessors) {
                    if (e1Succ1 == e1Succ2) {
                        continue;
                    }

                    if (e1Succ2.getTime().lessThan(e1Succ1.getTime())) {
                        directSuccessor = false;
                        break;
                    }
                }
                if (directSuccessor) {
                    e1DirectSuccessors.add(e1Succ1);
                }
            }
        }
        return e1DirectSuccessors;
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
                transitionsByActionAndTarget.get(transition.getRelation())
                        .get(transition.getTarget()).remove(transition);
            }
        }

    }

    @Override
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
        LinkedHashMap<LogEvent, List<Relation<LogEvent>>> forAction = transitionsByActionAndTarget
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
        return "[LogEvent: " + getAction() + " (" + hashCode() + ")" + "]";
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

    @Override
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
    public boolean isTerminal() {
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

    public String getLine() {
        return action.getLine();
    }

    public String getFile() {
        return action.getFileName();
    }

    public String getLineNum() {
        int lineNum = action.getLineNum();
        return lineNum == 0 ? "" : "" + action.getLineNum();
    }
}
