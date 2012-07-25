package dynoptic.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import synoptic.main.options.Options;

public class DynopticMain {
    public static Logger logger = null;

    /**
     * Main entrance into the application. See DynopticOptions for expected
     * args/options.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            runDynoptic(new DynopticOptions(args));
        } catch (OptionException e) {
            // During OptionExceptions, the problem has already been printed.
            return;
        }
    }

    public static void runDynoptic(DynopticOptions opts) throws Exception {
        setUpLogging(opts);
        handleOptions(opts);
    }

    public static void setUpLogging(DynopticOptions opts) {
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

    public static void handleOptions(DynopticOptions opts) throws Exception {
        String err = null;

        // Display help for all option groups, including unpublicized ones
        if (opts.allHelp) {
            opts.printLongHelp();
            err = "";
        }

        // Display help just for the 'publicized' option groups
        if (opts.help) {
            opts.printShortHelp();
            err = "";
        }

        if (opts.outputPathPrefix == null) {
            err = "Cannot output initial graph. Specify output path prefix using:\n\t"
                    + Options.getOptDesc("outputPathPrefix");
            logger.severe(err);
        }

        if (opts.logFilenames.size() == 0) {
            err = "No log filenames specified, exiting. Try cmd line option:\n\t"
                    + synoptic.main.options.Options.getOptDesc("help");
            logger.severe(err);
        }

        if (err != null) {
            throw new OptionException();
        }
    }

}
