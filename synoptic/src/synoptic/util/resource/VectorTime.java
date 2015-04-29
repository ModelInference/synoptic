package synoptic.util.resource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import synoptic.model.EventNode;

/**
 * A vector timestamps is a fixed array of integer "clocks". Typically, an
 * integer clock at index i represents the local time of process i. If the
 * system is distributed, then a process is a host. Therefore, the vector
 * represents a point in time in a multiprocess (or distributed) execution. A
 * vector time is a special resource itself and has no key identifying it.
 */
public class VectorTime extends AbstractResource {
    ArrayList<Integer> vector = new ArrayList<Integer>();

    /**
     * Determines and returns the ith event for node identified by nodeIndex in
     * a sequence of events. Precondition: nodeIndex must be valid.
     */
    public static EventNode determineIthEvent(int nodeIndex,
            List<EventNode> events, int i) {
        // The earliest found ith event at node nodeIndex so far.
        EventNode earliestEvent = null;

        for (EventNode e : events) {
            AbstractResource etime = e.getEvent().getTime();
            if (!(etime instanceof VectorTime)) {
                throw new WrongTimeTypeException();
            }
            if (((VectorTime) etime).vector.get(nodeIndex) != i) {
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
    public static List<List<EventNode>> mapLogEventsToNodes(
            List<EventNode> events) {
        if (events == null || events.size() == 0) {
            return null;
        }
        LinkedList<List<EventNode>> map = new LinkedList<List<EventNode>>();

        AbstractResource e0time = events.get(0).getEvent().getTime();
        if (!(e0time instanceof VectorTime)) {
            throw new WrongTimeTypeException();
        }

        // The number of nodes is indicated by the length of the vector time.
        int numNodes = ((VectorTime) e0time).vector.size();

        // For each node, for all i determine the ith local event at the node.
        EventNode e;
        LinkedList<EventNode> eventList;
        for (int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
            int i = 1;
            eventList = new LinkedList<EventNode>();
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
     */
    public VectorTime(String timeStr) throws IllegalArgumentException {
        // IB: Why is there no unique key to identify vector timestamps? Don't
        // you want these to be incomparable with other kinds of resources?
        super("");
        String[] times = timeStr.split(",");
        for (String t : times) {
            Integer i = Integer.parseInt(t);
            vector.add(i);
        }
    }

    /**
     * Builds a VectorTime from a vector
     * 
     * @param vector
     *            input vector
     */
    public VectorTime(List<Integer> vector) throws IllegalArgumentException {
        super("");
        this.vector.addAll(vector);
    }

    /**
     * Builds a VectorTime from a single Integer.
     * 
     * @param i
     */
    public VectorTime(Integer i) {
        super("");
        vector.add(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see synoptic.util.ITime#lessThan(synoptic.util.VectorTime)
     */
    @Override
    public boolean lessThan(AbstractResource t) {
        if (!(t instanceof VectorTime)) {
            throw new NonComparableResourceException(this, t);
        }
        VectorTime vTime = (VectorTime) t;

        boolean foundStrictlyLess = false;

        if (vector.size() != vTime.vector.size()) {
            // Two vectors are only comparable if they have the same length.
            throw new NotComparableVectorsException(this, t);
        }

        for (int i = 0; i < vector.size(); ++i) {
            if (vector.get(i) < vTime.vector.get(i)) {
                foundStrictlyLess = true;
            } else if (vector.get(i) > vTime.vector.get(i)) {
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
        List<Integer> vec = new ArrayList<Integer>();
        vec.addAll(this.vector);
        vec.set(index, vec.get(index) + 1);
        return new VectorTime(vec);
    }

    /*
     * (non-Javadoc)
     * 
     * @see synoptic.util.ITime#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (vector == null ? 0 : vector.hashCode());
        if (vector != null) {
            for (int i = 0; i < vector.size(); ++i) {
                result += prime * result + vector.get(i).hashCode();
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see synoptic.util.ITime#equals(java.lang.Object)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see synoptic.util.ITime#toString()
     */
    @Override
    public String toString() {
        return vector.toString();
    }

    /**
     * VectorTime is only partially ordered, therefore compareTo cannot be used
     * to derive a total ordering of a set of VectorTime instances. Instead,
     * compareTo can be used to topologically sort a set of VectorTimes.
     */
    @Override
    public int compareTo(AbstractResource t) {
        if (!(t instanceof VectorTime)) {
            throw new NonComparableResourceException(this, t);
        }
        if (this.equals(t)) {
            return 0;
        }
        if (this.lessThan(t)) {
            return -1;
        }
        return 0;
    }

    /**
     * Delta computation, incrementing, and division are undefined for vector
     * timestamps.
     */
    @Override
    public AbstractResource computeDelta(AbstractResource other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractResource incrBy(AbstractResource other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractResource divBy(int divisor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractResource normalize(AbstractResource relativeTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractResource getZeroResource() {
        return new VectorTime("0");
    }
}
