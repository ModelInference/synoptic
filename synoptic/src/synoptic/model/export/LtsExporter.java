package synoptic.model.export;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

/**
 * Export the Synoptic final model in Labeled Transition System (LTS) format.
 * The main entry method is exportLts(...).
 */
public class LtsExporter {
    /**
     * Export the Labeled Transition System (LTS) representation of a partition
     * graph to the filename specified
     * 
     * @param baseFilename
     *            The filename to which the LTS should be written sans file
     *            extension
     * @param graph
     *            The partition graph to output
     * @param systemName
     *            The name of the system that made the model, e.g., Synoptic
     */
    public static <T extends INode<T>> void exportLTS(String baseFilename,
            IGraph<T> graph, String systemName) {

        // The graph must be a partition graph
        assert graph instanceof PartitionGraph;
        PartitionGraph pGraph = (PartitionGraph) graph;

        // Output the final model map as an LTS model
        try {
            PrintWriter output = new PrintWriter(baseFilename + ".lts");
            output.print(buildLTS(pGraph, systemName));
            output.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * From a given state, whether ALL, only SOME, or NONE of the outgoing
     * transitions go to terminal.
     */
    enum TransitionsOut {
        AllToTerminal, SomeToTerminal, NoneToTerminal
    }

    /**
     * Build the actual LTS output and return as a string.
     * 
     * @param pGraph
     *            The partition graph to output
     * @param systemName
     *            The name of the system that made the model, e.g., Synoptic
     */
    private static String buildLTS(PartitionGraph pGraph, String systemName) {
        // Will eventually contain fully-built LTS
        StringBuilder ltsContent = new StringBuilder();

        // Holds a unique state ID for each partition
        HashMap<Partition, Integer> partIDs = new HashMap<>();

        // For each partition, whether ALL, only SOME, or NONE of its outgoing
        // transitions go to terminal
        HashMap<Partition, TransitionsOut> goesToTerminal = new HashMap<>();

        int nextID = 1;
        Partition initialPart = null;

        // Give each partition a unique state ID, and find the INITIAL partition
        for (Partition part : pGraph.getNodes()) {
            // Terminal is always S0
            if (part.isTerminal()) {
                partIDs.put(part, 0);
                continue;
            }

            // Check if this partition has any transitions TO and/or any NOT TO
            // terminal
            boolean anyToTerminal = false;
            boolean anyNotToTerminal = false;
            for (Partition nextPart : part.getAllSuccessors()) {
                if (nextPart.isTerminal()) {
                    anyToTerminal = true;
                } else {
                    anyNotToTerminal = true;
                }
            }

            // Mark if this partition has ALL, only SOME, or NONE of its
            // outgoing transitions going to terminal
            if (anyToTerminal) {
                if (anyNotToTerminal) {
                    goesToTerminal.put(part, TransitionsOut.SomeToTerminal);
                } else {
                    goesToTerminal.put(part, TransitionsOut.AllToTerminal);
                }
            } else {
                goesToTerminal.put(part, TransitionsOut.NoneToTerminal);
            }

            partIDs.put(part, nextID++);

            // Find INITIAL
            if (part.isInitial()) {
                initialPart = part;
            }
        }

        // Print entry to initial state
        String init = String.format("%s = S%d,", systemName,
                partIDs.get(initialPart));
        ltsContent.append(init);

        // Initialize fields for BFT (breadth-first traversal) over all
        // partitions
        LinkedBlockingQueue<Partition> bftQueue = new LinkedBlockingQueue<>();
        bftQueue.add(initialPart);
        HashSet<Partition> visited = new HashSet<>();

        // Perform a BFT over all partitions, along the way converting
        // Synoptic's internal event-based graph into a state-based one and
        // storing each state in the LTS representation
        while (!bftQueue.isEmpty()) {
            // Get a partition
            Partition part = bftQueue.poll();

            // Skip TERMINAL
            if (part.isTerminal()) {
                continue;
            }

            // Temp LTS representation for the current state, which is only
            // committed if it has any non-terminal transitions
            StringBuilder tmpLtsContent = new StringBuilder();

            tmpLtsContent
                    .append(String.format("\n\nS%d = (", partIDs.get(part)));

            // Loop over all outgoing transitions
            boolean transitionAdded = false;
            for (Partition nextPart : part.getAllSuccessors()) {
                // Skip transitions to terminal
                if (nextPart.isTerminal()) {
                    continue;
                }

                String eventType = nextPart.getEType().toString();

                // Retrieve whether outgoing transitions of this "next"
                // partition go to terminal
                TransitionsOut nextPartTransitions = goesToTerminal
                        .get(nextPart);

                // If some outgoing transitions of "next" (in event-based world)
                // go to terminal, then this state should transition to terminal
                // as well
                if (nextPartTransitions == TransitionsOut.AllToTerminal
                        || nextPartTransitions == TransitionsOut.SomeToTerminal) {
                    //
                    if (transitionAdded) {
                        tmpLtsContent.append("\n\t\t| ");
                    }
                    transitionAdded = true;

                    //
                    tmpLtsContent.append(eventType).append("? -> S0");
                }

                // If any outgoing transitions of "next" (in event-based world)
                // DON'T go to terminal, add transition to the "next" state
                // because it's a real state (in state-based world) that doesn't
                // ONLY go to terminal
                if (nextPartTransitions == TransitionsOut.SomeToTerminal
                        || nextPartTransitions == TransitionsOut.NoneToTerminal) {
                    //
                    if (transitionAdded) {
                        tmpLtsContent.append("\n\t\t| ");
                    }
                    transitionAdded = true;

                    //
                    tmpLtsContent.append(eventType).append("? -> S")
                            .append(partIDs.get(nextPart));
                }

                // Standard BFT: ensure partitions are visited exactly once
                if (!visited.contains(nextPart)) {
                    visited.add(nextPart);
                    bftQueue.add(nextPart);
                }
            }

            // Commit if there were any non-terminal transitions
            if (transitionAdded) {
                ltsContent.append(tmpLtsContent).append("),");
            }
        }

        // Write terminal state (always S0)
        ltsContent.append("\n\nS0 = STOP.");

        // Write concluding line
        ltsContent.append(String.format("\n\n||MTS_%s = (%s).\n", systemName,
                systemName));

        return ltsContent.toString();
    }
}
