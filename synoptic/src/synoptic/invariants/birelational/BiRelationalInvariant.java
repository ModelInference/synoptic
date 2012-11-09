package synoptic.invariants.birelational;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

/**
 * BiRelationalInvariants are invariants mined from subsets of input traces. The
 * mined subset of an input trace is the set subtraces over relation,
 * transitively connected by the ordering relation.
 * 
 * @author timjv
 */
public abstract class BiRelationalInvariant extends BinaryInvariant {

    protected String orderingRelation;
    
    protected BiGraphStateMachine biStates;

    public BiRelationalInvariant(EventType first, EventType second,
            String relation, String orderingRelation) {
        super(first, second, relation);
        this.orderingRelation = orderingRelation;
        
        Set<String> relations = new HashSet<String>();
        relations.add(relation);
        
        Set<String> orderingRelations = new HashSet<String>();
        orderingRelations.add(orderingRelation);
        
        this.biStates = new BiGraphStateMachine(relations, orderingRelations);
    }
    
    public <T extends INode<T>> void updateIncomingMap(
            Map<T, Set<String>> incomingMap, T srcMessage, T dstMessage) {
        incomingMap.put(dstMessage, getOutgoingRelations(srcMessage));
    }
    
    public <T extends INode<T>> Set<String> getOutgoingRelations(T message) {
        List<ITransition<T>> outgoingTransitions = 
                (List<ITransition<T>>) message.getAllTransitions();
        
        Set<String> outgoingRelations = new HashSet<String>();
        
        for (ITransition<T> trans : outgoingTransitions) {
            outgoingRelations.addAll(trans.getRelation());
        }
        
        return outgoingRelations;
    }
    
    
    public String getIncoming(Set<String> relations) {
        return getContainedRelation(relations);
    }
    
    public <T extends INode<T>> String getOutgoing(T message) {
        Set<String> outgoingRelations = getOutgoingRelations(message);
        return getContainedRelation(outgoingRelations);
    }
    
    private String getContainedRelation(Set<String> relations) {
        if (relations.contains(relation)) {
            return relation;
        } else if (relations.contains(orderingRelation)) {
            return orderingRelation;
        } else {
            throw new IllegalStateException("relations or invariant is invalid");
        }
    }

    
    public String getOrderingRelation() {
        return orderingRelation;
    }
    
    public boolean inProjectedGraph() {
        return biStates.in();
    }
    
    public void initializeBiStates(String relation) {
        Set<String> initialRelations = new HashSet<String>();
        initialRelations.add(Event.defTimeRelationStr);
        
        biStates.initialize(initialRelations);
    }
    
    public void transitionBiStates(String in, String out) {
        Set<String> outgoingRelations = new HashSet<String>();
        outgoingRelations.add(out);
        
        biStates.transition(in, outgoingRelations);
    }
}
