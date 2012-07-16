package synoptic.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import synoptic.main.SynopticMain;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
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

    List<Transition<EventNode>> transitions = new ArrayList<Transition<EventNode>>();

    /**
     * A map from a relation to a list of transitions that are associated with
     * the relation. A transition may be associated with multiple relations, and
     * may therefore appear in multiple sets.
     */
    LinkedHashMap<String, Set<Transition<EventNode>>> transitionsWithRelation = new LinkedHashMap<String, Set<Transition<EventNode>>>();

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

    /**
     * Add a transition from this node to node dest with multiple relations.
     * 
     * @param dest
     *            The destination of the transition.
     * @param relation
     *            The relation for which this transition is valid
     */
    public void addTransition(EventNode dest, Set<String> relations) {
        assert dest != null : "Transition Target cannot be null";

        addTransition(new Transition<EventNode>(this, dest, relations));
    }

    /**
     * Add a transition from this node to node dest, with a single relation.
     * 
     * @param dest
     *            The destination of the transition.
     * @param relation
     *            The relation for which this transition is valid
     */
    public void addTransition(EventNode dest, String relation) {
        assert dest != null : "Transition Target cannot be null";

        Set<String> relations = new LinkedHashSet<String>();
        relations.add(relation);
        addTransition(new Transition<EventNode>(this, dest, relations));
    }

    /**
     * Find all direct successors of all events. For an event e1, direct
     * successors are successors (in terms of vector-clock) that are not
     * preceded by any other successors of e1. That is, if e1 < x then x is a
     * direct successor if there is no other successor y to e1 such that y < x.
     * 
     * @param e1
     * @param group
     * @return
     */
    public static Set<EventNode> getDirectPOSuccessors(EventNode e1,
            List<EventNode> group) {
        LinkedHashSet<EventNode> e1DirectSuccessors = new LinkedHashSet<EventNode>();

        // Events in group are partially ordered. We have to do more
        // work in this case.

        // The first loop runs in O(n) and the second loop runs in
        // O(m^2) where m is the length of e1AllSuccessors list.
        // So the worst case run time is: O(n) + O(m^2) = O(m^2)

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

        return e1DirectSuccessors;
    }

    /**
     * Adds a new transition to the event node.
     */
    public void addTransition(Transition<EventNode> transition) {
        transitions.add(transition);
        for (String r : transition.getRelations()) {
            // For each relation associated with the new transition update the
            // mapping of relations to transitions that carry those relations.
            Set<Transition<EventNode>> ref = transitionsWithRelation.get(r);
            if (ref == null) {
                ref = new LinkedHashSet<Transition<EventNode>>();
                transitionsWithRelation.put(r, ref);
            }
            ref.add(transition);
        }

        EventNode dest = transition.getTarget();
        if (SynopticMain.getInstanceWithExistenceCheck().options.enablePerfDebugging) {
            if (dest.getTime() != null) {
                ITime delta = dest.getTime().computeDelta(this.getTime());
                transition.setDelta(delta);
            }
        }
    }

    /**
     * Check that all transitions are in local cache.
     * 
     * <pre>
     * TODO: refactor this out into a test.
     * </pre>
     */
    public void checkConsistency() {
        for (ITransition<EventNode> t : transitions) {
            assert (transitionsWithRelation.get(t.getRelations()).contains(t)) : "inconsistent transitions in message";
        }
    }

    public void addTransitions(Collection<Transition<EventNode>> transCollection) {
        for (Transition<EventNode> t : transCollection) {
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

    public Event getEvent() {
        return event;
    }

    public Set<String> getNodeRelations() {
        return transitionsWithRelation.keySet();
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

    @Override
    public Set<EventNode> getAllSuccessors() {
        Set<EventNode> successors = new LinkedHashSet<EventNode>();
        for (Transition<EventNode> e : transitions) {
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
    public int compareTo(EventNode other) {
        if (this == other) {
            return 0;
        }

        // Compare labels of the two message events.
        int labelCmp = event.getEType().compareTo(other.getEType());
        if (labelCmp != 0) {
            return labelCmp;
        }

        // Compare number of children.
        int transitionCntCmp = Integer.valueOf(transitions.size()).compareTo(
                other.transitions.size());
        if (transitionCntCmp != 0) {
            return transitionCntCmp;
        }

        // Compare transitions to children.
        ArrayList<WeightedTransition<EventNode>> thisTrans = new ArrayList<WeightedTransition<EventNode>>(
                this.getWeightedTransitions());
        ArrayList<WeightedTransition<EventNode>> otherTrans = new ArrayList<WeightedTransition<EventNode>>(
                other.getWeightedTransitions());

        Collections.sort(thisTrans);
        Collections.sort(otherTrans);
        for (int i = 0; i < thisTrans.size(); i++) {
            int transCmp = thisTrans.get(i).compareTo(otherTrans.get(i));
            if (transCmp != 0) {
                return transCmp;
            }
        }
        return 0;
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
    @Override
    public List<WeightedTransition<EventNode>> getWeightedTransitions() {
        List<WeightedTransition<EventNode>> result = new ArrayList<WeightedTransition<EventNode>>();
        int totalTrans = transitions.size();
        for (Transition<EventNode> tr : transitions) {
            double freq = (double) 1 / (double) totalTrans;
            WeightedTransition<EventNode> trWeighted = new WeightedTransition<EventNode>(
                    tr.getSource(), tr.getTarget(), tr.getRelations(), freq, 1);
            result.add(trWeighted);
        }
        return result;
    }

    @Override
    public List<Transition<EventNode>> getAllTransitions() {
        return transitions;
    }

    @Override
    public List<? extends ITransition<EventNode>> getTransitionsWithExactRelations(
            Set<String> relations) {
        Set<Transition<EventNode>> ret = null;
        // Iterate through all transitions, adding those to ret that have
        // exactly the input relations associated with them.
        for (Transition<EventNode> t : transitions) {
            if (t.getRelations().equals(relations)) {
                if (ret == null) {
                    ret = new LinkedHashSet<Transition<EventNode>>();
                }
                ret.add(t);
            }
        }
        if (ret == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Transition<EventNode>>(ret);
    }

    @Override
    public List<? extends ITransition<EventNode>> getTransitionsWithSubsetRelations(
            Set<String> relations) {
        Set<Transition<EventNode>> ret = null;
        // Iterate through all transitions, adding those to ret that have
        // exactly a subset of relations associated with them.
        for (Transition<EventNode> t : transitions) {
            if (relations.containsAll(t.getRelations())) {
                if (ret == null) {
                    ret = new LinkedHashSet<Transition<EventNode>>();
                }
                ret.add(t);
            }
        }
        if (ret == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Transition<EventNode>>(ret);
    }

    @Override
    public List<? extends ITransition<EventNode>> getTransitionsWithIntersectingRelations(
            Set<String> relations) {
        Set<Transition<EventNode>> ret = null;
        for (String r : relations) {
            if (transitionsWithRelation.containsKey(r)) {
                if (ret == null) {
                    ret = new LinkedHashSet<Transition<EventNode>>();
                }
                ret.addAll(transitionsWithRelation.get(r));
            }
        }
        if (ret == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Transition<EventNode>>(ret);
    }
}
