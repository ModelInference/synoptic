package synoptic.invariants;

import java.util.List;

import synoptic.model.Partition;

public class RelationPath<T> {
    public ITemporalInvariant invariant;
    public List<T> path;

    public RelationPath(ITemporalInvariant invariant, List<T> path) {
        this.invariant = invariant;
        this.path = path;
    }

    @Override
    public String toString() {
        // return invariant.toString();
        StringBuilder result = new StringBuilder();
        result.append(invariant.toString());
        result.append(": ");
        for (T n : path) {
            result.append(((Partition) n).getLabel());
            result.append(" ");
        }
        return result.toString();
    }
}
