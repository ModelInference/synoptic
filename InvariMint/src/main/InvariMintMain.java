package main;

import java.util.logging.Level;
import java.util.logging.Logger;

import model.EncodedAutomaton;
import model.InvModel;
import model.InvsModel;
import algorithms.InvariMintKTails;
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
 * 
 * @author Jenny
 */
public class InvariMintMain {
    public static Logger logger = null;

    /**
     * Main entrance into the application. See InvariMintOptions for expected
     * args/options.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            runInvariMint(new InvariMintOptions(args));
        } catch (OptionException e) {
            // During OptionExceptions, the problem has already been printed.
            return;
        }
    }

    public static boolean compareInvariMintSynoptic(InvariMintOptions opts)
            throws Exception {
        PGraphInvariMint invMintAlg = setUpAndGetAlg(opts);
        runStandardAlg(invMintAlg);
        InvsModel dfa = runInvariMint(invMintAlg);

        // Export the FINAL partition graph for standard alg.
        invMintAlg.exportStdAlgPGraph();

        // Export standard algorithm DFA.
        invMintAlg.exportStdAlgDFA();

        // Export InvariMint-StdAlg DFA.
        String exportFname = opts.outputPathPrefix + "."
                + invMintAlg.getInvMintAlgName() + ".dfa.dot";
        dfa.exportDotAndPng(exportFname);

        return invMintAlg.compareToStandardAlg();
    }

    private static PGraphInvariMint setUpAndGetAlg(InvariMintOptions opts)
            throws Exception {

        setUpLogging(opts);
        handleOptions(opts);

        PGraphInvariMint invMintAlg;
        if (opts.invMintSynoptic) {
            // Instantiate a Synoptic version of InvariMint.
            invMintAlg = new InvariMintSynoptic(opts);
        } else if (opts.invMintKTails) {
            // Instantiate a KTails version of InvariMint.
            invMintAlg = new InvariMintKTails(opts);
        } else {
            throw new Exception("InvariMint algorithm not specified.");
        }
        return invMintAlg;
    }

    private static void runStandardAlg(PGraphInvariMint invMintAlg) {
        logger.info("Running Standard Alg.");
        long startTime = System.nanoTime();
        long endTime;
        try {
            invMintAlg.runStdAlg();
        } finally {
            endTime = System.nanoTime();
        }
        // Convert nanoseconds to seconds
        double duration_secs = (endTime - startTime) / 1000000000.0;
        logger.info("DONE Running Standard Alg. Duration = " + duration_secs);
    }

    private static InvsModel runInvariMint(PGraphInvariMint invMintAlg)
            throws Exception {
        InvsModel dfa;
        logger.info("Running InvariMint Alg.");
        long startTime = System.nanoTime();
        long endTime;
        try {
            // Run the appropriate version of InvariMint.
            dfa = invMintAlg.runInvariMint();
        } finally {
            endTime = System.nanoTime();
        }
        // Convert nanoseconds to seconds
        double duration_secs = (endTime - startTime) / 1000000000.0;
        logger.info("DONE Running InvariMint Alg. Duration = " + duration_secs);

        return dfa;
    }

    /**
     * Performs InvariMint with the given set of options, returns the final dfa.
     */
    public static EncodedAutomaton runInvariMint(InvariMintOptions opts)
            throws Exception {

        PGraphInvariMint invMintAlg = setUpAndGetAlg(opts);
        runStandardAlg(invMintAlg);
        InvsModel dfa = runInvariMint(invMintAlg);

        // Optionally remove paths from the model not found in any input trace.
        if (opts.removeSpuriousEdges) {
            invMintAlg.removeSpuriousEdges();
        }

        // //////////// Below, only output (non-transforming) methods:

        // Export final model.
        String exportFname = opts.outputPathPrefix + "."
                + invMintAlg.getInvMintAlgName() + ".dfa.dot";
        dfa.exportDotAndPng(exportFname);

        // Export each of the mined DFAs (except NIFby invariants).
        if (opts.exportMinedInvariantDFAs) {
            int invID = 0;
            String path;
            for (InvModel invDFA : dfa.getInvariants()) {
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
            invMintAlg.compareToStandardAlg();
        }

        return dfa;
    }

    public static void setUpLogging(InvariMintOptions opts) {
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

    public static void handleOptions(InvariMintOptions opts) throws Exception {
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
            err = "Cannot output any models. Specify output path prefix using:\n\t"
                    + opts.getOptDesc("outputPathPrefix");
            logger.severe(err);
        }

        if (opts.logFilenames.size() == 0) {
            err = "No log filenames specified, exiting. Try cmd line option:\n\t"
                    + opts.getOptDesc("help");
            logger.severe(err);
        }

        if ((!opts.invMintSynoptic && !opts.invMintKTails)
                || (opts.invMintSynoptic && opts.invMintKTails)) {
            err = "Must specify either --invMintSynoptic or --invMintKTails option, but not both.";
            logger.severe(err);
        }

        if (err != null) {
            throw new OptionException();
        }
    }
}