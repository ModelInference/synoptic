package synoptic.main;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import synoptic.algorithms.Bisimulation;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.concurrency.NeverConcurrentInvariant;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ConstrainedInvMiner;
import synoptic.invariants.miners.DAGWalkingPOInvMiner;
import synoptic.invariants.miners.IPOInvariantMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.options.SynopticOptions;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.PartitionGraph;
import synoptic.model.Trace;
import synoptic.model.Transition;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.export.DotExportFormatter;
import synoptic.model.export.GmlExportFormatter;
import synoptic.model.export.GraphExportFormatter;
import synoptic.model.export.GraphExporter;
import synoptic.model.export.JsonExporter;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.IRelationPath;
import synoptic.model.testgeneration.AbstractTestCase;
import synoptic.tests.SynopticLibTest;
import synoptic.util.BriefLogFormatter;
import synoptic.util.InternalSynopticException;
import synoptic.util.SynopticJar;
import synoptic.util.time.ITime;

/**
 * Contains entry points for command line version of Synoptic, as well as for
 * libraries that want to use Synoptic from a jar. The instance of Main is a
 * singleton that maintains Synoptic options, and other state for a single run
 * of Synoptic.
 */
public class SynopticMain {
    public static Logger logger = null;

    /**
     * Singleton instance of this class.
     */
    public static SynopticMain instance = null;

    /**
     * The current Synoptic version.
     */
    public static final String versionString = "0.1";

    /**
     * Global source of pseudo-random numbers.
     */
    public Random random;

    /**
     * Formatter to use for exporting graphs (DOT/GML formatter).
     */
    public GraphExportFormatter graphExportFormatter = null;

    /**
     * Synoptic options parsed from the command line or set in some other way.
     */
    public SynopticOptions options = null;

    public static SynopticMain getInstanceWithExistenceCheck() {
        assert (instance != null);
        return instance;
    }

    public static SynopticMain getInstance() {
        return instance;
    }

    /**
     * The synoptic.main method to perform the inference algorithm. See user
     * documentation for an explanation of the options.
     * 
     * @param args
     *            Command-line options
     */
    public static void main(String[] args) throws Exception {
        SynopticMain mainInstance = processArgs(args);
        if (mainInstance == null) {
            return;
        }

        try {
            Locale.setDefault(Locale.US);

            PartitionGraph pGraph = mainInstance.createInitialPartitionGraph();
            if (pGraph != null) {
                mainInstance.runSynoptic(pGraph);
            }
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw InternalSynopticException.wrap(e);
        }
    }

    /**
     * Parses the set of arguments to the program, to set up static state in
     * Main. This state includes everything necessary to run Synoptic -- input
     * log files, regular expressions, etc. Returns null if there is a problem
     * with the parsed options.
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
    public static SynopticMain processArgs(String[] args) throws IOException,
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
    public static SynopticMain processArgs(SynopticOptions opts)
            throws IOException, URISyntaxException, IllegalArgumentException,
            IllegalAccessException {

        setUpLogging(opts);

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
            System.out
                    .println("Synoptic version " + SynopticMain.versionString);
            String changesetID = SynopticJar.getHgChangesetID();
            if (changesetID != null) {
                System.out.println("Synoptic changeset " + changesetID);
            }
            return null;
        }

        // Setup the appropriate graph export formatter object.
        GraphExportFormatter graphExportFormatter;
        if (opts.exportAsGML) {
            graphExportFormatter = new GmlExportFormatter();
        } else {
            graphExportFormatter = new DotExportFormatter();
        }

        if (opts.runAllTests) {
            List<String> testClasses = SynopticJar
                    .getTestsInPackage("synoptic.tests.units.");
            testClasses.addAll(SynopticJar
                    .getTestsInPackage("synoptic.tests.integration."));
            SynopticLibTest.runTests(testClasses);
        } else if (opts.runTests) {
            List<String> testClassesUnits = SynopticJar
                    .getTestsInPackage("synoptic.tests.units.");
            SynopticLibTest.runTests(testClassesUnits);
        }

        if (opts.logFilenames.size() == 0) {
            logger.severe("No log filenames specified, exiting. Specify log files at the end of the command line with no options.");
            return null;
        }

        if (opts.dumpIntermediateStages && opts.outputPathPrefix == null) {
            logger.severe("Cannot dump intermediate stages without an output path prefix. Set this prefix with:\n\t"
                    + opts.getOptDesc("outputPathPrefix"));
            return null;
        }

        if (opts.logLvlVerbose || opts.logLvlExtraVerbose) {
            opts.printOptionValues();
        }

        SynopticMain mainInstance = new SynopticMain(opts, graphExportFormatter);
        return mainInstance;
    }

    static private long loggerInfoStart(String msg) {
        logger.info(msg);
        return System.currentTimeMillis();
    }

    static private void loggerInfoEnd(String msg, long startTime) {
        logger.info(msg + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * Sets up and configures the Main.logger object based on command line
     * arguments.
     * 
     * <pre>
     * Assumes that Main.options is initialized.
     * </pre>
     */
    public static void setUpLogging(SynopticOptions opts) {
        if (logger != null) {
            return;
        }
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
        if (opts.logLvlQuiet) {
            logger.setLevel(Level.WARNING);
        } else if (opts.logLvlVerbose) {
            logger.setLevel(Level.FINE);
        } else if (opts.logLvlExtraVerbose) {
            logger.setLevel(Level.FINEST);
        } else {
            logger.setLevel(Level.INFO);
        }

        consoleHandler.setFormatter(new BriefLogFormatter());
        return;
    }

    /**
     * Given a potentially wild-carded file path, finds all those files that
     * match the expression. TODO: make sure that the same file doesn't appear
     * twice in the returned list
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

    /******************************************************************************/
    /**
     * Main instance methods below.
     */
    public SynopticMain(SynopticOptions opts,
            GraphExportFormatter graphExportFormatter) {
        setUpLogging(opts);

        if (SynopticMain.instance != null) {
            throw new RuntimeException(
                    "Cannot create multiple instance of singleton synoptic.main.Main");
        }
        this.options = opts;
        this.graphExportFormatter = graphExportFormatter;
        this.random = new Random(opts.randomSeed);
        logger.info("Using random seed: " + opts.randomSeed);
        SynopticMain.instance = this;
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
    public String getIntermediateDumpFilename(String stageName, int roundNum) {
        return options.outputPathPrefix + ".stage-" + stageName + ".round-"
                + roundNum;
    }

    /**
     * Serializes g using a dot/gml format and optionally outputs a png file
     * corresponding to the serialized format (dot format export only).
     * 
     * @throws IOException
     */
    private <T extends INode<T>> void exportGraph(String baseFilename,
            IGraph<T> g, boolean outputEdgeLabelsCondition,
            boolean imageGenCondition) {

        if (options.outputPathPrefix == null) {
            logger.warning("Cannot output initial graph. Specify output path prefix using:\n\t"
                    + options.getOptDesc("outputPathPrefix"));
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
     * Export the trace graph g.
     */
    public <T extends INode<T>> void exportTraceGraph(String baseFilename,
            IGraph<T> g) {
        // false below : never include edge labels on exported initial graphs

        // Main.dumpInitialGraphPngFile && !exportAsGML below : whether to
        // convert exported graph to a png file -- the user must have explicitly
        // requested this and the export must be in non-GML format (i.e., dot
        // format).
        exportGraph(baseFilename, g, false, options.dumpTraceGraphPngFile
                && !options.exportAsGML);
    }

    /**
     * Export g as a non-initial graph.
     */
    public <T extends INode<T>> void exportNonInitialGraph(String baseFilename,
            IGraph<T> g) {
        // Main.outputEdgeLabels below : the condition for including edge labels
        // on exported graphs.

        // !exportAsGML below : the condition for exporting an image to png file
        // is that it is not in GML format (i.e., it is in dot format so we can
        // use the 'dot' command).
        exportGraph(baseFilename, g, options.outputEdgeLabels,
                !options.exportAsGML);
    }

    private void processPOLog(TraceParser parser, List<EventNode> parsedEvents)
            throws ParseException, FileNotFoundException {
        // //////////////////
        DAGsTraceGraph traceGraph = genDAGsTraceGraph(parser, parsedEvents);
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

        // //////////////////
        TemporalInvariantSet minedInvs = minePOInvariants(
                options.useTransitiveClosureMining, traceGraph);
        // //////////////////

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
            logger.info("Mined invariants:\n" + minedInvs.toPrettyString());
        }

        if (options.outputInvariantsToFile) {
            String invariantsFilename = options.outputPathPrefix
                    + ".invariants.txt";
            logger.info("Outputting invarians to file: " + invariantsFilename);
            minedInvs.outputToFile(invariantsFilename);
        }
    }

    /**
     * Parses all the log filenames, constructing and returning a list of parsed
     * events.
     * 
     * @param parser
     * @param logFilenames
     * @return
     * @throws Exception
     */
    static public List<EventNode> parseEvents(TraceParser parser,
            List<String> logFilenames) throws Exception {
        long startTime = loggerInfoStart("Parsing input files..");

        List<EventNode> parsedEvents = new ArrayList<EventNode>();
        for (String fileArg : logFilenames) {
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
        loggerInfoEnd("Parsing took ", startTime);

        return parsedEvents;
    }

    static public ChainsTraceGraph genChainsTraceGraph(TraceParser parser,
            List<EventNode> parsedEvents) throws ParseException {
        long startTime = loggerInfoStart("Generating inter-event temporal relation...");
        ChainsTraceGraph inputGraph = parser
                .generateDirectTORelation(parsedEvents);
        loggerInfoEnd("Generating temporal relation took ", startTime);
        return inputGraph;
    }

    static public DAGsTraceGraph genDAGsTraceGraph(TraceParser parser,
            List<EventNode> parsedEvents) throws ParseException {
        long startTime = loggerInfoStart("Generating inter-event temporal relation...");
        DAGsTraceGraph traceGraph = parser
                .generateDirectPORelation(parsedEvents);
        loggerInfoEnd("Generating temporal relation took ", startTime);
        return traceGraph;
    }

    /**
     * Mines and returns the totally ordered invariants from the trace graph of
     * the input log.
     * 
     * @param useTransitiveClosureMining
     * @param traceGraph
     * @return
     */
    public TemporalInvariantSet mineTOInvariants(
            boolean useTransitiveClosureMining, ChainsTraceGraph traceGraph) {
        ITOInvariantMiner miner;

        if (useTransitiveClosureMining) {
            miner = new TransitiveClosureInvMiner();
        } else {
            miner = new ChainWalkingTOInvMiner();
        }

        long startTime = loggerInfoStart("Mining invariants ["
                + miner.getClass().getName() + "]..");
        TemporalInvariantSet minedInvs = miner.computeInvariants(traceGraph,
                options.multipleRelations);

        // Mine time-constrained invariants if requested
        if (options.enablePerfDebugging) {
            logger.info("Mining time-constrained invariants.");
            ConstrainedInvMiner constMiner = new ConstrainedInvMiner();
            minedInvs = constMiner.computeInvariants(traceGraph,
                    options.multipleRelations, minedInvs);
        }

        loggerInfoEnd("Mining took ", startTime);

        // Miner can be garbage-collected.
        miner = null;
        return minedInvs;
    }

    /**
     * Mines and returns a set of partially ordered invariants from the DAG
     * trace graph of an input log.
     * 
     * @param useTransitiveClosureMining
     * @param traceGraph
     * @return
     */
    public TemporalInvariantSet minePOInvariants(
            boolean useTransitiveClosureMining, DAGsTraceGraph traceGraph) {

        IPOInvariantMiner miner;
        if (useTransitiveClosureMining) {
            miner = new TransitiveClosureInvMiner();
        } else {
            miner = new DAGWalkingPOInvMiner(options.mineNeverConcurrentWithInv);
        }

        long startTime = loggerInfoStart("Mining invariants ["
                + miner.getClass().getName() + "]..");
        TemporalInvariantSet minedInvs = miner.computeInvariants(traceGraph);
        loggerInfoEnd("Mining took ", startTime);
        // Miner can be garbage-collected.
        miner = null;
        return minedInvs;
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
        TraceParser parser = new TraceParser(options.regExps,
                options.partitionRegExp, options.separatorRegExp);
        List<EventNode> parsedEvents;
        try {
            parsedEvents = parseEvents(parser, options.logFilenames);
        } catch (ParseException e) {
            logger.severe("Caught ParseException -- unable to continue, exiting. Try cmd line option:\n\t"
                    + options.getOptDesc("help"));
            logger.severe(e.toString());
            return null;
        }

        if (options.debugParse) {
            // Terminate since the user is interested in debugging the parser.
            logger.info("Terminating. To continue further, re-run without the debugParse option.");
            return null;
        }

        // PO Logs are processed differently.
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
        ChainsTraceGraph traceGraph = genChainsTraceGraph(parser, parsedEvents);
        // //////////////////

        // Parsing information can be garbage-collected.
        parser = null;
        parsedEvents = null;

        // Perform trace-wise normalization if requested
        if (options.traceNormalization) {
            SynopticMain.normalizeTraceGraph(traceGraph);
        }

        if (options.dumpTraceGraphDotFile) {
            logger.info("Exporting trace graph ["
                    + traceGraph.getNodes().size() + " nodes]..");
            exportTraceGraph(options.outputPathPrefix + ".tracegraph",
                    traceGraph);
        }

        // //////////////////
        TemporalInvariantSet minedInvs = mineTOInvariants(
                options.useTransitiveClosureMining, traceGraph);
        // //////////////////

        logger.info("Mined " + minedInvs.numInvariants() + " invariants");

        if (options.ignoreInvsOverETypeSet != null) {

            // Split string options.ignoreInvsOverETypeSet by the ";" delimiter:
            List<String> stringEtypesToIgnore = Arrays
                    .asList(options.ignoreInvsOverETypeSet.split(";"));

            logger.info("Ignoring invariants over event-types set: "
                    + stringEtypesToIgnore.toString());

            // Find invariants matching the filtering constraint.
            Set<ITemporalInvariant> invsToRemove = new LinkedHashSet<ITemporalInvariant>();

            boolean removeInv;
            for (ITemporalInvariant inv : minedInvs.getSet()) {
                // To remove an invariant inv, the event types associated with
                // inv must all come from the list stringEtypesToIgnore, we
                // check this here:
                removeInv = true;
                for (EventType eType : inv.getPredicates()) {
                    if (!stringEtypesToIgnore.contains(eType.getETypeLabel())) {
                        removeInv = false;
                        break;
                    }
                }
                if (removeInv) {
                    invsToRemove.add(inv);
                }
            }

            // Remove the invariants that matched the constraint:
            minedInvs.removeAll(invsToRemove);
        }

        if (options.dumpInvariants) {
            logger.info("Mined invariants:\n" + minedInvs.toPrettyString());
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
        long startTime = loggerInfoStart("Creating initial partition graph.");
        PartitionGraph pGraph = new PartitionGraph(traceGraph, true, minedInvs);
        loggerInfoEnd("Creating partition graph took ", startTime);
        // //////////////////

        if (options.dumpInitialPartitionGraph) {
            exportGraph(options.outputPathPrefix + ".condensed", pGraph, true,
                    true);
        }

        return pGraph;
    }

    /**
     * Perform trace-wise normalization on the trace graph. In other words,
     * scale each trace to the range [0,1] based on the min and max absolute
     * times of any event within that trace.
     * 
     * @param traceGraph
     *            An trace graph where events and transitions contain time
     *            information
     */
    public static void normalizeTraceGraph(ChainsTraceGraph traceGraph) {
        logger.info("Normalizing each trace to the range [0,1] ...");

        Set<IRelationPath> relationPaths = new HashSet<IRelationPath>();

        // Get all traces w.r.t. only the time relation
        for (Trace trace : traceGraph.getTraces()) {
            Set<IRelationPath> subgraphs = trace
                    .getSingleRelationPaths(Event.defTimeRelationStr);
            relationPaths.addAll(subgraphs);
        }

        // Traverse each trace to normalize the absolute times of its events
        for (IRelationPath relationPath : relationPaths) {
            ITime minTime = null;
            ITime maxTime = null;

            // Find the min and max absolute time of any event in this trace
            EventNode cur = relationPath.getFirstNode();
            while (!cur.getAllTransitions().isEmpty()) {
                if (maxTime == null || maxTime.lessThan(cur.getTime())) {
                    maxTime = cur.getTime();
                }
                if (minTime == null || cur.getTime().lessThan(minTime)) {
                    minTime = cur.getTime();
                }

                // Get the next event in this trace
                cur = cur
                        .getTransitionsWithIntersectingRelations(
                                traceGraph.getRelations()).get(0).getTarget();
            }

            ITime rangeTime = null;

            // Compute the range of this trace's times
            if (maxTime != null) {
                rangeTime = maxTime.computeDelta(minTime);
            } else {
                logger.fine("Warning: Trace beginning with "
                        + relationPath.getFirstNode()
                        + " cannot be normalized because it seems to contain no times");
                continue;
            }

            // Normalize absolute time of each of this trace's events by
            // subtracting the min and dividing by the range
            cur = relationPath.getFirstNode();
            while (!cur.getAllTransitions().isEmpty()) {
                cur.getEvent().setTime(
                        cur.getTime().computeDelta(minTime)
                                .normalize(rangeTime));

                // Get the next event in this trace
                cur = cur
                        .getTransitionsWithIntersectingRelations(
                                traceGraph.getRelations()).get(0).getTarget();
            }
        }

        // Update transition time deltas to match new normalized event times
        for (EventNode event : traceGraph.getNodes()) {
            for (Transition<EventNode> trans : event.getAllTransitions()) {

                // Get normalized times of the transition's source and target
                // events
                ITime srcTime = trans.getSource().getTime();
                ITime targetTime = trans.getTarget().getTime();

                // Compute and store normalized transition time delta
                if (targetTime != null) {
                    ITime delta = targetTime.computeDelta(srcTime);
                    trans.setTimeDelta(delta);
                }
            }
        }
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
        Bisimulation.splitUntilAllInvsSatisfied(pGraph);
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

            // if test generation is enabled, export all bounded, predicted
            // abstract tests
            if (options.testGeneration) {
                Set<AbstractTestCase> testSuite = SynopticTestGeneration
                        .deriveAbstractTests(pGraph);
                int testID = 0;
                for (AbstractTestCase testCase : testSuite) {
                    String baseFilename = options.outputPathPrefix + "-test"
                            + testID;
                    exportNonInitialGraph(baseFilename, testCase);
                    testID++;
                }
            }
        } else {
            logger.warning("Cannot output final graph. Specify output path prefix using:\n\t"
                    + options.getOptDesc("outputPathPrefix"));
        }

        // Export a JSON object if requested
        if (options.outputJSON) {
            logger.info("Exporting final graph as a JSON object...");
            startTime = System.currentTimeMillis();

            JsonExporter.exportJsonObject(options.outputPathPrefix, pGraph);

            logger.info("Exporting JSON object took "
                    + (System.currentTimeMillis() - startTime) + "ms");
        }
    }
}