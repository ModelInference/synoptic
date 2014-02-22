package synoptic.main.options;

public interface AbstractOptions {

    // General options

    String helpStr = "-h Print short usage message";
    String allHelpStr = "-H Print extended usage message (includes debugging options)";
    String versionStr = "-V Print program version";

    // Execution Options

    String logLvlQuietStr = "-q Be quiet, do not print much information";
    String logLvlVerboseStr = "-v Print detailed information during execution";
    String useFSMCheckerStr = "-f Use FSM checker instead of the default NASA LTL-based checker";
    String randomSeedStr = "Use a specific random seed for pseudo-random number generator";
    String separateVTimeIndexSetsStr = "Vector time index sets for partitioning the graph by system node type, e.g. '1,2;3,4'";
    String multipleRelationsStr = "Mine multiple relations from the trace graph";
    String stateProcessingStr = "Enable state processing";
    String testGenerationStr = "-t Enable abstract test generation";

    // Parser Options

    String separatorRegExpStr = "-s Partitions separator reg-exp: log lines below and above the matching line are placed into different partitions";
    String regExpsStr = "-r Parser reg-exp: extracts event type and event time from a log line";
    String partitionRegExpStr = "-m Partitions mapping reg-exp: maps a log line to a partition";
    String ignoreNonMatchingLinesStr = "-i Ignore lines that do not match any of the passed regular expressions";
    String outputJSONStr = "-j Output the final model as a JSON object";
    String traceNormalizationStr = "Independently normalize each trace";
    String recoverFromParseErrorsStr = "Ignore parser warnings and attempt to recover from parse errors if possible";
    String debugParseStr = "Debug the parser by printing field values extracted from the log and then terminate.";

    // Input options

    String argsFilenameStr = "-c Command line arguments input filename";

    // Output options

    String outputPathPrefixStr = "-o Output path prefix for generating Graphviz dot files graphics";
    String outputInvariantsToFileStr = "Output invariants to a file";
    String exportAsGMLStr = "Export models as GML and not DOT files";
    String dotExecutablePathStr = "-d Path to the Graphviz dot command executable to use";
    String outputEdgeLabelsStr = "Output edge labels on graphs to indicate transition probabilities";
    String showTerminalNodeStr = "Show TERMINAL node in generated graphs.";
    String showInitialNodeStr = "Show INITIAL node in generated graphs.";

    // Verbosity Options

    String dumpInvariantsStr = "Dump complete list of mined invariant to stdout";
    String dumpTraceGraphDotFileStr = "Dump the DOT file for the trace graph to file <outputPathPrefix>.tracegraph.dot";
    String dumpTraceGraphPngFileStr = "Dump the PNG of the trace graph to file <outputPathPrefix>.tracegraph.dot.png";
    String dumpInitialPartitionGraphStr = "Dump the initial condensed partition graph";
    String dumpIntermediateStagesStr = "Dump dot files from intermediate Synoptic stages to files of form outputPathPrefix.stage-S.round-R.dot";

    // Debugging Options

    String logLvlExtraVerboseStr = "Print extra detailed information during execution";
    String ignoreInvsOverETypeSetStr = "Ignore invariants that include event types from the following set (use ';' to separate event types).";
    String useTransitiveClosureMiningStr = "Use the transitive closure invariant mining algorithm (usually slower)";
    String mineNeverConcurrentWithInvStr = "Mine the NeverConcurrentWith invariant (only changes behavior for PO traces with useTransitiveClosureMining=false)";
    String onlyMineInvariantsStr = "Mine invariants and then quit.";
    String noCoarseningStr = "Do not perform the coarsening stage";
    String doBenchmarkingStr = "Perform benchmarking and output benchmark information";
    String internCommonStringsStr = "Intern commonly occurring strings, such as event types, as a memory-usage optimization";
    String runTestsStr = "Run all tests in synoptic.tests.units, and then terminate.";
    String runAllTestsStr = "Run all tests in synoptic.tests, and then terminate.";
    String performExtraChecksStr = "Perform extra correctness checks at the expense of cpu and memory usage.";
    String noRefinementStr = "Do not perform refinement";
}
