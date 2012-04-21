package synoptic.model;

import java.util.Set;

import synoptic.model.interfaces.ITransition;

/**
 * A relation that supports storing two more pieces of information with a
 * transition: the fraction of all events that take this transition, and the
 * number of events that this transition. The fraction is the count divided by
 * the number of events belonging to the source node.
 * 
 * @author Sigurd Schneider
 * @param <NodeType>
 */
public class WeightedTransition<NodeType> extends Transition<NodeType> {
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
            Set<String> relations, double fraction, int count) {
        super(source, target, relations);
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
    public int compareTo(ITransition<NodeType> other) {
        int cmpSuper = super.compareTo(other);
        if (cmpSuper != 0) {
            return cmpSuper;
        }

        WeightedTransition<NodeType> otherW = (WeightedTransition<NodeType>) (other);

        // Compare fractions.
        int fracCmp = Double.compare(this.getFraction(), otherW.getFraction());
        if (fracCmp != 0) {
            return fracCmp;
        }

        // Compare counts.
        int countsCmp = ((Integer) this.getCount())
                .compareTo(otherW.getCount());
        return countsCmp;
    }
}
