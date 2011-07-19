/*
 * This code is in part based on Clemens Hammacher's code.
 * 
 * Source: https://ccs.hammacher.name
 * 
 * License: Eclipse Public License v1.0.
 */

package synoptic.model.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import synoptic.main.Main;
import synoptic.model.WeightedTransition;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Used to export a graph object to a file.
 * 
 * <pre>
 * Currently supports:
 * - GraphViz dot file format
 * - GML file format
 * </pre>
 */
public class GraphExporter {
    static Logger logger = Logger.getLogger("GraphExporter");

    /**
     * A list of common paths to try when searching for the dot executable.
     * Directory paths to the dot executable should be added here.
     */
    static final String[] dotCommands = { "/usr/bin/dot", "/usr/local/bin/dot",
            "C:\\Programme\\Graphviz2.26\\bin\\dot.exe",
            "C:\\Program Files (x86)\\Graphviz2.26.3\\bin\\dot.exe",
            "C:\\Program Files\\Graphviz 2.28\\bin\\dot.exe" };

    /**
     * @return Returns the dot command executable or null on error
     * @throws InternalSynopticException
     *             problem looking up a command line option description
     */
    private static String getDotCommand() {
        for (String dotCommand : dotCommands) {
            File f = new File(dotCommand);
            if (f.exists()) {
                return dotCommand;
            }
        }
        if (Main.dotExecutablePath == null) {
            logger.severe("Unable to locate the dot command executable, use cmd line option:\n\t"
                    + Main.getCmdLineOptDesc("dotExecutablePath"));
        }
        return Main.dotExecutablePath;
    }

    /**
     * Converts a dot file as a png image file using dot. The png file will be
     * created in the same place as the dot file.
     * 
     * @param dotFile
     *            dot file filename
     */
    public static void generatePngFileFromDotFile(String fileName) {
        File dotFile = new File(fileName);

        String dotCommand = getDotCommand();
        if (dotCommand == null) {
            // could not locate a dot executable
            return;
        }

        String imageExt = "png";

        String execCommand = dotCommand + " -O -T" + imageExt + " "
                + dotFile.getAbsolutePath();

        logger.info("Exporting graph to: " + dotFile.toString() + "."
                + imageExt);

        Process dotProcess;
        try {
            dotProcess = Runtime.getRuntime().exec(execCommand);
        } catch (IOException e) {
            logger.severe("Could not run dotCommand '" + execCommand + "': "
                    + e.getMessage());
            return;
        }
        try {
            dotProcess.waitFor();
        } catch (InterruptedException e) {
            logger.severe("Waiting for dot process interrupted '" + execCommand
                    + "': " + e.getMessage());
        }
    }

    /**
     * Exports the graph to a format determined by Main.graphExportFormatter,
     * writing the resulting string to a file specified by fileName.
     */
    public static <T extends INode<T>> void serializeGraph(String fileName,
            IGraph<T> graph, boolean outputEdgeLabels) throws IOException {
        File f = new File(fileName);
        logger.info("Exporting graph to: " + fileName);
        final PrintWriter writer;
        try {
            writer = new PrintWriter(f);
        } catch (final IOException e) {
            throw new RuntimeException("Error opening file for graph export: "
                    + e.getMessage(), e);
        }
        // /////////////
        serializeGraph(writer, graph, outputEdgeLabels);
        // /////////////
        writer.close();
    }

    /**
     * Exports the graph to a format determined by Main.graphExportFormatter,
     * writing the resulting string to writer. The export is done canonically --
     * two isomorphic graphs will have equivalent outputs. The generated dot/gml
     * files may then be diff-ed to check if they represent the same graphs.
     * 
     * @param <T>
     *            Graph node type
     * @param writer
     *            The writer to use for dot output
     * @param graph
     *            The graph to export
     * @param isInitialGraph
     *            Whether or not the graph is an initial graph
     * @throws IOException
     *             In case there is a problem using the writer
     */
    public static <T extends INode<T>> void serializeGraph(Writer writer,
            IGraph<T> graph, boolean outputEdgeLabels) throws IOException {

        try {
            // Begin graph.
            writer.write(Main.graphExportFormatter.beginGraphString());

            // ////////////////////////// Write out graph body.

            // A mapping between nodes in the graph and the their integer
            // identifiers in the dot output.
            LinkedHashMap<T, Integer> nodeToInt = new LinkedHashMap<T, Integer>();
            // Collects all transitions between nodes of different relations.
            LinkedList<WeightedTransition<T>> allTransitions = new LinkedList<WeightedTransition<T>>();
            // Node identifier generator, updated in exportRelationNodes
            int nodeCnt = 0;

            // TODO: determine which relations exist in graph and only export
            // these relations.

            // Export nodes in the 't' relation.
            nodeCnt = exportRelationNodes(writer, graph, "t", allTransitions,
                    nodeToInt, nodeCnt);

            // Export nodes in the 'i' relation.
            exportRelationNodes(writer, graph, "i", allTransitions, nodeToInt,
                    nodeCnt);

            // Output the edges:
            for (WeightedTransition<T> trans : allTransitions) {
                int nodeSrc = nodeToInt.get(trans.getSource());
                int nodeDst = nodeToInt.get(trans.getTarget());
                writer.write(Main.graphExportFormatter.edgeToString(
                        outputEdgeLabels, nodeSrc, nodeDst,
                        trans.getFraction(), trans.getRelation()));
            }

            // //////////////////////////

            // End graph.
            writer.write(Main.graphExportFormatter.endGraphString());

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error writing to file during graph export: "
                            + e.getMessage(), e);
        }
        return;
    }

    /**
     * Exports all the nodes in the graph for a particular relation, using
     * output stream writer. This function only exports the nodes, but it also
     * builds up a list of allTransitions that these nodes include, maintains a
     * nodeCnt (which is then returned), and also maintains the nodeToInt map.
     * All of these are also passed as arguments.
     * 
     * @param <T>
     *            The node type in the graph being exported
     * @param writer
     *            The writer to use for writing out node records
     * @param graph
     *            The graph whose nodes will be exported
     * @param relation
     *            The relation to export (must be present in graph)
     * @param allTransitions
     *            A list of transitions that will be modified to include
     *            transitions in graph.
     * @param nodeToInt
     *            A mapping between nodes in the graph and the node's id in the
     *            output file. This is used to consistently output transitions
     *            between nodes.
     * @param nodeCnt
     *            The node count -- used to generate unique node ids in
     *            nodeToInt map
     * @return the updated node count
     * @throws IOException
     *             in case there is a problem using the writer
     */
    private static <T extends INode<T>> int exportRelationNodes(
            final Writer writer, IGraph<T> graph, String relation,
            LinkedList<WeightedTransition<T>> allTransitions,
            LinkedHashMap<T, Integer> nodeToInt, int nodeCnt)
            throws IOException {

        LinkedList<T> rootNodes = new LinkedList<T>(
                graph.getInitialNodes(relation));

        if (rootNodes.size() == 0) {
            logger.fine("Cannot export a graph[relation "
                    + relation
                    + "] with no initial nodes: will result in empty graph output.");
            return nodeCnt;
        }

        if (!Main.showInitialNode) {
            // Follow each node in rootNodes one transition forward and mark the
            // destination as the new root. This will
            // ignore the dummy initial node.
            LinkedList<T> newRootNodes = new LinkedList<T>();
            for (T root : rootNodes) {
                for (ITransition<T> trans : root.getTransitions()) {
                    newRootNodes.add(trans.getTarget());
                }
            }
            rootNodes = newRootNodes;
        }

        // Start walking the graph from the rootNodes.
        LinkedList<T> parentNodes = new LinkedList<T>(rootNodes);

        Comparator<T> comparator = null;
        if (parentNodes.size() != 0) {
            comparator = rootNodes.get(0).getComparator();
        }

        // A breadth first exploration of the graph to touch all nodes.
        while (parentNodes.size() != 0) {
            LinkedList<T> childNodes = new LinkedList<T>();
            // For canonical output sort the parents.
            Collections.sort(parentNodes, comparator);

            for (T node : parentNodes) {
                boolean isTerminal, isInitial;

                if (nodeToInt.containsKey(node)) {
                    // Skip nodes that have already been processed.
                    continue;
                }

                if (!Main.showTerminalNode && node.isTerminal()) {
                    // Skip the terminal node.
                    continue;
                }
                // A node is not terminal unless shown to be otherwise.
                isTerminal = false;

                List<WeightedTransition<T>> transitions = node
                        .getWeightedTransitions();

                for (WeightedTransition<T> trans : transitions) {
                    T child = trans.getTarget();
                    childNodes.add(child);
                    if (!Main.showTerminalNode && child.isTerminal()) {
                        // Mark nodes preceding the terminal node as terminal.
                        isTerminal = true;
                    } else {
                        // Include all transitions, except when
                        // (!showTerminalNode && the transition is to the
                        // terminal node) -- the if branch above.
                        allTransitions.add(trans);
                    }
                }

                isInitial = rootNodes.contains(node);

                // Output the node record -- its id along with its attributes.
                writer.write(Main.graphExportFormatter.nodeToString(nodeCnt,
                        node, isInitial, isTerminal, relation));
                // Remember the identifier assigned to this node.
                nodeToInt.put(node, nodeCnt);
                nodeCnt += 1;
            }
            parentNodes = childNodes;
        }
        writer.flush();
        return nodeCnt;
    }

    // private static void exportSCCsWithInvariants(GraphVizExporter e,
    // PartitionGraph pg) throws Exception {
    // StronglyConnectedComponents<Partition> sccs = new
    // StronglyConnectedComponents<Partition>(
    // pg);
    // int partN = 0;
    // for (Set<Partition> scc : sccs) {
    // Graph<Partition> graph = new Graph<Partition>();
    // Graph<LogEvent> messageGraph = new Graph<LogEvent>();
    // for (Partition p : scc) {
    // graph.add(p);
    // for (LogEvent m : p.getMessages()) {
    // messageGraph.add(m);
    // }
    // }
    // String prefix = "";
    // e.exportAsDotAndPngFast(
    // prefix + "output/peterson/messageGraph.dot", messageGraph);
    // e.exportAsDotAndPngFast(prefix + "output/peterson/partition-"
    // + partN + ".dot", graph);
    // System.out.println(scc);
    // TemporalInvariantSet.generateStructuralInvariants = true;
    // TemporalInvariantSet s2 = TemporalInvariantSet
    // .computeInvariantsUsingTC(messageGraph);
    // e.exportAsDotAndPngFast(prefix + "output/peterson/partition-"
    // + partN + "-synoptic.invariants.dot",
    // s2.getInvariantGraph("AP"));
    // TemporalInvariantSet.generateStructuralInvariants = false;
    // partN++;
    // }
    // }
}
