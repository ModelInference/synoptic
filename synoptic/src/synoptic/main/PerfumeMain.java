package synoptic.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Random;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ConstrainedInvMiner;
import synoptic.main.options.AbstractOptions;
import synoptic.main.options.PerfumeOptions;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphExportFormatter;
import synoptic.util.InternalSynopticException;

/**
 * Contains entry points for the command line version of Perfume.
 */
public class PerfumeMain extends AbstractMain {

    /**
     * Return the singleton instance of PerfumeMain, first asserting that the
     * instance isn't null.
     */
    public static PerfumeMain getInstance() {
        assert (instance != null);
        assert (instance instanceof PerfumeMain);
        return (PerfumeMain) instance;
    }

    /**
     * The synoptic.main method to perform the Perfume inference algorithm. See
     * user documentation for an explanation of the options.
     * 
     * @param args
     *            Command-line options
     */
    public static void main(String[] args) throws Exception {
        PerfumeMain mainInstance = processArgs(args);
        if (mainInstance == null) {
            return;
        }

        try {
            Locale.setDefault(Locale.US);

            PartitionGraph pGraph = mainInstance.createInitialPartitionGraph();
            if (pGraph != null) {
                mainInstance.runSynoptic(pGraph);
            }
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw InternalSynopticException.wrap(e);
        }
    }

    /**
     * Parses the set of arguments to the program, to set up static state in
     * PerfumeMain. This state includes everything necessary to run Perfume --
     * input log files, regular expressions, etc. Returns null if there is a
     * problem with the parsed options.
     * 
     * @param args
     *            Command line arguments that specify how Perfume should behave.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    public static PerfumeMain processArgs(String[] args) throws IOException,
            URISyntaxException, IllegalArgumentException,
            IllegalAccessException, ParseException {
        // Parse and process command line options
        AbstractOptions options = new PerfumeOptions(args).toAbstractOptions();
        GraphExportFormatter graphExportFormatter = processArgs(options);
        if (graphExportFormatter == null) {
            return null;
        }

        // Construct and return main object
        PerfumeMain newMain = new PerfumeMain(options, graphExportFormatter);
        return newMain;
    }

    /**
     * Constructor that simply stores parameters in fields and initializes the
     * pseudo RNG.
     * 
     * @param opts
     *            Processed options from the command line
     * @param graphExportFormatter
     *            Graph export formatter for outputting the model
     */
    public PerfumeMain(AbstractOptions opts,
            GraphExportFormatter graphExportFormatter) {
        setUpLogging(opts);

        if (AbstractMain.instance != null) {
            throw new RuntimeException(
                    "Cannot create multiple instance of singleton synoptic.main.AbstractMain");
        }
        this.options = opts;
        this.graphExportFormatter = graphExportFormatter;
        this.random = new Random(opts.randomSeed);
        logger.info("Using random seed: " + opts.randomSeed);
        AbstractMain.instance = this;
    }

    @Override
    public TemporalInvariantSet mineTOInvariants(
            boolean useTransitiveClosureMining, ChainsTraceGraph traceGraph) {

        if (useTransitiveClosureMining) {
            logger.warning("Using transitive closure mining was requested, but this is not supported by Perfume. Continuing without transitive closure mining.");
        }

        // Mine unconstrained Synoptic invariants
        TemporalInvariantSet unconstrainedInvs = mineTOInvariantsCommon(false,
                traceGraph);

        // Mine performance-constrained invariants
        long startTime = loggerInfoStart("Mining performance-constrained invariants...");
        ConstrainedInvMiner constrainedMiner = new ConstrainedInvMiner();

        // Augment unconstrained invariants with performance information. A
        // 'false' parameter is hard-coded because Perfume does not support the
        // multipleRelations flag.
        TemporalInvariantSet allInvs = constrainedMiner.computeInvariants(
                traceGraph, false, unconstrainedInvs);

        loggerInfoEnd("Constrained mining took ", startTime);

        return allInvs;
    }
}
