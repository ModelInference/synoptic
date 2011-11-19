package synopticjung;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import synoptic.main.Main;
import synoptic.model.PartitionGraph;
import synoptic.util.InternalSynopticException;

/**
 * Main entrance for the JUNG version of Synoptic. This version uses the JUNG
 * library to visualize the steps taken by the Synoptic algorithm. The program
 * takes the exactly the same command line options as the Main Synoptic
 * program/library. Refer to that project for an explanation of the various
 * command line options.
 */
public class SynopticJungMain implements Callable<Integer> {
    public static Logger logger = null;

    static String instructions = "<html>"
            + "<h3>Edges:</h3>"
            + "<ul>"
            + "<li>Labels denote the number of log events that were observed to make the transition along the edge</li>"
            + "</ul>"
            + "<h3>Node Colors:</h3>"
            + "<ul>"
            + "<li>Grey: initial and terminal nodes</li>"
            + "<li>Yellow: nodes that did not change in the last refinement step</li>"
            + "<li>Dark Green: nodes that <b>changed</b> in the last refinement step</li>"
            + "<li>Light Green: nodes that <b>created</b> by the last refinement step</li>"
            + "</ul>"
            + "<h3>All Modes:</h3>"
            + "<ul>"
            + "<li>Mousewheel scales with a crossover value of 1.0.<p>"
            + "     - scales the graph layout when the combined scale is greater than 1<p>"
            + "     - scales the graph view when the combined scale is less than 1"
            + "</ul>"
            + "<h3>Transforming Mode: (right mouse clicks)</h3>"
            + "<ul>"
            + "<li>Mouse3+drag pans the graph"
            + "<li>Mouse3+Shift+drag rotates the graph"
            + "<li>Mouse3+CTRL(or Command)+drag shears the graph"
            + "</ul>"
            + "<h3>Picking Mode: (left mouse clicks)</h3>"
            + "<ul>"
            + "<li>Mouse1 on a Vertex selects the vertex"
            + "<li>Mouse1 elsewhere unselects all Vertices"
            + "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"
            + "<li>Mouse1+drag on a Vertex moves all selected Vertices"
            + "<li>Mouse1+drag elsewhere selects Vertices in a region"
            + "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"
            + "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"
            + "<li>Mouse1 double-click on a vertex allows you to view the log lines represented by that node"
            + "</ul>" + "</html>";

    /**
     * Main entry into the program.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        logger = Logger.getLogger("");

        Integer ret;
        SynopticJungMain mainInstance;
        try {
            mainInstance = new SynopticJungMain(args);
        } catch (RuntimeException e) {
            logger.severe(e.toString());
            return;
        }

        try {
            ret = mainInstance.call();
        } catch (Exception e) {
            throw new InternalSynopticException(e);
        }

        logger.fine("SynopticJungMain.call() returned " + ret.toString());
    }

    /***********************************************************/

    /**
     * We instantiate an instance of Synoptic Main class to create the initla
     * partition graph based on the command line options.
     */
    Main synopticMainInstance;

    /**
     * This is used to store the initial graph model.
     */
    PartitionGraph pGraph;

    public SynopticJungMain(String[] args) throws Exception {
        synopticMainInstance = Main.processArgs(args);
        if (synopticMainInstance == null) {
            throw new RuntimeException(
                    "Problem parsing command line arguments.");

        }
        pGraph = synopticMainInstance.createInitialPartitionGraph();
    }

    @Override
    public Integer call() throws Exception {
        JungGui gui = new JungGui(pGraph, Main.options);
        gui.init();
        synchronized (gui) {
            gui.wait();
        }
        return new Integer(0);
    }
}
