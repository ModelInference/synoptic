package model;

import java.util.HashMap;
import java.util.Map;

import dk.brics.automaton.State;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.Transition;

public class SynopticModel extends EncodedAutomaton {

    public SynopticModel(PartitionGraph synoptic) {

        Map<Partition, State> preEventStates = new HashMap<Partition, State>();

        State initial = new State();

        for (Partition initialPartition : synoptic.getDummyInitialNodes()) {
            convert(initialPartition, initial, preEventStates);
        }

        super.setInitialState(initial);
    }

    /*
     * Converts the given partition into a transition from the given prev state
     * to either a fresh state or the state before this partition's original
     * transition if we've explored this partition before. If we have not
     * explored this partition before, we then explore this partition's
     * children.
     */
    private void convert(Partition eventNode, State prev,
            Map<Partition, State> preEventStates) {

        if (preEventStates.containsKey(eventNode)) {
            // We've already seen eventNode, and should transition from prev to
            // the pre-state stored in preEventStates.

            dk.brics.automaton.Transition t = new dk.brics.automaton.Transition(
                    super.getEncoding(eventNode.getEType().toString()),
                    preEventStates.get(eventNode));

            prev.addTransition(t);

        } else {

            State next = new State();

            dk.brics.automaton.Transition t = new dk.brics.automaton.Transition(
                    super.getEncoding(eventNode.getEType().toString()), next);

            preEventStates.put(eventNode, next);
            prev.addTransition(t);

            if (eventNode.getEType().isTerminalEventType()) {
                next.setAccept(true);
            }

            // Convert child partitions, who should transition from the 'next'
            // state.
            for (Transition<Partition> synTrans : eventNode.getTransitions()) {
                convert(synTrans.getTarget(), next, preEventStates);
            }
        }
    }
}