package synoptic.model;

import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.ITime;

/**
 * An implementation of a transition.
 * 
 * @author Sigurd Schneider
 * @param <NodeType>
 */
public class Transition<NodeType> implements ITransition<NodeType> {
    protected NodeType source;
    protected NodeType target;
    protected final String relation;
    protected ITime delta;

    /**
     * Create a new transition.
     * 
     * @param source
     *            source node
     * @param target
     *            target node
     * @param relation
     *            the label of the transition
     */
    public Transition(NodeType source, NodeType target, String relation) {
    	this(source, target, relation, null);
    }
    
    public Transition(NodeType source, NodeType target, String relation, ITime delta) {
        assert source != null;
        assert target != null;
        assert relation != null;

        this.source = source;
        this.target = target;
        this.relation = relation;
        this.delta = delta;
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
    public String getRelation() {
        return relation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (relation == null ? 0 : relation.hashCode());
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
        if (relation == null) {
            if (other.relation != null) {
                return false;
            }
        } else if (!relation.equals(other.relation)) {
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
        return getRelation();
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
        return this.relation.compareTo(other.getRelation());
    }
}