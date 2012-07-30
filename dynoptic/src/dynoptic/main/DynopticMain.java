package dynoptic.main;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mcscm.McScM;
import dynoptic.model.fifosys.gfsm.GFSM;
import dynoptic.model.fifosys.gfsm.GFSMState;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.util.Pair;

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
     * Runs Dynoptic based on setting in opts, but uses the pre-created GFSM
     * with observations and a set of invariants invs. Satisfies all of the
     * invariants invs in g.
     * 
     * @param g
     *            A fifosys model that partitions concrete observations into
     *            abstract states.
     * @param invs
     *            Invariants to satisfy in g.
     */
    public void run(GFSM g, TemporalInvariantSet invs) {
        for (ITemporalInvariant inv : invs) {
            // TODO
            // 1. Iterate over invs.
        }
    }

    /**
     * Converts a temporal invariant inv into a set S of pairs of GFSMStates.
     * This set S satisfies the condition that:
     * 
     * <pre>
     * inv is true iff \forall <p,q> \in S there is no path from p to q in g.
     * </pre>
     * 
     * @param g
     * @return
     */
    public Set<Pair<GFSMState, GFSMState>> invToBadStates(GFSM g,
            ITemporalInvariant inv) {
        // TODO: assert that inv is composed of events that are in g's alphabet
        // assert g.getAlphabet().contains(
        Set<Pair<GFSMState, GFSMState>> ret = new LinkedHashSet<Pair<GFSMState, GFSMState>>();

        // The basic strategy, regardless of invariant, is to create a
        // separate FIFO queue that will be used to record the sequence of
        // executed events that are relevant to the invariant.
        //
        // For instance, for a AFby b invariant, create a queue Q_ab. Modify any
        // state p that has an outgoing "a" transition, add a synthetic state
        // p_synth, and redirect the "a" transition from p to p_synth. Then, add
        // just one outgoing transition on "Q_ab ! a" from p_synth to the
        // original state target of "a" in state p. That is, whenever "a"
        // occurs, we will add "a" to Q_ab. Do the same for event "b".
        //
        // For a AFby b bad state pairs within the modified GFSM (per above
        // procedure) are all initial state and all states where all queues
        // except Q_ab are empty, and where Q_ab = [*a], and where the process
        // states are terminal. In a sense, we've added Q_ab to track "a" and
        // "b" executions, and not interfere with the normal execution of the
        // FIFO system.
        //
        // For a AP b, the procedure is identical, but the second bad state in
        // every pair would have Q_ab = [b*]. For a NFby b, Q_ab = [*a*b*]. In a
        // sense, we've expressed LTL properties as regular expressions of Q_ab
        // queue contents.

        // TODO:

        return ret;
    }

    /**
     * <p>
     * Refines g until there is no path from badStates.left to badStates.right
     * in g. Throws an exception if (1) no such refinement is possible, or (2)
     * if the model checker that checks the abstract model corresponding to g
     * (i.e., the CFSM derived from g) has exceeded an execution time-bound.
     * </p>
     * <p>
     * NOTE: this method mutates g.
     * </p>
     * 
     * @param g
     * @param badStates
     */
    public void run(GFSM g, Pair<GFSMState, GFSMState> badStates) {
        // TODO
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
