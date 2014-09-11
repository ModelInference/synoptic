package synoptic.main;

import synoptic.main.options.AbstractOptions;
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

    // TODO: implement with new parsers to run with exporting intermediate
    // models. Modify Perfume so each event can be associated with multiple
    // metrics. During refinement process, allow options to output the
    // intermediate model to file and accept an input file describing the
    // refinement steps.

    // TODO: design the refinement process to allow intermediate export and
    // refinement command.

}
