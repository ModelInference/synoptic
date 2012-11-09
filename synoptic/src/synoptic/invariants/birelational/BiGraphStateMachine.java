package synoptic.invariants.birelational;

import java.util.HashSet;
import java.util.Set;

public class BiGraphStateMachine {

    public enum State {
        BEFORE, IN, SEENBUTOUT
    }
    
    private HashSet<String> relations;
    private HashSet<String> closureRelations;
    
    private State state;
    
    private boolean initialized;
    
    public BiGraphStateMachine(Set<String> relations, Set<String> closureRelations) {
        this.relations = new HashSet<String>();
        this.relations.addAll(relations);
        this.closureRelations = new HashSet<String>();
        this.closureRelations.addAll(closureRelations);
        initialized = false;
    }
    
    public void transition(String incoming, Set<String> outgoing) {
        
        if (!initialized) {
            throw new IllegalStateException("Unitialized");
        }
        
        if (state == State.BEFORE || state == State.SEENBUTOUT) {
            if (setsIntersect(relations, outgoing)) {
                state = State.IN;
            }
        } else if (state == State.IN) {
            if (!relations.contains(incoming)) {
                state = State.SEENBUTOUT;
            }
        } else {
            throw new IllegalStateException(state + " is not a valid state");
        }
    }
    
    public void initialize(Set<String> outgoing) {
        
        if (setsIntersect(relations, outgoing)) {
            state = State.IN;
        } else {
            state = State.BEFORE;
        }
        
        initialized = true;
    }
    
    public boolean before() {
        return state == State.BEFORE;
    }
    
    public boolean in() {
        return state == State.IN;
    }
    
    public boolean seenButOut() {
        return state == State.SEENBUTOUT;
    }
    
    public static boolean setsIntersect(Set<String> a, Set<String> b) {
        for (String aString : a) {
            if (b.contains(aString)) {
                return true;
            }
        }
        return false;
    }
    
}
