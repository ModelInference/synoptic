package synoptic.main;

import java.io.IOException;
import java.net.URISyntaxException;

import synoptic.invariants.TemporalInvariantSet;
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
        // TODO: implement with new parsers to run with exporting intermediate
        // models. Modify Perfume so each event can be associated with multiple
        // metrics. During refinement process, allow options to output the
        // intermediate model to file and accept an input file describing the
        // refinement steps.
        return null;
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
