package synoptic.main;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.runner.JUnitCore;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.gui.JungGui;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.DAGWalkingPOInvMiner;
import synoptic.invariants.miners.InvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.model.EventNode;
import synoptic.model.Graph;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphVizExporter;
import synoptic.util.BriefLogFormatter;
import synoptic.util.InternalSynopticException;

public class Main implements Callable<Integer> {
    public static Logger logger = null;

    /**
     * The current Synoptic version.
     */
    public static final String versionString = "0.0.5";

    /**
     * Global source of pseudo-random numbers.
     */
    public static Random random;

    // //////////////////////////////////////////////////
    /**
     * Print the short usage message. This does not include verbosity or
     * debugging options.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public static boolean help = false;

    /**
     * Print the extended usage message. This includes verbosity and debugging
     * options but not internal options.
     */
    @Option("-H Print extended usage message (includes debugging options)")
    public static boolean allHelp = false;

    /**
     * Print the current Synoptic version.
     */
    @Option(value = "-V Print program version", aliases = { "-version" })
    public static boolean version = false;
    // end option group "General Options"

    // //////////////////////////////////////////////////
    /**
     * Be quiet, do not print much information. Sets the log level to WARNING.
     */
    @OptionGroup("Execution Options")
    @Option(value = "-q Be quiet, do not print much information",
            aliases = { "-quiet" })
    public static boolean logLvlQuiet = false;

    /**
     * Be verbose, print extra detailed information. Sets the log level to FINE.
     */
    @Option(value = "-v Print detailed information during execution",
            aliases = { "-verbose" })
    public static boolean logLvlVerbose = false;

    /**
     * Use the new FSM checker instead of the LTL checker.
     */
    @Option(
            value = "-f Use FSM checker instead of the default NASA LTL-based checker",
            aliases = { "-use-fsm-checker" })
    public static boolean useFSMChecker = true;

    /**
     * Sets the random seed for Synoptic's source of pseudo-random numbers.
     */
    @Option(
            value = "Use a specific random seed for pseudo-random number generator")
    public static Long randomSeed = null;

    /**
     * Use vector time indexes to partition the output graph into a set of
     * graphs, one per distributed system node type.
     */
    @Option(
            value = "Vector time index sets for partitioning the graph by system node type, e.g. '1,2;3,4'")
    public static String separateVTimeIndexSets = null;
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
    public static String separatorRegExp = null;

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
    @Option(
            value = "-r Parser reg-exp: extracts event type and event time from a log line",
            aliases = { "-regexp" })
    public static List<String> regExps = null;

    /**
     * A substitution expression, used to express how to map the trace lines
     * into partition traces, to be considered as an individual sample of the
     * behavior of the system.
     */
    public static final String partitionRegExpDefault = "\\k<FILE>";
    @Option(
            value = "-m Partitions mapping reg-exp: maps a log line to a partition",
            aliases = { "-partition-mapping" })
    public static String partitionRegExp = partitionRegExpDefault;

    /**
     * This flag indicates whether Synoptic should partition traces by file
     */
    public static boolean partitionViaFile = true;

    /**
     * This option relieves the user from writing regular expressions to parse
     * lines that they are not interested in. This also help to avoid parsing of
     * lines that are corrupted.
     */
    @Option(
            value = "-i Ignore lines that do not match any of the passed regular expressions")
    public static boolean ignoreNonMatchingLines = false;

    /**
     * This allows users to get away with sloppy\incorrect regular expressions
     * that might not fully cover the range of log lines appearing in the log
     * files.
     */
    @Option(
            value = "Ignore parser warnings and attempt to recover from parse errors if possible",
            aliases = { "-ignore-parse-errors" })
    public static boolean recoverFromParseErrors = false;

    /**
     * Output the fields extracted from each log line and terminate.
     */
    @Option(
            value = "Debug the parser by printing field values extracted from the log and then terminate.",
            aliases = { "-debugParse" })
    public static boolean debugParse = false;
    // end option group "Parser Options"

    // //////////////////////////////////////////////////
    /**
     * Command line arguments input filename to use.
     */
    @OptionGroup("Input Options")
    @Option(value = "-c Command line arguments input filename",
            aliases = { "-argsfile" })
    public static String argsFilename = null;
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
    public static String outputPathPrefix = null;

    /**
     * Whether or not to output the list of invariants to a file, with one
     * invariant per line.
     */
    @Option(value = "Output invariants to a file")
    public static boolean outputInvariantsToFile = false;

    /**
     * The absolute path to the dot command executable to use for outputting
     * graphical representations of Synoptic models
     */
    @Option(value = "-d Path to the Graphviz dot command executable to use",
            aliases = { "-dot-executable" })
    public static String dotExecutablePath = null;

    /**
     * This sets the output edge labels on graphs that are exported.
     */
    @Option(
            value = "Output edge labels on graphs to indicate transition probabilities",
            aliases = { "-outputEdgeLabels" })
    public static boolean outputEdgeLabels = true;

    /**
     * Whether or not the output graphs include the common TERMINAL state, to
     * which all final trace nodes have an edge.
     */
    @Option(value = "Show TERMINAL node in generated graphs.")
    public static boolean showTerminalNode = true;

    /**
     * Whether or not the output graphs include the common INITIAL state, which
     * has an edge to all the start trace nodes.
     */
    @Option(value = "Show INITIAL node in generated graphs.")
    public static boolean showInitialNode = true;

    /**
     * Whether or not to show the Synoptic GUI.
     */
    @Option(value = "Show the GUI.")
    public static boolean showGui = false;
    // end option group "Output Options"

    // //////////////////////////////////////////////////
    /**
     * Dump the complete list of mined synoptic.invariants for the set of input
     * files to stdout. This option is <i>unpublicized</i>; it will not appear
     * in the default usage message
     */
    @OptionGroup(value = "Verbosity Options", unpublicized = true)
    @Option("Dump complete list of mined invariant to stdout")
    public static boolean dumpInvariants = false;

    /**
     * Dump the dot representation of the initial graph to file. The file will
     * have the name <outputPathPrefix>.initial.dot, where 'outputPathPrefix' is
     * the filename of the final Synoptic output. This option is
     * <i>unpublicized</i>; it will not appear in the default usage message
     */
    @Option("Dump the initial graph to file <outputPathPrefix>.initial.dot")
    public static boolean dumpInitialGraph = true;

    /**
     * Dump png of graph to file. The file will have the name
     * <outputPathPrefix>.initial.dot, where 'outputPathPrefix' is the filename
     * of the final Synoptic output. This option is <i>unpublicized</i>; it will
     * not appear in the default usage message
     */
    @Option("Dump the initial graph to file <outputPathPrefix>.initial.dot")
    public static boolean dumpPNG = true;

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
    public static boolean dumpIntermediateStages = false;
    // end option group "Verbosity Options"

    // //////////////////////////////////////////////////
    @OptionGroup(value = "Debugging Options", unpublicized = true)
    /**
     * Be extra verbose, print extra detailed information. Sets the log level to
     * FINEST.
     */
    @Option(value = "Print extra detailed information during execution")
    public static boolean logLvlExtraVerbose = false;

    /**
     * Used to select the algorithm for mining invariants.
     */
    @Option("Use the transitive closure invariant mining algorithm (usually slower)")
    public static boolean useTransitiveClosureMining = false;

    /**
     * Used to tell Synoptic to not go past mining invariants.
     */
    @Option("Mine invariants and then quit.")
    public static boolean onlyMineInvariants = false;

    /**
     * Do not perform the coarsening stage in Synoptic, and as final output use
     * the most refined representation. This option is <i>unpublicized</i>; it
     * will not appear in the default usage message
     */
    @Option("Do not perform the coarsening stage")
    public static boolean noCoarsening = false;

    /**
     * Perform benchmarking and output benchmark information. This option is
     * <i>unpublicized</i>; it will not appear in the default usage message
     */
    @Option("Perform benchmarking and output benchmark information")
    public static boolean doBenchmarking = false;

    /**
     * Intern commonly occurring strings, such as event types, as a memory-usage
     * optimization. This option is <i>unpublicized</i>; it will not appear in
     * the default usage message
     */
    @Option("Intern commonly occurring strings, such as event types, as a memory-usage optimization")
    public static boolean internCommonStrings = true;

    /**
     * Run all tests in synoptic.tests.units -- all the unit tests, and then
     * terminate. This option is <i>unpublicized</i>; it will not appear in the
     * default usage message
     */
    @Option("Run all tests in synoptic.tests.units, and then terminate.")
    public static boolean runTests = false;

    /**
     * Run a benchmark to evaluate PO log invariant mining algorithms on a
     * hard-coded directory of input log files. This option is
     * <i>unpublicized</i>; it will not appear in the default usage message
     */
    @Option("Benchmark the PO log mining.")
    public static boolean benchPOMining = false;

    /**
     * Run all tests in synoptic.tests -- unit and integration tests, and then
     * terminate. This option is <i>unpublicized</i>; it will not appear in the
     * default usage message
     */
    @Option("Run all tests in synoptic.tests, and then terminate.")
    public static boolean runAllTests = false;

    /**
     * Turns on correctness checks that are disabled by default due to their
     * expensive cpu\memory usage profiles.
     */
    @Option("Perform extra correctness checks at the expense of cpu and memory usage.")
    public static boolean performExtraChecks = false;

    /**
     * Do not perform the refinement (and therefore do not perform coarsening)
     * and do not produce any representation as output. This is useful for just
     * printing the list of mined synoptic.invariants (using the option
     * 'dumpInvariants' above). This option is <i>unpublicized</i>; it will not
     * appear in the default usage message
     */
    @Option("Do not perform refinement")
    public static boolean noRefinement = false;
    // end option group "Debugging Options"

    /**
     * Input log files to run Synoptic on. These should appear without any
     * options as the final elements in the command line.
     */
    public static List<String> logFilenames = null;

    /** One line synopsis of usage */
    private static String usage_string = "synoptic [options] <logfiles-to-analyze>";

    /**
     * The synoptic.main method to perform the inference algorithm. See user
     * documentation for an explanation of the options.
     * 
     * @param args
     *            - command-line options
     */
    public static void main(String[] args) throws Exception {
        // this directly sets the static member options of the Main class
        Options options = new Options(usage_string, Main.class);
        String[] cmdLineArgs = options.parse_or_usage(args);

        if (argsFilename != null) {
            // read program arguments from a file
            InputStream argsStream = new FileInputStream(argsFilename);
            ListedProperties props = new ListedProperties();
            props.load(argsStream);
            String[] cmdLineFileArgs = props.getCmdArgsLine();
            // the file-based args become the default args
            options.parse_or_usage(cmdLineFileArgs);
        }

        // Parse the command line args to override any of the above config file
        // args
        options.parse_or_usage(args);

        // The remainder of the command line is treated as a list of log
        // filenames to process
        logFilenames = Arrays.asList(cmdLineArgs);

        setUpLogging();

        // Display help for all option groups, including unpublicized ones
        if (allHelp) {
            System.out.println("Usage: " + usage_string);
            System.out
                    .println(options.usage("General Options",
                            "Execution Options", "Parser Options",
                            "Input Options", "Output Options",
                            "Verbosity Options", "Debugging Options"));
            return;
        }

        // Display help just for the 'publicized' option groups
        if (help) {
            options.print_usage();
            return;
        }

        if (version) {
            System.out.println("Synoptic version " + Main.versionString);
            return;
        }

        if (runAllTests) {
            List<String> testClasses = getTestsInPackage("synoptic.tests.units.");
            testClasses
                    .addAll(getTestsInPackage("synoptic.tests.integration."));
            runTests(testClasses);
        } else if (runTests) {
            List<String> testClassesUnits = getTestsInPackage("synoptic.tests.units.");
            runTests(testClassesUnits);
        } else if (benchPOMining) {
            runBenchPOMining("/Users/ivan/synoptic/trunk/traces/abstract/slaml11-benchmarking-po-traces/nodes-2_etypes-20_events-1000_execs-100.txt");
        }

        if (logFilenames.size() == 0) {
            logger.severe("No log filenames specified, exiting. Try cmd line option:\n\t"
                    + Main.getCmdLineOptDesc("help"));
            return;
        }

        if (dumpIntermediateStages && outputPathPrefix == null) {
            logger.severe("Cannot dump intermediate stages without an output path prefix. Set this prefix with:\n\t"
                    + Main.getCmdLineOptDesc("outputPathPrefix"));
            return;
        }

        Main mainInstance = new Main();

        if (logLvlVerbose || logLvlExtraVerbose) {
            mainInstance.printOptions();
        }

        if (randomSeed == null) {
            Main.randomSeed = System.currentTimeMillis();
        }
        Main.random = new Random(randomSeed);
        logger.info("Using random seed: " + randomSeed);

        Integer ret;
        try {
            ret = mainInstance.call();
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw InternalSynopticException.Wrap(e);
        }

        logger.fine("Main.call() returned " + ret.toString());
        System.exit(ret);
    }

    /**
     * Returns a command line option description for an option name
     * 
     * @param optName
     *            The option variable name
     * @return a string description of the option
     * @throws InternalSynopticException
     *             if optName cannot be accessed
     */
    public static String getCmdLineOptDesc(String optName)
            throws InternalSynopticException {
        Field field;
        try {
            field = Main.class.getField(optName);
        } catch (SecurityException e) {
            throw InternalSynopticException.Wrap(e);
        } catch (NoSuchFieldException e) {
            throw InternalSynopticException.Wrap(e);
        }
        Option opt = field.getAnnotation(Option.class);
        String desc = opt.value();
        if (desc.length() > 0 && desc.charAt(0) != '-') {
            // For options that do not have a short option form,
            // include the long option trigger in the description.
            desc = "--" + optName + " " + desc;
        }
        return desc;
    }

    /**
     * Runs all the synoptic unit tests
     * 
     * @throws URISyntaxException
     *             if Main.class can't be located
     */
    public static List<String> getTestsInPackage(String packageName)
            throws URISyntaxException {
        // If we are running from within a jar then jarName contains the path to
        // the jar
        // otherwise, it contains the path to where Main.class is located on the
        // filesystem
        String jarName = Main.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI().getPath();
        System.out.println("Looking for tests in: " + jarName);

        // We assume that the tests we want to run are classes within
        // packageName, which can be found with the corresponding packagePath
        // filesystem offset
        String packagePath = packageName.replaceAll("\\.", File.separator);

        ArrayList<String> testClasses = new ArrayList<String>();

        try {
            // Case1: running from within a jar
            // Open the jar file and locate the tests by their path
            JarInputStream jarFile = new JarInputStream(new FileInputStream(
                    jarName));
            JarEntry jarEntry;
            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                String className = jarEntry.getName();
                if (className.startsWith(packagePath)
                        && className.endsWith(".class")) {
                    int endIndex = className.lastIndexOf(".class");
                    className = className.substring(0, endIndex);
                    testClasses.add(className.replaceAll("/", "\\."));
                }
            }
        } catch (java.io.FileNotFoundException e) {
            // Case2: not running from within a jar
            // Find the tests by walking through the directory structure
            File folder = new File(jarName + packagePath);
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                String className = listOfFiles[i].getName();
                if (listOfFiles[i].isFile() && className.endsWith(".class")) {
                    int endIndex = className.lastIndexOf(".class");
                    className = className.substring(0, endIndex);
                    testClasses.add(packageName + className);
                }
            }
        } catch (Exception e) {
            throw InternalSynopticException.Wrap(e);
        }

        // Remove anonymous inner classes from the list, these look
        // 'TraceParserTests$1.class'
        ArrayList<String> anonClasses = new ArrayList<String>();
        for (String testClass : testClasses) {
            if (testClass.contains("$")) {
                anonClasses.add(testClass);
            }
        }
        testClasses.removeAll(anonClasses);

        return testClasses;
    }

    /**
     * Takes a list of paths that point to JUnit test classes and executes them
     * using JUnitCore runner.
     * 
     * @param testClasses
     */
    public static void runTests(List<String> testClasses) {
        System.out.println("Running tests: " + testClasses);
        String[] testClassesAr = new String[testClasses.size()];
        testClassesAr = testClasses.toArray(testClassesAr);
        JUnitCore.main(testClassesAr);
    }

    public static void runBenchPOMining(String fname) {
        InvariantMiner miner;

        miner = new TransitiveClosureInvMiner(false);
        // new TransitiveClosureTOInvMiner(true)
        // miner = new DAGWalkingPOInvMiner();

        TraceParser parser = new TraceParser();
        // parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        // parser.addPartitionsSeparator("^--$");

        // for (int iter = 0; iter < numIterations; iter++) {

        // }
    }

    /**
     * Sets up and configures the Main.logger object based on command line
     * arguments
     */
    public static void setUpLogging() {
        // Get the top Logger instance
        logger = Logger.getLogger("");

        // Handler for console (reuse it if it already exists)
        Handler consoleHandler = null;

        // See if there is already a console handler
        for (Handler handler : logger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandler = handler;
                break;
            }
        }

        if (consoleHandler == null) {
            // No console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            logger.addHandler(consoleHandler);
        }

        // The consoleHandler will write out anything the logger gives it
        consoleHandler.setLevel(Level.ALL);

        // consoleHandler.setFormatter(new CustomFormatter());

        // Set the logger's log level based on command line arguments
        if (logLvlQuiet) {
            logger.setLevel(Level.WARNING);
        } else if (logLvlVerbose) {
            logger.setLevel(Level.FINE);
        } else if (logLvlExtraVerbose) {
            logger.setLevel(Level.FINEST);
        } else {
            logger.setLevel(Level.INFO);
        }

        consoleHandler.setFormatter(new BriefLogFormatter());
        return;
    }

    /**
     * Given a potentially wild-carded file path, finds all those which match.
     * TODO: make sure that the same file doesn't appear twice in the returned
     * list
     * 
     * @param fileArg
     *            The file path which may potentially contain wildcards.
     * @return An array of File handles which match.
     * @throws Exception
     */
    public static File[] getFiles(String fileArg) throws Exception {
        int wildix = fileArg.indexOf("*");
        if (wildix == -1) {
            return new File[] { new File(fileArg) };
        } else {
            String uptoWild = fileArg.substring(0, wildix);
            String path = FilenameUtils.getFullPath(uptoWild);
            String filter = FilenameUtils.getName(uptoWild)
                    + fileArg.substring(wildix);
            File dir = new File(path).getAbsoluteFile();
            // TODO: check that listFiles is working properly recursively here.
            File[] results = dir.listFiles((FileFilter) new WildcardFileFilter(
                    filter));
            if (results == null) {
                throw new Exception("Wildcard match failed: "
                        + (dir.isDirectory() ? dir.toString()
                                + " not a directory" : " for unknown reason"));
            }
            return results;
        }
    }

    /**
     * Returns the filename for an intermediate dot file based on the given
     * stage name and round number. Adheres to the convention specified above in
     * usage, namely that the filename is of the format:
     * outputPathPrefix.stage-S.round-R.dot
     * 
     * @param stageName
     *            Stage name string, e.g. "r" for refinement
     * @param roundNum
     *            Round number within the stage
     * @return string filename for an intermediate dot file
     */
    public static String getIntermediateDumpFilename(String stageName,
            int roundNum) {
        return outputPathPrefix + ".stage-" + stageName + ".round-" + roundNum
                + ".dot";
    }

    /***********************************************************/

    public Main() {
        // TODO: can set up graphical state here
    }

    /**
     * Prints the values of all the options for this instance of Main class
     * 
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void printOptions() throws IllegalArgumentException,
            IllegalAccessException {
        String optsString = "Synoptic options:\n";
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getAnnotation(Option.class) != null) {
                optsString += "\t" + field.getName() + ": ";
                if (field.get(this) != null) {
                    optsString += field.get(this).toString() + "\n";
                } else {
                    optsString += "null\n";
                }
            }
        }
        System.out.println(optsString);
    }

    public static TraceParser newTraceParser(List<String> regExps,
            String partitionRegExp, String separatorRegExp)
            throws ParseException {
        TraceParser parser = new TraceParser();

        logger.fine("Setting up the log file parser.");
        if (partitionRegExp == Main.partitionRegExpDefault) {
            logger.info("Using the default partitions mapping regex: "
                    + Main.partitionRegExpDefault);
        }

        if (!regExps.isEmpty()) {
            // The user provided custom regular expressions.
            for (String exp : regExps) {
                logger.fine("\taddRegex with exp:" + exp);
                parser.addRegex(exp);
            }

            parser.setPartitionsMap(partitionRegExp);
        } else {
            // No custom regular expressions provided - warn and use defaults.
            logger.warning("Using a default regular expression to parse log-lines: "
                    + "will map the entire log line to an event type."
                    + "\nTo use a custom regular expressions use the option:\n\t"
                    + Main.getCmdLineOptDesc("regExps") + "\n\t");
            // TODO: is this next statement necessary?
            // parser.addRegex("^\\s*$(?<SEPCOUNT++>)");
            parser.addRegex("(?<TYPE>.*)");
            parser.setPartitionsMap(partitionRegExp);
        }

        if (separatorRegExp != null) {
            parser.addPartitionsSeparator(separatorRegExp);
            if (partitionRegExp != Main.partitionRegExpDefault) {
                logger.warning("Partition separator and partition mapping regex are both specified. This may result in difficult to understand parsing behavior.");
            }
        }
        return parser;
    }

    /**
     * The workhorse method, which uses TraceParser to parse the input files,
     * and calls the primary Synoptic functions to perform refinement\coarsening
     * and finally outputs the final graph to the output file (specified as a
     * command line option).
     */
    @Override
    public Integer call() throws Exception {
        Locale.setDefault(Locale.US);
        TraceParser parser = newTraceParser(Main.regExps, Main.partitionRegExp,
                Main.separatorRegExp);
        long startTime;

        // Parses all the log filenames, constructing the parsedEvents List.
        ArrayList<EventNode> parsedEvents = new ArrayList<EventNode>();

        logger.info("Parsing input files..");
        startTime = System.currentTimeMillis();

        for (String fileArg : Main.logFilenames) {
            logger.fine("\tprocessing fileArg: " + fileArg);
            File[] files = getFiles(fileArg);
            for (File file : files) {
                logger.fine("\tcalling parseTraceFile with file: "
                        + file.getAbsolutePath());
                try {
                    parsedEvents.addAll(parser.parseTraceFile(file, -1));
                } catch (ParseException e) {
                    logger.severe("Caught ParseException -- unable to continue, exiting. Try cmd line option:\n\t"
                            + Main.getCmdLineOptDesc("help"));
                    logger.severe(e.toString());
                    return new Integer(1);
                }
            }
        }

        logger.info("Parsing took " + (System.currentTimeMillis() - startTime)
                + "ms");

        if (Main.debugParse) {
            // Terminate since the user is interested in debugging the parser.
            logger.info("Terminating. To continue further, re-run without the debugParse option.");
            return new Integer(0);
        }

        // If we parsed any events, then run Synoptic.
        logger.info("Running Synoptic...");

        logger.info("Generating inter-event temporal relation...");
        startTime = System.currentTimeMillis();
        Graph<EventNode> inputGraph = parser
                .generateDirectTemporalRelation(parsedEvents);
        logger.info("Generating temporal relation took "
                + (System.currentTimeMillis() - startTime) + "ms");

        GraphVizExporter exporter = new GraphVizExporter();

        if (dumpInitialGraph) {
            // If we were given an output filename then export the resulting
            // graph into outputPathPrefix.initial.dot
            if (Main.outputPathPrefix != null) {
                logger.info("Exporting initial graph ["
                        + inputGraph.getNodes().size() + " nodes]..");
                exporter.exportAsDotAndPngFast(Main.outputPathPrefix
                        + ".initial.dot", inputGraph, true);
            } else {
                logger.warning("Cannot output initial graph. Specify output path prefix using:\n\t"
                        + Main.getCmdLineOptDesc("outputPathPrefix"));
            }
        }

        if (separateVTimeIndexSets != null) {
            // separateVTimeIndexSets is assumed to be in a format like:
            // "1,2;3;4,5,6" where the sets are {1,2}, {3}, {4,5,6}.
            LinkedList<LinkedHashSet<Integer>> indexSets = new LinkedList<LinkedHashSet<Integer>>();
            for (String strSet : separateVTimeIndexSets.split(";")) {
                LinkedHashSet<Integer> iSet = new LinkedHashSet<Integer>();
                indexSets.add(iSet);
                for (String index : strSet.split(",")) {
                    iSet.add(Integer.parseInt(index));
                }
            }
        }

        // Invariant minders depend on total/partial ordering of the log.
        InvariantMiner miner;
        if (parser.logTimeTypeIsTotallyOrdered()) {
            miner = new ChainWalkingTOInvMiner();
        } else {
            if (useTransitiveClosureMining) {
                miner = new TransitiveClosureInvMiner();
            } else {
                miner = new DAGWalkingPOInvMiner();
            }
        }
        // Parser can be garbage-collected.
        parser = null;

        logger.info("Mining invariants [" + miner.getClass().getName() + "]..");
        startTime = System.currentTimeMillis();
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);
        logger.info("Mining took " + (System.currentTimeMillis() - startTime)
                + "ms");
        // Miner can be garbage-collected.
        miner = null;

        logger.info("Mined " + minedInvs.numInvariants() + " invariants");
        if (dumpInvariants) {
            logger.info("Mined invariants: " + minedInvs);
        }

        if (onlyMineInvariants) {
            return new Integer(0);
        }

        if (outputInvariantsToFile) {
            String invariantsFilename = outputPathPrefix + ".invariants.txt";
            logger.info("Outputting invarians to file: " + invariantsFilename);
            minedInvs.outputToFile(invariantsFilename);
        }

        // Create the initial partitioning graph.
        startTime = System.currentTimeMillis();
        PartitionGraph pGraph = new PartitionGraph(inputGraph, true, minedInvs);
        logger.info("Creating partition graph took "
                + (System.currentTimeMillis() - startTime) + "ms");
        // inputGraph can be garbage-collected.
        inputGraph = null;

        if (showGui) {
            JungGui gui = new JungGui(pGraph);
            gui.init();
            synchronized (gui) {
                gui.wait();
            }
            return new Integer(0);
        }

        if (logLvlVerbose || logLvlExtraVerbose) {
            System.out.println("");
            System.out.println("");
        }
        logger.info("Refining (Splitting)...");
        startTime = System.currentTimeMillis();
        Bisimulation.splitPartitions(pGraph);
        logger.info("Splitting took "
                + (System.currentTimeMillis() - startTime) + "ms");

        if (logLvlVerbose || logLvlExtraVerbose) {
            System.out.println("");
            System.out.println("");
        }
        logger.info("Coarsening (Merging)..");
        startTime = System.currentTimeMillis();
        Bisimulation.mergePartitions(pGraph);
        logger.info("Merging took " + (System.currentTimeMillis() - startTime)
                + "ms");

        // TODO: check that none of the initially mined synoptic.invariants are
        // unsatisfied in the result

        // export the resulting graph
        if (Main.outputPathPrefix != null) {
            logger.info("Exporting final graph [" + pGraph.getNodes().size()
                    + " nodes]..");
            startTime = System.currentTimeMillis();
            exporter.exportAsDotAndPngFast(Main.outputPathPrefix + ".dot",
                    pGraph);
            logger.info("Exporting took "
                    + (System.currentTimeMillis() - startTime) + "ms");
        } else {
            logger.warning("Cannot output final graph. Specify output path prefix using:\n\t"
                    + Main.getCmdLineOptDesc("outputPathPrefix"));
        }

        return new Integer(0);
    }
}
