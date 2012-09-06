package dynoptic.main;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import plume.Option;
import plume.OptionGroup;

import synoptic.main.options.Options;

/**
 * Options relevant to the dynoptic binary.
 */
public class DynopticOptions extends Options {

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
     * Sets the random seed for Dynoptic's source of pseudo-random numbers.
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
     * This flag indicates whether Dynoptic should partition traces by file
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

    // end option group "Input Options"
    // //////////////////////////////////////////////////

    /**
     * Specifies the prefix of where to store the model outputs.
     */
    @OptionGroup("Verify Options")
    @Option(
            value = "-mc Complete path to the verification McScM model checker",
            aliases = { "-mc-path" })
    public String mcPath = null;

    // end option group "Verify Options"
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

    /** One line synopsis of usage */
    public static final String usageString = "dynoptic [options] <logfiles-to-analyze>";

    /**
     * Use this constructor to create a blank set of options, that can then be
     * populated manually, one at a time. This is useful when Dynoptic is used
     * as a library or in tests, and options do not come from the command line.
     */
    public DynopticOptions() {
        randomSeed = System.currentTimeMillis();
        logFilenames = new LinkedList<String>();
    }

    public DynopticOptions(String[] args) throws IOException {
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
                "Input Options", "Verify Options", "Output Options"));
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
