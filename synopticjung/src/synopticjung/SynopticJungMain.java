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

    public static void main(String[] args) throws Exception {
        logger = Logger.getLogger("");

        Integer ret;
        SynopticJungMain mainInstance = new SynopticJungMain(args);

        try {
            ret = mainInstance.call();
        } catch (Exception e) {
            throw InternalSynopticException.Wrap(e);
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
        pGraph = synopticMainInstance.createInitialPartitionGraph();
    }

    @Override
    public Integer call() throws Exception {
        JungGui gui = new JungGui(pGraph);
        gui.init();
        synchronized (gui) {
            gui.wait();
        }
        return new Integer(0);
    }
}
