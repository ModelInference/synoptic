package synoptic.main;

import java.util.Random;

import synoptic.main.options.SynopticOptions;
import synoptic.model.export.GraphExportFormatter;

/**
 * Contains entry points for the command line version of Synoptic, as well as
 * for libraries that want to use Synoptic from a jar. The instance of Main is a
 * singleton that maintains Synoptic options, and other state for a single run
 * of Synoptic.
 */
public class SynopticMain extends AbstractMain {

    public SynopticMain(SynopticOptions opts,
            GraphExportFormatter graphExportFormatter) {
        setUpLogging(opts);

        if (AbstractMain.instance != null) {
            throw new RuntimeException(
                    "Cannot create multiple instance of singleton synoptic.main.Main");
        }
        this.options = opts;
        this.graphExportFormatter = graphExportFormatter;
        this.random = new Random(opts.randomSeed);
        logger.info("Using random seed: " + opts.randomSeed);
        AbstractMain.instance = this;
    }
}