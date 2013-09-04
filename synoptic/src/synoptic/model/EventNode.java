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
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.model.state.State;
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
     * Pre-event state.
     */
    private State preEventState;
    
    /**
     * Post-event state.
     */
    private State postEventState;
    
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

    /**
     * The process local successor node -- node with closest larger timestamp
     * corresponding to the same process as this node. This is set during
     * immediate successor computation and is used by Dynoptic
     */
    private EventNode processLocalSucc = null;

    private void setProcessLocalSuccessor(EventNode processLocalSucc) {
        assert this.processLocalSucc == null;

        this.processLocalSucc = processLocalSucc;
    }

    public EventNode getProcessLocalSuccessor() {
        return processLocalSucc;
    }

    /**
     * Updates the transition probabilities of all the transitions emitted from
     * this event node. This is used when the set of transitions is somehow
     * changed (e.g., a new transition is added).
     */
    private void updateTransitionProbabilities() {
        int totalTrans = transitions.size();
        for (Transition<EventNode> tr : transitions) {
            double freq = (double) 1 / (double) totalTrans;
            tr.setProbability(freq);
        }
    }

    public EventNode(EventNode copyFrom) {
        assert copyFrom != null;

        parent = copyFrom.parent;
        event = copyFrom.event;
        preEventState = copyFrom.preEventState;
        postEventState = copyFrom.postEventState;
    }

    public EventNode(Event eventArg) {
        assert eventArg != null;

        event = eventArg;
        parent = null;
        preEventState = null;
        postEventState = null;
    }

    @Override
    public Partition getParent() {
        return parent;
    }

    @Override
    public void setParent(Partition parent) {
        this.parent = parent;
    }

    public void setPreEventState(State state) {
        this.preEventState = state;
    }
    
    public void setPostEventState(State state) {
        this.postEventState = state;
    }
    
    public State getPreEventState() {
        return preEventState;
    }
    
    public State getPostEventState() {
        return postEventState;
    }
    
    @Override
    public String toString() {
        return "[EventNode: " + getEvent() + " (" + hashCode() + ")" + "]";
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
     * Given an event node e1, and a set of event nodes allNodes, this methods
     * finds all _direct_ successors of e1 in allNodes. Direct successors are
     * successors (in terms of vector-clock) that are not preceded by any other
     * successors of e1. That is, if e1 < e2 then e2 is a direct successor if
     * there is no other successor e3 to e1 such that e3 < e2.
     * 
     * @param e1
     * @param allNodes
     * @return
     */
    public static Set<EventNode> getDirectPOSuccessors(EventNode e1,
            List<EventNode> allNodes) {
        LinkedHashSet<EventNode> e1DirectSuccessors = new LinkedHashSet<EventNode>();

        // Events in group are partially ordered. We have to do more
        // work in this case.

        // The first loop runs in O(n) and the second loop runs in
        // O(m^2) where m is the length of e1AllSuccessors list.
        // So the worst case run time is: O(n) + O(m^2) = O(m^2)

        // First find all all events that succeed e1, store this set in
        // e1AllSuccessors.
        LinkedHashSet<EventNode> e1AllSuccessors = new LinkedHashSet<EventNode>();
        for (EventNode e2 : allNodes) {
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

        // ///////////////// Dynoptic-related:
        // As we search for direct successors, we also find the nearest
        // process-local successor. This is used by Dynoptic.
        //
        // NOTE: for this to work, the DistEventType must be first interpreted
        // with interpretEType()
        int localPid = ((DistEventType) e1.getEType()).getPid();
        EventNode processLocalSucc = null;
        // /////////////////

        // Now out of all successors find all direct successors of e1.
        for (EventNode e1Succ1 : e1AllSuccessors) {
            // Whether or not e1Succ1 is a direct successor of e2.
            boolean directSuccessor = true;

            // ///////////////// Dynoptic-related:
            if (((DistEventType) e1Succ1.getEType()).getPid() == localPid) {
                if (processLocalSucc == null
                        || e1Succ1.getTime().lessThan(
                                processLocalSucc.getTime())) {
                    processLocalSucc = e1Succ1;
                }
            }
            // /////////////////

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

        // ///////////////// Dynoptic-related:
        e1.setProcessLocalSuccessor(processLocalSucc);

        return e1DirectSuccessors;
    }

    /**
     * Adds a new transition to the event node.
     */
    public void addTransition(Transition<EventNode> transition) {
        transitions.add(transition);
        for (String r : transition.getRelation()) {
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
                transition.setTimeDelta(delta);
            }
        }
        updateTransitionProbabilities();
        // Set the count on the newly added transition.
        transition.setCount(1);
    }

    public void addTransitions(Collection<Transition<EventNode>> transCollection) {
        for (Transition<EventNode> t : transCollection) {
            this.addTransition(t);
        }
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
        List<? extends ITransition<EventNode>> thisTrans = this
                .getWeightedTransitions();
        List<? extends ITransition<EventNode>> otherTrans = other
                .getWeightedTransitions();

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
    public List<? extends ITransition<EventNode>> getWeightedTransitions() {
        return transitions;
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
            if (t.getRelation().equals(relations)) {
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
            if (relations.containsAll(t.getRelation())) {
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
