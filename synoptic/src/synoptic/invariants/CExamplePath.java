package synoptic.invariants;

import java.util.List;

import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.ITime;

/**
 * Represents a counter-example path in a model -- e.g., a PartitionGraph.
 * Associates an invariant with the path -- the path violates this invariant.
 * 
 * @param <T>
 *            Type of the nodes along the path.
 */
public class CExamplePath<T> {
    public ITemporalInvariant invariant;
    public List<T> path;
    public List<List<ITransition<EventNode>>> transitionsList;
    public List<ITime> tDeltas;
    public int violationStart;
    public int violationEnd;

    /**
     * Create a counter-example path
     * 
     * @param inv
     *            The invariant violated by this counter-example path
     * @param p
     *            The list of nodes in the path
     */
    public CExamplePath(ITemporalInvariant inv, List<T> p) {
        invariant = inv;
        path = p;
        transitionsList = null;
        tDeltas = null;
        violationStart = -1;
        violationEnd = -1;
    }

    /**
     * Create a counter-example path including concrete transitions and time
     * deltas
     * 
     * @param inv
     *            The invariant violated by this counter-example path
     * @param p
     *            Nodes in the path
     * @param transList
     *            Concrete transitions followed in this counter-example path
     * @param deltas
     *            Running time deltas for each node since t=0 state
     * @param vioStart
     *            The counter-example path index where the violation started
     * @param vioEnd
     *            The counter-example path index wher ethe violation ended
     */
    public CExamplePath(ITemporalInvariant inv, List<T> p,
            List<List<ITransition<EventNode>>> transList, List<ITime> deltas,
            int vioStart, int vioEnd) {
        invariant = inv;
        path = p;
        transitionsList = transList;
        tDeltas = deltas;
        violationStart = vioStart;
        violationEnd = vioEnd;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(invariant.toString());
        result.append(": ");
        for (T n : path) {
            if (n instanceof Partition) {
                result.append(((Partition) n).getEType().toString());
            } else {
                result.append(n.toString());
            }
            result.append(" ");
        }
        return result.toString();
    }
}
