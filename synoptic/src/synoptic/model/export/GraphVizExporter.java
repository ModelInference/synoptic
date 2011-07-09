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
import java.io.StringWriter;
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

public class GraphVizExporter {
    static Logger logger = Logger.getLogger("GraphVizExporter");

    /**
     * Maps commonly used relations to the edge colors used in dot output.
     */
    static final LinkedHashMap<String, String> relationColors;

    static {
        relationColors = new LinkedHashMap<String, String>();
        relationColors.put("t", "");
        relationColors.put("i", "blue");
    }

    /**
     * A list of common paths to try when searching for the dot executable.
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

    public <T extends INode<T>> void export(File dotFile, IGraph<T> newHead)
            throws Exception {
        final PrintWriter writer;
        try {
            writer = new PrintWriter(dotFile);
        } catch (final IOException e) {
            throw new Exception("Error opening .dot-File: " + e.getMessage(), e);
        }

        export(writer, newHead, false);
    }

    /**
     * Exports a dot file as a png image file. The png file will be created in
     * the same place as the dot file.
     *
     * @param dotFile
     *            dot file filename
     */
    public void exportPng(File dotFile) {
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

        try {
            Runtime.getRuntime().exec(execCommand);
        } catch (IOException e) {
            logger.severe("Could not run dotCommand '" + execCommand + "': "
                    + e.getMessage());
        }
    }

    private <T extends INode<T>> void export(final Writer writer,
            IGraph<T> graph, boolean fast) throws IOException {
        export(writer, graph, fast, false);
    }

    private <T extends INode<T>> void export(final Writer writer,
            IGraph<T> graph, boolean fast, boolean isInitialGraph)
            throws IOException {

        // Begin graph.
        writer.write("digraph {\n");
        // Write out graph body.
        exportGraphCanonically(writer, graph, isInitialGraph);
        // End graph.
        writer.write("} // digraph\n");

        // close the dot file
        writer.close();
    }

    private <T extends INode<T>> String nodeDotAttributes(T node,
            boolean initial, boolean terminal, String color) {
        String attributes = "label=\"" + quote(node.getEType().toString())
                + "\"";
        if (initial) {
            attributes = attributes + ",shape=box";
        } else if (terminal) {
            attributes = attributes + ",shape=diamond";
        }
        if (!color.equals("")) {
            attributes = attributes + ",color=" + color;
        }
        return attributes;
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
     *            dot output file. This is used to consistently output
     *            transitions between nodes.
     * @param nodeCnt
     *            The node count -- used to generate unique node ids in
     *            nodeToInt map
     * @return the updated node count
     * @throws IOException
     *             in case there is a problem using the writer
     */
    private <T extends INode<T>> int exportRelationNodes(final Writer writer,
            IGraph<T> graph, String relation,
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
                String attrs = nodeDotAttributes(node, isInitial, isTerminal,
                        relationColors.get(relation));
                // Output the node record -- its id along with its attributes.
                writer.write("  " + nodeCnt + " [" + attrs + "];\n");
                // Remember the identifier assigned to this node.
                nodeToInt.put(node, nodeCnt);
                nodeCnt += 1;
            }
            parentNodes = childNodes;
        }
        writer.flush();
        return nodeCnt;
    }

    /**
     * Exports a graph in a dot format using the writer. It does so canonically
     * -- two isomorphic graphs will have equivalent dot outputs. The generated
     * Graphviz dot files may then be diff-ed to check if they represent the
     * same graphs.
     *
     * @param <T>
     *            Graph node type
     * @param writer
     *            The writer to use for dot output
     * @param graph
     *            The graph to export
     * @throws IOException
     *             In case there is a problem using the writer
     */
    private <T extends INode<T>> void exportGraphCanonically(
            final Writer writer, IGraph<T> graph, boolean isInitialGraph)
            throws IOException {
        // A mapping between nodes in the graph and the their integer
        // identifiers in the dot output.
        LinkedHashMap<T, Integer> nodeToInt = new LinkedHashMap<T, Integer>();
        // Collects all transitions between nodes of different relations.
        LinkedList<WeightedTransition<T>> allTransitions = new LinkedList<WeightedTransition<T>>();
        // Node identifier generator, updated in exportRelationNodes
        int nodeCnt = 0;

        // TODO: determine which relations exist in graph and only export these
        // relations.

        // Export nodes in the 't' relation.
        nodeCnt = exportRelationNodes(writer, graph, "t", allTransitions,
                nodeToInt, nodeCnt);

        // Export nodes in the 'i' relation.
        exportRelationNodes(writer, graph, "i", allTransitions, nodeToInt,
                nodeCnt);

        // Output all the edges:
        for (WeightedTransition<T> trans : allTransitions) {
            int sourceInt = nodeToInt.get(trans.getSource());
            int targetInt = nodeToInt.get(trans.getTarget());
            writer.write(sourceInt + "->" + targetInt + " [");
            if (Main.outputEdgeLabels && !isInitialGraph) {
                String freq = quote(String.format("%.2f", trans.getFraction()));
                writer.write("label=\"" + freq + "\", weight=\"" + freq + "\",");
            }
            if (trans.getRelation().equals("i")) {
                writer.write(",color=blue");
            }
            writer.write("];" + "\n");
        }
        return;
    }

    private static String quote(String string) {
        final StringBuilder sb = new StringBuilder(string.length() + 2);
        for (int i = 0; i < string.length(); ++i) {
            final char c = string.charAt(i);
            switch (c) {
            case '\\':
                sb.append("\\\\");
                break;
            case '"':
                sb.append("\\\"");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    public <T extends INode<T>> void exportAsDotAndPng(String fileName,
            IGraph<T> g) throws Exception {
        File f = new File(fileName);
        export(f, g);
        if (Main.dumpPNG) {
            exportPng(f);
        }
    }

    public static <T extends INode<T>> void quickExport(String fileName,
            IGraph<T> g) {
        GraphVizExporter e = new GraphVizExporter();
        try {
            e.exportAsDotAndPng(fileName, g);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public <T extends INode<T>> String export(IGraph<T> g) {
        StringWriter s = new StringWriter();

        try {
            export(s, g, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s.toString();
    }

    public <T extends INode<T>> void exportAsDotAndPngFast(String fileName,
            IGraph<T> pg) throws Exception {
        exportAsDotAndPngFast(fileName, pg, false);
    }

    public <T extends INode<T>> void exportAsDotAndPngFast(String fileName,
            IGraph<T> pg, boolean isInitialGraph) throws Exception {
        File f = new File(fileName);
        logger.info("Exporting dot file to: " + fileName);
        final PrintWriter writer;
        try {
            writer = new PrintWriter(f);
        } catch (final IOException e) {
            throw new Exception("Error opening .dot-File: " + e.getMessage(), e);
        }

        export(writer, pg, true, isInitialGraph);
        writer.close();
        if (Main.dumpPNG) {
            exportPng(f);
        }
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
