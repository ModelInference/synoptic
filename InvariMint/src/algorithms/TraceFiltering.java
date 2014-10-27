package algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.EncodedAutomaton;
import model.EventTypeEncodings;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;

import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.EventType;

public class TraceFiltering {

    /**
     * Removes edges from the provided dfa that cannot be mapped to any trace in
     * the input trace graph g.
     */
    public static void removeSpuriousEdges(EncodedAutomaton dfa,
            ChainsTraceGraph g, EventTypeEncodings encodings,
            EventType initialEvent, EventType terminalEvent) {
        // TODO: just build the new dfa -- no seen map

        Map<StatePair, Set<Character>> seenTransitions = new HashMap<StatePair, Set<Character>>();
        EventNode initNode = g.getDummyInitialNode();

        // Iterate through all the traces -- each transition from the INITIAL
        // node holds a single trace.
        for (EventNode curNode : initNode.getAllSuccessors()) {
            // Set curState to the state immediately following the INITIAL
            // transition.
            State curState = dfa.getInitialState();
            curState = fetchDestination(curState, encodings, initialEvent,
                    seenTransitions);

            while (curNode.getAllTransitions().size() != 0) {

                curState = fetchDestination(curState, encodings,
                        curNode.getEType(), seenTransitions);

                if (curState == null) {
                    throw new IllegalStateException(
                            "Unable to fetch valid destination for ");
                }

                // Move on to the next node in the trace.
                assert (curNode.getAllTransitions().size() > 0);
                curNode = curNode.getAllTransitions().get(0).getTarget();
            }

            fetchDestination(curState, encodings, terminalEvent,
                    seenTransitions);
        }

        dfa.setInitialState(replicate(seenTransitions, dfa.getInitialState(),
                new HashMap<State, State>(), encodings));

        // dfa.minimize();
    }

    /**
     * Given a State and an EventType, returns the State to which the source
     * state would transition given the Event if such a state exists. Also
     * updates the seenTransitions map with the transition used to find the
     * destination.
     */
    private static State fetchDestination(State source,
            EventTypeEncodings encodings, EventType currentEvent,
            Map<StatePair, Set<Character>> seenTransitions) {

        for (Transition t : source.getTransitions()) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                if (currentEvent.toString().equals(encodings.getString(c))) {
                    StatePair curTransition = new StatePair(source, t.getDest());
                    if (!seenTransitions.containsKey(curTransition)) {
                        seenTransitions.put(curTransition,
                                new HashSet<Character>());
                    }
                    seenTransitions.get(curTransition).add(new Character(c));
                    return t.getDest();
                }
            }
        }
        return null;
    }

    /**
     * Recursively replicates the given automata starting from the current state
     * but eliminates transitions that were not 'seen'.
     */
    private static State replicate(
            Map<StatePair, Set<Character>> seenTransitions, State current,
            Map<State, State> visited, EventTypeEncodings encodings) {

        if (visited.containsKey(current)) {
            return visited.get(current);
        }

        State replica = new State();
        replica.setAccept(current.isAccept());
        visited.put(current, replica);

        for (Transition t : current.getTransitions()) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                StatePair curTransition = new StatePair(current, t.getDest());
                if (seenTransitions.containsKey(curTransition)
                        && seenTransitions.get(curTransition).contains(
                                new Character(c))) {
                    replica.addTransition(new Transition(c, replicate(
                            seenTransitions, t.getDest(), visited, encodings)));
                }
            }
        }
        return replica;
    }

}
