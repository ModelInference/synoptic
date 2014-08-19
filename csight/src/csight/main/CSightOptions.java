package csight.main;

import java.io.IOException;
import java.util.List;

import csight.util.Util;

import plume.Option;
import plume.OptionGroup;

import synoptic.main.options.Options;

/**
 * Options relevant to the csight binary.
 */
public class CSightOptions extends Options {

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
    @Option(
            value = "-H Print extended usage message (includes debugging options)")
    public boolean allHelp = false;

    // end option group "General Options"
    // //////////////////////////////////////////////////

    /**
     * Command line arguments input filename to use.
     */
    @OptionGroup("Input Options")
    @Option(value = "-c Command line arguments input filename",
            aliases = { "-argsfile" })
    public String argsFilename = null;

    /**
     * Sets the random seed for CSight's source of pseudo-random numbers.
     */
    @Option(
            value = "Use a specific random seed for pseudo-random number generator")
    public Long randomSeed = null;

    /**
     * Regular expression separator string. When lines are found which match
     * this expression, the lines before and after are considered to be in
     * different 'traces', each to be considered an individual sample of the
     * behavior of the system. This is implemented by augmenting the separator
     * expression with an incrementor, (?<SEPCOUNT++>), and adding \k<SEPCOUNT>
     * to the partitioner.
     */
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
     * A substitution expression, used to express how to map the trace lines
     * into partition traces, to be considered as an individual sample of the
     * behavior of the system.
     */
    @Option(
            value = "-q Queue/channel specification. For example, 'M:0->1;A:1->0' specifies channels 'M' and 'A' in the log. For 'M' pid 0 is the sender and pid 1 is the receiver.",
            aliases = { "-channel-spec" })
    public String channelSpec = null;

    /**
     * This option relieves the user from writing regular expressions to parse
     * lines that they are not interested in. This also help to avoid parsing of
     * lines that are corrupted.
     */
    @Option(
            value = "-i Ignore lines that do not match any of the passed regular expressions")
    public boolean ignoreNonMatchingLines = false;

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

    /**
     * Pattern defining the format of dates within a log (required by DATETIME)
     */
    // FIXME: this can become inconsistent with synoptic's
    // AbstractOptions.dateFormatStr
    @Option(
            value = "Format of the dates contained in the log (required by DATETIME)",
            aliases = { "-dateFormat" })
    public String dateFormat = "dd/MMM/yyyy:HH:mm:ss";

    @Option(
            value = "Set number of top queue elements to compare states for partition graph construction.",
            aliases = { "-topK" })
    public int topKElements = 1;

    // end option group "Input Options"
    // //////////////////////////////////////////////////

    // //////////////////////////////////////////////////
    /**
     * Used to select the algorithm for mining invariants.
     */
    @OptionGroup(value = "Debugging Options", unpublicized = true)
    @Option(
            value = "-t Use the transitive closure invariant mining algorithm (usually slower)")
    public boolean useTransitiveClosureMining = false;

    // end option group "Debugging Options"
    // //////////////////////////////////////////////////

    /**
     * Specifies the prefix of where to store the model outputs.
     */
    @OptionGroup("Model Checking Options")
    @Option(value = "Complete path to the model checker binary",
            aliases = { "-mc-path" })
    public String mcPath = null;

    @Option(
            value = "Model checker type to use. Must be either 'spin' or 'mcscm'. Spin is only supported on 64-bit Linux.")
    public String mcType = "mcscm";

    @Option(
            value = "Default channel capacity to use when using the spin model checker.")
    public int spinChannelCapacity = 8;

    @Option(
            value = "Check multiple invariants per model checking run when using Spin.")
    public boolean spinMultipleInvs = true;

    @Option(
            value = "Run model checking processes in parallel. (Currently only for McScM)",
            aliases = { "-p" })
    public boolean runParallel = false;

    /**
     * The default parallelization factor is set as number of cores available,
     * which provides the most optimal performance.
     */
    @Option(value = "Number of model checking processes to run in parallel.",
            aliases = { "-pFactor" })
    public int numParallel = Runtime.getRuntime().availableProcessors();

    /**
     * The base timeout that is used to time out invocations of verification
     * (which may run indefinitely).
     */
    @Option(
            value = "Initial timeout (in seconds) that is used to time out a model-checker run.",
            aliases = { "-base-timeout" })
    public int baseTimeout = 20;

    /**
     * The amount of time added to baseTimeout before retrying verification.
     */
    @Option(
            value = "Time (in seconds) to add to -base-timeout after each time the model checker times out, before reaching max timeout.",
            aliases = { "-timeout-delta" })
    public int timeoutDelta = 10;

    /**
     * Maximum timeout value to use for McScM verification.
     */
    @Option(
            value = "Maximum timeout (in seconds) to use for a model checking run.",
            aliases = { "-max-timeout" })
    public int maxTimeout = 60;

    // end option group "Model Checking Options"
    // //////////////////////////////////////////////////

    /**
     * Specifies the prefix of where to store the model outputs.
     */
    @OptionGroup("Output Options")
    @Option(
            value = "-o Output path prefix for generating Graphviz dot files graphics",
            aliases = { "-output-prefix" })
    public String outputPathPrefix = null;

    /**
     * Dump the complete list of mined synoptic.invariants for the set of input
     * files to stdout.
     */
    @Option(value = "-d Output complete list of mined invariants to stdout")
    public boolean dumpInvariants = false;

    /**
     * What level of logging to use.
     */
    @Option(value = "Quietest logging, warnings only")
    public boolean logLvlQuiet = false;

    @Option(value = "Verbose logging")
    public boolean logLvlVerbose = false;

    @Option(value = "Extra verbose logging")
    public boolean logLvlExtraVerbose = false;

    // end option group "Output Options"
    // //////////////////////////////////////////////////

    /**
     * Whether or not to assume that process i is in one unique state at the
     * start of all executions.
     */
    @OptionGroup("Strategy Options")
    @Option(
            value = "Each process begins execution in the same initial state across all traces in the log",
            aliases = "-consistent-init-state")
    public boolean consistentInitState = true;

    @Option(
            value = "Minimize each of the process FSMs during GFSM to CFSM conversion",
            aliases = "-minimize")
    public boolean minimize = false;

    // end option group "Strategy Options"
    // //////////////////////////////////////////////////

    /** One line synopsis of usage */
    public static final String usageString = "csight [options] <logfiles-to-analyze>";

    // The arguments that this options instance corresponds to.
    public final String[] args;

    /**
     * Use this constructor to create a blank set of options, that can then be
     * populated manually, one at a time. This is useful when CSight is used as
     * a library or in tests, and options do not come from the command line.
     */
    public CSightOptions() {
        randomSeed = System.currentTimeMillis();
        logFilenames = Util.newList();
        args = null;
    }

    /** Sets up an instance of this class based on an array of arguments. */
    public CSightOptions(String[] args) throws IOException {
        plumeOptions = new plume.Options(getUsageString(), this);
        setOptions(args);
        if (randomSeed == null) {
            randomSeed = System.currentTimeMillis();
        }
        this.args = args;
    }

    /**
     * Prints help for all option groups, including unpublicized ones.
     */
    public void printLongHelp() {
        System.out.println("Usage: " + getUsageString());
        System.out.println(plumeOptions.usage("General Options",
                "Input Options", "Debugging Options", "Model Checking Options",
                "Output Options", "Strategy Options"));
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
