package dynoptic.model.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import dynoptic.model.AbsFSM;
import dynoptic.model.AbsFSMState;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.gfsm.GFSM;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;
import dynoptic.util.Util;

import synoptic.main.SynopticMain;
import synoptic.model.event.IDistEventType;
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
     * Exports the CFSM to a format determined by GraphExportFormatter, writing
     * the resulting string to a file specified by fileName. Each FSM in CFSM is
     * exported as one graph.
     */
    public static void exportCFSM(String fileName, CFSM cfsm)
            throws IOException {
        File f = new File(fileName);
        logger.info("Exporting CFSM to: " + fileName);
        final PrintWriter writer;
        try {
            writer = new PrintWriter(f);
        } catch (final IOException e) {
            throw new RuntimeException("Error opening file for graph export: "
                    + e.getMessage(), e);
        }
        // /////////////
        exportCFSM(writer, cfsm);
        // /////////////
        writer.close();
    }

    /**
     * Exports the GFSM to a format determined by GraphExportFormatter, writing
     * the resulting string to a file specified by fileName.
     */
    public static void exportGFSM(String fileName, GFSM gfsm) {
        File f = new File(fileName);
        logger.info("Exporting GFSM to: " + fileName);
        final PrintWriter writer;
        try {
            writer = new PrintWriter(f);
        } catch (final IOException e) {
            throw new RuntimeException("Error opening file for graph export: "
                    + e.getMessage(), e);
        }
        // /////////////
        exportAbsFSM(writer, gfsm, "gfsm [pnum=" + gfsm.getNumProcesses() + "]");
        // /////////////
        writer.close();
    }

    public static void exportObsFifoSys(String fileName, ObsFifoSys obsFifoSys) {
        File f = new File(fileName);
        logger.info("Exporting ObsFifoSys to: " + fileName);
        final PrintWriter writer;
        try {
            writer = new PrintWriter(f);
        } catch (final IOException e) {
            throw new RuntimeException("Error opening file for graph export: "
                    + e.getMessage(), e);
        }
        // /////////////
        exportAbsFSM(writer, obsFifoSys,
                "ObsFifoSys [pnum=" + obsFifoSys.getNumProcesses() + "]");
        // /////////////
        writer.close();
    }

    /**
     * Exports the CFSM to a format determined by Main.graphExportFormatter,
     * writing the resulting string to writer. Each FSM in CFSM is exported as
     * one graph. The export is done canonically -- two isomorphic graphs will
     * have equivalent outputs. The generated dot/gml files may then be diff-ed
     * to check if they represent the same graphs.
     * 
     * @param writer
     *            The writer to use for dot output
     * @param cfsmGraph
     *            The CFSM graph to export
     * @throws IOException
     *             In case there is a problem using the writer
     */
    public static void exportCFSM(Writer writer, CFSM cfsmGraph)
            throws IOException {
        // Write out each FSM in CFSM as one graph.
        for (FSM fsmGraph : cfsmGraph.getFSMs()) {
            exportAbsFSM(writer, fsmGraph, "pid " + fsmGraph.getPid());
        }
    }

    /**
     * Exports any instance that has AbsFSM type to a string and writes that
     * string using the passed writer.
     */
    private static <State extends AbsFSMState<State, TxnEType>, TxnEType extends IDistEventType> void exportAbsFSM(
            Writer writer, AbsFSM<State, TxnEType> fsmGraph, String title) {
        GraphExportFormatter formatter = new DotExportFormatter();

        try {
            // Begin graph.
            writer.write(formatter.beginGraphString());

            // A mapping between nodes in the graph and the their integer
            // identifiers in the dot output.
            Map<State, Integer> nodeToInt = Util.newMap();

            // A unique identifier used to represent nodes in the exported file.
            int nodeCnt = 0;

            // NOTE: we must create a new collection so that we do not modify
            // the set maintained by the graph!
            Set<State> nodes = Util.newSet(fsmGraph.getStates());

            // /////////////////////
            // EXPORT NODES:
            for (State node : nodes) {
                // Output the node record -- its id along with its attributes.
                writer.write(formatter.nodeToString(nodeCnt, node,
                        node.isInitial(), node.isAccept()));
                // Remember the identifier assigned to this node (used for
                // outputting transitions between nodes).
                nodeToInt.put(node, nodeCnt);
                nodeCnt += 1;
            }

            // As the last node add a title node, which is not part of the
            // graph.
            writer.write("title_node [label=\"" + title
                    + "\",shape=box, style=rounded];");

            // /////////////////////
            // EXPORT EDGES:
            // Export all the edges corresponding to the nodes in the graph.
            for (State node : nodes) {
                int nodeSrc = nodeToInt.get(node);
                Set<TxnEType> transitions = node.getTransitioningEvents();

                for (TxnEType trans : transitions) {
                    Set<State> nextNodes = node.getNextStates(trans);

                    for (State nextNode : nextNodes) {
                        assert nodeToInt.containsKey(nextNode);

                        int nodeDst = nodeToInt.get(nextNode);

                        String s = formatter.edgeToStringWithDistEvent(nodeSrc,
                                nodeDst, trans, null);
                        writer.write(s);
                    }
                }
            }
            // End graph.
            writer.write(formatter.endGraphString());
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error writing to file during CFSM export: "
                            + e.getMessage(), e);
        }
    }

}