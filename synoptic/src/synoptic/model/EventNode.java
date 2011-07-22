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

/**
 * The event node class -- a node in a graph that contains an event.
 * 
 * @author Sigurd Schneider
 */
public class EventNode implements INode<EventNode> {
    /**
     * The partition that contains the event node
     */
    private Partition parent;

    /**
     * The event this Node corresponds to
     */
    private final Event event;

    /**
     * A Unique trace identifier
     */
    private int traceID = 0;

    // TODO: For totally ordered traces, the transitions becomes a single
    // element, and transitionsByEvents becomes superfluous.

    List<Transition<EventNode>> transitions = new ArrayList<Transition<EventNode>>();
    LinkedHashMap<String, List<Transition<EventNode>>> transitionsByRelation = new LinkedHashMap<String, List<Transition<EventNode>>>();

    public EventNode(EventNode copyFrom) {
        assert copyFrom != null;

        parent = copyFrom.parent;
        event = copyFrom.event;
    }

    public EventNode(Event eventArg) {
        assert eventArg != null;

        event = eventArg;
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
        return "[" + getEvent().getEType() + "]";
    }

    @Override
    public void addTransition(EventNode dest, String relation) {
        if (dest == null) {
            throw new InternalSynopticException("Dest was null");
        }
        addTransition(new Transition<EventNode>(this, dest, relation));
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
     *            this may contain the EventNode e1.
     * @return set of direct successors of e1
     * @throws ParseException
     *             when we detect that some two events in group have the same
     *             timestamp or if they have different length vector timestamps
     *             (comparison error).
     */
    public static Set<EventNode> getDirectSuccessors(EventNode e1,
            List<EventNode> group, boolean totallyOrderedTrace) {
        LinkedHashSet<EventNode> e1DirectSuccessors = new LinkedHashSet<EventNode>();

        if (totallyOrderedTrace) {
            // All events in group are totally ordered. Therefore,
            // the problem of finding direct successors has been reduced
            // to finding _one_ element in group with the smallest
            // time-stamp that exceeds e1's timestamp. We can do this with a
            // single scan = O(n) time.

            EventNode directSuccessor = null;
            for (EventNode e2 : group) {
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
            LinkedHashSet<EventNode> e1AllSuccessors = new LinkedHashSet<EventNode>();
            for (EventNode e2 : group) {
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
            for (EventNode e1Succ1 : e1AllSuccessors) {
                boolean directSuccessor = true; // whether or not e1Succ1 is
                                                // a direct successor of e2
                for (EventNode e1Succ2 : e1AllSuccessors) {
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

    public void addTransition(Transition<EventNode> transition) {
        transitions.add(transition);
        String eventLabel = transition.getRelation();
        List<Transition<EventNode>> ref = transitionsByRelation.get(eventLabel);
        if (ref == null) {
            ref = new ArrayList<Transition<EventNode>>();
            transitionsByRelation.put(eventLabel, ref);
        }
        ref.add(transition);
    }

    // public void removeTransitions(List<Transition<LogEvent>> transitions) {
    // this.transitions.removeAll(transitions);
    // for (Transition<LogEvent> transition : transitions) {
    //
    // if (transitionsByEvent.containsKey(transition.getRelation())) {
    // transitionsByEvent.get(transition.getRelation()).remove(
    // transition);
    // }
    //
    // if (transitionsByEventAndTarget.containsKey(transition
    // .getRelation())
    // && transitionsByEventAndTarget.get(
    // transition.getRelation()).containsKey(
    // transition.getTarget())) {
    // transitionsByEventAndTarget.get(transition.getRelation())
    // .get(transition.getTarget()).remove(transition);
    // }
    // }
    //
    // }

    @Override
    public final List<Transition<EventNode>> getTransitions() {
        return transitions;
    }

    @Override
    public List<Transition<EventNode>> getTransitions(String relation) {
        // checkConsistency();
        List<Transition<EventNode>> res = transitionsByRelation.get(relation);
        if (res == null) {
            return Collections.emptyList();
        }
        return res;
    }

    /**
     * Check that all transitions are in local cache.
     */
    public void checkConsistency() {
        for (ITransition<EventNode> t : transitions) {
            assert (transitionsByRelation.get(t.getRelation()).contains(t)) : "inconsistent transitions in message";
        }
    }

    // public List<Transition<LogEvent>> getTransitions(Partition target,
    // String relation) {
    // List<Transition<LogEvent>> forEvent = transitionsByEvent
    // .get(relation);
    // if (forEvent == null) {
    // return Collections.emptyList();
    // }
    //
    // List<Transition<LogEvent>> res = new ArrayList<Transition<LogEvent>>();
    // for (Transition<LogEvent> t : forEvent) {
    // if (t.getTarget().getParent() == target) {
    // res.add(t);
    // }
    // }
    // return res;
    // }

    // public List<Transition<LogEvent>> getTransitions(LogEvent target,
    // String relation) {
    // LinkedHashMap<LogEvent, List<Transition<LogEvent>>> forEvent =
    // transitionsByEventAndTarget
    // .get(relation);
    // if (forEvent == null) {
    // return Collections.emptyList();
    // }
    // List<Transition<LogEvent>> res = forEvent.get(target);
    // if (res == null) {
    // return Collections.emptyList();
    // }
    // return res;
    // }

    public void addTransitions(Collection<Transition<EventNode>> transitions) {
        for (Transition<EventNode> t : transitions) {
            this.addTransition(t);
        }
    }

    public void setTransitions(ArrayList<Transition<EventNode>> t) {
        transitions.clear();
        transitions.addAll(t);
    }

    public String toStringFull() {
        return "[EventNode: " + getEvent() + " (" + hashCode() + ")" + "]";
    }

    @Override
    public IIterableIterator<Transition<EventNode>> getTransitionsIterator() {
        return IterableAdapter.make(getTransitions().iterator());
    }

    @Override
    public IIterableIterator<Transition<EventNode>> getTransitionsIterator(
            String relation) {
        return IterableAdapter.make(getTransitions(relation).iterator());
    }

    // @Override
    // public ITransition<LogEvent> getTransition(LogEvent target, String
    // relation) {
    // List<Transition<LogEvent>> list = getTransitions(target, relation);
    // return list.size() == 0 ? null : list.get(0);
    // }

    public Event getEvent() {
        return event;
    }

    // TODO: order
    public Set<String> getRelations() {
        return transitionsByRelation.keySet();
    }

    /**
     * Get the timestamp associated with the event.
     */
    public ITime getTime() {
        return event.getTime();
    }

    /**
     * Return the label associated with the event.
     */
    @Override
    public EventType getEType() {
        return event.getEType();
    }

    public Set<EventNode> getSuccessors(String relation) {
        // TODO: avoid creating a new LinkedHashSet here
        Set<EventNode> successors = new LinkedHashSet<EventNode>();
        for (Transition<EventNode> e : getTransitionsIterator(relation)) {
            successors.add(e.getTarget());
        }
        return successors;
    }

    @Override
    public boolean isTerminal() {
        return event.getEType().isTerminalEventType();
    }

    @Override
    public boolean isInitial() {
        return event.getEType().isInitialEventType();
    }

    @Override
    public Comparator<EventNode> getComparator() {
        class EventNodeComparator implements Comparator<EventNode> {

            @Override
            public int compare(EventNode arg0, EventNode arg1) {
                if (arg0 == arg1) {
                    return 0;
                }

                // compare labels of the two message events
                int labelCmp = arg0.getEType().compareTo(arg1.getEType());
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
                ArrayList<WeightedTransition<EventNode>> arg0SortedTrans = new ArrayList<WeightedTransition<EventNode>>(
                        arg0.getWeightedTransitions());
                ArrayList<WeightedTransition<EventNode>> arg1SortedTrans = new ArrayList<WeightedTransition<EventNode>>(
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
        return new EventNodeComparator();
    }

    public String getLine() {
        return event.getLine();
    }

    public void setTraceID(int traceID) {
        this.traceID = traceID;
    }

    public int getTraceID() {
        return traceID;
    }

    public String getFullFileName() {
        return event.getFileName();
    }

    public String getShortFileName() {
        // Extract and return just the filename from the path.
        return new File(getFullFileName()).getName();
    }

    public int getLineNum() {
        return event.getLineNum();
    }

    /**
     * This method returns the set of transitions augmenting each transition
     * with information about frequency and number of observations.
     */
    private List<WeightedTransition<EventNode>> getWeightedTransitions(
            List<Transition<EventNode>> trans) {
        List<WeightedTransition<EventNode>> result = new ArrayList<WeightedTransition<EventNode>>();
        int totalTrans = trans.size();
        for (Transition<EventNode> tr : trans) {
            double freq = (double) 1 / (double) totalTrans;
            WeightedTransition<EventNode> trWeighted = new WeightedTransition<EventNode>(
                    tr.getSource(), tr.getTarget(), tr.getRelation(), freq, 1);
            result.add(trWeighted);
        }
        return result;
    }

    @Override
    public List<WeightedTransition<EventNode>> getWeightedTransitions() {
        return getWeightedTransitions(getTransitions());
    }

    @Override
    public List<WeightedTransition<EventNode>> getWeightedTransitions(
            String relation) {
        return getWeightedTransitions(getTransitions(relation));
    }

    @Override
    public ITransition<EventNode> getTransition(EventNode node, String relation) {
        for (ITransition<EventNode> t : getTransitions(relation)) {
            if (t.getTarget().equals(node)) {
                return t;
            }
        }
        return null;
    }
}
