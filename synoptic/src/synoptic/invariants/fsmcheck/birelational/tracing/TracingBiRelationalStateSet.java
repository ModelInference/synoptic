package synoptic.invariants.fsmcheck.birelational.tracing;

import java.util.HashSet;
import java.util.Set;

import synoptic.invariants.fsmcheck.TracingStateSet;
import synoptic.main.SynopticMain;
import synoptic.model.interfaces.INode;

public abstract class TracingBiRelationalStateSet<T extends INode<T>> extends TracingStateSet<T> {

    // We have not yet seen the projected graph.
    protected boolean beforeProjectedGraph;
    // Whether or not we are traversing the projected graph.
    protected boolean inProjectedGraph;
    /* Make sure beforeProjectedGraph and inProjectedGraph get
     * set before calling transition.
     */
    protected boolean initialized;
    
    protected Set<String> relations;
    protected Set<String> closureRelations;
    
    protected TracingStateSet<T> tracingSet;
    
    /* Keeps track of the potential counter-example while the stateset has not
     * yet seen the projected graph.
     */
    protected HistoryNode preHistory;
    
    public TracingBiRelationalStateSet(TracingStateSet<T> tracingSet) {
        this.initialized = false; 
        
        this.relations = new HashSet<String>();
        this.closureRelations = new HashSet<String>();
        
        this.preHistory = null;
        
        setTracingSet(tracingSet);
    }
    
    protected void setTracingSet(TracingStateSet<T> tracingSet){
        this.tracingSet = tracingSet;
    }
    
    public void addRelation(String relation) {
        relations.add(relation);
    }
    
    public boolean tracksRelation(String relation) {
        return relations.contains(relation);
    }
    
    public void addClosureRelation(String closureRelation) {
        closureRelations.add(closureRelation);
    }
    
    public void addAllClosureRelations(Set<String> closureRelations) {
        this.relations.addAll(closureRelations);
    }
    
    public void setPreHistory(HistoryNode preHistory) {
        this.preHistory = preHistory;
    }
    
    public boolean tracksClosureRelation(String closureRelation) {
        return closureRelations.contains(closureRelation);
    }
    

    
    public void extendPreHistory(T x) {
        if (preHistory == null) {
            preHistory = new HistoryNode(x, null, 1);
        } else {
            preHistory = extendIfNonNull(x, preHistory);
        }
    }
    
    public static boolean setsIntersect(Set<String> a, Set<String> b) {
        for (String aString : a) {
            if (b.contains(aString)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public HistoryNode failpath() {
        if (!beforeProjectedGraph) {
            return tracingSet.failpath();
        } else {
            return null;
        }
    }

    @Override
    public void mergeWith(TracingStateSet<T> other) {
        if (other instanceof TracingBiRelationalStateSet) {
            TracingBiRelationalStateSet<T> o = (TracingBiRelationalStateSet<T>) other;
            tracingSet.mergeWith(o.getMonoTracingSet());
            this.preHistory = yieldShorter(preHistory, o.preHistory);
            // The merge can cause problems because we might merge an
            // into an uninitialized set which gets its state wiped later on
            // actual initialization.
        } else {
            throw new IllegalArgumentException("Cannot merge mono and multirelational state sets");
        }
    }

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
        if (other instanceof TracingBiRelationalStateSet) {
            TracingBiRelationalStateSet<T> o = (TracingBiRelationalStateSet<T>) other;
            return tracingSet.isSubset(o.getMonoTracingSet()) && 
                    beforeProjectedGraph == o.beforeProjectedGraph && 
                    inProjectedGraph == o.inProjectedGraph; 
        } else {
            throw new IllegalArgumentException("Cannot compare mono and multirelational state sets");
        }
    }

    @Override
    public String toString() {
        return tracingSet.toString();
    }
    
    @Override
    public boolean isFail() {
        return !beforeProjectedGraph && super.isFail();
    }
    
    public TracingStateSet<T> getMonoTracingSet() {
        return tracingSet;
    }
    
    public abstract TracingBiRelationalStateSet<T> copy();
}

    
