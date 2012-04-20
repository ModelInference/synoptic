package synoptic.model;

import java.util.LinkedHashSet;
import java.util.Set;

import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.MultipleRelations;
import synoptic.util.time.ITime;
import synoptic.util.time.ITimeSeries;

/**
 * An implementation of a transition.
 * 
 * @author Sigurd Schneider
 * @param <NodeType>
 */
public class Transition<NodeType> implements ITransition<NodeType> {
    protected NodeType source;
    protected NodeType target;
    protected ITime delta = null;
    protected ITimeSeries<ITime> series = null;

    protected Set<String> relations;

    private Transition(NodeType source, NodeType target) {
        assert source != null;
        if (target == null) {
            return;
        }
        assert target != null;

        this.source = source;
        this.target = target;
    }

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

    @Override
    public NodeType getTarget() {
        return target;
    }

    @Override
    public NodeType getSource() {
        return source;
    }

    @Override
    public Set<String> getRelations() {
        return relations;
    }

    @Override
    public ITime getDelta() {
        return delta;
    }

    @Override
    public void setDelta(ITime d) {
        assert d != null;
        delta = d;
    }

    @Override
    public ITimeSeries<ITime> getDeltaSeries() {
		createSeriesIfEmpty();
        return this.series;
    }

    @Override
    public void addDelta(ITime newDelta) {
        // If delta is null, do not add anything.
        if (newDelta == null) {
            return;
        }

		createSeriesIfEmpty();
        this.series.addDelta(newDelta);
    }

	/**
	 * This method is here to create the series if one does not exist
	 * already.  If the series is then created, this will add the already
	 * existing delta contained within the object into the series and then
	 * set it to null.
	 */
	private void createSeriesIfEmpty() {
		// If there is already a series, add the delta
        // to the series, then set the delta field to null.
		if (this.series == null) {
			this.series = new ITimeSeries<ITime>();
			if (this.delta != null) {
				this.series.addDelta(this.delta);
				this.delta = null;
			}
		}
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (relations == null ? 0 : relations.hashCode());
        result = prime * result + (source == null ? 0 : source.hashCode());
        result = prime * result + (target == null ? 0 : target.hashCode());
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
        return true;
    }

    @Override
    public void setSource(NodeType source) {
        this.source = source;
    }

    @Override
    public void setTarget(NodeType target) {
        this.target = target;
    }

    @Override
    public String toStringConcise() {
        return getRelations().toString();
    }

    @Override
    public int compareTo(ITransition<NodeType> other) {
        // First compare the sources of the two transitions.
        int cmpSrc = ((INode<NodeType>) this.source).getEType().compareTo(
                ((INode<NodeType>) other.getSource()).getEType());
        if (cmpSrc != 0) {
            return cmpSrc;
        }

        // Then, compare the targets of the two transitions.
        int cmpTarget = ((INode<NodeType>) this.target).getEType().compareTo(
                ((INode<NodeType>) other.getTarget()).getEType());
        if (cmpTarget != 0) {
            return cmpTarget;
        }
        // If both the sources and the targets are equal then we use the
        // relations for possible disambiguation.

        return MultipleRelations.compareMultipleRelations(this.relations,
                other.getRelations());
    }
}
