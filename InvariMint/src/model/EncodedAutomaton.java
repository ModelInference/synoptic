package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.MinimizationOperations;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import synoptic.model.event.EventType;
import synoptic.model.export.GraphExporter;

/**
 * Wrapper class for dk.brics.automaton.Automaton which provides character
 * encodings for building Automaton with EventTypes rather than characters.
 * 
 * @author Jenny
 */
public abstract class EncodedAutomaton {

    public static Logger logger;
    static {
        logger = Logger.getLogger("EncodedAutomaton");
    }

    // The Automaton wrapped with String encodings.
    private Automaton model;

    // The encoding scheme for the Automaton.
    private EventTypeEncodings encodings;

    /**
     * Constructs a new EncodedAutomaton using the given encodings. The initial
     * model accepts any sequence of EventTypes made up of EventTypes encoded in
     * the given encodings.
     */
    public EncodedAutomaton(EventTypeEncodings encodings) {
        this.encodings = encodings;
        model = encodings.getInitialModel();
    }

    public boolean subsetOf(EncodedAutomaton other) {
        return model.subsetOf(other.model);
    }

    /**
     * Returns true if the given sequence of Strings are accepted by this model.
     */
    public boolean run(List<EventType> events) {
        StringBuilder builder = new StringBuilder();
        for (EventType e : events) {
            builder.append(encodings.getEncoding(e));
        }
        return model.run(builder.toString());
    }

    /**
     * Performs Hopcroft's algorithm to minimize this Automaton.
     */
    public void minimize() {
        MinimizationOperations.minimizeHopcroft(model);
    }

    /**
     * Intersects this Automaton with a model created with the given (encoded)
     * regular expression.
     * 
     * @throws IllegalStateException
     *             if this intersection generates a DFA accepting only the empty
     *             string
     */
    public void intersectWithRE(String re) {
        intersectWithRE(re, null);
    }

    /**
     * Intersects this Automaton with a model created with the given (encoded)
     * regular expression.
     * 
     * @throws IllegalStateException
     *             if this intersection generates a DFA accepting only the empty
     *             string, exception contains the errorHint message
     */
    public void intersectWithRE(String re, String errorHint) {
        model = BasicOperations.intersection(model,
                new RegExp(re).toAutomaton());
        checkEmptyLanguage(errorHint);
    }

    /**
     * Intersects this Automaton with the given Automaton, such that this
     * Automaton is the result of intersection. Visible for testing.
     * 
     * @throws IllegalArgumentException
     *             if this Automaton is not using the same EventTypeEncoding as
     *             other
     * @throws IllegalStateException
     *             if this intersection generates a DFA accepting only the empty
     *             string
     */
    public void intersectWith(EncodedAutomaton other) {
        intersectWith(other, null);
    }

    /**
     * Intersects this Automaton with the given Automaton, such that this
     * Automaton is the result of intersection. Visible for testing.
     * 
     * @throws IllegalArgumentException
     *             if this Automaton is not using the same EventTypeEncoding as
     *             other
     * @throws IllegalStateException
     *             if this intersection generates a DFA accepting only the empty
     *             string, exception contains the errorHint message
     */
    public void intersectWith(EncodedAutomaton other, String errorHint) {
        if (!this.encodings.equals(other.encodings)) {
            throw new IllegalArgumentException(
                    "Cannot intersect Automata using different encoding schemes");
        }
        // logger.info("Performing NFA intersection");
        model = BasicOperations.intersection(model, other.model);

        // logger.info("Checking empty language");
        // checkEmptyLanguage(errorHint);

        /*
         * logger.info("Determinizing (NFA->DFA)"); model.determinize();
         */
    }

    /*
     * Throws an IllegalStateException if model is empty, attaches errorHint to
     * the exception if errorHint != null
     */
    private void checkEmptyLanguage(String errorHint) {
        /*
         * if (model.isEmpty()) { throw new IllegalStateException(
         * "DFA intersection generated the empty language" + (errorHint == null
         * ? "" : ": " + errorHint)); }
         */
    }

    /**
     * Exports this Builder's model as a Graphviz dot file and associated png.
     * Limits the alphabet of this model to currently encoded characters, such
     * that no additional Strings can be encoded for use in this model.
     * 
     * @param filename
     *            the name of the dot file
     * @param exportFinal
     *            whether to export the final or initial model
     * @throws IOException
     */
    public void exportDotAndPng(String filename) throws IOException {
        // finalizeAlphabet();
        String dot = toGraphviz();
        Writer output = new BufferedWriter(new FileWriter(new File(filename)));
        try {
            output.write(dot);
        } finally {
            output.close();
        }
        GraphExporter.generatePngFileFromDotFile(filename);
    }

    /** Constructs a Graphviz dot representation of the model. */
    public String toGraphviz() {
        StringBuilder b = new StringBuilder("digraph {\n");

        // Assign states consecutive numbers.
        Set<State> states = model.getStates();
        Map<State, Integer> stateOrdering = new HashMap<State, Integer>();
        int number = 0;
        for (State s : states) {
            stateOrdering.put(s, number++);
        }

        for (State s : states) {
            b.append("  ").append(stateOrdering.get(s));
            if (s.isAccept())
                b.append(" [shape=doublecircle,label=\"\"];\n");
            else
                b.append(" [shape=circle,label=\"\"];\n");
            if (s == model.getInitialState()) {
                b.append("  initial [shape=plaintext,label=\"\"];\n");
                b.append("  initial -> ").append(stateOrdering.get(s))
                        .append("\n");
            }
            for (Transition t : s.getTransitions()) {
                int source = stateOrdering.get(s);
                int dest = stateOrdering.get(t.getDest());
                char cur = t.getMin();
                appendDotTransition(b, encodings.getString(cur), source, dest);
                while (cur < t.getMax()) {
                    cur++;
                    appendDotTransition(b, encodings.getString(cur), source,
                            dest);
                }
            }
        }
        return b.append("}\n").toString();
    }

    /**
     * Add a transition line to the given StringBuilder with the given label,
     * from the source to the dest nodes.
     */
    private void appendDotTransition(StringBuilder b, String label, int source,
            int dest) {
        b.append("  ").append(source);
        b.append(" -> ").append(dest).append(" [label=\"");
        b.append(label);
        b.append("\"]\n");
    }

    /**
     * Sets the initial state of the wrapped Automaton. Calls
     * setDeterministic(false) and restoreInvariant() on the model since it has
     * been manipulated manually. Visible for testing.
     */
    public void setInitialState(State initial) {
        model.setInitialState(initial);
        model.setDeterministic(false);
        model.restoreInvariant();
    }

    public State getInitialState() {
        return model.getInitialState();
    }

    public EventTypeEncodings getEventEncodings() {
        return this.encodings;
    }

    /**
     * Pair class used to avoid the miserable sense of equality between
     * transitions in dk brics (two transitions are considered equal if the have
     * the same labels and destination, regardless of whether they emanate from
     * the same source.
     * 
     * @author Jenny
     */
    class StatePair {

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

}