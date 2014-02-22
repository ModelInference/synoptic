package synoptic.main.options;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import plume.Option;
import plume.OptionGroup;

/**
 * This class defines and maintains command line arguments to the Synoptic
 * process. It uses plume-lib for defining command line options, their types,
 * and the corresponding help messages. This library also provides support for
 * parsing and populating instances of these options.
 */
public class SynopticOptions extends Options implements AbstractOptions {
    // //////////////////////////////////////////////////
    /**
     * Print the short usage message. This does not include verbosity or
     * debugging options.
     */
    @OptionGroup("General Options")
    @Option(value = helpStr, aliases = { "-help" })
    public boolean help = false;

    /**
     * Print the extended usage message. This includes verbosity and debugging
     * options but not internal options.
     */
    @Option(allHelpStr)
    public boolean allHelp = false;

    /**
     * Print the current Synoptic version.
     */
    @Option(value = versionStr, aliases = { "-version" })
    public boolean version = false;
    // end option group "General Options"

    // //////////////////////////////////////////////////
    /**
     * Be quiet, do not print much information. Sets the log level to WARNING.
     */
    @OptionGroup("Execution Options")
    @Option(value = logLvlQuietStr, aliases = { "-quiet" })
    public boolean logLvlQuiet = false;

    /**
     * Be verbose, print extra detailed information. Sets the log level to FINE.
     */
    @Option(value = logLvlVerboseStr, aliases = { "-verbose" })
    public boolean logLvlVerbose = false;

    /**
     * Use the new FSM checker instead of the LTL checker.
     */
    @Option(value = useFSMCheckerStr, aliases = { "-use-fsm-checker" })
    public boolean useFSMChecker = true;

    /**
     * Sets the random seed for Synoptic's source of pseudo-random numbers.
     */
    @Option(randomSeedStr)
    public Long randomSeed = null;

    /**
     * Use vector time indexes to partition the output graph into a set of
     * graphs, one per distributed system node type.
     */
    @Option(separateVTimeIndexSetsStr)
    public String separateVTimeIndexSets = null;

    /**
     * Mine multiple-relations
     */
    @Option(multipleRelationsStr)
    public boolean multipleRelations = false;

    /**
     * States are parsed. Enable state processing.
     */
    @Option(stateProcessingStr)
    public boolean stateProcessing = false;

    /**
     * Enable abstract test generation.
     */
    @Option(value = testGenerationStr, aliases = { "-test-generation" })
    public boolean testGeneration = false;

    // //////////////////////////////////////////////////
    /**
     * Regular expression separator string. When lines are found which match
     * this expression, the lines before and after are considered to be in
     * different 'traces', each to be considered an individual sample of the
     * behavior of the system. This is implemented by augmenting the separator
     * expression with an incrementor, (?<SEPCOUNT++>), and adding \k<SEPCOUNT>
     * to the partitioner.
     */
    @OptionGroup("Parser Options")
    @Option(value = separatorRegExpStr, aliases = { "-partition-separator" })
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
    public static final String regExpDefault = "(?<TYPE>.*)";
    @Option(value = regExpsStr, aliases = { "-regexp" })
    public List<String> regExps = null;

    /**
     * A substitution expression, used to express how to map the trace lines
     * into partition traces, to be considered as an individual sample of the
     * behavior of the system.
     */
    public static final String partitionRegExpDefault = "\\k<FILE>";
    @Option(value = partitionRegExpStr, aliases = { "-partition-mapping" })
    public String partitionRegExp = partitionRegExpDefault;

    /**
     * This flag indicates whether Synoptic should partition traces by file
     */
    public boolean partitionViaFile = true;

    /**
     * This option relieves the user from writing regular expressions to parse
     * lines that they are not interested in. This also help to avoid parsing of
     * lines that are corrupted.
     */
    @Option(ignoreNonMatchingLinesStr)
    public boolean ignoreNonMatchingLines = false;

    /**
     * This indicates that the Synoptic algorithm is run without including
     * performance information, i.e., Perfume is not run. TODO: make final,
     * modify test libraries that rely on this option, rename
     */
    public boolean enablePerfDebugging = false;

    /**
     * Allows outputting the final model as a JSON object to the output prefix
     * specified by -o or -output-prefix.
     */
    @Option(value = outputJSONStr, aliases = { "-output-json" })
    public boolean outputJSON = false;

    /**
     * Allows performing trace-wise normalization, requires enablePerfDebugging
     */
    @Option(value = traceNormalizationStr, aliases = { "-trace-norm" })
    public boolean traceNormalization = false;

    /**
     * This allows users to get away with sloppy\incorrect regular expressions
     * that might not fully cover the range of log lines appearing in the log
     * files.
     */
    @Option(value = recoverFromParseErrorsStr,
            aliases = { "-ignore-parse-errors" })
    public boolean recoverFromParseErrors = false;

    /**
     * Output the fields extracted from each log line and terminate.
     */
    @Option(value = debugParseStr, aliases = { "-debugParse" })
    public boolean debugParse = false;
    // end option group "Parser Options"

    // //////////////////////////////////////////////////
    /**
     * Command line arguments input filename to use.
     */
    @OptionGroup("Input Options")
    @Option(value = argsFilenameStr, aliases = { "-argsfile" })
    public String argsFilename = null;
    // end option group "Input Options"

    // //////////////////////////////////////////////////
    /**
     * Specifies the prefix of where to store the final Synoptic representation
     * output. This prefix is also used to determine filenames of intermediary
     * files as well, like corresponding dot file and intermediate stage
     * representations and dot files (if specified, e.g. with
     * --dumpIntermediateStages).
     */
    @OptionGroup("Output Options")
    @Option(value = outputPathPrefixStr, aliases = { "-output-prefix" })
    public String outputPathPrefix = null;

    /**
     * Whether or not to output the list of invariants to a file, with one
     * invariant per line.
     */
    @Option(outputInvariantsToFileStr)
    public boolean outputInvariantsToFile = false;

    /**
     * Whether or not models should be exported as GML (graph modeling language)
     * files (the default format is DOT file format).
     */
    @Option(value = exportAsGMLStr, aliases = { "-export-as-gml" })
    public boolean exportAsGML = false;

    /**
     * The absolute path to the dot command executable to use for outputting
     * graphical representations of Synoptic models
     */
    @Option(value = dotExecutablePathStr, aliases = { "-dot-executable" })
    public String dotExecutablePath = null;

    /**
     * This sets the output edge labels on graphs that are exported.
     */
    @Option(value = outputEdgeLabelsStr, aliases = { "-outputEdgeLabels" })
    public boolean outputEdgeLabels = true;

    /**
     * Whether or not the output graphs include the common TERMINAL state, to
     * which all final trace nodes have an edge.
     */
    @Option(showTerminalNodeStr)
    public boolean showTerminalNode = true;

    /**
     * Whether or not the output graphs include the common INITIAL state, which
     * has an edge to all the start trace nodes.
     */
    @Option(showInitialNodeStr)
    public boolean showInitialNode = true;

    // end option group "Output Options"

    // //////////////////////////////////////////////////
    /**
     * Dump the complete list of mined synoptic.invariants for the set of input
     * files to stdout. This option is <i>unpublicized</i>; it will not appear
     * in the default usage message
     */
    @OptionGroup(value = "Verbosity Options", unpublicized = true)
    @Option(dumpInvariantsStr)
    public boolean dumpInvariants = false;

    /**
     * Dump the DOT representation of the parsed trace graph to file. The file
     * will have the name <outputPathPrefix>.tracegraph.dot, where
     * 'outputPathPrefix' is the filename of the final Synoptic output. This
     * option is <i>unpublicized</i>; it will not appear in the default usage
     * message
     */
    @Option(dumpTraceGraphDotFileStr)
    public boolean dumpTraceGraphDotFile = false;

    /**
     * Dump PNG of parsed trace graph to file. The file will have the name
     * <outputPathPrefix>.tracegraph.dot.png, where 'outputPathPrefix' is the
     * filename of the final Synoptic output. This option is
     * <i>unpublicized</i>; it will not appear in the default usage message
     */
    @Option(dumpTraceGraphPngFileStr)
    public boolean dumpTraceGraphPngFile = false;

    /**
     * Dumps PNG of initial condensed partition graph to file. The file will
     * have the name <outputPathPrefix>.condensed.dot.png, where
     * 'outputPathPrefix' is the filename of the final Synoptic output. This
     * option is <i>unpublicized</i>; it will not appear in the default usage
     * message.
     */
    @Option(dumpInitialPartitionGraphStr)
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
    @Option(dumpIntermediateStagesStr)
    public boolean dumpIntermediateStages = false;
    // end option group "Verbosity Options"

    // //////////////////////////////////////////////////
    @OptionGroup(value = "Debugging Options", unpublicized = true)
    /**
     * Be extra verbose, print extra detailed information. Sets the log level to
     * FINEST.
     */
    @Option(value = logLvlExtraVerboseStr)
    public boolean logLvlExtraVerbose = false;

    /**
     * Used to select the algorithm for mining invariants.
     */
    @Option(ignoreInvsOverETypeSetStr)
    public String ignoreInvsOverETypeSet = null;

    /**
     * Used to select the algorithm for mining invariants.
     */
    @Option(useTransitiveClosureMiningStr)
    public boolean useTransitiveClosureMining = false;

    /**
     * Tell Synoptic to mine/not mine the NeverConcurrentWith invariant. When
     * false, this option changes mining behavior when
     * useTransitiveClosureMining = false (i.e., it only works for the DAG
     * walking invariant miner, not the TC-based miner).
     */
    @Option(mineNeverConcurrentWithInvStr)
    public boolean mineNeverConcurrentWithInv = true;

    /**
     * Used to tell Synoptic to not go past mining invariants.
     */
    @Option(onlyMineInvariantsStr)
    public boolean onlyMineInvariants = false;

    /**
     * Do not perform the coarsening stage in Synoptic, and as final output use
     * the most refined representation. This option is <i>unpublicized</i>; it
     * will not appear in the default usage message
     */
    @Option(noCoarseningStr)
    public boolean noCoarsening = false;

    /**
     * Perform benchmarking and output benchmark information. This option is
     * <i>unpublicized</i>; it will not appear in the default usage message
     */
    @Option(doBenchmarkingStr)
    public boolean doBenchmarking = false;

    /**
     * Intern commonly occurring strings, such as event types, as a memory-usage
     * optimization. This option is <i>unpublicized</i>; it will not appear in
     * the default usage message
     */
    @Option(internCommonStringsStr)
    public boolean internCommonStrings = true;

    /**
     * Run all tests in synoptic.tests.units -- all the unit tests, and then
     * terminate. This option is <i>unpublicized</i>; it will not appear in the
     * default usage message
     */
    @Option(runTestsStr)
    public boolean runTests = false;

    /**
     * Run all tests in synoptic.tests -- unit and integration tests, and then
     * terminate. This option is <i>unpublicized</i>; it will not appear in the
     * default usage message
     */
    @Option(runAllTestsStr)
    public boolean runAllTests = false;

    /**
     * Turns on correctness checks that are disabled by default due to their
     * expensive cpu\memory usage profiles.
     */
    @Option(performExtraChecksStr)
    public boolean performExtraChecks = false;

    /**
     * Do not perform the refinement (and therefore do not perform coarsening)
     * and do not produce any representation as output. This is useful for just
     * printing the list of mined synoptic.invariants (using the option
     * 'dumpInvariants' above). This option is <i>unpublicized</i>; it will not
     * appear in the default usage message
     */
    @Option(noRefinementStr)
    public boolean noRefinement = false;
    // end option group "Debugging Options"

    /** One line synopsis of usage */
    private static final String usageString = "synoptic [options] <logfiles-to-analyze>";

    /**
     * Use this constructor to create a blank set of options, that can then be
     * populated manually, one at a time. This is useful when Synoptic is used
     * as a library or in tests, and options do not come from the command line.
     */
    public SynopticOptions() {
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
    public SynopticOptions(String[] args) throws IOException {
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
}
