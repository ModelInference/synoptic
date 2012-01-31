package main;

import java.util.logging.Logger;

import plume.Option;
import plume.OptionGroup;
import plume.Options;
import plume.Options.ArgException;

import synoptic.main.SynopticOptions;

/**
 * Options relevant to the InvDFAMinimization project.
 */
public class InvDFAMinimizationOptions {
    private static Logger logger = Logger
            .getLogger("InvDFAMinimizationOptions Logger");

    /**
     * Initial model output filename.
     */
    @OptionGroup("InvDFAMinimization Options")
    @Option(value = "-i Initial model output filename",
            aliases = { "-initialModelFile" })
    public String initialModelFile = "initialDfaModel.dot";

    /**
     * Final model output filename.
     */
    @Option(value = "-f Final model output filename",
            aliases = { "-finalModelFile" })
    public String finalModelFile = "finalDfaModel.dot";

    /**
     * Translated Synoptic model output filename.
     */
    @Option(value = "-s Synoptic model output filename",
            aliases = { "-synopticModelFile" })
    public String synopticModelFile = "convertedSynopticModel.dot";

    private Options plumeOptions;

    public InvDFAMinimizationOptions(String[] args) {
        // Sets the fields in this class annotated with @Option
        plumeOptions = new Options(SynopticOptions.usage_string, this);
        try {
            plumeOptions.parse(args);
        } catch (ArgException e) {
            // Ignore exceptions due unknown options.
            // TODO: Is there a way to disambiguate between ArgException due to
            // unknown options, versus those that are due to errors in specific
            // options.
            logger.warning("Problem parsing options: " + e.getMessage());
        }
    }
}
