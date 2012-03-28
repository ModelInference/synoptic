package synoptic.invariants.miners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.model.EventType;

/**
 * Storage of k tails for KTailInvariantMiner. Creates a KTail for each unique
 * series of events, and tracks number of instances of each tail as well as the
 * set of possible following events.
 * 
 * @author jennyabrahamson
 */
public class KTail {

    // Set of all kTails already created
    private static Map<List<EventType>, KTail> tails = new HashMap<List<EventType>, KTail>();

    // This tail's list of events
    private final List<EventType> tail;

    // The set of events that immediately follow this tail
    private Set<EventType> following;

    // The number of instances of this tail
    private int instances;

    // Creates a new tail for the given series of events.
    private KTail(List<EventType> eventTail) {
        this.tail = Collections.unmodifiableList(new ArrayList<EventType>(
                eventTail));
        this.following = new HashSet<EventType>();
        this.instances = 0;
    }

    /**
     * Returns the single KTail for the given series of events. Also updates the
     * count of instances and set of following events for this tail.
     */
    public static KTail getTail(List<EventType> eventTail, EventType follow) {
        KTail theTail = tails.get(eventTail);
        if (theTail == null) {
            theTail = new KTail(eventTail);
            tails.put(theTail.tail, theTail);
        }
        theTail.following.add(follow);
        theTail.instances++;
        return theTail;
    }

    /**
     * Returns the number of mined instances of this tail.
     */
    public int getInstances() {
        return instances;
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

    @Override
    public String toString() {
        return tail + " follow by " + following;
    }
}
