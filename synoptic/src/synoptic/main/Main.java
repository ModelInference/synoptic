package synoptic.main;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.runner.JUnitCore;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverConcurrentInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.DAGWalkingPOInvMiner;
import synoptic.invariants.miners.POInvariantMiner;
import synoptic.invariants.miners.TOInvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.PartitionGraph;
import synoptic.model.export.DotExportFormatter;
import synoptic.model.export.GmlExportFormatter;
import synoptic.model.export.GraphExportFormatter;
import synoptic.model.export.GraphExporter;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
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

    /**
     * Formatter to use for exporting graphs (DOT/GML formatter).
     */
    public static GraphExportFormatter graphExportFormatter = null;

    /**
     * Synoptic options parsed from the command line or set in some other way.
     */
    public static SynopticOptions options = null;

    /**
     * Retrieve and return the ChangesetID attribute in the manifest of the jar
     * that contains this Main class. If not running from a jar, returns null.
     */
    public static String getHgChangesetID() {
        String changesetID = null;
        try {
            // Find the jar corresponding to Main (this) class.
            URL res = Main.class.getResource(Main.class.getSimpleName()
                    + ".class");
            JarURLConnection conn = (JarURLConnection) res.openConnection();
            // Grab attributes from the manifest of the jar (synoptic.jar)
            Manifest mf = conn.getManifest();
            Attributes atts = mf.getMainAttributes();
            // Extract ChangesetID from the attributes and print it out.
            changesetID = atts.getValue("ChangesetID");
        } catch (Exception e) {
            // We might get an exception in the case that we're not running
            // from inside a jar. In this case, simply don't print the
            // ChangesetID.
            return null;
        }
        return changesetID;
    }

    /**
     * The synoptic.main method to perform the inference algorithm. See user
     * documentation for an explanation of the options.
     * 
     * @param args
     *            Command-line options
     */
    public static void main(String[] args) throws Exception {
        Main mainInstance = processArgs(args);
        if (mainInstance == null) {
            return;
        }

        Integer ret;
        try {
            ret = mainInstance.call();
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw InternalSynopticException.wrap(e);
        }

        logger.fine("Main.call() returned " + ret.toString());

    }

    /**
     * Parses the set of arguments (args) to the program, to set up static state
     * in Main. This state includes everything necessary to run Synoptic --
     * input log files, regular expressions, etc.
     * 
     * @param args
     *            Command line arguments that specify how Synoptic should
     *            behave.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    public static Main processArgs(String[] args) throws IOException,
            URISyntaxException, IllegalArgumentException,
            IllegalAccessException, ParseException {
        SynopticOptions opts = new SynopticOptions(args);
        return processArgs(opts);
    }

    /**
     * Uses the parsed opts to set up static state in Main. This state includes
     * everything necessary to run Synoptic -- input log files, regular
     * expressions, etc.
     * 
     * @param opts
     *            Parsed command line arguments.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ParseException
     */
    public static Main processArgs(SynopticOptions opts) throws IOException,
            URISyntaxException, IllegalArgumentException,
            IllegalAccessException {

        Main.options = opts;

        setUpLogging();

        // Display help for all option groups, including unpublicized ones
        if (opts.allHelp) {
            opts.printLongHelp();
            return null;
        }

        // Display help just for the 'publicized' option groups
        if (opts.help) {
            opts.printShortHelp();
            return null;
        }

        if (opts.version) {
            System.out.println("Synoptic version " + Main.versionString);
            String changesetID = getHgChangesetID();
            if (changesetID != null) {
                System.out.println("Synoptic changeset " + changesetID);
            }
            return null;
        }

        // Setup the appropriate graph export formatter object.
        if (opts.exportAsGML) {
            graphExportFormatter = new GmlExportFormatter();
        } else {
            graphExportFormatter = new DotExportFormatter();
        }

        if (opts.runAllTests) {
            List<String> testClasses = getTestsInPackage("synoptic.tests.units.");
            testClasses
                    .addAll(getTestsInPackage("synoptic.tests.integration."));
            runTests(testClasses);
        } else if (opts.runTests) {
            List<String> testClassesUnits = getTestsInPackage("synoptic.tests.units.");
            runTests(testClassesUnits);
        }

        if (opts.logFilenames.size() == 0
                || opts.logFilenames.get(0).equals("")) {
            logger.severe("No log filenames specified, exiting. Try cmd line option:\n\t"
                    + SynopticOptions.getOptDesc("help"));
            return null;
        }

        if (opts.dumpIntermediateStages && opts.outputPathPrefix == null) {
            logger.severe("Cannot dump intermediate stages without an output path prefix. Set this prefix with:\n\t"
                    + SynopticOptions.getOptDesc("outputPathPrefix"));
            return null;
        }

        if (opts.logLvlVerbose || opts.logLvlExtraVerbose) {
            opts.printOptionValues();
        }

        Main.random = new Random(opts.randomSeed);
        logger.info("Using random seed: " + opts.randomSeed);

        Main mainInstance = new Main();
        return mainInstance;
    }

    /**
     * Runs all the synoptic unit tests
     * 
     * @throws URISyntaxException
     *             if Main.class can't be located
     * @throws IOException
     */
    public static List<String> getTestsInPackage(String packageName)
            throws URISyntaxException, IOException {
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

        JarInputStream jarFile = null;
        try {
            // Case1: running from within a jar
            // Open the jar file and locate the tests by their path
            jarFile = new JarInputStream(new FileInputStream(jarName));
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
            throw InternalSynopticException.wrap(e);
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
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

    /**
     * Sets up and configures the Main.logger object based on command line
     * arguments.
     * 
     * <pre>
     * Assumes that Main.options is initialized.
     * </pre>
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
        if (Main.options.logLvlQuiet) {
            logger.setLevel(Level.WARNING);
        } else if (Main.options.logLvlVerbose) {
            logger.setLevel(Level.FINE);
        } else if (Main.options.logLvlExtraVerbose) {
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
        }
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
                    + (dir.isDirectory() ? dir.toString() + " not a directory"
                            : " for unknown reason"));
        }
        return results;
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
        return Main.options.outputPathPrefix + ".stage-" + stageName
                + ".round-" + roundNum;
    }

    /**
     * Serializes g using a dot/gml format and optionally outputs a png file
     * corresponding to the serialized format (dot format export only).
     * 
     * @throws IOException
     */
    private static <T extends INode<T>> void exportGraph(String baseFilename,
            IGraph<T> g, boolean outputEdgeLabelsCondition,
            boolean imageGenCondition) {

        if (options.outputPathPrefix == null) {
            logger.warning("Cannot output initial graph. Specify output path prefix using:\n\t"
                    + SynopticOptions.getOptDesc("outputPathPrefix"));
            return;
        }

        String filename = null;
        if (options.exportAsGML) {
            filename = baseFilename + ".gml";
        } else {
            filename = baseFilename + ".dot";
        }
        try {
            GraphExporter.exportGraph(filename, g, outputEdgeLabelsCondition);
        } catch (IOException e) {
            logger.fine("Unable to export graph to " + filename);
        }

        if (imageGenCondition) {
            // Currently we support only .dot -> .png generation
            GraphExporter.generatePngFileFromDotFile(filename);
        }
    }

    /**
     * Export g as an initial graph.
     */
    public static <T extends INode<T>> void exportInitialGraph(
            String baseFilename, IGraph<T> g) {
        // false below : never include edge labels on exported initial graphs

        // Main.dumpInitialGraphPngFile && !exportAsGML below : whether to
        // convert exported graph to a png file -- the user must have explicitly
        // requested this and the export must be in non-GML format (i.e., dot
        // format).
        exportGraph(baseFilename, g, false,
                Main.options.dumpInitialGraphPngFile
                        && !Main.options.exportAsGML);
    }

    /**
     * Export g as a non-initial graph.
     */
    public static <T extends INode<T>> void exportNonInitialGraph(
            String baseFilename, IGraph<T> g) {
        // Main.outputEdgeLabels below : the condition for including edge labels
        // on exported graphs.

        // !exportAsGML below : the condition for exporting an image to png file
        // is that it is not in GML format (i.e., it is in dot format so we can
        // use the 'dot' command).
        exportGraph(baseFilename, g, Main.options.outputEdgeLabels,
                !Main.options.exportAsGML);
    }

    /***********************************************************/

    private Main() {
        // TODO: initialize instance state.
    }

    public static TraceParser newTraceParser(List<String> rExps,
            String partitioningRegExp, String sepRegExp) throws ParseException {
        assert (partitioningRegExp != null);

        TraceParser parser = new TraceParser();

        logger.fine("Setting up the log file parser.");
        if (partitioningRegExp.equals(SynopticOptions.partitionRegExpDefault)) {
            logger.info("Using the default partitions mapping regex: "
                    + SynopticOptions.partitionRegExpDefault);
        }

        if (!rExps.isEmpty()) {
            // The user provided custom regular expressions.
            for (String exp : rExps) {
                logger.fine("\taddRegex with exp:" + exp);
                parser.addRegex(exp);
            }

            parser.setPartitionsMap(partitioningRegExp);
        } else {
            // No custom regular expressions provided - warn and use defaults.
            logger.warning("Using a default regular expression to parse log-lines: "
                    + "will map the entire log line to an event type."
                    + "\nTo use a custom regular expressions use the option:\n\t"
                    + SynopticOptions.getOptDesc("regExps") + "\n\t");
            // TODO: is this next statement necessary?
            // parser.addRegex("^\\s*$(?<SEPCOUNT++>)");
            parser.addRegex(SynopticOptions.regExpDefault);
            parser.setPartitionsMap(partitioningRegExp);
        }

        if (sepRegExp != null) {
            parser.addPartitionsSeparator(sepRegExp);
            if (!partitioningRegExp
                    .equals(SynopticOptions.partitionRegExpDefault)) {
                logger.warning("Partition separator and partition mapping regex are both specified. This may result in difficult to understand parsing behavior.");
            }
        }
        return parser;
    }

    public List<EventNode> parseFiles(TraceParser parser, List<String> filenames)
            throws Exception {
        List<EventNode> parsedEvents = new ArrayList<EventNode>();
        for (String fileArg : filenames) {
            logger.fine("\tprocessing fileArg: " + fileArg);
            File[] files = getFiles(fileArg);
            if (files.length == 0) {
                throw new ParseException(
                        "The set of input files is empty. Please specify a set of existing files to parse.");
            }
            for (File file : files) {
                logger.fine("\tcalling parseTraceFile with file: "
                        + file.getAbsolutePath());
                parsedEvents.addAll(parser.parseTraceFile(file, -1));
            }
        }
        return parsedEvents;
    }

    private long loggerInfoStart(String msg) {
        logger.info(msg);
        return System.currentTimeMillis();
    }

    private void loggerInfoEnd(String msg, long startTime) {
        logger.info(msg + (System.currentTimeMillis() - startTime) + "ms");
    }

    private void processPOLog(TraceParser parser, List<EventNode> parsedEvents)
            throws ParseException, FileNotFoundException {
        // //////////////////
        long startTime = loggerInfoStart("Generating inter-event temporal relation...");
        DAGsTraceGraph inputGraph = parser
                .generateDirectPORelation(parsedEvents);
        loggerInfoEnd("Generating temporal relation took ", startTime);
        // //////////////////

        // Parser can be garbage-collected.
        parser = null;

        // TODO: vector time index sets aren't used yet.
        if (options.separateVTimeIndexSets != null) {
            // separateVTimeIndexSets is assumed to be in a format like:
            // "1,2;3;4,5,6" where the sets are {1,2}, {3}, {4,5,6}.
            LinkedList<LinkedHashSet<Integer>> indexSets = new LinkedList<LinkedHashSet<Integer>>();
            for (String strSet : options.separateVTimeIndexSets.split(";")) {
                LinkedHashSet<Integer> iSet = new LinkedHashSet<Integer>();
                indexSets.add(iSet);
                for (String index : strSet.split(",")) {
                    iSet.add(Integer.parseInt(index));
                }
            }
        }

        POInvariantMiner miner;
        if (options.useTransitiveClosureMining) {
            miner = new TransitiveClosureInvMiner();
        } else {
            miner = new DAGWalkingPOInvMiner(options.mineNeverConcurrentWithInv);
        }

        // //////////////////
        startTime = loggerInfoStart("Mining invariants ["
                + miner.getClass().getName() + "]..");
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);
        loggerInfoEnd("Mining took ", startTime);
        // //////////////////

        // Miner can be garbage-collected.
        miner = null;

        logger.info("Mined " + minedInvs.numInvariants() + " invariants");

        int totalNCwith = 0;
        for (ITemporalInvariant inv : minedInvs.getSet()) {
            if (inv instanceof NeverConcurrentInvariant) {
                totalNCwith++;
            }
        }
        logger.info("\tMined " + totalNCwith
                + " NeverConcurrentWith invariants");

        if (options.dumpInvariants) {
            logger.info("Mined invariants: " + minedInvs);
        }

        if (options.outputInvariantsToFile) {
            String invariantsFilename = options.outputPathPrefix
                    + ".invariants.txt";
            logger.info("Outputting invarians to file: " + invariantsFilename);
            minedInvs.outputToFile(invariantsFilename);
        }
    }

    /**
     * The top-level method that uses TraceParser to parse the input files, and
     * calls the primary Synoptic functions to perform refinement\coarsening and
     * then to output the final graph to the output file (specified as a command
     * line option).
     */
    @Override
    public Integer call() throws Exception {
        PartitionGraph pGraph = createInitialPartitionGraph();
        if (pGraph != null) {
            runSynoptic(pGraph);
        }
        return Integer.valueOf(0);
    }

    /**
     * Uses the values of static variables in Main to (1) read and parse the
     * input log files, (2) to mine invariants from the parsed files, and (3)
     * construct an initial partition graph model of the parsed files.
     * 
     * @return The initial partition graph built from the parsed files or null.
     *         Returns null when the arguments passed to Main require an early
     *         termination.
     * @throws Exception
     */
    public PartitionGraph createInitialPartitionGraph() throws Exception {
        Locale.setDefault(Locale.US);
        TraceParser parser = newTraceParser(options.regExps,
                options.partitionRegExp, options.separatorRegExp);
        long startTime;

        // //////////////////
        // Parses all the log filenames, constructing the parsedEvents List.
        startTime = loggerInfoStart("Parsing input files..");
        List<EventNode> parsedEvents;
        try {
            parsedEvents = parseFiles(parser, options.logFilenames);
        } catch (ParseException e) {
            logger.severe("Caught ParseException -- unable to continue, exiting. Try cmd line option:\n\t"
                    + SynopticOptions.getOptDesc("help"));
            logger.severe(e.toString());
            return null;
        }
        loggerInfoEnd("Parsing took ", startTime);
        // //////////////////

        if (options.debugParse) {
            // Terminate since the user is interested in debugging the parser.
            logger.info("Terminating. To continue further, re-run without the debugParse option.");
            return null;
        }

        // PO Logs are processed separately.
        if (!parser.logTimeTypeIsTotallyOrdered()) {
            logger.warning("Partially ordered log input detected. Only mining invariants since refinement/coarsening is not yet supported.");
            processPOLog(parser, parsedEvents);
            return null;
        }

        if (parsedEvents.size() == 0) {
            logger.severe("Did not parse any events from the input log files. Stopping.");
            return null;
        }

        // //////////////////
        startTime = loggerInfoStart("Generating inter-event temporal relation...");
        ChainsTraceGraph inputGraph = parser
                .generateDirectTORelation(parsedEvents);
        loggerInfoEnd("Generating temporal relation took ", startTime);
        // //////////////////

        if (options.dumpInitialGraphDotFile) {
            logger.info("Exporting initial graph ["
                    + inputGraph.getNodes().size() + " nodes]..");
            exportInitialGraph(options.outputPathPrefix + ".initial",
                    inputGraph);
        }

        TOInvariantMiner miner;
        if (options.useTransitiveClosureMining) {
            miner = new TransitiveClosureInvMiner();
        } else {
            miner = new ChainWalkingTOInvMiner();
        }

        // Parser can be garbage-collected.
        parser = null;

        // //////////////////
        startTime = loggerInfoStart("Mining invariants ["
                + miner.getClass().getName() + "]..");
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);
        loggerInfoEnd("Mining took ", startTime);
        // //////////////////

        // Miner can be garbage-collected.
        miner = null;

        logger.info("Mined " + minedInvs.numInvariants() + " invariants");

        if (options.dumpInvariants) {
            logger.info("Mined invariants: " + minedInvs);
        }

        if (options.outputInvariantsToFile) {
            String invariantsFilename = options.outputPathPrefix
                    + ".invariants.txt";
            logger.info("Outputting invarians to file: " + invariantsFilename);
            minedInvs.outputToFile(invariantsFilename);
        }

        if (options.onlyMineInvariants) {
            return null;
        }

        // //////////////////
        // Create the initial partitioning graph.
        startTime = loggerInfoStart("Creating initial partition graph.");
        PartitionGraph pGraph = new PartitionGraph(inputGraph, true, minedInvs);
        loggerInfoEnd("Creating partition graph took ", startTime);
        // //////////////////

        if (options.dumpInitialPartitionGraph) {
            exportGraph(options.outputPathPrefix + ".condensed", pGraph, true,
                    true);
        }

        return pGraph;
    }

    /**
     * Runs the Synoptic algorithm starting from the initial graph (pGraph). The
     * pGraph is assumed to be fully initialized and ready for refinement. The
     * Synoptic algorithm first runs a refinement algorithm, and then runs a
     * coarsening algorithm.
     * 
     * @param pGraph
     *            The initial graph model to start refining.
     */
    public void runSynoptic(PartitionGraph pGraph) {
        long startTime;

        if (options.logLvlVerbose || options.logLvlExtraVerbose) {
            System.out.println("");
            System.out.println("");
        }

        // //////////////////
        startTime = loggerInfoStart("Refining (Splitting)...");
        Bisimulation.splitPartitions(pGraph);
        loggerInfoEnd("Splitting took ", startTime);
        // //////////////////

        if (options.logLvlVerbose || options.logLvlExtraVerbose) {
            System.out.println("");
            System.out.println("");
        }

        // //////////////////
        startTime = loggerInfoStart("Coarsening (Merging)..");
        Bisimulation.mergePartitions(pGraph);
        loggerInfoEnd("Merging took ", startTime);
        // //////////////////

        // At this point, we have the final model in the pGraph object.

        // TODO: check that none of the initially mined synoptic.invariants are
        // unsatisfied in the result

        // export the resulting graph
        if (options.outputPathPrefix != null) {
            logger.info("Exporting final graph [" + pGraph.getNodes().size()
                    + " nodes]..");
            startTime = System.currentTimeMillis();

            exportNonInitialGraph(options.outputPathPrefix, pGraph);

            logger.info("Exporting took "
                    + (System.currentTimeMillis() - startTime) + "ms");
        } else {
            logger.warning("Cannot output final graph. Specify output path prefix using:\n\t"
                    + SynopticOptions.getOptDesc("outputPathPrefix"));
        }
    }
}
