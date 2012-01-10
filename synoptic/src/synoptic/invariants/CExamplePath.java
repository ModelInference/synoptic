package synoptic.invariants;

import java.util.List;

import synoptic.model.Partition;

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

    public CExamplePath(ITemporalInvariant inv, List<T> p) {
        invariant = inv;
        path = p;
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
