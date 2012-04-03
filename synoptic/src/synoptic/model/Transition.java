package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.FTotalTime;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;

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
    protected final List<ITime> allDeltas = new ArrayList<ITime>();

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

    /**
     * Create a new transition.
     * 
     * @param source
     *            source node
     * @param target
     *            target node
     * @param relation
     *            the label of the transition
     * @param delta
     * 			  an associated time delta for transition
     */
    public Transition(NodeType source, NodeType target, String relation,
            ITime delta) {
        assert source != null;
        assert target != null;
        assert relation != null;

        this.source = source;
        this.target = target;
        this.relation = relation;
        this.delta = delta;

        if (delta != null) {
            this.allDeltas.add(delta);
        }
    }

    /**
     * Adds a change in time (delta) to the total list.
     * 
     * @param delta
     */
    public void addDelta(ITime delta) {
        assert delta != null;
        this.allDeltas.add(delta);
    }

    public void addAllDeltas(Collection<ITime> deltas) {
        assert deltas != null;
        this.allDeltas.addAll(deltas);
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

    public ITime getDelta() {
        return delta;
    }

    public List<ITime> getAllDeltas() {
        return allDeltas;
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
    
    /**
     * @return 
     * 		mode delta time for transition, null if transition has zero delta times.
     */
    public ITime computeModeDelta() {
    	
    	if (this.allDeltas.isEmpty()) {
            return null;
        }
    	
        Map<ITime, Integer> counts = new HashMap<ITime, Integer>();
        ITime mostCommon = null;
        int max = 1;
        for (ITime delta : this.allDeltas) {
            Integer count = counts.get(delta);

            if (count == null) {
                count = 1;
            } else {
                count++;
            }

            if (count > max) {
                mostCommon = delta;
                max = count;
            }

            counts.put(delta, count);
        }

        return mostCommon;
    }
    
    /**
     * @return 
     * 		median delta time for transition, null if transition has zero delta times.
     */
    public ITime computeMedianDelta() {
        
        if (this.allDeltas.isEmpty()) {
            return null;
        }
        
        Collections.sort(this.allDeltas);

        // Simple case of picking about the middle every time.
        // Calculate between the halfway values if the size
        // of the list is even.
        if (this.allDeltas.size() % 2 == 0) {
        	int mid = this.allDeltas.size() / 2;
        	ITime t1 = allDeltas.get(mid - 1);
        	ITime t2 = allDeltas.get(mid);
        	
        	if (t1 instanceof DTotalTime) {
        		double d = (((DTotalTime) t1).time - ((DTotalTime) t2).time) / 2;
        		return new DTotalTime(d);
        	} else if (t1 instanceof FTotalTime) {
        		float f = (((FTotalTime) t1).time - ((FTotalTime) t2).time) / 2;
        		return new FTotalTime(f);
        	} else if (t1 instanceof ITotalTime) {
        		int i = (((ITotalTime) t1).time - ((ITotalTime) t2).time) / 2;
        		return new ITotalTime(i);
        	}
        }
        
        return this.allDeltas.get((this.allDeltas.size() / 2));
    }
    
    /**
     * @return
     * 		mean delta time for transition, null if transition has zero delta times.
     */
    //TODO hackish implementation, improve
    public ITime computeMeanDelta() {
    	
    	if (this.allDeltas.isEmpty()) {
            return null;
        }
    	
    	boolean isDTotalTime = false;
    	boolean isFTotalTime = false;
    	boolean isITotalTime = false;
    	
    	ITime t1 = allDeltas.get(0);
    	
    	if (t1 instanceof DTotalTime) {
    		isDTotalTime = true;
    	} else if (t1 instanceof FTotalTime) {
    		isFTotalTime = true;
    	} else if (t1 instanceof ITotalTime) {
    		isITotalTime = true;
    	} 
    	
    	double d = 0;
    	float f = 0;
    	int i = 0;
    	
    	for (ITime t : allDeltas) {
    		if (isDTotalTime) {
    			d += ((DTotalTime) t).time;
    		} else if (isFTotalTime) {
    			f += ((FTotalTime) t).time;
    		} else if (isITotalTime) {
    			i += ((ITotalTime) t).time;
    		}
    	}
    	
    	if (isDTotalTime) {
    		return new DTotalTime(d / allDeltas.size());
    	} else if (isFTotalTime) {
    		return new FTotalTime(f / allDeltas.size());
    	} else {
    		return new ITotalTime(i / allDeltas.size());
    	}
    		
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