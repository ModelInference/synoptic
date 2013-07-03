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
public class SynopticOptions extends Options {
    // //////////////////////////////////////////////////
    /**
     * Print the short usage message. This does not include verbosity or
     * debugging options.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public boolean help = false;

    /**
     * Print the extended usage message. This includes verbosity and debugging
     * options but not internal options.
     */
    @Option("-H Print extended usage message (includes debugging options)")
    public boolean allHelp = false;

    /**
     * Print the current Synoptic version.
     */
    @Option(value = "-V Print program version", aliases = { "-version" })
    public boolean version = false;
    // end option group "General Options"

    // //////////////////////////////////////////////////
    /**
     * Be quiet, do not print much information. Sets the log level to WARNING.
     */
    @OptionGroup("Execution Options")
    @Option(value = "-q Be quiet, do not print much information",
            aliases = { "-quiet" })
    public boolean logLvlQuiet = false;

    /**
     * Be verbose, print extra detailed information. Sets the log level to FINE.
     */
    @Option(value = "-v Print detailed information during execution",
            aliases = { "-verbose" })
    public boolean logLvlVerbose = false;

    /**
     * Use the new FSM checker instead of the LTL checker.
     */
    @Option(
            value = "-f Use FSM checker instead of the default NASA LTL-based checker",
            aliases = { "-use-fsm-checker" })
    public boolean useFSMChecker = true;

    /**
     * Sets the random seed for Synoptic's source of pseudo-random numbers.
     */
    @Option(
            value = "Use a specific random seed for pseudo-random number generator")
    public Long randomSeed = null;

    /**
     * Use vector time indexes to partition the output graph into a set of
     * graphs, one per distributed system node type.
     */
    @Option(
            value = "Vector time index sets for partitioning the graph by system node type, e.g. '1,2;3,4'")
    public String separateVTimeIndexSets = null;

    /**
     * Mine multiple-relations
     */
    @Option("Mine multiple relations from the trace graph")
    public boolean multipleRelations = false;
    // end option group "Execution Options"

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
    @Option(
            value = "-s Partitions separator reg-exp: log lines below and above the matching line are placed into different partitions",
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
    public static final String regExpDefault = "(?<TYPE>.*)";
    @Option(
            value = "-r Parser reg-exp: extracts event type and event time from a log line",
            aliases = { "-regexp" })
    public List<String> regExps = null;

    /**
     * A substitution expression, used to express how to map the trace lines
     * into partition traces, to be considered as an individual sample of the
     * behavior of the system.
     */
    public static final String partitionRegExpDefault = "\\k<FILE>";
    @Option(
            value = "-m Partitions mapping reg-exp: maps a log line to a partition",
            aliases = { "-partition-mapping" })
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
    @Option(
            value = "-i Ignore lines that do not match any of the passed regular expressions")
    public boolean ignoreNonMatchingLines = false;

    /**
     * This option allows the user to debug their system in terms of
     * performance.
     */
    @Option(value = "-p Enable performance debugging support")
    public boolean enablePerfDebugging = false;

    /**
     * This allows users to get away with sloppy\incorrect regular expressions
     * that might not fully cover the range of log lines appearing in the log
     * files.
     */
    @Option(
            value = "Ignore parser warnings and attempt to recover from parse errors if possible",
            aliases = { "-ignore-parse-errors" })
    public boolean recoverFromParseErrors = false;

    /**
     * Output the fields extracted from each log line and terminate.
     */
    @Option(
            value = "Debug the parser by printing field values extracted from the log and then terminate.",
            aliases = { "-debugParse" })
    public boolean debugParse = false;
    // end option group "Parser Options"

    // //////////////////////////////////////////////////
    /**
     * Command line arguments input filename to use.
     */
    @OptionGroup("Input Options")
    @Option(value = "-c Command line arguments input filename",
            aliases = { "-argsfile" })
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
    @Option(
            value = "-o Output path prefix for generating Graphviz dot files graphics",
            aliases = { "-output-prefix" })
    public String outputPathPrefix = null;

    /**
     * Whether or not to output the list of invariants to a file, with one
     * invariant per line.
     */
    @Option(value = "Output invariants to a file")
    public boolean outputInvariantsToFile = false;

    /**
     * Whether or not models should be exported as GML (graph modeling language)
     * files (the default format is DOT file format).
     */
    @Option(value = "Export models as GML and not DOT files",
            aliases = { "-export-as-gml" })
    public boolean exportAsGML = false;

    /**
     * The absolute path to the dot command executable to use for outputting
     * graphical representations of Synoptic models
     */
    @Option(value = "-d Path to the Graphviz dot command executable to use",
            aliases = { "-dot-executable" })
    public String dotExecutablePath = null;

    /**
     * This sets the output edge labels on graphs that are exported.
     */
    @Option(
            value = "Output edge labels on graphs to indicate transition probabilities",
            aliases = { "-outputEdgeLabels" })
    public boolean outputEdgeLabels = true;

    /**
     * Whether or not the output graphs include the common TERMINAL state, to
     * which all final trace nodes have an edge.
     */
    @Option(value = "Show TERMINAL node in generated graphs.")
    public boolean showTerminalNode = true;

    /**
     * Whether or not the output graphs include the common INITIAL state, which
     * has an edge to all the start trace nodes.
     */
    @Option(value = "Show INITIAL node in generated graphs.")
    public boolean showInitialNode = true;

    // end option group "Output Options"

    // //////////////////////////////////////////////////
    /**
     * Dump the complete list of mined synoptic.invariants for the set of input
     * files to stdout. This option is <i>unpublicized</i>; it will not appear
     * in the default usage message
     */
    @OptionGroup(value = "Verbosity Options", unpublicized = true)
    @Option("Dump complete list of mined invariant to stdout")
    public boolean dumpInvariants = false;

    /**
     * Dump the DOT representation of the parsed trace graph to file. The file
     * will have the name <outputPathPrefix>.tracegraph.dot, where
     * 'outputPathPrefix' is the filename of the final Synoptic output. This
     * option is <i>unpublicized</i>; it will not appear in the default usage
     * message
     */
    @Option("Dump the DOT file for the trace graph to file <outputPathPrefix>.tracegraph.dot")
    public boolean dumpTraceGraphDotFile = false;

    /**
     * Dump PNG of parsed trace graph to file. The file will have the name
     * <outputPathPrefix>.tracegraph.dot.png, where 'outputPathPrefix' is the
     * filename of the final Synoptic output. This option is
     * <i>unpublicized</i>; it will not appear in the default usage message
     */
    @Option("Dump the PNG of the trace graph to file <outputPathPrefix>.tracegraph.dot.png")
    public boolean dumpTraceGraphPngFile = false;

    /**
     * Dumps PNG of initial condensed partition graph to file. The file will
     * have the name <outputPathPrefix>.condensed.dot.png, where
     * 'outputPathPrefix' is the filename of the final Synoptic output. This
     * option is <i>unpublicized</i>; it will not appear in the default usage
     * message.
     */
    @Option("Dump the initial condensed partition graph")
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
    @Option("Dump dot files from intermediate Synoptic stages to files of form outputPathPrefix.stage-S.round-R.dot")
    public boolean dumpIntermediateStages = false;
    // end option group "Verbosity Options"

    // //////////////////////////////////////////////////
    @OptionGroup(value = "Debugging Options", unpublicized = true)
    /**
     * Be extra verbose, print extra detailed information. Sets the log level to
     * FINEST.
     */
    @Option(value = "Print extra detailed information during execution")
    public boolean logLvlExtraVerbose = false;

    /**
     * Used to select the algorithm for mining invariants.
     */
    @Option("Use the transitive closure invariant mining algorithm (usually slower)")
    public boolean useTransitiveClosureMining = false;

    /**
     * Tell Synoptic to mine/not mine the NeverConcurrentWith invariant. When
     * false, this option changes mining behavior when
     * useTransitiveClosureMining = false (i.e., it only works for the DAG
     * walking invariant miner, not the TC-based miner).
     */
    @Option("Mine the NeverConcurrentWith invariant (only changes behavior for PO traces with useTransitiveClosureMining=false)")
    public boolean mineNeverConcurrentWithInv = true;

    /**
     * Used to tell Synoptic to not go past mining invariants.
     */
    @Option("Mine invariants and then quit.")
    public boolean onlyMineInvariants = false;

    /**
     * Do not perform the coarsening stage in Synoptic, and as final output use
     * the most refined representation. This option is <i>unpublicized</i>; it
     * will not appear in the default usage message
     */
    @Option("Do not perform the coarsening stage")
    public boolean noCoarsening = false;

    /**
     * Perform benchmarking and output benchmark information. This option is
     * <i>unpublicized</i>; it will not appear in the default usage message
     */
    @Option("Perform benchmarking and output benchmark information")
    public boolean doBenchmarking = false;

    /**
     * Intern commonly occurring strings, such as event types, as a memory-usage
     * optimization. This option is <i>unpublicized</i>; it will not appear in
     * the default usage message
     */
    @Option("Intern commonly occurring strings, such as event types, as a memory-usage optimization")
    public boolean internCommonStrings = true;

    /**
     * Run all tests in synoptic.tests.units -- all the unit tests, and then
     * terminate. This option is <i>unpublicized</i>; it will not appear in the
     * default usage message
     */
    @Option("Run all tests in synoptic.tests.units, and then terminate.")
    public boolean runTests = false;

    /**
     * Run all tests in synoptic.tests -- unit and integration tests, and then
     * terminate. This option is <i>unpublicized</i>; it will not appear in the
     * default usage message
     */
    @Option("Run all tests in synoptic.tests, and then terminate.")
    public boolean runAllTests = false;

    /**
     * Turns on correctness checks that are disabled by default due to their
     * expensive cpu\memory usage profiles.
     */
    @Option("Perform extra correctness checks at the expense of cpu and memory usage.")
    public boolean performExtraChecks = false;

    /**
     * Do not perform the refinement (and therefore do not perform coarsening)
     * and do not produce any representation as output. This is useful for just
     * printing the list of mined synoptic.invariants (using the option
     * 'dumpInvariants' above). This option is <i>unpublicized</i>; it will not
     * appear in the default usage message
     */
    @Option("Do not perform refinement")
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
