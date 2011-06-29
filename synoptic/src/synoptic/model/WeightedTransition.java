package synoptic.model;

/**
 * A relation that supports storing two more pieces of information with a
 * transition: the fraction of all events that take this transition, and the
 * number of events that this transition. The fraction is the count divided by
 * the number of events belonging to the source node.
 * 
 * @author Sigurd Schneider
 * @param <NodeType>
 */
public class WeightedTransition<NodeType> extends Transition<NodeType>
        implements Comparable<WeightedTransition<NodeType>> {
    double fraction = 0.0;
    private int count = 0;

    /**
     * Creates a relation.
     * 
     * @param source
     *            source node
     * @param target
     *            target node
     * @param relation
     *            relation name
     * @param fraction
     *            fraction of all events that take this transition
     * @param count
     *            number of events that take this transition
     */
    public WeightedTransition(NodeType source, NodeType target,
            String relation, double fraction, int count) {
        super(source, target, relation);
        this.fraction = fraction;
        this.count = count;
    }

    /**
     * Get the fraction of events that take this transition.
     */
    public double getFraction() {
        return fraction;
    }

    /**
     * Get the number of events that take this transition.
     */
    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(WeightedTransition<NodeType> other) {
        // compare references
        if (this == other) {
            return 0;
        }

        // compare fractions associated with relation
        int fracCmp = Double.compare(this.getFraction(), other.getFraction());
        if (fracCmp != 0) {
            return fracCmp;
        }

        // compare counts associated with relation
        int countsCmp = ((Integer) this.getCount()).compareTo(other.getCount());
        if (countsCmp != 0) {
            return countsCmp;
        }

        // compare just the labels of the StateTypes
        int actionCmp = action.compareTo(other.action);
        return actionCmp;
    }
}
