package synoptic.main;

import java.io.IOException;
import java.net.URISyntaxException;

import synoptic.main.options.AbstractOptions;
import synoptic.main.options.ExtPerfumeOptions;
import synoptic.main.parser.ParseException;
import synoptic.model.export.GraphExportFormatter;

/**
 * The extended PerfumeMain with features to export intermediate models to
 * files, and perform refinement as specified via an input file, and support
 * multiple performance metrics per event transition.
 */
public class ExtPerfumeMain extends PerfumeMain {

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

    // TODO: implement with new parsers to run with exporting intermediate
    // models. Modify Perfume so each event can be associated with multiple
    // metrics. During refinement process, allow options to output the
    // intermediate model to file and accept an input file describing the
    // refinement steps.

    // TODO: design the refinement process to allow intermediate export and
    // refinement command.

}
