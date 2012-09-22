package dynoptic.model.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import dynoptic.model.AbsFSM;
import dynoptic.model.AbsFSMState;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.cfsm.fsm.FSM;

import synoptic.main.SynopticMain;
import synoptic.model.event.DistEventType;
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
     * Exports the CFSM graph to a format determined by Main.graphExportFormatter,
     * writing the resulting string to a file specified by fileName.
     */
    public static void exportCFSMGraph(String fileName, CFSM graph,
    		boolean outputEdgeLabels) {
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
        exportCFSMGraph(writer, graph, outputEdgeLabels);
        // /////////////
        writer.close();
    }
    
    public static void exportCFSMGraph(Writer writer, CFSM cfsmGraph,
    		boolean outputEdgeLabels) {
    	GraphExportFormatter formatter = new DotExportFormatter();
        
        try {
	        // Begin graph.
	        writer.write(formatter.beginGraphString());
	        
	        // Write out each FSM in CFSM.
	        for (FSM fsmGraph : cfsmGraph.getFSMs()) {
	        	exportFSMGraph(writer, fsmGraph, outputEdgeLabels);
	        }
	
	        // End graph.
	        writer.write(formatter.endGraphString());
	        
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error writing to file during graph export: "
                            + e.getMessage(), e);
        }
    }
    
    public static <State extends AbsFSMState<State>> void exportFSMGraph(Writer writer,
    		AbsFSM<State> fsmGraph, boolean outputEdgeLabels) {
    	GraphExportFormatter formatter = new DotExportFormatter();
    	
    	try {
    		// A mapping between nodes in the graph and the their integer
            // identifiers in the dot output.
            LinkedHashMap<State, Integer> nodeToInt = new LinkedHashMap<State, Integer>();

            // A unique identifier used to represent nodes in the exported file.
            int nodeCnt = 0;

            // NOTE: we must create a new collection so that we do not modify
            // the set maintained by the graph!
            Set<State> nodes = new LinkedHashSet<State>(fsmGraph.getStates());
    		
	        // /////////////////////
	        // EXPORT NODES:
            for (State node : nodes) {
            	// Output the node record -- its id along with its attributes.
                writer.write(formatter.nodeToString(nodeCnt,
                        node, node.isInitial(), node.isAccept()));
                // Remember the identifier assigned to this node (used for
                // outputting transitions between nodes).
                nodeToInt.put(node, nodeCnt);
                nodeCnt += 1;
            }
            
	        // /////////////////////
	        // EXPORT EDGES:
	        // Export all the edges corresponding to the nodes in the graph.
            for (State node : nodes) {
            	int nodeSrc = nodeToInt.get(node);
            	Set<DistEventType> transitions = node.getTransitioningEvents();
            	
            	for (DistEventType trans : transitions) {
            		Set<State> nextNodes = node.getNextStates(trans);
            		
                    for (State nextNode : nextNodes) {
                    	int nodeDst = nodeToInt.get(nextNode);
                    	
                    	String s = formatter.edgeToStringWithDistEvent(nodeSrc, nodeDst, trans, null);
                    	writer.write(s);
                    }
            	}
            }
    	} catch (IOException e) {
            throw new RuntimeException(
                    "Error writing to file during graph export: "
                            + e.getMessage(), e);
        }
    }
}