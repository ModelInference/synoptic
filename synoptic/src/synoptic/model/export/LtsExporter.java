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
 *
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

    enum TransitionsOut {
        AllToTerminal, SomeToTerminal, NoneToTerminal
    }

    /**
     * 
     */
    private static String buildLTS(PartitionGraph pGraph, String systemName) {
        // Will eventually contain fully-built LTS
        StringBuilder ltsContent = new StringBuilder();

        // Holds a unique state ID for each partition
        HashMap<Partition, Integer> partIDs = new HashMap<>();

        //
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

            //
            boolean anyToTerminal = false;
            boolean anyNotToTerminal = false;
            for (Partition nextPart : part.getAllSuccessors()) {
                if (nextPart.isTerminal()) {
                    anyToTerminal = true;
                } else {
                    anyNotToTerminal = true;
                }
            }

            //
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

        // Print entry to INITIAL
        String init = String.format("%s = S%d,", systemName,
                partIDs.get(initialPart));
        ltsContent.append(init);

        //
        LinkedBlockingQueue<Partition> bfsQueue = new LinkedBlockingQueue<>();
        bfsQueue.add(initialPart);
        HashSet<Partition> visited = new HashSet<>();

        //
        while (!bfsQueue.isEmpty()) {
            // Get a partition
            Partition part = bfsQueue.poll();

            // Skip TERMINAL
            if (part.isTerminal()) {
                continue;
            }

            //
            StringBuilder tmpLtsContent = new StringBuilder();

            tmpLtsContent
                    .append(String.format("\n\nS%d = (", partIDs.get(part)));

            boolean transitionAdded = false;
            for (Partition nextPart : part.getAllSuccessors()) {
                // Skip transitions to TERMINAL
                if (nextPart.isTerminal()) {
                    continue;
                }

                String eventType = nextPart.getEType().toString();

                //
                TransitionsOut nextPartTransitions = goesToTerminal
                        .get(nextPart);
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

                //
                if (!visited.contains(nextPart)) {
                    visited.add(nextPart);
                    bfsQueue.add(nextPart);
                }
            }

            // Commit if there were any non-TERMINAL transitions
            if (transitionAdded) {
                ltsContent.append(tmpLtsContent).append("),");
            }
        }

        // Write terminal state (always S0)
        ltsContent.append("\n\nS0 = STOP.");

        //
        ltsContent.append(String.format("\n\n||MTS_%s = (%s).\n", systemName,
                systemName));

        return ltsContent.toString();
    }
}
