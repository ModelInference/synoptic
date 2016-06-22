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
     */
    public static <T extends INode<T>> void exportLTS(String baseFilename, IGraph<T> graph) {

        // The graph must be a partition graph
        assert graph instanceof PartitionGraph;
        PartitionGraph pGraph = (PartitionGraph) graph;

        // Output the final model map as an LTS model
        try {
            PrintWriter output = new PrintWriter(baseFilename + ".lts");
            output.print(buildLTS(pGraph));
            output.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build the actual LTS output and return as a string.
     * 
     * @param pGraph
     *            The partition graph to output
     */
    private static String buildLTS(PartitionGraph pGraph) {
        // Will eventually contain fully-built LTS
        StringBuilder ltsContent = new StringBuilder();

        // Holds a unique state ID for each partition
        HashMap<Partition, Integer> partIDs = new HashMap<Partition, Integer>();

        int nextID = 0;
        Partition initialPart = null;

        // Give each partition a unique state ID, and find the initial partition
        for (Partition part : pGraph.getNodes()) {
            // Store ID, but skip terminal
            if (!part.isTerminal()) {
                partIDs.put(part, nextID++);
            }

            // Find initial
            if (part.isInitial()) {
                initialPart = part;
            }
        }

        // Print entry to initial state
        String init = String.format("Synoptic = S%d", partIDs.get(initialPart));
        ltsContent.append(init);

        // Initialize fields for BFT (breadth-first traversal) over all
        // partitions
        LinkedBlockingQueue<Partition> bftQueue = new LinkedBlockingQueue<Partition>();
        bftQueue.add(initialPart);
        HashSet<Partition> visited = new HashSet<Partition>();

        // Perform a BFT over all partitions, along the way converting
        // Synoptic's internal event-based graph into a state-based one and
        // storing each state in the LTS representation
        while (!bftQueue.isEmpty()) {
            // Get a partition
            Partition part = bftQueue.poll();

            ltsContent.append(String.format(",\n\nS%d = ", partIDs.get(part)));

            // Loop over all outgoing transitions
            boolean transitionAdded = false;
            for (Partition nextPart : part.getAllSuccessors()) {
                // Skip transitions to terminal
                if (nextPart.isTerminal()) {
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
                String eventType = nextPart.getEType().toString();
                ltsContent.append(eventType).append(" -> S").append(partIDs.get(nextPart));

                // Standard BFT: ensure partitions are visited exactly once
                if (!visited.contains(nextPart)) {
                    visited.add(nextPart);
                    bftQueue.add(nextPart);
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
        ltsContent.append(".\n\n||MTS_Synoptic = (Synoptic).\n");

        return ltsContent.toString();
    }
}
