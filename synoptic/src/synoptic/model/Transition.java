package synoptic.model;

import java.util.LinkedHashSet;
import java.util.Set;

import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.ITime;
import synoptic.util.time.TimeSeries;

/**
 * An implementation of a transition.
 * 
 * @param <NodeType>
 */
public class Transition<NodeType> implements ITransition<NodeType> {

    protected NodeType source;
    protected NodeType target;

    TransitionLabelsMap labels;

    // TODO: currently, we are using this field to represent multiple relations,
    // but eventually we will refactor this away and instead use the
    // RelationsSet maintained by labels[RELATIONS_LABEL].
    protected Set<String> relations;

    private Transition(NodeType source, NodeType target) {
        assert source != null;
        assert target != null;

        this.source = source;
        this.target = target;
        this.labels = new TransitionLabelsMap();
    }

    // //////////////////////////////////////////////////////////////////////
    // Constructors.

    /**
     * Create a new transition with multiple relations.
     * 
     * @param source
     *            source node
     * @param target
     *            target node
     * @param relation
     *            the label of the transition
     */
    public Transition(NodeType source, NodeType target, Set<String> relations) {
        this(source, target);
        assert relations != null;
        this.relations = relations;
    }

    /**
     * Create a new transition with a single relation.
     * 
     * @param source
     *            source node
     * @param target
     *            target node
     * @param relation
     *            the label of the transition
     */
    public Transition(NodeType source, NodeType target, String relation) {
        this(source, target);
        this.relations = new LinkedHashSet<String>();
        this.relations.add(relation);
    }

    // //////////////////////////////////////////////////////////////////////

    @Override
    public NodeType getTarget() {
        return target;
    }

    @Override
    public NodeType getSource() {
        return source;
    }

    @Override
    public TransitionLabelsMap getLabels() {
        return labels;
    }

    @Override
    public Set<String> getRelation() {
        return relations;
    }

    @Override
    public void setSource(NodeType source) {
        this.source = source;
    }

    @Override
    public void setTarget(NodeType target) {
        this.target = target;
    }

    // //////////////////////////////////////////////////////////////////////
    // Methods delegating label-related functionality to labels
    // TransitionLabelsMap instance.

    @Override
    public Double getProbability() {
        return this.labels.getProbability();
    }

    @Override
    public void setProbability(double fraction) {
        this.labels.setLabel(TransitionLabelType.PROBABILITY_LABEL, fraction);
    }

    @Override
    public Integer getCount() {
        return this.labels.getCount();
    }

    @Override
    public void setCount(int count) {
        this.labels.setLabel(TransitionLabelType.COUNT_LABEL, count);
    }

    // ////////////////////////////
    // Methods below are also delegating to the labels instance, but are more
    // elaborate because they also check for state consistency. For example, a
    // transition is not allowed to maintain both a delta series and a time
    // delta at the same time.

    @Override
    public ITime getTimeDelta() {
        if (this.labels.getTimeDeltaSeries() != null) {
            throw new IllegalStateException("Series initialized");
        }
        return this.labels.getTimeDelta();
    }

    @Override
    public void setTimeDelta(ITime d) {
        if (d == null) {
            throw new IllegalArgumentException();
        }

        if (this.labels.getTimeDeltaSeries() != null) {
            throw new IllegalStateException("Series initialized.");
        }

        this.labels.setLabel(TransitionLabelType.TIME_DELTA_LABEL, d);
    }

    @Override
    public TimeSeries<ITime> getDeltaSeries() {
        if (this.labels.getTimeDelta() != null) {
            throw new IllegalStateException("Delta already set.");
        }

        createSeriesIfEmpty();
        return this.labels.getTimeDeltaSeries();
    }

    @Override
    public void addTimeDeltaToSeries(ITime newDelta) {
        // Should not be able to have a delta and a series at the same
        // time.
        if (this.labels.getTimeDelta() != null) {
            throw new IllegalStateException("Delta already set.");
        }

        // If delta is null, do not add anything.
        if (newDelta == null) {
            return;
        }

        createSeriesIfEmpty();
        TimeSeries<ITime> series = this.labels.getTimeDeltaSeries();
        series.addDelta(newDelta);
    }

    /**
     * Helper method -- creates the time delta series if one does not exist.
     */
    private void createSeriesIfEmpty() {
        if (this.labels.getTimeDeltaSeries() == null) {
            this.labels.setLabel(TransitionLabelType.TIME_DELTA_SERIES_LABEL,
                    new TimeSeries<ITime>());
        }
    }

    // //////////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (relations == null ? 0 : relations.hashCode());
        result = prime * result + (source == null ? 0 : source.hashCode());
        result = prime * result + (target == null ? 0 : target.hashCode());
        // TODO: implement a custom hashCode method for TransitionLabelsMap.
        result = prime * result + (labels == null ? 0 : labels.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
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
        Transition<NodeType> other = (Transition<NodeType>) obj;
        if (relations == null) {
            if (other.relations != null) {
                return false;
            }
        } else if (!relations.equals(other.relations)) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }

        int cmp = this.labels.compareTo(other.getLabels());
        return (cmp == 0);
    }

    @Override
    public int compareTo(ITransition<NodeType> other) {
        // First compare the sources of the two transitions.
        int cmp = ((INode<NodeType>) this.source).getEType().compareTo(
                ((INode<NodeType>) other.getSource()).getEType());
        if (cmp != 0) {
            return cmp;
        }

        // Then, compare the targets of the two transitions.
        cmp = ((INode<NodeType>) this.target).getEType().compareTo(
                ((INode<NodeType>) other.getTarget()).getEType());
        if (cmp != 0) {
            return cmp;
        }
        // If both the sources and the targets are equal then compare the
        // relations:
        cmp = RelationsSet.compareMultipleRelations(this.relations,
                other.getRelation());
        if (cmp != 0) {
            return cmp;
        }

        return this.labels.compareTo(other.getLabels());
    }

    @Override
    public String toString() {
        return source + "->" + target;
    }
}
