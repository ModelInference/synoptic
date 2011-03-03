package synoptic.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import synoptic.model.LogEvent;

public class VectorTime {
    ArrayList<Integer> vector = new ArrayList<Integer>();

    /**
     * Determines and returns the ith event for node identified by nodeIndex in
     * a sequence of events. Precondition: nodeIndex must be valid.
     */
    public static LogEvent determineIthEvent(int nodeIndex,
            List<LogEvent> events, int i) {
        // The earliest found ith event at node nodeIndex so far.
        LogEvent earliestEvent = null;

        for (LogEvent e : events) {
            if (e.getAction().getTime().vector.get(nodeIndex) != i) {
                continue;
            }
            if (earliestEvent == null) {
                earliestEvent = e;
                continue;
            }
            if (e.getTime().lessThan(earliestEvent.getTime())) {
                earliestEvent = e;
            }
        }
        return earliestEvent;
    }

    /**
     * Determines the totally ordered sequence of local events for each node in
     * a distributed system given the set of all events in the system
     * time-stamped with vector clocks.
     * 
     * <pre>
     * The ith event for node n is the event e such that:
     * for all e' in events, e'.time[n] == i, e < e'
     * </pre>
     * 
     * @param events
     * @return A list in which an item a index j is a (totally ordered) list of
     *         events that occurred locally at node j.
     */
    public static List<List<LogEvent>> mapLogEventsToNodes(List<LogEvent> events) {
        if (events == null || events.size() == 0) {
            return null;
        }
        LinkedList<List<LogEvent>> map = new LinkedList<List<LogEvent>>();
        // The number of nodes is indicated by the length of the vector time.
        int numNodes = events.get(0).getAction().getTime().vector.size();

        // For each node, for all i determine the ith local event at the node.
        int i;
        LogEvent e;
        LinkedList<LogEvent> eventList;
        for (int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
            i = 0;
            eventList = new LinkedList<LogEvent>();
            while (true) {
                e = determineIthEvent(nodeIndex, events, i);
                if (e == null) {
                    // No ith event exists for nodeIndex.
                    break;
                }
                eventList.add(e);
                i += 1;
            }
            map.add(eventList);
        }
        return map;
    }

    /**
     * Builds a VectorTime from a string that looks like "1,2,3"
     * 
     * @param timeStr
     *            string input representing a vtime
     * @throws IllegalArgumentException
     *             when timeStr contains negative integers
     */
    public VectorTime(String timeStr) throws IllegalArgumentException {
        String[] times = timeStr.split(",");
        for (String t : times) {
            Integer i = Integer.parseInt(t);
            if (i < 0) {
                throw new IllegalArgumentException();
            }
            vector.add(i);
        }
    }

    /**
     * Builds a VectorTime from a vector
     * 
     * @param vector
     *            input vector
     * @throws IllegalArgumentException
     *             when vector contains negative integers
     */
    public VectorTime(List<Integer> vector) throws IllegalArgumentException {
        for (Integer i : vector) {
            if (i < 0) {
                throw new IllegalArgumentException();
            }
        }
        this.vector.addAll(vector);
    }

    /**
     * Builds a VectorTime from a single Integer.
     * 
     * @param i
     * @throws IllegalArgumentException
     *             when i is negative
     */
    public VectorTime(Integer i) {
        if (i < 0) {
            throw new IllegalArgumentException();
        }
        vector.add(i);
    }

    /**
     * Returns true if (this < t), otherwise returns false
     * 
     * @param t
     *            the other vtime
     * @return
     */
    public boolean lessThan(VectorTime t) {
        boolean foundStrictlyLess = false;

        if (vector.size() != t.vector.size()) {
            // Two vectors are only comparable if they have the same length.
            throw new NotComparableVectorsException(this, t);
        }

        for (int i = 0; i < vector.size(); ++i) {
            if (vector.get(i) < t.vector.get(i)) {
                foundStrictlyLess = true;
            } else if (vector.get(i) > t.vector.get(i)) {
                return false;
            }
        }
        return foundStrictlyLess;
    }

    /**
     * @return Whether or not this is a unit vector
     */
    public boolean isUnitVector() {
        boolean sawOne = false;
        for (int i = 0; i < vector.size(); ++i) {
            if (sawOne && vector.get(i) == 1) {
                return false;
            }
            if (vector.get(i) == 1) {
                sawOne = true;
            }
            if (vector.get(i) > 1) {
                return false;
            }
        }
        return sawOne;
    }

    /**
     * @return Whether or not the vector is of length 1
     */
    public boolean isSingular() {
        return vector.size() == 1;
    }

    /**
     * Increments vtime at an index and returns the new vtime
     * 
     * @param index
     * @return the newly created, incremented vtime
     */
    public VectorTime step(int index) {
        List<Integer> vector = new ArrayList<Integer>();
        vector.addAll(this.vector);
        vector.set(index, vector.get(index) + 1);
        return new VectorTime(vector);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (vector == null ? 0 : vector.hashCode());
        for (int i = 0; i < vector.size(); ++i) {
            result += prime * result + vector.get(i).hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VectorTime other = (VectorTime) obj;
        if (vector == null) {
            if (other.vector != null) {
                return false;
            }
        } else {
            if (vector.size() != other.vector.size()) {
                // Two vectors are only comparable if they have the same length.
                throw new NotComparableVectorsException(this, other);
            }
            for (int i = 0; i < vector.size(); ++i) {
                if (vector.get(i) != other.vector.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return vector.toString();
    }
}