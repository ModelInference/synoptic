package model;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import synoptic.model.interfaces.INode;

/**
 * Export the Invarimint final model in Labeled Transition System (LTS) format.
 * The main entry method is exportLts(...).
 */
public class LtsExporter {
    /**
     * Export the Labeled Transition System (LTS) representation of a model to
     * the filename specified
     * 
     * @param baseFilename
     *            The filename to which the LTS should be written sans file
     *            extension
     * @param model
     *            The model to output
     * @param encodings
     *            The model's event type encodings
     */
    public static <T extends INode<T>> void exportLTS(String baseFilename,
            Automaton model, EventTypeEncodings encodings) {

        // Output the final model as in LTS format
        try {
            PrintWriter output = new PrintWriter(baseFilename + ".lts");
            output.print(buildLTS(model, encodings));
            output.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build the actual LTS output and return as a string.
     * 
     * @param model
     *            The model to output
     * @param encodings
     *            The model's event type encodings
     */
    private static String buildLTS(Automaton model, EventTypeEncodings encodings) {
        // Will eventually contain fully-built LTS
        StringBuilder ltsContent = new StringBuilder();

        // Holds a unique ID for each state
        HashMap<State, Integer> stateIDs = new HashMap<>();

        int nextID = 0;
        State initialState = null;

        // Assign unique state IDs, and find the initial state
        for (State state : model.getStates()) {
            // Skip pre-initial state; store initial state
            if (state == model.getInitialState()) {
                initialState = state.getTransitions().iterator().next()
                        .getDest();
                continue;
            }

            // Store ID
            stateIDs.put(state, nextID++);
        }

        // Print entry to initial state
        String init = String.format("Invarimint = S%d",
                stateIDs.get(initialState));
        ltsContent.append(init);

        // Initialize fields for BFT (breadth-first traversal) over all
        // partitions
        LinkedBlockingQueue<State> bftQueue = new LinkedBlockingQueue<>();
        bftQueue.add(initialState);
        HashSet<State> visited = new HashSet<>();

        // Perform a BFT over all states, storing each in the LTS representation
        while (!bftQueue.isEmpty()) {
            // Get a state
            State state = bftQueue.poll();

            ltsContent
                    .append(String.format(",\n\nS%d = ", stateIDs.get(state)));

            // Loop over all outgoing transitions
            boolean transitionAdded = false;
            for (Transition nextTrans : state.getTransitions()) {
                // Skip terminal transitions
                if (encodings.getString(nextTrans.getMin()).equals("TERMINAL")) {
                    continue;
                }

                // Output formatting just before this transition
                if (transitionAdded) {
                    ltsContent.append("\n\t\t| ");
                } else {
                    ltsContent.append("(");
                }
                transitionAdded = true;

                // Output transition to the next state
                char nextTransChar = nextTrans.getMin();
                String eventType = encodings.getString(nextTransChar);
                State dest = nextTrans.getDest();
                ltsContent.append(eventType).append(" -> S")
                        .append(stateIDs.get(dest));

                // Standard BFT: ensure states are visited exactly once
                if (!visited.contains(dest)) {
                    visited.add(dest);
                    bftQueue.add(dest);
                }
            }

            // Close if there were any non-terminal transitions, else this is a
            // STOP state
            if (transitionAdded) {
                ltsContent.append(")");
            } else {
                ltsContent.append("STOP");
            }
        }

        // Write concluding line
        ltsContent.append(".\n\n||MTS_Invarimint = (Invarimint).\n");

        return ltsContent.toString();
    }
}
