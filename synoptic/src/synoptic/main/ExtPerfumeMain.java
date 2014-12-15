package synoptic.main;

import java.io.IOException;
import java.net.URISyntaxException;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ConstrainedInvMiner;
import synoptic.main.options.AbstractOptions;
import synoptic.main.options.ExtPerfumeOptions;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphExportFormatter;

/**
 * The extended PerfumeMain with features to export intermediate models to
 * files, and perform refinement as specified via an input file, and support
 * multiple performance metrics per event transition.
 */
public class ExtPerfumeMain extends PerfumeMain {

    /**
     * Return the singleton instance of ExtPerfumeMain, first asserting that the
     * instance isn't null.
     */
    public static ExtPerfumeMain getInstance() {
        assert (instance != null);
        assert (instance instanceof ExtPerfumeMain);
        return (ExtPerfumeMain) instance;
    }

    public ExtPerfumeMain(AbstractOptions opts,
            GraphExportFormatter graphExportFormatter) {
        super(opts, graphExportFormatter);
    }

    /**
     * Parses the set of arguments to the program, to set up static state in
     * ExtPerfumeMain. This state includes everything necessary to run Extended
     * Perfume -- input log files, regular expressions, etc. Returns null if
     * there is a problem with the parsed options.
     * 
     * @param args
     *            Command line arguments that specify how Extended Perfume
     *            should behave.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    public static ExtPerfumeMain processArgs(String[] args) throws IOException,
            URISyntaxException, IllegalArgumentException,
            IllegalAccessException, ParseException {
        // Parse and process command line options
        AbstractOptions options = new ExtPerfumeOptions(args)
                .toAbstractOptions();
        GraphExportFormatter graphExportFormatter = processArgs(options);
        if (graphExportFormatter == null) {
            return null;
        }

        // Construct and return main object
        ExtPerfumeMain newMain = new ExtPerfumeMain(options,
                graphExportFormatter);
        return newMain;
    }

    /**
     * Uses the values of static variables in Main to (1) read and parse the
     * input log files, (2) to mine invariants from the parsed files, and (3)
     * construct an initial partition graph model of the parsed files.
     * 
     * @return The initial partition graph built from the parsed files or null.
     *         Returns null when the arguments passed to Main require an early
     *         termination.
     * @throws Exception
     */
    public PartitionGraph createInitialPartitionGraph() throws Exception {
        // TODO modify to use multi resource parsing
        return null;
    }

    /**
     * Mines and returns the totally ordered invariants from the trace graph of
     * the input log. Extended Perfume will mine multiple performance metrics
     * per event.
     * 
     * @param useTransitiveClosureMining
     * @param traceGraph
     * @return
     */
    @Override
    public TemporalInvariantSet mineTOInvariants(
            boolean useTransitiveClosureMining, ChainsTraceGraph traceGraph) {
        if (useTransitiveClosureMining) {
            logger.warning("Using transitive closure mining was requested, but this is not supported by Perfume. Continuing without transitive closure mining.");
        }

        // Mine unconstrained Synoptic invariants
        TemporalInvariantSet unconstrainedInvs = mineTOInvariantsCommon(false,
                traceGraph);

        // TODO: implement with new parsers to run with exporting intermediate
        // models. Modify Perfume so each event can be associated with multiple
        // metrics. During refinement process, allow options to output the
        // intermediate model to file and accept an input file describing the
        // refinement steps.

        // TraceParser mine time as resource constraint. We need to change this
        // to parse multiple resources of given name.
        // Should we extend this or make a completely new parser???
        // Also change ConstrainedInvMiner so that invariants can be augmented
        // with multiple resource metrics.

        // Event and EventNode also needs to be changed so resources metrics can
        // be stored. Perhaps we need a class for storing multiple resource
        // metrics?

        // Mine performance-constrained invariants TODO above
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

    /**
     * Runs the Extended Perfume Synoptic algorithm starting from the initial
     * graph (pGraph). The pGraph is assumed to be fully initialized and ready
     * for refinement. The Extended Perfume Synoptic algorithm outputs an
     * intermediate model and waits for refinement instructions until the model
     * is fully refined, or until instructed to stop.
     * 
     * @param pGraph
     *            The initial graph model to start refining.
     */
    @Override
    public void runSynoptic(PartitionGraph pGraph) {
        // TODO: design the refinement process to allow intermediate export and
        // refinement command.
    }

}
