package model;

import java.util.HashMap;
import java.util.Map;

import dk.brics.automaton.State;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.Transition;

/**
 * Translates a Synoptic model from PartitionGraph to EncodedAutomaton DFA.
 * 
 * @author Jenny
 */
public class SynopticModel extends EncodedAutomaton {

    public SynopticModel(PartitionGraph synoptic, EventTypeEncodings encodings) {
        super(encodings);

        // The set of partitions we've visited, mapped to the source state for
        // each partition's generated transition.
        Map<Partition, State> preEventStates = new HashMap<Partition, State>();

        // We manually construct the DFA from this initial state.
        State initial = new State();

        // Convert all partitions starting from the INITIAL node.
        for (Partition initialPartition : synoptic.getDummyInitialNodes()) {
            convert(initialPartition, initial, preEventStates, encodings);
        }

        // Set the automaton to our newly constructed model.
        super.setInitialState(initial);
    }

    /**
     * Converts the given partition into a transition from the given prev state
     * to either a fresh state or if we've explored this partition before the
     * state that this partition's original transition led to. If we have not
     * explored this partition before we then explore this partition's children.
     */
    private void convert(Partition eventNode, State prev,
            Map<Partition, State> preEventStates, EventTypeEncodings encodings) {

        if (preEventStates.containsKey(eventNode)) {

            // We've already seen eventNode, and should transition from prev to
            // the pre-state stored in preEventStates.
            dk.brics.automaton.Transition t = new dk.brics.automaton.Transition(
                    encodings.getEncoding(eventNode.getEType()),
                    preEventStates.get(eventNode));

            prev.addTransition(t);

        } else {

            // We haven't seen this partition before, and so should transition
            // to a new state.
            State next = new State();
            dk.brics.automaton.Transition t = new dk.brics.automaton.Transition(
                    encodings.getEncoding(eventNode.getEType()), next);
            prev.addTransition(t);

            // Update map of partitions we've seen.
            preEventStates.put(eventNode, next);

            // The 'next' state should be an accept state if eventNode is
            // TERMINAL.
            if (eventNode.getEType().isTerminalEventType()) {
                next.setAccept(true);
            }

            // Convert child partitions, who should transition from the 'next'
            // state.
            for (Transition<Partition> synTrans : eventNode.getTransitions()) {
                convert(synTrans.getTarget(), next, preEventStates, encodings);
            }
        }
    }
}