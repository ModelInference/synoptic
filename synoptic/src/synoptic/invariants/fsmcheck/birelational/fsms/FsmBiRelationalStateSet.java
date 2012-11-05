package synoptic.invariants.fsmcheck.birelational.fsms;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.FsmStateSet;
import synoptic.main.SynopticMain;
import synoptic.model.interfaces.INode;

public abstract class FsmBiRelationalStateSet<T extends INode<T>> extends FsmStateSet<T> {
  
    // We have not yet seen the projected graph.
    private boolean beforeProjectedGraph;
    // Whether or not we are traversing the projected graph.
    private boolean inProjectedGraph;
    /* Make sure beforeProjectedGraph and inProjectedGraph get
     * set before calling transition.
     */
    private boolean initialized;
    
    private Set<String> relations;
    private Set<String> closureRelations;
    
    private FsmStateSet<T> fsms;
    
    protected List<BinaryInvariant> invariants;
    
    public FsmBiRelationalStateSet(FsmStateSet<T> fsms, List<BinaryInvariant> invariants) {
        super(new ArrayList<BinaryInvariant>(), 0);
        
        this.initialized = false; 
        
        this.relations = new HashSet<String>();
        this.closureRelations = new HashSet<String>();
        
        setFsms(fsms);
        
        this.invariants = invariants;
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
    
    public boolean tracksClosureRelation(String closureRelation) {
        return closureRelations.contains(closureRelation);
    }
    
    // --relation--> (input)
    @Override
    public void transition(T input, String relation, Set<String> outgoingRelations) {
        SynopticMain synopticMain = SynopticMain.getInstanceWithExistenceCheck();
        
        boolean multipleRelationsEnabled = synopticMain.options.multipleRelations;
        
        if (!multipleRelationsEnabled) {
            throw new IllegalStateException("Multiple relations disabled.");
        }
        
        if (!initialized) {
            throw new IllegalStateException("Fsms uninitialized.");
        }
        
        if (outgoingRelations == null) {
            throw new NullPointerException();
        }
        
        if (!(tracksRelation(relation) || tracksClosureRelation(relation))) {
            throw new IllegalStateException("Relation: " + relation +
                    " is not being tracked by this StateSet");
        } 
        
        boolean relationsIntersectOutgoing = setsIntersect(relations, outgoingRelations);
        
        if (beforeProjectedGraph) {
            if (relationsIntersectOutgoing) {
                beforeProjectedGraph = false;
                inProjectedGraph = true;
                setInitial(input);
            }
        } else if (inProjectedGraph) {
            if (!relations.contains(relation)) {
                inProjectedGraph = false;
            } else {
                transition(input);
            }
        } else { // not in projected graph
            if (relationsIntersectOutgoing) {
                inProjectedGraph = true;
                transition(input);
            }
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
    
    /**
     * Use setInitial(T x, Set<String> outgoingRelations) when doing
     * multi-relational model checking or else you will encounter 
     * initialization bugs between setInitial calls.
     */
    @Override
    public void setInitial(T x) {
        fsms.setInitial(x);
    }
    
    /**
     * Must check to see if we are already in the projected graph on
     * the initial node.
     */
    @Override
    public void setInitial(T x, Set<String> outgoingRelations) {
        initialized = true;
        if (setsIntersect(relations, outgoingRelations)) {
            beforeProjectedGraph = false;
            inProjectedGraph = true;
            setInitial(x);
        } else {
            beforeProjectedGraph = true;
            inProjectedGraph = false;
        }
    }
    
    protected void setFsms(FsmStateSet<T> fsms) {
        this.fsms = fsms;
    }

    @Override
    public boolean isFail() {
        return beforeProjectedGraph || fsms.isFail();
    }

    @Override
    public BitSet whichFail() {
        return fsms.whichFail();
    }

    @Override
    public BitSet whichPermanentFail() {
        return fsms.whichPermanentFail();
    }

    @Override
    public void transition(T input) {
        fsms.transition(input);
    }
}
