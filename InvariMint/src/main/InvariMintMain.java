package main;

import java.util.logging.Level;
import java.util.logging.Logger;

import model.InvModel;
import model.InvsModel;
import algorithms.InvariMintKTails;
import algorithms.InvariMintPropTypes;
import algorithms.InvariMintSynoptic;
import algorithms.PGraphInvariMint;

import synoptic.invariants.NeverImmediatelyFollowedInvariant;

/**
 * InvariMint accepts a log file and regular expression arguments and constructs
 * a DFA model of the system which generated the input log. InvariMint relies on
 * Synoptic for log parsing and invariant mining. Default behavior is to mine
 * traditional Synoptic invariants, though the --performKTails and --
 * kTailLength options allow users to specify that kTail invariants should be
 * mined instead. In either case, implicit NIFby invariants along with an
 * Initial/Terminal invariant are used to construct an initial DFA model. The
 * initial model is then intersected with each of the mined invariants to
 * construct and export a final model.
 */
public class InvariMintMain {
    public Logger logger = null;

    // Options that configure this InvariMint instance.
    private InvariMintOptions opts = null;

    // Instance of the invarimint algorithm that will execute.
    private PGraphInvariMint invMintAlg = null;

    // The invarimint model derived with call to runInvariMint()
    private InvsModel invmintDfa = null;

    // Whether or not the invarimint model is identical to the model derived
    // using the standard algorithm.
    private boolean equalToStdAlg;

    /**
     * Main entrance into the application. See InvariMintOptions for expected
     * args/options.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            InvariMintOptions opts = new InvariMintOptions(args);
            InvariMintMain main = new InvariMintMain(opts);
            main.runInvariMint();
        } catch (OptionException e) {
            // During OptionExceptions, the problem has already been printed.
            return;
        }
    }

    // //////////////////////////////////////////////////////////////////////////

    public InvariMintMain(InvariMintOptions opts) throws Exception {
        this.opts = opts;

        setUpLogging();
        handleOptions();

        if (opts.invMintSynoptic) {
            // Instantiate a Synoptic version of InvariMint.
            invMintAlg = new InvariMintSynoptic(opts);
        } else if (opts.invMintKTails) {
            // Instantiate a KTails version of InvariMint.
            invMintAlg = new InvariMintKTails(opts);
        }
    }

    /**
     * Runs InvariMint with the given set of options.
     */
    public void runInvariMint() throws Exception {

        // Hack to avoid conflicts caused by the fact InvariMintPropTypes
        // does not implement PGraph.
        if (opts.alwaysFollowedBy || opts.alwaysPrecedes
                || opts.neverFollowedBy || opts.neverImmediatelyFollowedBy) {
            runPropTypes();
            return;
        }
        // This sets the invmintDfa instance.
        runAlg(false);

        // Optionally remove paths from the model not found in any input trace.
        if (opts.removeSpuriousEdges) {
            invMintAlg.removeSpuriousEdges();
        }

        // //////////// Below, only output (non-transforming) methods:

        // Export final model.
        String exportFname = opts.outputPathPrefix + "."
                + invMintAlg.getInvMintAlgName() + ".dfa.dot";
        invmintDfa.exportDotAndPng(exportFname);

        // Export each of the mined DFAs (except NIFby invariants).
        if (opts.exportMinedInvariantDFAs) {
            int invID = 0;
            String path;
            for (InvModel invDFA : invmintDfa.getInvariants()) {
                if (!(invDFA.getInvariant() instanceof NeverImmediatelyFollowedInvariant)) {
                    path = opts.outputPathPrefix + "."
                            + invMintAlg.getInvMintAlgName() + ".InvDFA"
                            + invID + ".dot";
                    invDFA.exportDotAndPng(path);
                    invID++;
                }
            }
        }

        if (opts.exportStdAlgPGraph) {
            invMintAlg.exportStdAlgPGraph();
        }

        if (opts.exportStdAlgDFA) {
            invMintAlg.exportStdAlgDFA();
        }

        if (opts.compareToStandardAlg) {
            // Run the standard algorithm.
            runAlg(true);
            // Compare the two models: InvariMint model and the standard
            // algorithm model.
            equalToStdAlg = invMintAlg.compareToStandardAlg();
        }
    }

    public InvsModel getInvariMintModel() {
        // Make sure that the model was created.
        assert invmintDfa != null;

        return invmintDfa;
    }

    public boolean isEqualToStandardAlg() {
        // Make sure that the comparison was actually performed.
        assert opts.compareToStandardAlg;

        return equalToStdAlg;
    }

    // //////////////////////////////////////////////////////////////////////////////////

    /**
     * Parses and checks command line options for consistency.
     * 
     * @throws OptionException
     *             if there was an issue with the passed in options.
     */
    private void handleOptions() throws OptionException {
        // Display help for all option groups, including unpublicized ones
        if (opts.allHelp) {
            opts.printLongHelp();
            throw new OptionException();
        }

        // Display help just for the 'publicized' option groups
        if (opts.help) {
            opts.printShortHelp();
            throw new OptionException();
        }

        String err = null;
        Boolean invMintPropTypes = opts.alwaysFollowedBy || opts.alwaysPrecedes
                || opts.neverFollowedBy || opts.neverImmediatelyFollowedBy;

        if (opts.outputPathPrefix == null) {
            err = "Cannot output any models. Specify output path prefix using:\n\t"
                    + opts.getOptDesc("outputPathPrefix");
        }

        if (opts.logFilenames.size() == 0) {
            err = "No log filenames specified, exiting. Try cmd line option:\n\t"
                    + opts.getOptDesc("help");
        }

        // hacky fix since InvariMintPropTypes does not implement PGraph so
        // cannot use these options
        if (invMintPropTypes
                && (opts.exportStdAlgPGraph || opts.exportStdAlgDFA
                        || opts.compareToStandardAlg || opts.removeSpuriousEdges)) {
            err = "Cannot use exportStdAlgPGraph, exportStdAlgDFA, compareToStandardAlg, "
                    + "removeSpuriousEdges with individually specfied property types. "
                    + "Remove these options or specify --invMintSynoptic or --invMintKTails "
                    + "instead of individual property types.";
        }
        if (opts.invMintKTails && invMintPropTypes || opts.invMintSynoptic
                && invMintPropTypes) {
            err = "Cannot specify individual property types with --invMintSynoptic or --invMintKTails.";
        }
        if ((opts.invMintSynoptic && opts.invMintKTails)) {
            err = "Must specify either --invMintSynoptic or --invMintKTails option, but not both.";
        }

        if (!opts.invMintSynoptic && !opts.invMintKTails && !invMintPropTypes) {
            err = "InvariMint algorithm not specified.";
        }

        if (err != null) {
            logger.severe(err);
            throw new OptionException(err);
        }
    }

    private void setUpLogging() {
        // Get the top Logger instance
        logger = Logger.getLogger("InvariMintMain");

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

    private void runAlg(boolean standardAlg) throws Exception {
        String strAlg = "InvariMint Alg";
        if (standardAlg) {
            strAlg = "Standard Alg";
        }
        logger.info("Running " + strAlg);
        long startTime = System.nanoTime();
        long endTime;
        try {
            if (standardAlg) {
                invMintAlg.runStdAlg();
            } else {
                invmintDfa = invMintAlg.runInvariMint();
            }
        } finally {
            endTime = System.nanoTime();
        }
        // Convert nanoseconds to seconds
        double duration_secs = (endTime - startTime) / 1000000000.0;
        logger.info("DONE Running " + strAlg + ". Duration = " + duration_secs);
    }

    /**
     * runAlg for InvariMintPropTypes. Since InvariMintPropTypes does implement
     * PGraphInvariMint, the algorithm cannot be instantiated into invMintAlg,
     * and so runAlg cannot be applied.
     * 
     * @throws Exception
     */
    private void runPropTypes() throws Exception {
        InvariMintPropTypes invMintPropTypes = new InvariMintPropTypes(opts);

        logger.info("Running InvariMintPropTypes");
        long startTime = System.nanoTime();
        long endTime;

        invmintDfa = invMintPropTypes.runInvariMint();

        endTime = System.nanoTime();
        // Convert nanoseconds to seconds
        double duration_secs = (endTime - startTime) / 1000000000.0;
        logger.info("DONE Running InvariMintPropTypes. Duration = "
                + duration_secs);

        // Export final model.
        String exportFname = opts.outputPathPrefix + "." + "InvMintPropTypes"
                + ".dfa.dot";
        invmintDfa.exportDotAndPng(exportFname);

        // Export each of the mined DFAs
        if (opts.exportMinedInvariantDFAs) {
            int invID = 0;
            String path;
            for (InvModel invDFA : invmintDfa.getInvariants()) {
                path = opts.outputPathPrefix + "." + "InvMintPropTypes"
                        + ".InvDFA" + invID + ".dot";
                invDFA.exportDotAndPng(path);
                invID++;

            }
        }
    }
}
