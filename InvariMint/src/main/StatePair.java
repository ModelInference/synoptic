package main;

import dk.brics.automaton.State;

/**
 * Pair class used to avoid the miserable sense of equality between transitions
 * in dk brics (two transitions are considered equal if the have the same labels
 * and destination, regardless of whether they emanate from the same source.
 * 
 * @author Jenny
 */
public class StatePair {

    private final State first;
    private final State second;

    public StatePair(State first, State second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StatePair) {
            StatePair o = (StatePair) other;
            return first.equals(o.first) && second.equals(o.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return first.hashCode() * 17 + second.hashCode() * 111;
    }
}
