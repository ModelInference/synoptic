package synoptic.main.options;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import plume.Option;
import plume.OptionGroup;

import synoptic.main.AbstractMain;

/**
 * <p>
 * Defines the command line arguments for the extended version of Perfume
 * (ExtPerfume). It uses plume-lib for defining command line options, their
 * types, and the corresponding help messages. This library also provides
 * support for parsing and populating instances of these options. All options
 * can be exported to AbstractOptions (the common options for all Synoptic
 * projects) using toAbstractOptions().
 * </p>
 * <p>
 * Options common between this class and SynopticOptions cannot be pushed up
 * into a superclass because plume-lib doesn't support inheritance.
 * </p>
 */
public class ExtPerfumeOptions extends Options {
    // TODO: Filter the options below copied from Perfume Options

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
     * Print the current Perfume version.
     */
    @Option(value = AbstractOptions.versionStr, aliases = { "-version" })
    public boolean version = false;
    // end option group "General Options"

    // //////////////////////////////////////////////////
    @OptionGroup("Extended Perfume Options")
    // Execution Option
    /**
     * True to terminate when intermediate model output is generated. False to
     * block after intermediate model output and wait for external refinement
     * instructions.
     */
    @Option(value = AbstractOptions.termOnIntermediateModelStr)
    public boolean termOnIntermediateModel = false;

    // Input Options
    /**
     * The file name for an intermediate Perfume model for Extended Perfume.
     */
    @Option(value = AbstractOptions.modelFileStr, aliases = { "model-file" })
    public String modelFile = null;

    /**
     * The file name for refinement instructions for an intermediate Perfume
     * model for Extended Perfume.
     */
    @Option(value = AbstractOptions.refinementFileStr, aliases = { "refinement-file" })
    public String refinementFile = null;

    /**
     * The location for input files for Extended Perfume
     */
    @Option(value = AbstractOptions.intermediateInPathStr, aliases = { "input-path" })
    public String intermediateInPath = "extended-perfume/in/";

    /**
     * The location to output intermediate models for Extended Perfume
     */
    @Option(value = AbstractOptions.intermediateOutPathStr, aliases = { "output-path" })
    public String intermediateOutPath = "extended-perfume/out/";
    // end option group "Extended Perfume Options"

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
     * Sets the random seed for Perfume's source of pseudo-random numbers.
     */
    @Option(AbstractOptions.randomSeedStr)
    public Long randomSeed = null;

    /**
     * Perfume doesn't support vector time
     */
    public final String separateVTimeIndexSets = null;

    /**
     * Perfume doesn't support multiple relations
     */
    public final boolean multipleRelations = false;

    /**
     * Perfume doesn't support state processing
     */
    public final boolean stateProcessing = false;

    /**
     * Pefume doesn't support abstract test generation
     */
    public final boolean testGeneration = false;

    /**
     * Whether to ignore (filter out) IntrBy invariants
     */
    @Option(AbstractOptions.ignoreIntrByInvsStr)
    public boolean ignoreIntrByInvs = false;

    /**
     * Whether to ignore (filter out) NFby invariants
     */
    @Option(AbstractOptions.ignoreNFbyInvsStr)
    public boolean ignoreNFbyInvs = false;

    // //////////////////////////////////////////////////
    /**
     * Regular expression separator string. When lines are found which match
     * this expression, the lines before and after are considered to be in
     * different 'traces', each to be considered an individual sample of the
     * behavior of the system. This is implemented by augmenting the separator
     * expression with an incrementor, (?<SEPCOUNT++>), and adding \k
     * <SEPCOUNT> to the partitioner.
     */
    @OptionGroup("Parser Options")
    @Option(value = AbstractOptions.separatorRegExpStr, aliases = { "-partition-separator" })
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
    @Option(value = AbstractOptions.partitionRegExpStr, aliases = { "-partition-mapping" })
    public String partitionRegExp = AbstractOptions.partitionRegExpDefault;

    /**
     * This flag indicates whether Perfume should partition traces by file
     */
    public boolean partitionViaFile = true;

    /**
     * This option relieves the user from writing regular expressions to parse
     * lines that they are not interested in. This also help to avoid parsing of
     * lines that are corrupted.
     */
    @Option(AbstractOptions.ignoreNonMatchingLinesStr)
    public boolean ignoreNonMatchingLines = false;

    /**
     * Perfume always uses performance information.
     */
    public final static boolean usePerformanceInfo = true;

    /**
     * Perform trace-wise normalization
     */
    @Option(value = AbstractOptions.traceNormalizationStr, aliases = { "-trace-norm" })
    public boolean traceNormalization = false;

    /**
     * Keep events in log order and do not sort by supplied resource values
     */
    @Option(value = AbstractOptions.keepOrderStr)
    public boolean keepOrder = true;

    /**
     * This allows users to get away with sloppy\incorrect regular expressions
     * that might not fully cover the range of log lines appearing in the log
     * files.
     */
    @Option(value = AbstractOptions.recoverFromParseErrorsStr, aliases = { "-ignore-parse-errors" })
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
     * Specifies the prefix of where to store the final Perfume representation
     * output. This prefix is also used to determine filenames of intermediary
     * files, like corresponding dot file and intermediate stage representations
     * (if specified, e.g. with --dumpIntermediateStages).
     */
    @OptionGroup("Output Options")
    @Option(value = AbstractOptions.outputPathPrefixStr, aliases = { "-output-prefix" })
    public String outputPathPrefix = null;

    /**
     * Whether or not to output the list of invariants to a file, with one
     * invariant per line.
     */
    @Option(AbstractOptions.outputInvariantsToFileStr)
    public boolean outputInvariantsToFile = false;

    /**
     * Whether or not models should be exported as GML (graph modeling language)
     * files (the default format is DOT file format).
     */
    @Option(value = AbstractOptions.exportAsGMLStr, aliases = { "-export-as-gml" })
    public boolean exportAsGML = false;

    /**
     * The absolute path to the dot command executable to use for outputting
     * graphical representations of Perfume models
     */
    @Option(value = AbstractOptions.dotExecutablePathStr, aliases = { "-dot-executable" })
    public String dotExecutablePath = null;

    /**
     * Whether or not probabilities are displayed on edge labels in addition to
     * metric ranges, which are always displayed
     */
    @Option(value = AbstractOptions.outputProbLabelsStr)
    public boolean outputProbLabels = false;

    /**
     * Whether or not transition counts are displayed on edge labels in addition
     * to metric ranges, which are always displayed
     */
    @Option(value = AbstractOptions.outputCountLabelsStr)
    public boolean outputCountLabels = false;

    /**
     * Whether or not to show the median metric value on edges between the min
     * and max, e.g., [1,5,9] instead of [1,9] for min 1, median 5, max 9
     */
    @Option(AbstractOptions.showMedianStr)
    public boolean showMedian = false;

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
     * Dump the complete list of mined Perfume invariants for the set of input
     * files to stdout. This option is <i>unpublicized</i>; it will not appear
     * in the default usage message
     */
    @OptionGroup(value = "Verbosity Options", unpublicized = true)
    @Option(AbstractOptions.dumpInvariantsStr)
    public boolean dumpInvariants = false;

    /**
     * Dump the DOT representation of the parsed trace graph to file. The file
     * will have the name <outputPathPrefix>.tracegraph.dot, where
     * 'outputPathPrefix' is the filename of the final Perfume output. This
     * option is <i>unpublicized</i>; it will not appear in the default usage
     * message
     */
    @Option(AbstractOptions.dumpTraceGraphDotFileStr)
    public boolean dumpTraceGraphDotFile = false;

    /**
     * Dump PNG of parsed trace graph to file. The file will have the name
     * <outputPathPrefix>.tracegraph.dot.png, where 'outputPathPrefix' is the
     * filename of the final Perfume output. This option is <i>unpublicized</i>;
     * it will not appear in the default usage message
     */
    @Option(AbstractOptions.dumpTraceGraphPngFileStr)
    public boolean dumpTraceGraphPngFile = false;

    /**
     * Dumps PNG of initial condensed partition graph to file. The file will
     * have the name <outputPathPrefix>.condensed.dot.png, where
     * 'outputPathPrefix' is the filename of the final Perfume output. This
     * option is <i>unpublicized</i>; it will not appear in the default usage
     * message.
     */
    @Option(AbstractOptions.dumpInitialPartitionGraphStr)
    public boolean dumpInitialPartitionGraph = false;

    /**
     * Dump the dot representations for intermediate Perfume steps to file. Each
     * of these files will have a name like:
     * outputPathPrefix.stage-S.round-R.dot where 'outputPathPrefix' is the
     * filename of the final Perfume output, 'S' is the name of the stage (e.g.
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
     * Ignore invariants including certain event types.
     */
    @Option(AbstractOptions.ignoreInvsOverETypeSetStr)
    public String ignoreInvsOverETypeSet = null;

    /**
     * Perfume doesn't support transitive closure mining
     */
    public final boolean useTransitiveClosureMining = false;

    /**
     * Perfume doesn't support the NeverConcurrentWith invariant
     */
    public final boolean mineNeverConcurrentWithInv = false;

    /**
     * Used to tell Perfume to not go past mining invariants.
     */
    @Option(AbstractOptions.onlyMineInvariantsStr)
    public boolean onlyMineInvariants = false;

    /**
     * Do not perform the coarsening stage in Perfume, and as final output use
     * the most refined representation. This option is <i>unpublicized</i>; it
     * will not appear in the default usage message
     */
    @Option(AbstractOptions.noCoarseningStr)
    public boolean noCoarsening = false;

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

    /**
     * Do not perform the refinement (and therefore do not perform coarsening)
     * and do not produce any representation as output. This is useful for just
     * printing the list of mined synoptic.invariants (using the option
     * 'dumpInvariants' above). This option is <i>unpublicized</i>; it will not
     * appear in the default usage message
     */
    @Option(AbstractOptions.noRefinementStr)
    public boolean noRefinement = false;
    // end option group "Debugging Options"

    /** One line synopsis of usage */
    public static final String usageString = "extended perfume [options] <logfiles-to-analyze>";

    /**
     * Use this constructor to create a blank set of options, that can then be
     * populated manually, one at a time. This is useful when Perfume is used as
     * a library or in tests, and options do not come from the command line.
     */
    public ExtPerfumeOptions() {
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
    public ExtPerfumeOptions(String[] args) throws IOException {
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
        System.out.println(plumeOptions.usage("General Options", "Execution Options", "Parser Options", "Input Options",
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

        // Extended Perfume options

        absOpts.termOnIntermediateModel = termOnIntermediateModel;
        absOpts.modelFile = modelFile;
        absOpts.refinementFile = refinementFile;
        absOpts.intermediateInPath = intermediateInPath;
        absOpts.intermediateOutPath = intermediateOutPath;

        // Execution options

        absOpts.logLvlQuiet = logLvlQuiet;
        absOpts.logLvlVerbose = logLvlVerbose;
        absOpts.randomSeed = randomSeed;
        AbstractOptions.separateVTimeIndexSets = separateVTimeIndexSets;
        absOpts.multipleRelations = multipleRelations;
        absOpts.stateProcessing = stateProcessing;
        absOpts.testGeneration = testGeneration;
        absOpts.ignoreIntrByInvs = ignoreIntrByInvs;
        absOpts.ignoreNFbyInvs = ignoreNFbyInvs;

        // Parser options

        AbstractOptions.separatorRegExp = separatorRegExp;
        absOpts.regExps = regExps;
        AbstractOptions.partitionRegExp = partitionRegExp;
        absOpts.ignoreNonMatchingLines = ignoreNonMatchingLines;
        absOpts.usePerformanceInfo = usePerformanceInfo;
        absOpts.traceNormalization = traceNormalization;
        absOpts.keepOrder = keepOrder;
        absOpts.recoverFromParseErrors = recoverFromParseErrors;
        absOpts.debugParse = debugParse;
        absOpts.dateFormat = dateFormat;

        // Input options

        AbstractOptions.argsFilename = argsFilename;

        // Output options

        AbstractOptions.outputPathPrefix = outputPathPrefix;
        absOpts.outputInvariantsToFile = outputInvariantsToFile;
        absOpts.exportAsGML = exportAsGML;
        AbstractOptions.dotExecutablePath = dotExecutablePath;
        absOpts.outputProbLabels = outputProbLabels;
        absOpts.outputCountLabels = outputCountLabels;
        absOpts.showMedian = showMedian;
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
