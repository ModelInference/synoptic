package synoptic.main.options;

import java.util.List;

public interface AbstractOptions {

    // ////////////////////////////
    // General options
    // ////////////////////////////

    String helpStr = "-h Print short usage message";
    public boolean help = false;

    String allHelpStr = "-H Print extended usage message (includes debugging options)";
    public boolean allHelp = false;

    String versionStr = "-V Print program version";
    public boolean version = false;

    // ////////////////////////////
    // Execution Options
    // ////////////////////////////

    String logLvlQuietStr = "-q Be quiet, do not print much information";
    public boolean logLvlQuiet = false;

    String logLvlVerboseStr = "-v Print detailed information during execution";
    public boolean logLvlVerbose = false;

    String useFSMCheckerStr = "-f Use FSM checker instead of the default NASA LTL-based checker";
    public boolean useFSMChecker = false;

    String randomSeedStr = "Use a specific random seed for pseudo-random number generator";
    public Long randomSeed = null;

    String separateVTimeIndexSetsStr = "Vector time index sets for partitioning the graph by system node type, e.g. '1,2;3,4'";
    public String separateVTimeIndexSets = null;

    String multipleRelationsStr = "Mine multiple relations from the trace graph";
    public boolean multipleRelations = false;

    String stateProcessingStr = "Enable state processing";
    public boolean stateProcessing = false;

    String testGenerationStr = "-t Enable abstract test generation";
    public boolean testGeneration = false;

    // ////////////////////////////
    // Parser Options
    // ////////////////////////////

    String separatorRegExpStr = "-s Partitions separator reg-exp: log lines below and above the matching line are placed into different partitions";
    public String separatorRegExp = null;

    public static final String regExpDefault = "(?<TYPE>.*)";
    String regExpsStr = "-r Parser reg-exp: extracts event type and event time from a log line";
    public List<String> regExps = null;

    public static final String partitionRegExpDefault = "\\k<FILE>";
    public String partitionRegExp = partitionRegExpDefault;

    public boolean partitionViaFile = true;

    String partitionRegExpStr = "-m Partitions mapping reg-exp: maps a log line to a partition";
    public boolean ignoreNonMatchingLines = false;

    String ignoreNonMatchingLinesStr = "-i Ignore lines that do not match any of the passed regular expressions";
    public boolean usePerformanceInfo = false;

    String outputJSONStr = "-j Output the final model as a JSON object";
    public final boolean outputJSON = false;

    String traceNormalizationStr = "Independently normalize each trace";
    public final boolean traceNormalization = false;

    String recoverFromParseErrorsStr = "Ignore parser warnings and attempt to recover from parse errors if possible";
    public boolean recoverFromParseErrors = false;

    String debugParseStr = "Debug the parser by printing field values extracted from the log and then terminate.";
    public boolean debugParse = false;

    // ////////////////////////////
    // Input options
    // ////////////////////////////

    String argsFilenameStr = "-c Command line arguments input filename";
    public String argsFilename = null;

    // ////////////////////////////
    // Output options
    // ////////////////////////////

    String outputPathPrefixStr = "-o Output path prefix for generating Graphviz dot files graphics";
    public String outputPathPrefix = null;

    String outputInvariantsToFileStr = "Output invariants to a file";
    public boolean outputInvariantsToFile = false;

    String exportAsGMLStr = "Export models as GML and not DOT files";
    public boolean exportAsGML = false;

    String dotExecutablePathStr = "-d Path to the Graphviz dot command executable to use";
    public String dotExecutablePath = null;

    String outputEdgeLabelsStr = "Output edge labels on graphs to indicate transition probabilities";
    public boolean outputEdgeLabels = true;

    String showTerminalNodeStr = "Show TERMINAL node in generated graphs.";
    public boolean showTerminalNode = true;

    String showInitialNodeStr = "Show INITIAL node in generated graphs.";
    public boolean showInitialNode = true;

    // ////////////////////////////
    // Verbosity Options
    // ////////////////////////////

    String dumpInvariantsStr = "Dump complete list of mined invariant to stdout";
    public boolean dumpInvariants = false;

    String dumpTraceGraphDotFileStr = "Dump the DOT file for the trace graph to file <outputPathPrefix>.tracegraph.dot";
    public boolean dumpTraceGraphDotFile = false;

    String dumpTraceGraphPngFileStr = "Dump the PNG of the trace graph to file <outputPathPrefix>.tracegraph.dot.png";
    public boolean dumpTraceGraphPngFile = false;

    String dumpInitialPartitionGraphStr = "Dump the initial condensed partition graph";
    public boolean dumpInitialPartitionGraph = false;

    String dumpIntermediateStagesStr = "Dump dot files from intermediate Synoptic stages to files of form outputPathPrefix.stage-S.round-R.dot";
    public boolean dumpIntermediateStages = false;

    // ////////////////////////////
    // Debugging Options
    // ////////////////////////////

    String logLvlExtraVerboseStr = "Print extra detailed information during execution";
    public boolean logLvlExtraVerbose = false;

    String ignoreInvsOverETypeSetStr = "Ignore invariants that include event types from the following set (use ';' to separate event types).";
    public String ignoreInvsOverETypeSet = null;

    String useTransitiveClosureMiningStr = "Use the transitive closure invariant mining algorithm (usually slower)";
    public boolean useTransitiveClosureMining = false;

    String mineNeverConcurrentWithInvStr = "Mine the NeverConcurrentWith invariant (only changes behavior for PO traces with useTransitiveClosureMining=false)";
    public boolean mineNeverConcurrentWithInv = true;

    String onlyMineInvariantsStr = "Mine invariants and then quit.";
    public boolean onlyMineInvariants = false;

    String noCoarseningStr = "Do not perform the coarsening stage";
    public boolean noCoarsening = false;

    String doBenchmarkingStr = "Perform benchmarking and output benchmark information";
    public boolean doBenchmarking = false;

    String internCommonStringsStr = "Intern commonly occurring strings, such as event types, as a memory-usage optimization";
    public boolean internCommonStrings = true;

    String runTestsStr = "Run all tests in synoptic.tests.units, and then terminate.";
    public boolean runTests = false;

    String runAllTestsStr = "Run all tests in synoptic.tests, and then terminate.";
    public boolean runAllTests = false;

    String performExtraChecksStr = "Perform extra correctness checks at the expense of cpu and memory usage.";
    public boolean performExtraChecks = false;

    String noRefinementStr = "Do not perform refinement";
    public boolean noRefinement = false;
}
