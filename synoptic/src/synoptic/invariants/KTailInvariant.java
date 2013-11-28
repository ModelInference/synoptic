package synoptic.invariants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nasa.ltl.graph.Graph;

import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * Temporal Invariant representing a kTail for some length k. Not a binary
 * invariant as tails are of varying length. Currently construction of regular
 * expressions for kTail invariants is handled in the InvariMint.model.InvModel
 * class. TODO: move that logic here.
 * 
 */
public class KTailInvariant implements ITemporalInvariant {

    // Set of all kTail invariants already created
    private static Map<List<EventType>, KTailInvariant> tails = new HashMap<List<EventType>, KTailInvariant>();

    private final String relations;

    // This tail's list of events
    private final List<EventType> tail;

    // The set of events that immediately follow this tail
    private final Set<EventType> following;

    /**
     * Returns a KTailInvariant for the given series. On construction, the
     * invariant has a tail but no follow events.
     */
    public KTailInvariant(List<EventType> eventTail,
            Set<EventType> followEvents, String relation) {
        this.relations = relation;
        this.tail = Collections.unmodifiableList(new ArrayList<EventType>(
                eventTail));
        this.following = new HashSet<EventType>(followEvents);
    }

    /**
     * Returns a KTailInvariant for the given series. On construction, the
     * invariant has a tail but no follow events.
     */
    public KTailInvariant(List<EventType> eventTail, Set<EventType> followEvents) {
        this(eventTail, followEvents, Event.defTimeRelationStr);
    }

    /**
     * Returns a list of the (ordered) events in this tail.
     */
    public List<EventType> getTailEvents() {
        return this.tail;
    }

    /**
     * Returns a list of the events that can immediately follow this tail.
     */
    public List<EventType> getFollowEvents() {
        return new ArrayList<EventType>(following);
    }

    /**
     * Returns a regular expression for a tail with the given tail events and
     * follow set.
     */
    public static String getRegex(List<Character> tailEvents,
            List<Character> followSet) {

        // Build the events that make up the contiguous tail.
        String tail = "";
        for (int i = 0; i < tailEvents.size(); i++) {
            tail += tailEvents.get(i);
        }

        // Build up a set of events that can potentially follow the tail.
        String follows = "";
        for (int i = 0; i < followSet.size(); i++) {
            follows += followSet.get(i);
            if (i != followSet.size() - 1) {
                follows += " | ";
            }
        }

        String ret;

        // 1. Build the negation of the language we want to accept.
        ret = "~(";
        // 2. The negation is a tail followed by something that is not in the
        // follows set of events (and this must occur at least once). We also
        // allow arbitrary strings before and after this pattern -- thus the .*
        // at start and end.
        ret += "(.*(" + tail + "([^ " + follows + "]))+.*)";

        // 3. The pattern above does not capture a tail followed by an empty
        // string (i.e., a trace terminating in just tail), so we add this.
        ret += "|" + ".*" + tail;

        ret += ")"; // match 1.
        return ret;

    }

    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        return path;
    }

    @Override
    public String getShortName() {
        return "kTail";
    }

    @Override
    public String getLongName() {
        return "kTail Invariant";
    }

    @Override
    public String toString() {
        return tail + " tail followed by " + following;
    }

    @Override
    public String getRelation() {
        return relations;
    }

    @Override
    public Set<EventType> getPredicates() {
        return new HashSet<EventType>(tail);
    }

    /**
     * This invariant is not used during refinement or coarsening, so LTL has
     * been left undefined
     */
    @Override
    public String getLTLString() {
        throw new UnsupportedOperationException();
    }

    /**
     * This invariant is not used during refinement or coarsening, so LTL has
     * been left undefined
     */
    @Override
    public Graph getAutomaton() {
        throw new UnsupportedOperationException();
    }
}
