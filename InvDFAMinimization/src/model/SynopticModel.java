package model;

import java.util.HashMap;
import java.util.Map;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.Transition;

public class SynopticModel extends EncodedAutomaton {
    private Map<Partition, State> preEventState;

    public SynopticModel(PartitionGraph synoptic) {
        model = new Automaton();

        preEventState = new HashMap<Partition, State>();

        State initial = model.getInitialState();

        for (Partition initialPartition : synoptic.getDummyInitialNodes()) {
            convert(initialPartition, initial);
        }
    }

    /*
     * TODO: incorporate kTails algorithm for ease in modifying k later.
     * 
     * @param eventNode the partition we're currently interested in converting
     * 
     * @param prev the state we're transitioning from, will need to make a new
     * transition from prev represented by the eventNode partition.
     * 
     * @param visited the set of partitions we've already seen
     * 
     * @param preEventState mapping from previously seen partitions to their
     * source states
     */
    private void convert(Partition eventNode, State prev) {

        // We've already seen eventNode, and should transition from prev to the
        // pre-state stored in preEventState.
        if (preEventState.containsKey(eventNode)) {

            dk.brics.automaton.Transition t = new dk.brics.automaton.Transition(
                    super.getEncoding(eventNode.getEType().toString()),
                    preEventState.get(eventNode));
            prev.addTransition(t);

        } else {

            State next = new State();

            dk.brics.automaton.Transition t = new dk.brics.automaton.Transition(
                    super.getEncoding(eventNode.getEType().toString()), next);

            preEventState.put(eventNode, next);
            prev.addTransition(t);

            if (eventNode.getEType().isTerminalEventType()) {
                next.setAccept(true);
            }
            for (Transition<Partition> synTrans : eventNode.getTransitions()) {
                convert(synTrans.getTarget(), next);
            }
        }
    }
}