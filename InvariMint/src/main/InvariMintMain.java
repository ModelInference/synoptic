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
import synoptic.main.options.Options;

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
     * Main entrance into the application. Application arguments (args) are
     * processed using Synoptic's build-in argument parser, extended with a few
     * InvDFAMinimization-specific arguments. For more information, see
     * InvDFAMinimizationOptions class.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        InvariMintOptions opts = new InvariMintOptions(args);
        EncodedAutomaton dfa = runInvariMint(opts);

        // Export final model.
        dfa.exportDotAndPng(opts.outputPathPrefix + ".invarimintDFA.dot");
    }

    /**
     * Performs InvariMint with the given set of options, returns the final dfa.
     */
    public static EncodedAutomaton runInvariMint(InvariMintOptions opts)
            throws Exception {

        setUpLogging(opts);
        handleOptions(opts);

        InvsModel dfa;
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

        // Run the appropriate version of InvariMint.
        dfa = invMintAlg.runInvariMint();

        // Optionally remove paths from the model not found in any input trace.
        if (opts.removeSpuriousEdges) {
            invMintAlg.removeSpuriousEdges();
        }

        // //////////// Below, only output (non-transforming) methods:

        // Export each of the mined DFAs (except NIFby invariants).
        if (opts.exportMinedInvariantDFAs) {
            int invID = 0;
            String path;
            for (InvModel invDFA : dfa.getInvariants()) {
                if (!(invDFA.getInvariant() instanceof NeverImmediatelyFollowedInvariant)) {
                    path = opts.outputPathPrefix + ".InvDFA" + invID + ".dot";
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

    public static void handleOptions(InvariMintOptions opts) {
        // Display help for all option groups, including unpublicized ones
        if (opts.allHelp) {
            opts.printLongHelp();
            System.exit(0);
        }

        // Display help just for the 'publicized' option groups
        if (opts.help) {
            opts.printShortHelp();
            System.exit(0);
        }

        if (opts.outputPathPrefix == null) {
            logger.warning("Cannot output initial graph. Specify output path prefix using:\n\t"
                    + Options.getOptDesc("outputPathPrefix"));
            System.exit(0);
        }

        if (opts.logFilenames.size() == 0) {
            logger.severe("No log filenames specified, exiting. Try cmd line option:\n\t"
                    + synoptic.main.options.Options.getOptDesc("help"));
            System.exit(0);
        }

        if ((!opts.invMintSynoptic && !opts.invMintKTails)
                || opts.invMintSynoptic && opts.invMintKTails) {

            logger.severe("Must specify either invMintSynoptic or invMintKTails option, but not both.");
        }
    }

}