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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import synoptic.main.SynopticMain;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;
import synoptic.util.time.ITime;

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
        SynopticMain syn = SynopticMain.getInstanceWithExistenceCheck();
        if (syn.options.dotExecutablePath == null) {
            logger.severe("Unable to locate the dot command executable, use cmd line option:\n\t"
                    + syn.options.getOptDesc("dotExecutablePath"));
        }
        return syn.options.dotExecutablePath;
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
    public static <T extends INode<T>> void exportGraph(String fileName,
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
        exportGraph(writer, graph, outputEdgeLabels);
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
     * @param outputEdgeLabels
     *            Whether or not to output edge labels
     * @throws IOException
     *             In case there is a problem using the writer
     */
    public static <T extends INode<T>> void exportGraph(Writer writer,
            IGraph<T> graph, boolean outputEdgeLabels) throws IOException {

        SynopticMain syn = SynopticMain.getInstanceWithExistenceCheck();
        try {
            // Begin graph.
            writer.write(syn.graphExportFormatter.beginGraphString());

            // ////////////////////////// Write out graph body.

            // A mapping between nodes in the graph and the their integer
            // identifiers in the dot output.
            LinkedHashMap<T, Integer> nodeToInt = new LinkedHashMap<T, Integer>();

            // A unique identifier used to represent nodes in the exported file.
            int nodeCnt = 0;

            // NOTE: we must create a new collection so that we do not modify
            // the set maintained by the graph!
            List<T> nodes = new ArrayList<T>(graph.getNodes());
            Collections.sort(nodes);

            // /////////////////////
            // EXPORT NODES:
            Iterator<T> nodesIter = nodes.iterator();
            while (nodesIter.hasNext()) {
                T node = nodesIter.next();

                // On user request, do not show the initial/terminal nodes.
                if ((!syn.options.showInitialNode && node.isInitial())
                        || (!syn.options.showTerminalNode && node.isTerminal())) {
                    // Remove the node from nodes to export (so that we do not
                    // show the edges corresponding to the nodes).
                    nodesIter.remove();
                    continue;
                }

                // Output the node record -- its id along with its attributes.
                writer.write(syn.graphExportFormatter.nodeToString(nodeCnt,
                        node, node.isInitial(), node.isTerminal()));
                // Remember the identifier assigned to this node (used for
                // outputting transitions between nodes).
                nodeToInt.put(node, nodeCnt);
                nodeCnt += 1;
            }

            // /////////////////////
            // EXPORT EDGES:
            // Export all the edges corresponding to the nodes in the graph.
            for (INode<T> node : nodes) {
                List<? extends ITransition<T>> transitions;
                // If perf debugging isn't enabled, then output weights, else
                // add the edge labels later.
                if (outputEdgeLabels && !syn.options.enablePerfDebugging) {
                    transitions = node.getWeightedTransitions();
                } else {
                    transitions = node.getAllTransitions();
                }
                // Sort the transitions for canonical output.
                Collections.sort(transitions);

                for (ITransition<T> trans : transitions) {
                    // If for some reason we don't have a unique identifier for
                    // the source or the target node then we skip this
                    // transition. For example, this may occur if the target is
                    // a terminal node and Main.showTerminalNode is false.
                    if (!nodeToInt.containsKey(trans.getSource())
                            || !nodeToInt.containsKey(trans.getTarget())) {
                        continue;
                    }
                    int nodeSrc = nodeToInt.get(trans.getSource());
                    int nodeDst = nodeToInt.get(trans.getTarget());
                    String s = "";

                    // FIXME: special casing to handle PO trace graphs
                    // correctly (trace graphs are composed of EventNodes).

                    if (graph.getClass() == DAGsTraceGraph.class) {

                        // NOTE: The extra casts are necessary for this to work
                        // in Java 1.6, see here:
                        // http://bugs.sun.com/view_bug.do?bug_id=6932571
                        assert (((INode<?>) (trans.getSource())) instanceof EventNode);
                        s = syn.graphExportFormatter.edgeToStringWithTraceId(
                                nodeSrc, nodeDst,
                                ((EventNode) ((INode<?>) trans.getSource()))
                                        .getTraceID(), trans.getRelation());
                    } else {
                        if (outputEdgeLabels) {

                            if (syn.options.enablePerfDebugging) {
                                // TODO Simply calculate the mean for now, but
                                // this should be more robust and conform to
                                // what the
                                // user wants in particular.
                                ITime time = null;
                                if (trans.getDeltaSeries() != null) {
                                    time = trans.getDeltaSeries().computeMean();
                                }
                                s = syn.graphExportFormatter
                                        .edgeToStringWithITime(nodeSrc,
                                                nodeDst, time,
                                                trans.getRelation());

                            } else {
                                double prob = trans.getProbability();
                                s = syn.graphExportFormatter
                                        .edgeToStringWithProb(nodeSrc, nodeDst,
                                                prob, trans.getRelation());
                            }
                        } else {
                            s = syn.graphExportFormatter
                                    .edgeToStringWithNoProb(nodeSrc, nodeDst,
                                            trans.getRelation());
                        }
                    }
                    writer.write(s);

                }
            }

            // //////////////////////////

            // End graph.
            writer.write(syn.graphExportFormatter.endGraphString());

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error writing to file during graph export: "
                            + e.getMessage(), e);
        }
        return;
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
