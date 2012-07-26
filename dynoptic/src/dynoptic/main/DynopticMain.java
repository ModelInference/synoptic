package dynoptic.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import mcscm.McScM;

/**
 * <p>
 * This class wraps everything together to provide a command-line interface, as
 * well as an API, to run Dynoptic programatically.
 * </p>
 * <p>
 * Unlike the synoptic code-base, DynopticMain is not a singleton and can be
 * instantiated for every new execution of Dynoptic. However, DynopticMain
 * cannot be re-used. That is, a new version _must_ be instantiated for each new
 * execution of the Dynoptic process.
 * </p>
 * <p>
 * For options that Dynoptic recognizes, see DynopticOptions.
 * </p>
 */
public class DynopticMain {
    DynopticOptions opts = null;
    Logger logger = null;

    // The Java McScM model checker bridge instance that interfaces with the
    // McScM verify binary.
    McScM mcscm = null;

    public DynopticMain(String[] args) throws Exception {
        this(new DynopticOptions(args));
    }

    public DynopticMain(DynopticOptions opts) throws Exception {
        this.opts = opts;
        setUpLogging(opts);
        checkOptions(opts);
        mcscm = new McScM(opts.mcPath);
    }

    /**
     * Runs Dynoptic based on setting in opts, but uses the log from the passed
     * in String, and not from the logFilenames defined in opts.
     * 
     * @param log
     */
    public void run(String log) {
        // TODO
    }

    /**
     * Runs the Dynoptic process based on the settings in opts. In particular,
     * we expect that the logFilenames are specified in opts.
     */
    public void run() {
        if (opts.logFilenames.size() == 0) {
            String err = "No log filenames specified, exiting. Specify log files at the end of the command line with no options.";
            logger.severe(err);
            throw new OptionException();
        }

        try {

            // TODO:
            // 1. read in and parse input logs into traces
            // 2. mine invariants from traces.
            // 3. create initial partitioning from traces
            // 4. check invariant/refine loop
            // 5. coarsen
            // 6. output final model

        } catch (OptionException e) {
            // During OptionExceptions, the problem has already been printed.
            return;
        }
    }

    /**
     * Sets up project-global logging based on command line options.
     * 
     * @param opts
     */
    private void setUpLogging(DynopticOptions opts) {
        // Get the top Logger instance
        logger = Logger.getLogger("DynopticMain");

        // Set the logger's log level based on command line arguments
        if (opts.logLvlQuiet) {
            logger.setLevel(Level.WARNING);
        } else if (opts.logLvlVerbose) {
            logger.setLevel(Level.FINE);
        } else if (opts.logLvlExtraVerbose) {
            logger.setLevel(Level.FINEST);
        } else {
            logger.setLevel(Level.INFO);
        }
        return;
    }

    /**
     * Checks the input Dynoptic options for consistency and omissions.
     * 
     * @param optns
     * @throws Exception
     */
    private void checkOptions(DynopticOptions optns) throws Exception {
        String err = null;

        // Display help for all option groups, including unpublicized ones
        if (optns.allHelp) {
            optns.printLongHelp();
            err = "";
        }

        // Display help just for the 'publicized' option groups
        if (optns.help) {
            optns.printShortHelp();
            err = "";
        }

        if (optns.outputPathPrefix == null) {
            err = "Cannot output any generated models. Specify output path prefix using:\n\t"
                    + opts.getOptDesc("outputPathPrefix");
            logger.severe(err);
        }

        if (optns.mcPath == null) {
            err = "Specify path of the McScM model checker to use for verification:\n\t"
                    + opts.getOptDesc("mcPath");
            logger.severe(err);
        }

        if (err != null) {
            throw new OptionException();
        }
    }

}
