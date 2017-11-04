package synoptic.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Random;

import synoptic.algorithms.Bisimulation;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.options.AbstractOptions;
import synoptic.main.options.KTailsOptions;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphExportFormatter;
import synoptic.util.InternalSynopticException;

/**
 * Contains the command line entry point to run Synoptic's implementation of the
 * k-tails algorithm.
 */
/**
 * @author ohmann
 */
public class KTailsMain extends AbstractMain {

    /**
     * Return the singleton instance of KTailsMain, first asserting that the
     * instance isn't null.
     */
    public static KTailsMain getInstance() {
        assert (instance != null);
        assert (instance instanceof KTailsMain);
        return (KTailsMain) instance;
    }

    /**
     * The synoptic.main method to perform the k-tails inference algorithm. See
     * user documentation for an explanation of the options.
     * 
     * @param args
     *            Command-line options
     */
    public static void main(String[] args) throws Exception {
        KTailsMain mainInstance = processArgs(args);
        if (mainInstance == null) {
            return;
        }

        try {
            Locale.setDefault(Locale.US);

            // Parse traces into a chains trace graph
            ChainsTraceGraph traceGraph = mainInstance.parseIntoTraceGraph();
            if (traceGraph == null) {
                return;
            }
            long startTime = loggerInfoStart(
                    "Creating initial partition graph");
            PartitionGraph pGraph = new PartitionGraph(traceGraph, false, null);
            loggerInfoEnd("Creating partition graph took ", startTime);

            runKTails(pGraph, 2);
            mainInstance.exportGraph(pGraph);
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw InternalSynopticException.wrap(e);
        }
    }

    /**
     * Parses the set of arguments to the program, to set up static state in
     * KTailsMain. This state includes everything necessary to run Synoptic --
     * input log files, regular expressions, etc. Returns null if there is a
     * problem with the parsed options.
     * 
     * @param args
     *            Command line arguments that specify how Synoptic should
     *            behave.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    public static KTailsMain processArgs(String[] args)
            throws IOException, URISyntaxException, IllegalArgumentException,
            IllegalAccessException, ParseException {
        // Parse and process command line options
        AbstractOptions options = new KTailsOptions(args).toAbstractOptions();
        GraphExportFormatter graphExportFormatter = processArgs(options);
        if (graphExportFormatter == null) {
            return null;
        }

        // Construct and return main object
        KTailsMain newMain = new KTailsMain(options, graphExportFormatter);
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
    public KTailsMain(AbstractOptions opts,
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
        return new TemporalInvariantSet();
    }

    /**
     * Run the k-tails algorithm on {@code pGraph} using k-equivalence for the
     * specified {@code k} value
     * 
     * @param pGraph
     * @param k
     */
    private static void runKTails(PartitionGraph pGraph, int k) {
        long startTime = loggerInfoStart("Running k-tails...");
        Bisimulation.mergePartitions(pGraph, null, k);
        loggerInfoEnd("K-tails took ", startTime);
    }
}