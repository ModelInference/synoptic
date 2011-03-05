package synoptic.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import synoptic.main.ParseException;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.IIterableIterator;
import synoptic.util.InternalSynopticException;
import synoptic.util.IterableAdapter;
import synoptic.util.time.EqualVectorTimestampsException;
import synoptic.util.time.ITime;
import synoptic.util.time.VectorTime;

/**
 * The event class. This class may need some work.
 * 
 * @author Sigurd Schneider
 */
public class LogEvent implements INode<LogEvent> {
    /**
     * The partition that contains this log event.
     */
    private Partition parent;
    private final Action action;

    List<Transition<LogEvent>> transitions = new ArrayList<Transition<LogEvent>>();
    LinkedHashMap<String, List<Transition<LogEvent>>> transitionsByAction = new LinkedHashMap<String, List<Transition<LogEvent>>>();
    LinkedHashMap<String, LinkedHashMap<LogEvent, List<Transition<LogEvent>>>> transitionsByActionAndTarget = new LinkedHashMap<String, LinkedHashMap<LogEvent, List<Transition<LogEvent>>>>();

    public LogEvent(LogEvent copyFrom) {
        parent = copyFrom.parent;
        action = copyFrom.action;
    }

    public LogEvent(Action signature) {
        action = signature;
        parent = null;
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
        addTransition(new Transition<LogEvent>(this, dest, relation));
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

        if (!(e1.getTime() instanceof VectorTime)) {
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

    // public void addTransition(LogEvent dest, String relation, double
    // probability) {
    // if (dest == null) {
    // throw new InternalSynopticException("Dest was null");
    // }
    // addTransition(new Transition<LogEvent>(this, dest, relation,
    // probability));
    // }

    public void addTransition(Transition<LogEvent> transition) {
        transitions.add(transition);
        String action = transition.getRelation();
        LogEvent target = transition.getTarget();
        List<Transition<LogEvent>> ref = transitionsByAction.get(action);
        if (ref == null) {
            ref = new ArrayList<Transition<LogEvent>>();
            transitionsByAction.put(action, ref);
        }
        ref.add(transition);

        LinkedHashMap<LogEvent, List<Transition<LogEvent>>> ref1 = transitionsByActionAndTarget
                .get(action);
        if (ref1 == null) {
            ref1 = new LinkedHashMap<LogEvent, List<Transition<LogEvent>>>();
            transitionsByActionAndTarget.put(action, ref1);
        }
        List<Transition<LogEvent>> ref2 = ref1.get(target);
        if (ref2 == null) {
            ref2 = new ArrayList<Transition<LogEvent>>();
            ref1.put(target, ref2);
        }
        ref2.add(transition);
    }

    public void removeTransitions(List<Transition<LogEvent>> transitions) {
        this.transitions.removeAll(transitions);
        for (Transition<LogEvent> transition : transitions) {

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
    public final List<Transition<LogEvent>> getTransitions() {
        // Set<Relation<LogEvent>> set = new
        // LinkedHashSet<Relation<LogEvent>>();
        // set.addAll(transitions);
        return transitions;
    }

    public List<Transition<LogEvent>> getTransitions(String relation) {
        // checkConsistency();
        List<Transition<LogEvent>> res = transitionsByAction.get(relation);
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

    public List<Transition<LogEvent>> getTransitions(Partition target,
            String relation) {
        List<Transition<LogEvent>> forAction = transitionsByAction
                .get(relation);
        if (forAction == null) {
            return Collections.emptyList();
        }

        List<Transition<LogEvent>> res = new ArrayList<Transition<LogEvent>>();
        for (Transition<LogEvent> t : forAction) {
            if (t.getTarget().getParent() == target) {
                res.add(t);
            }
        }
        return res;
    }

    public List<Transition<LogEvent>> getTransitions(LogEvent target,
            String relation) {
        LinkedHashMap<LogEvent, List<Transition<LogEvent>>> forAction = transitionsByActionAndTarget
                .get(relation);
        if (forAction == null) {
            return Collections.emptyList();
        }
        List<Transition<LogEvent>> res = forAction.get(target);
        if (res == null) {
            return Collections.emptyList();
        }
        return res;
    }

    public void addTransitions(Collection<Transition<LogEvent>> transitions) {
        for (Transition<LogEvent> t : transitions) {
            this.addTransition(t);
        }
    }

    public void setTransitions(ArrayList<Transition<LogEvent>> t) {
        transitions.clear();
        transitions.addAll(t);
    }

    public String toStringFull() {
        return "[LogEvent: " + getAction() + " (" + hashCode() + ")" + "]";
    }

    @Override
    public IIterableIterator<Transition<LogEvent>> getTransitionsIterator() {
        return IterableAdapter.make(getTransitions().iterator());
    }

    @Override
    public IIterableIterator<Transition<LogEvent>> getTransitionsIterator(
            String relation) {
        return IterableAdapter.make(getTransitions(relation).iterator());
    }

    @Override
    public ITransition<LogEvent> getTransition(LogEvent target, String relation) {
        List<Transition<LogEvent>> list = getTransitions(target, relation);
        return list.size() == 0 ? null : list.get(0);
    }

    public Action getAction() {
        return action;
    }

    // TODO: order
    public Set<String> getRelations() {
        return transitionsByAction.keySet();
    }

    /**
     * Get the timestamp associated with the event.
     */
    public ITime getTime() {
        return action.getTime();
    }

    /**
     * Return the label associated with the event.
     */
    @Override
    public String getLabel() {
        return action.getLabel();
    }

    public Set<LogEvent> getSuccessors(String relation) {
        // TODO: avoid creating a new LinkedHashSet here
        Set<LogEvent> successors = new LinkedHashSet<LogEvent>();
        for (Transition<LogEvent> e : getTransitionsIterator(relation)) {
            successors.add(e.getTarget());
        }
        return successors;
    }

    @Override
    public boolean isTerminal() {
        return transitions.isEmpty();
    }

    @Override
    public Comparator<LogEvent> getComparator() {
        class PartitionComparator implements Comparator<LogEvent> {
            @Override
            public int compare(LogEvent arg0, LogEvent arg1) {
                if (arg0 == arg1) {
                    return 0;
                }

                // compare labels of the two message events
                int labelCmp = arg0.getLabel().compareTo(arg1.getLabel());
                if (labelCmp != 0) {
                    return labelCmp;
                }

                // compare number of children
                int transitionCntCmp = new Integer(arg0.transitions.size())
                        .compareTo(arg1.transitions.size());
                if (transitionCntCmp != 0) {
                    return transitionCntCmp;
                }

                // compare transitions to children
                ArrayList<WeightedTransition<LogEvent>> arg0SortedTrans = new ArrayList<WeightedTransition<LogEvent>>(
                        arg0.getWeightedTransitions());
                ArrayList<WeightedTransition<LogEvent>> arg1SortedTrans = new ArrayList<WeightedTransition<LogEvent>>(
                        arg1.getWeightedTransitions());

                Collections.sort(arg0SortedTrans);
                Collections.sort(arg1SortedTrans);
                for (int i = 0; i < arg0SortedTrans.size(); i++) {
                    int childCmp = arg0SortedTrans.get(i).compareTo(
                            arg1SortedTrans.get(i));
                    if (childCmp != 0) {
                        return childCmp;
                    }
                }

                return 0;
            }
        }
        return new PartitionComparator();
    }

    public String getLine() {
        return action.getLine();
    }

    public String getFullFileName() {
        return action.getFileName();
    }

    public String getShortFileName() {
        // Extract and return just the filename from the path.
        return new File(getFullFileName()).getName();
    }

    public String getLineNum() {
        int lineNum = action.getLineNum();
        return lineNum == 0 ? "" : "" + action.getLineNum();
    }

    @Override
    public List<WeightedTransition<LogEvent>> getWeightedTransitions() {
        List<WeightedTransition<LogEvent>> result = new ArrayList<WeightedTransition<LogEvent>>();
        List<Transition<LogEvent>> allTrans = getTransitions();
        int totalTrans = allTrans.size();
        for (Transition<LogEvent> tr : allTrans) {
            double freq = (double) 1 / (double) totalTrans;
            WeightedTransition<LogEvent> trWeighted = new WeightedTransition<LogEvent>(
                    tr.getSource(), tr.getTarget(), tr.getRelation(), freq, 1);
            result.add(trWeighted);
        }
        return result;
    }
}
