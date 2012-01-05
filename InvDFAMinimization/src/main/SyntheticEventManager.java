package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.InvModel;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysImmediatelyFollowedInvariant;
import synoptic.invariants.AlwaysImmediatelyPrecededInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.NeverImmediatelyFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.TraceParser;
import synoptic.model.EventType;
import synoptic.model.StringEventType;

public class SyntheticEventManager {
    private EventTypeEncodings encodings;

    private Map<EventType, Set<EventType>> CIFbys;

    private Set<EventType> syntheticEvents;

    public SyntheticEventManager(Set<EventType> allEvents,
            Map<EventType, Set<EventType>> CIFbys) {

        this.CIFbys = CIFbys;

        // Generate synthetic events
        this.syntheticEvents = new HashSet<EventType>();

        for (EventType a : CIFbys.keySet()) {
            for (EventType b : CIFbys.get(a)) {
                EventType synEvent = new StringEventType(a.toString()
                        + b.toString());
                allEvents.add(synEvent);
                syntheticEvents.add(synEvent);
            }
        }

        encodings = new EventTypeEncodings(allEvents);
    }

    public EventTypeEncodings getEncodings() {
        return encodings;
    }

    public EncodedAutomaton removeSyntheticEvents(EncodedAutomaton dfa) {
        dfa.setInitialState(explore(dfa.getInitialState(),
                new HashMap<State, State>()));
        dfa.minimize();
        return dfa;
    }

    public EncodedAutomaton intersectWithSyntheticInvariants(
            EncodedAutomaton dfa, TemporalInvariantSet minedInvariants,
            TemporalInvariantSet initialSyntheticInvs) {
        for (EventType a : CIFbys.keySet()) {
            for (EventType b : CIFbys.get(a)) {
                TemporalInvariantSet syntheticInvariants = new TemporalInvariantSet();

                dfa.intersectWith(new InvModel(
                        new NeverImmediatelyFollowedInvariant(a, b,
                                TraceParser.defaultRelation), encodings));

                EventType tempInv = new StringEventType(a.toString()
                        + b.toString());

                dfa.intersectWith(new InvModel(
                        new AlwaysImmediatelyPrecededInvariant(a, tempInv,
                                TraceParser.defaultRelation), encodings));
                dfa.intersectWith(new InvModel(
                        new AlwaysImmediatelyFollowedInvariant(tempInv, b,
                                TraceParser.defaultRelation), encodings));

                dfa.minimize();

                for (ITemporalInvariant inv : minedInvariants) {
                    if (inv instanceof NeverFollowedInvariant) {
                        if ((inv.getFirst().equals(a) || inv.getFirst().equals(
                                b))
                                && !inv.getSecond().equals(b)) {
                            syntheticInvariants.add(new NeverFollowedInvariant(
                                    tempInv, inv.getSecond(),
                                    TraceParser.defaultRelation));
                        } else if (inv.getSecond().equals(a)
                                || inv.getSecond().equals(b)) {
                            syntheticInvariants.add(new NeverFollowedInvariant(
                                    inv.getFirst(), tempInv,
                                    TraceParser.defaultRelation));
                        }
                    } else if (inv instanceof AlwaysPrecedesInvariant
                            && (inv.getSecond().equals(a) || inv.getSecond()
                                    .equals(b))) {
                        syntheticInvariants.add(new AlwaysPrecedesInvariant(inv
                                .getFirst(), tempInv,
                                TraceParser.defaultRelation));
                    } else if (inv instanceof AlwaysFollowedInvariant
                            && (inv.getFirst().equals(a) || inv.getSecond()
                                    .equals(b))) {
                        syntheticInvariants.add(new AlwaysFollowedInvariant(
                                tempInv, inv.getSecond(),
                                TraceParser.defaultRelation));
                    }
                }
                dfa.intersectWith(DFAMain.getMinModelFromInvs(
                        syntheticInvariants, encodings));
                dfa.minimize();
            }
        }

        dfa.intersectWith(DFAMain.getMinModelFromInvs(initialSyntheticInvs,
                encodings));
        dfa.minimize();
        return dfa;
    }

    /**
     * To remove synthetic edges, the idea is to walk the automaton and
     * replicate each node we'd like to appear in the final model. Edges that
     * are not synthetic are copied for replicated nodes as is. Synthetic xy-y
     * pairs of edges are replaced with a single y edge. Synthetic xx-x edges
     * are a special case, in which a self x loop must be added to the current
     * replicated node and non xx outgoing edges from the x target node are
     * copied for the replica node.
     */
    private State explore(State current, Map<State, State> seenStates) {

        if (seenStates.containsKey(current)) {
            return seenStates.get(current);
        }

        State replica = new State();
        if (current.isAccept()) {
            replica.setAccept(true);
        }
        seenStates.put(current, replica);

        for (Entry<String, State> transition : getTransitions(current)
                .entrySet()) {
            String transitionLabel = transition.getKey();
            State transitionDestination = transition.getValue();
            EventType transitionEvent = new StringEventType(transitionLabel);

            if (!syntheticEvents.contains(transitionEvent)) {
                // This edge is not synthetic, and should be replicated.
                copyTransition(replica, transitionDestination, transitionEvent,
                        seenStates);
            } else {
                // We have a synthetic edge. The edge is xx if the first
                // half of transition matches the second half.
                String firstHalf = transitionLabel.substring(0,
                        transitionLabel.length() / 2);

                if (!firstHalf.equals(transitionLabel.substring(transitionLabel
                        .length() / 2))) {
                    // This is a synthetic xy event.
                    replaceXYtransition(transitionLabel, transitionDestination,
                            seenStates, replica);
                } else {
                    // This is an xx synthetic edge.
                    replica.addTransition(new Transition(encodings
                            .getEncoding(new StringEventType(firstHalf)),
                            replica));

                    // There is no case where there should be any transitions
                    // out of the transitionDestination other than an x
                    // transition.
                    for (Entry<String, State> nextTransition : getTransitions(
                            transitionDestination).entrySet()) {
                        String nextTransitionLabel = nextTransition.getKey();
                        if (!nextTransitionLabel.equals(firstHalf)) {
                            throw new IllegalStateException("The transition "
                                    + nextTransitionLabel
                                    + " should not exist here");
                        }

                        for (Entry<String, State> thirdTransition : getTransitions(
                                nextTransition.getValue()).entrySet()) {
                            String thirdTransitionLabel = thirdTransition
                                    .getKey();
                            if (!thirdTransitionLabel.equals(transition)) {
                                // Process this transition as a new
                                // edge for replica.
                                EventType thirdEvent = new StringEventType(
                                        thirdTransitionLabel);
                                if (!syntheticEvents.contains(thirdEvent)) {
                                    // This edge is not synthetic,
                                    // and should be replicated.
                                    copyTransition(replica,
                                            thirdTransition.getValue(),
                                            thirdEvent, seenStates);
                                } else {
                                    // This is a synthetic xy event.
                                    replaceXYtransition(thirdTransitionLabel,
                                            thirdTransition.getValue(),
                                            seenStates, replica);
                                }
                            }
                        }
                    }
                }
            }
        }
        return replica;
    }

    private void replaceXYtransition(String transitionLabel,
            State transitionDestination, Map<State, State> seenStates,
            State replica) {

        for (Entry<String, State> secondTransition : getTransitions(
                transitionDestination).entrySet()) {

            String secondTransitionLabel = secondTransition.getKey();
            if (!secondTransitionLabel.equals(transitionLabel
                    .substring(transitionLabel.length()
                            - secondTransitionLabel.length()))) {
                throw new IllegalStateException("The transition "
                        + secondTransitionLabel + " should not exist here");
            }

            copyTransition(replica, secondTransition.getValue(),
                    new StringEventType(secondTransitionLabel), seenStates);
        }
    }

    private void copyTransition(State replica, State transitionDestination,
            EventType transitionEvent, Map<State, State> seenStates) {
        State dest = explore(transitionDestination, seenStates);
        replica.addTransition(new Transition(encodings
                .getEncoding(transitionEvent), dest));
    }

    private Map<String, State> getTransitions(State state) {
        Map<String, State> transitions = new HashMap<String, State>();
        for (Transition t : state.getTransitions()) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                transitions.put(encodings.getString(c), t.getDest());
            }
        }
        return transitions;
    }
}
