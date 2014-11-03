package model;

import java.util.HashMap;
import java.util.Map;

import dk.brics.automaton.State;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;

/**
 * Translates a Synoptic-style partition graph model to an EncodedAutomaton DFA
 * model type.
 * 
 */
public class PartitionGraphAutomaton extends EncodedAutomaton {

    public PartitionGraphAutomaton(PartitionGraph pGraph,
            EventTypeEncodings encodings) {
        super(encodings);

        // The set of partitions we've visited, mapped to the source state for
        // each partition's generated transition.
        Map<Partition, State> preEventStates = new HashMap<Partition, State>();

        // We manually construct the DFA from the initial state.
        State initial = new State();

        // Convert all partitions starting from the INITIAL node.
        Partition initialPartition = pGraph.getDummyInitialNode();
        convert(initialPartition, initial, preEventStates);

        // Set the automaton to our newly constructed model.
        super.setInitialState(initial);

        // This minimization step will first determinize the model -- from
        // the dk brics documentation.
        // this.minimize();
    }

    /**
     * Converts the given partition into a transition from the given prev state
     * to either a fresh state or if we have not explored this partition before,
     * otherwise transition to the state that this partition's original
     * transition led to. And, if we have not explored this partition before,
     * then we explore this partition's children.
     */
    private void convert(Partition eventNode, State prev,
            Map<Partition, State> preEventStates) {

        if (preEventStates.containsKey(eventNode)) {

            // We've already seen eventNode, and should transition from prev to
            // the pre-state stored in preEventStates.
            dk.brics.automaton.Transition t = new dk.brics.automaton.Transition(
                    encodings.getEncoding(eventNode.getEType()),
                    preEventStates.get(eventNode));

            prev.addTransition(t);

        } else {

            // We haven't seen this partition before, and so we should
            // transition to a new state.
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
            for (Partition child : eventNode.getAllSuccessors()) {
                convert(child, next, preEventStates);
            }
        }
    }
}
