package synoptic.main.options;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import plume.Option;
import plume.OptionGroup;

import synoptic.main.AbstractMain;

/**
 * <p>
 * This class defines and maintains command line arguments to Synoptic's
 * implementation of the k-tails algorithm. It uses plume-lib for defining
 * command line options, their types, and the corresponding help messages. This
 * library also provides support for parsing and populating instances of these
 * options. All options can be exported to AbstractOptions (the common options
 * for all Synoptic projects) using toAbstractOptions().
 * <p>
 * Options common between this and other options classes cannot be pushed up
 * into a superclass because plume-lib doesn't support inheritance.
 */
public class KTailsOptions extends Options {

    // Features that k-tails doesn't support
    public final static String separateVTimeIndexSets = null;
    public final static boolean multipleRelations = false;
    public final static boolean stateProcessing = false;
    public final static boolean testGeneration = false;
    public final static int supportCountThreshold = 0;
    public final static boolean ignoreIntrByInvs = false;
    public final static boolean ignoreNFbyInvs = false;
    public final static boolean usePerformanceInfo = false;
    public final static boolean traceNormalization = false;
    public final static boolean outputSupportCount = false;
    public final static boolean outputInvariantsToFile = false;
    public final static boolean dumpInvariants = false;
    public final static String ignoreInvsOverETypeSet = null;
    public final static boolean useTransitiveClosureMining = false;
    public final static boolean mineNeverConcurrentWithInv = false;
    public final static boolean onlyMineInvariants = false;
    public final static boolean noCoarsening = false;
    public final static boolean noRefinement = false;

    // //////////////////////////////////////////////////
    /**
     * Print the short usage message. This does not include verbosity or
     * debugging options.
     */
    @OptionGroup("General Options")
    @Option(value = AbstractOptions.helpStr, aliases = { "-help" })
    public boolean help = false;

    /**
     * Print the extended usage message. This includes verbosity and debugging
     * options but not internal options.
     */
    @Option(AbstractOptions.allHelpStr)
    public boolean allHelp = false;

    /**
     * Print the current Synoptic version.
     */
    @Option(value = AbstractOptions.versionStr, aliases = { "-version" })
    public boolean version = false;
    // end option group "General Options"

    // //////////////////////////////////////////////////
    /**
     * Be quiet, do not print much information. Sets the log level to WARNING.
     */
    @OptionGroup("Execution Options")
    @Option(value = AbstractOptions.logLvlQuietStr, aliases = { "-quiet" })
    public boolean logLvlQuiet = false;

    /**
     * Be verbose, print extra detailed information. Sets the log level to FINE.
     */
    @Option(value = AbstractOptions.logLvlVerboseStr, aliases = { "-verbose" })
    public boolean logLvlVerbose = false;

    /**
     * Sets the random seed for Synoptic's source of pseudo-random numbers.
     */
    @Option(AbstractOptions.randomSeedStr)
    public Long randomSeed = null;

    /**
     * The k parameter for determining k-equality when running the k-tails
     * algorithm
     */
    // @Option(AbstractOptions.kStr)
    // public int k = 2;

    // //////////////////////////////////////////////////
    /**
     * Regular expression separator string. When lines are found which match
     * this expression, the lines before and after are considered to be in
     * different 'traces', each to be considered an individual sample of the
     * behavior of the system. This is implemented by augmenting the separator
     * expression with an incrementor, (?<SEPCOUNT++>), and adding \k <SEPCOUNT>
     * to the partitioner.
     */
    @OptionGroup("Parser Options")
    @Option(value = AbstractOptions.separatorRegExpStr,
            aliases = { "-partition-separator" })
    public String separatorRegExp = null;

    /**
     * Regular expressions used for parsing the trace file. This parameter may,
     * and is often repeated, in order to express the different formats of log
     * lines which should be parsed. The ordering is significant, and matching
     * is attempted in the order in which the expressions are given. These
     * 'regular' expressions are a bit specialized, in that they have named
     * group matches of the form (?<name>regex), in order to extract the
     * significant components of the log line. There are a few more variants on
     * this, detailed in the online documentation.
     */
    @Option(value = AbstractOptions.regExpsStr, aliases = { "-regexp" })
    public List<String> regExps = null;

    /**
     * A substitution expression, used to express how to map the trace lines
     * into partition traces, to be considered as an individual sample of the
     * behavior of the system.
     */
    @Option(value = AbstractOptions.partitionRegExpStr,
            aliases = { "-partition-mapping" })
    public String partitionRegExp = AbstractOptions.partitionRegExpDefault;

    /**
     * This option relieves the user from writing regular expressions to parse
     * lines that they are not interested in. This also help to avoid parsing of
     * lines that are corrupted.
     */
    @Option(AbstractOptions.ignoreNonMatchingLinesStr)
    public boolean ignoreNonMatchingLines = false;

    // Option that causes k-tails to run instead of Synoptic or Perfume
    public final static boolean onlyRunKTails = true;

    /**
     * Synoptic usually sorts events within a trace by supplied resource values,
     * assumed to be time
     */
    @Option(value = AbstractOptions.keepOrderStr)
    public boolean keepOrder = false;

    /**
     * This allows users to get away with sloppy\incorrect regular expressions
     * that might not fully cover the range of log lines appearing in the log
     * files.
     */
    @Option(value = AbstractOptions.recoverFromParseErrorsStr,
            aliases = { "-ignore-parse-errors" })
    public boolean recoverFromParseErrors = false;

    /**
     * Output the fields extracted from each log line and terminate.
     */
    @Option(value = AbstractOptions.debugParseStr, aliases = { "-debugParse" })
    public boolean debugParse = false;

    /**
     * Pattern defining the format of dates within a log (required by DATETIME)
     */
    @Option(value = AbstractOptions.dateFormatStr, aliases = { "-dateFormat" })
    public String dateFormat = "dd/MMM/yyyy:HH:mm:ss";
    // end option group "Parser Options"

    // //////////////////////////////////////////////////
    /**
     * Command line arguments input filename to use.
     */
    @OptionGroup("Input Options")
    @Option(value = AbstractOptions.argsFilenameStr, aliases = { "-argsfile" })
    public String argsFilename = null;
    // end option group "Input Options"

    // //////////////////////////////////////////////////
    /**
     * Specifies the prefix of where to store the final Synoptic representation
     * output. This prefix is also used to determine filenames of intermediary
     * files, like corresponding dot file and intermediate stage representations
     * (if specified, e.g. with --dumpIntermediateStages).
     */
    @OptionGroup("Output Options")
    @Option(value = AbstractOptions.outputPathPrefixStr,
            aliases = { "-output-prefix" })
    public String outputPathPrefix = null;

    /**
     * Do not output the final model unless a format is explicitly requested.
     */
    @Option(AbstractOptions.noModelOutputStr)
    public boolean noModelOutput = false;

    /**
     * Whether or not models should be exported as GML (graph modeling language)
     * files (the default format is DOT file format).
     */
    @Option(value = AbstractOptions.exportAsGMLStr,
            aliases = { "-export-as-gml" })
    public boolean exportAsGML = false;

    /**
     * Output the LTS representation of the final model to the output prefix
     * specified by -o or -output-prefix.
     */
    @Option(value = AbstractOptions.outputLTSStr, aliases = { "-lts" })
    public boolean outputLTS = false;

    /**
     * The absolute path to the dot command executable to use for outputting
     * graphical representations of Synoptic models
     */
    @Option(value = AbstractOptions.dotExecutablePathStr,
            aliases = { "-dot-executable" })
    public String dotExecutablePath = null;

    /**
     * Whether or not probabilities are displayed on edge labels
     */
    @Option(value = AbstractOptions.outputProbLabelsStr,
            aliases = { "-outputProbLabels" })
    public boolean outputProbLabels = true;

    /**
     * Whether or not transition counts are displayed on edge labels
     */
    @Option(value = AbstractOptions.outputCountLabelsStr,
            aliases = { "-outputCountLabels" })
    public boolean outputCountLabels = false;

    /**
     * Whether or not the output graphs include the common TERMINAL state, to
     * which all final trace nodes have an edge.
     */
    @Option(AbstractOptions.showTerminalNodeStr)
    public boolean showTerminalNode = true;

    /**
     * Whether or not the output graphs include the common INITIAL state, which
     * has an edge to all the start trace nodes.
     */
    @Option(AbstractOptions.showInitialNodeStr)
    public boolean showInitialNode = true;

    /**
     * Output a JSON object of the final model to the output prefix specified by
     * -o or -output-prefix.
     */
    @Option(value = AbstractOptions.outputJSONStr, aliases = { "-output-json" })
    public boolean outputJSON = false;

    // end option group "Output Options"

    // //////////////////////////////////////////////////
    /**
     * Dump the complete list of mined synoptic.invariants for the set of input
     * files to stdout. This option is <i>unpublicized</i>; it will not appear
     * in the default usage message
     */
    @OptionGroup(value = "Verbosity Options", unpublicized = true)

    /**
     * Dump the DOT representation of the parsed trace graph to file. The file
     * will have the name <outputPathPrefix>.tracegraph.dot, where
     * 'outputPathPrefix' is the filename of the final Synoptic output. This
     * option is <i>unpublicized</i>; it will not appear in the default usage
     * message
     */
    @Option(AbstractOptions.dumpTraceGraphDotFileStr)
    public boolean dumpTraceGraphDotFile = false;

    /**
     * Dump PNG of parsed trace graph to file. The file will have the name
     * <outputPathPrefix>.tracegraph.dot.png, where 'outputPathPrefix' is the
     * filename of the final Synoptic output. This option is <i>unpublicized</i>
     * ; it will not appear in the default usage message
     */
    @Option(AbstractOptions.dumpTraceGraphPngFileStr)
    public boolean dumpTraceGraphPngFile = false;

    /**
     * Dumps PNG of initial condensed partition graph to file. The file will
     * have the name <outputPathPrefix>.condensed.dot.png, where
     * 'outputPathPrefix' is the filename of the final Synoptic output. This
     * option is <i>unpublicized</i>; it will not appear in the default usage
     * message.
     */
    @Option(AbstractOptions.dumpInitialPartitionGraphStr)
    public boolean dumpInitialPartitionGraph = false;

    /**
     * Dump the dot representations for intermediate Synoptic steps to file.
     * Each of these files will have a name like:
     * outputPathPrefix.stage-S.round-R.dot where 'outputPathPrefix' is the
     * filename of the final Synoptic output, 'S' is the name of the stage (e.g.
     * r for refinement, and c for coarsening), and 'R' is the round number
     * within the stage. This option requires that the outputPathPrefix is set
     * with the -o option (see above). This option is <i>unpublicized</i>; it
     * will not appear in the default usage message
     */
    @Option(AbstractOptions.dumpIntermediateStagesStr)
    public boolean dumpIntermediateStages = false;
    // end option group "Verbosity Options"

    // //////////////////////////////////////////////////
    @OptionGroup(value = "Debugging Options", unpublicized = true)
    /**
     * Be extra verbose, print extra detailed information. Sets the log level to
     * FINEST.
     */
    @Option(AbstractOptions.logLvlExtraVerboseStr)
    public boolean logLvlExtraVerbose = false;

    /**
     * Perform benchmarking and output benchmark information. This option is
     * <i>unpublicized</i>; it will not appear in the default usage message
     */
    @Option(AbstractOptions.doBenchmarkingStr)
    public boolean doBenchmarking = false;

    /**
     * Intern commonly occurring strings, such as event types, as a memory-usage
     * optimization. This option is <i>unpublicized</i>; it will not appear in
     * the default usage message
     */
    @Option(AbstractOptions.internCommonStringsStr)
    public boolean internCommonStrings = true;

    /**
     * Run all tests in synoptic.tests.units -- all the unit tests, and then
     * terminate. This option is <i>unpublicized</i>; it will not appear in the
     * default usage message
     */
    @Option(AbstractOptions.runTestsStr)
    public boolean runTests = false;

    /**
     * Run all tests in synoptic.tests -- unit and integration tests, and then
     * terminate. This option is <i>unpublicized</i>; it will not appear in the
     * default usage message
     */
    @Option(AbstractOptions.runAllTestsStr)
    public boolean runAllTests = false;

    /**
     * Turns on correctness checks that are disabled by default due to their
     * expensive cpu\memory usage profiles.
     */
    @Option(AbstractOptions.performExtraChecksStr)
    public boolean performExtraChecks = false;
    // end option group "Debugging Options"

    /** One line synopsis of usage */
    public static final String usageString = "ktails [options] <logfiles-to-analyze>";

    /**
     * Use this constructor to create a blank set of options, that can then be
     * populated manually, one at a time. This is useful when k-tails is used as
     * a library or in tests, and options do not come from the command line.
     */
    public KTailsOptions() {
        randomSeed = System.currentTimeMillis();
        logFilenames = new LinkedList<String>();
    }

    /**
     * This constructor is used to actually process input command line
     * arguments. If the randomSeed option is not specified, this constructor
     * initializes it.
     * 
     * @param args
     *            an array of command line arguments
     * @throws IOException
     */
    public KTailsOptions(String[] args) throws IOException {
        plumeOptions = new plume.Options(getUsageString(), this);
        setOptions(args);
        if (randomSeed == null) {
            randomSeed = System.currentTimeMillis();
        }
    }

    /**
     * Prints help for all option groups, including unpublicized ones.
     */
    public void printLongHelp() {
        System.out.println("Usage: " + getUsageString());
        System.out.println(plumeOptions.usage("General Options",
                "Execution Options", "Parser Options", "Input Options",
                "Output Options", "Verbosity Options", "Debugging Options"));
    }

    @Override
    public String getUsageString() {
        return usageString;
    }

    @Override
    public String getArgsFilename() {
        return argsFilename;
    }

    public AbstractOptions toAbstractOptions() {
        AbstractOptions absOpts = new AbstractOptions();

        // General options

        absOpts.help = help;
        absOpts.allHelp = allHelp;
        absOpts.version = version;

        // Execution options

        absOpts.logLvlQuiet = logLvlQuiet;
        absOpts.logLvlVerbose = logLvlVerbose;
        absOpts.randomSeed = randomSeed;
        AbstractOptions.separateVTimeIndexSets = separateVTimeIndexSets;
        absOpts.multipleRelations = multipleRelations;
        absOpts.stateProcessing = stateProcessing;
        absOpts.testGeneration = testGeneration;
        absOpts.supportCountThreshold = supportCountThreshold;
        absOpts.ignoreIntrByInvs = ignoreIntrByInvs;
        absOpts.ignoreNFbyInvs = ignoreNFbyInvs;
        // absOpts.k = k;

        // Parser options

        AbstractOptions.separatorRegExp = separatorRegExp;
        absOpts.regExps = regExps;
        AbstractOptions.partitionRegExp = partitionRegExp;
        absOpts.ignoreNonMatchingLines = ignoreNonMatchingLines;
        absOpts.usePerformanceInfo = usePerformanceInfo;
        absOpts.onlyRunKTails = onlyRunKTails;
        absOpts.traceNormalization = traceNormalization;
        absOpts.keepOrder = keepOrder;
        absOpts.recoverFromParseErrors = recoverFromParseErrors;
        absOpts.debugParse = debugParse;
        absOpts.dateFormat = dateFormat;

        // Input options

        AbstractOptions.argsFilename = argsFilename;

        // Output options

        AbstractOptions.outputPathPrefix = outputPathPrefix;
        absOpts.outputSupportCount = outputSupportCount;
        absOpts.outputInvariantsToFile = outputInvariantsToFile;
        absOpts.noModelOutput = noModelOutput;
        absOpts.exportAsGML = exportAsGML;
        absOpts.outputLTS = outputLTS;
        AbstractOptions.dotExecutablePath = dotExecutablePath;
        absOpts.outputProbLabels = outputProbLabels;
        absOpts.outputCountLabels = outputCountLabels;
        absOpts.showTerminalNode = showTerminalNode;
        absOpts.showInitialNode = showInitialNode;
        absOpts.outputJSON = outputJSON;

        // Verbosity Options

        absOpts.dumpInvariants = dumpInvariants;
        absOpts.dumpTraceGraphDotFile = dumpTraceGraphDotFile;
        absOpts.dumpTraceGraphPngFile = dumpTraceGraphPngFile;
        absOpts.dumpInitialPartitionGraph = dumpInitialPartitionGraph;
        absOpts.dumpIntermediateStages = dumpIntermediateStages;

        // Debugging Options

        absOpts.logLvlExtraVerbose = logLvlExtraVerbose;
        AbstractOptions.ignoreInvsOverETypeSet = ignoreInvsOverETypeSet;
        absOpts.useTransitiveClosureMining = useTransitiveClosureMining;
        absOpts.mineNeverConcurrentWithInv = mineNeverConcurrentWithInv;
        absOpts.onlyMineInvariants = onlyMineInvariants;
        absOpts.noCoarsening = noCoarsening;
        absOpts.doBenchmarking = doBenchmarking;
        absOpts.internCommonStrings = internCommonStrings;
        absOpts.runTests = runTests;
        absOpts.runAllTests = runAllTests;
        absOpts.performExtraChecks = performExtraChecks;
        absOpts.noRefinement = noRefinement;

        // Set this as the definitive plume options object in AbstractMain and
        // AbstractOptions
        AbstractMain.plumeOpts = this;
        AbstractOptions.plumeOpts = this;

        return absOpts;
    }
}
