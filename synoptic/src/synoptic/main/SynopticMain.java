package synoptic.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Random;

import synoptic.main.options.AbstractOptions;
import synoptic.main.options.SynopticOptions;
import synoptic.main.parser.ParseException;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphExportFormatter;
import synoptic.util.InternalSynopticException;

/**
 * Contains entry points for the command line version of Synoptic, as well as
 * for libraries that want to use Synoptic from a jar.
 */
public class SynopticMain extends AbstractMain {

    /**
     * Return the singleton instance of SynopticMain, first asserting that the
     * instance isn't null.
     */
    public static SynopticMain getInstance() {
        assert (instance != null);
        assert (instance instanceof SynopticMain);
        return (SynopticMain) instance;
    }

    /**
     * The synoptic.main method to perform the Synoptic inference algorithm. See
     * user documentation for an explanation of the options.
     * 
     * @param args
     *            Command-line options
     */
    public static void main(String[] args) throws Exception {
        SynopticMain mainInstance = processArgs(args);
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
     * SynopticMain. This state includes everything necessary to run Synoptic --
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
    public static SynopticMain processArgs(String[] args) throws IOException,
            URISyntaxException, IllegalArgumentException,
            IllegalAccessException, ParseException {
        // Parse and process command line options
        AbstractOptions options = new SynopticOptions(args).toAbstractOptions();
        GraphExportFormatter graphExportFormatter = processArgs(options);
        if (graphExportFormatter == null) {
            return null;
        }

        // Construct and return main object
        SynopticMain newMain = new SynopticMain(options, graphExportFormatter);
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
    public SynopticMain(AbstractOptions opts,
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
}