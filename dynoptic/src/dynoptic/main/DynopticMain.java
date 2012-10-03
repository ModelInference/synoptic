package dynoptic.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mcscm.McScM;
import mcscm.VerifyResult;
import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.invariants.EventuallyHappens;
import dynoptic.invariants.NeverFollowedBy;
import dynoptic.model.export.GraphExporter;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.gfsm.CompleteGFSMCExample;
import dynoptic.model.fifosys.gfsm.GFSM;
import dynoptic.model.fifosys.gfsm.PartialGFSMCExample;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.dag.ObsDAG;
import dynoptic.model.fifosys.gfsm.observed.dag.ObsDAGNode;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.SynopticMain;
import synoptic.main.options.SynopticOptions;
import synoptic.main.parser.TraceParser;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;
import synoptic.model.event.StringEventType;
import synoptic.model.export.DotExportFormatter;
import synoptic.util.InternalSynopticException;

/**
 * <p>
 * This class wraps everything together to provide a command-line interface, as
 * well as an API, to run Dynoptic programmatically.
 * </p>
 * <p>
 * Unlike the synoptic code-base, DynopticMain is not a singleton and can be
 * instantiated for every new execution of Dynoptic. However, DynopticMain
 * cannot be re-used. That is, a new version _must_ be instantiated for each new
 * execution of the Dynoptic process.
 * </p>
 * <p>
 * For options that Dynoptic recognizes, see DynopticOptions.
 * </p>
 */
public class DynopticMain {
    static public boolean assertsOn = false;
    static {
        // Dynamic check for asserts. Note: without the '== true' a conservative
        // compiler complaints that the assert is not checking a condition.
        assert (assertsOn = true) == true;
    }

    public static Logger logger = null;

    /**
     * The main entrance to the command line version of Dynoptic.
     * 
     * @param args
     *            Command-line options
     */
    public static void main(String[] args) throws Exception {
        DynopticOptions opts = new DynopticOptions(args);
        DynopticMain main = new DynopticMain(opts);
        try {
            main.run();
        } catch (Exception e) {
            if (e.toString() != "") {
                logger.severe(e.toString());
                logger.severe("Unable to continue, exiting. Try cmd line option:\n\t"
                        + opts.getOptDesc("help"));
            }
        }
        return;
    }

    /**
     * Sets up project-global logging based on command line options.
     * 
     * @param opts
     */
    public static void setUpLogging(DynopticOptions opts) {
        if (logger != null) {
            return;
        }

        // Get the top Logger instance
        logger = Logger.getLogger("DynopticMain");

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
        return;
    }

    // //////////////////////////////////////////////////////////////////

    private DynopticOptions opts = null;

    // The Java McScM model checker bridge instance that interfaces with the
    // McScM verify binary.
    private McScM mcscm = null;

    // The channels associated with this Dynoptic execution. These are parsed in
    // checkOptions().
    private List<ChannelId> channelIds = null;

    // Instance of SynopticMain used for parsing the logs and mining invariants.
    private SynopticMain synMain = null;

    // Total number of processes in the log processed by this instance of
    // DynopticMain
    private int numProcesses = -1;

    /** Prepares a new DynopticMain instance based on opts. */
    public DynopticMain(DynopticOptions opts) throws Exception {
        this.opts = opts;
        setUpLogging(opts);
        checkOptions(opts);
        mcscm = new McScM(opts.mcPath);
    }

    /**
     * Checks the input Dynoptic options for consistency and omissions.
     * 
     * @param optns
     * @throws Exception
     */
    public void checkOptions(DynopticOptions optns) throws Exception {
        String err = null;

        // Display help for all option groups, including unpublicized ones
        if (optns.allHelp) {
            optns.printLongHelp();
            err = "";
            throw new OptionException(err);
        }

        // Display help just for the 'publicized' option groups
        if (optns.help) {
            optns.printShortHelp();
            err = "";
            throw new OptionException(err);
        }

        if (optns.channelSpec == null) {
            err = "Cannot parse a communications log without a channel specification:\n\t"
                    + opts.getOptDesc("channelSpec");
            throw new OptionException(err);
        }

        channelIds = ChannelId.parseChannelSpec(opts.channelSpec);
        if (channelIds.size() == 0) {
            err = "Could not parse the channel specification:\n\t"
                    + opts.getOptDesc("channelSpec");
            throw new OptionException(err);
        }

        if (optns.outputPathPrefix == null) {
            err = "Cannot output any generated models. Specify output path prefix using:\n\t"
                    + opts.getOptDesc("outputPathPrefix");
            throw new OptionException(err);
        }

        if (optns.mcPath == null) {
            err = "Specify path of the McScM model checker to use for verification:\n\t"
                    + opts.getOptDesc("mcPath");
            throw new OptionException(err);
        }
    }

    public int getNumProcesses() {
        return numProcesses;
    }

    public List<ChannelId> getChannelIds() {
        return channelIds;
    }

    // //////////////////////////////////////////////////
    // "run" Methods that glue together all the major pieces to implement the
    // complete Dynoptic pipeline:

    /**
     * Runs the Dynoptic process based on the settings in opts. In particular,
     * we expect that the logFilenames are specified in opts.
     * 
     * @throws Exception
     */
    public void run() throws Exception {
        if (this.synMain == null) {
            initializeSynoptic();
        }

        if (opts.logFilenames.size() == 0) {
            String err = "No log filenames specified, exiting. Specify log files at the end of the command line.";
            throw new OptionException(err);
        }

        // //////////////////
        // Parse the input log files into _Synoptic_ structures.
        TraceParser parser = new TraceParser(opts.regExps,
                opts.partitionRegExp, opts.separatorRegExp);

        List<EventNode> parsedEvents = parseEventsFromFiles(parser,
                opts.logFilenames);

        // //////////////////
        // Generate the Synoptic DAG from parsed events
        DAGsTraceGraph traceGraph = SynopticMain.genDAGsTraceGraph(parser,
                parsedEvents);

        // Parser can now be garbage-collected.
        parser = null;

        run(traceGraph);
    }

    /**
     * Runs Dynoptic based on setting in opts, but uses the log from the passed
     * in String, and not from the logFilenames defined in opts.
     * 
     * @param log
     * @throws Exception
     * @throws InterruptedException
     * @throws IOException
     */
    public void run(String log) throws IOException, InterruptedException,
            Exception {
        if (this.synMain == null) {
            initializeSynoptic();
        }

        // //////////////////
        // Parse the input string into _Synoptic_ structures.
        TraceParser parser = new TraceParser(opts.regExps,
                opts.partitionRegExp, opts.separatorRegExp);

        List<EventNode> parsedEvents = parseEventsFromString(parser, log);

        // //////////////////
        // Generate the Synoptic DAG from parsed events
        DAGsTraceGraph traceGraph = SynopticMain.genDAGsTraceGraph(parser,
                parsedEvents);

        // Parser can now be garbage-collected.
        parser = null;

        run(traceGraph);
    }

    /**
     * Runs Dynoptic based on setting sin opts, and uses the Synoptic traceGraph
     * passed as an argument instead of parsing files or a string directly.
     * 
     * @param traceGraph
     * @throws Exception
     * @throws InterruptedException
     * @throws IOException
     */
    public void run(DAGsTraceGraph traceGraph) throws IOException,
            InterruptedException, Exception {
        // //////////////////
        // Mine Synoptic invariants
        TemporalInvariantSet minedInvs = synMain.minePOInvariants(
                opts.useTransitiveClosureMining, traceGraph);

        logger.info("Mined " + minedInvs.numInvariants() + " invariants");

        if (opts.dumpInvariants) {
            logger.info("Mined invariants:\n" + minedInvs.toPrettyString());
        }

        if (minedInvs.numInvariants() == 0) {
            logger.info("Mined 0 Synoptic invariants. Stopping.");
            return;
        }

        // //////////////////
        // Use Synoptic event nodes and ordering constraints
        // between these to generate ObsFSMStates (anonymous states),
        // obsDAGNodes (to contain obsFSMStates and encode dependencies between
        // them), and an ObsDag per execution parsed from the log.
        logger.info("Generating ObsFifoSys from DAGsTraceGraph...");
        List<ObsFifoSys> traces = synTraceGraphToDynObsFifoSys(traceGraph);

        // ///////////////////
        // Express/convert Synoptic invariants as Dynoptic invariants.
        logger.info("Converting Synoptic invariants to Dynoptic invariants...");
        List<BinaryInvariant> dynInvs = synInvsToDynInvs(minedInvs);

        if (dynInvs.size() == 0) {
            logger.info("Mined 0 Dynoptic invariants. Stopping.");
            return;
        }

        // ///////////////////
        // Create a partition graph (GFSM instance) of the ObsFifoSys instances
        // we've created above. Use the default initial partitioning strategy,
        // based on head of all of the queues of each ObsFifoSysState.
        logger.info("Generating the initial partition graph (GFSM)...");
        GFSM pGraph = new GFSM(traces);

        // ///////////////////
        // Model check, refine loop. Check each invariant in the model, and
        // refine the model as needed until all invariants hold.
        checkInvsRefineGFSM(dynInvs, pGraph);

        // ///////////////////
        // Output the final CFSM model (corresponding to pGraph) using GraphViz
        // (dot-format).

        logger.info("Final scm model:");
        logger.info(pGraph.getCFSM().toScmString("final model"));

        CFSM cfsm = pGraph.getCFSM();
        String outputFileName = opts.outputPathPrefix + ".dot";
        GraphExporter.exportCFSM(outputFileName, cfsm);
        GraphExporter.generatePngFileFromDotFile(outputFileName);
    }

    // //////////////////////////////////////////////////
    // Various helper methods that integrate with Synoptic and manipulate model
    // data structures. Ordered roughly in the order of their use in the run
    // methods above.

    /** Initializes a version of SynopticMain based on Dynoptic options. */
    public void initializeSynoptic() {
        assert synMain == null;

        SynopticOptions synOpts = new SynopticOptions();
        synOpts.ignoreNonMatchingLines = opts.ignoreNonMatchingLines;
        synOpts.recoverFromParseErrors = opts.recoverFromParseErrors;
        synOpts.debugParse = opts.debugParse;
        this.synMain = new SynopticMain(synOpts, new DotExportFormatter());
    }

    /**
     * Uses parser to parse a set of log files into a list of event nodes. These
     * event nodes are post-processed and ready to be further used to build
     * Synoptic/Dynoptic DAG structures. <br/>
     * <br/>
     * This function also sets the numProcesses field to the number of processes
     * that have been observed in the log.
     * 
     * @param parser
     *            parser to use for parsing the log string
     * @param logFilenames
     *            log filenames to parse using the parser
     * @return
     * @throws Exception
     */
    public List<EventNode> parseEventsFromFiles(TraceParser parser,
            List<String> logFilenames) throws Exception {
        assert parser != null;
        assert synMain != null;
        assert logFilenames != null;
        assert logFilenames.size() != 0;

        List<EventNode> parsedEvents;

        parsedEvents = SynopticMain.parseEvents(parser, logFilenames);

        if (parser.logTimeTypeIsTotallyOrdered()) {
            throw new OptionException(
                    "Dynoptic expects a log that is partially ordered.");
        }

        postParseEvents(parsedEvents);
        return parsedEvents;
    }

    /**
     * Like the method above, uses parser to parse a log string into a list of
     * event nodes. These event nodes are post-processed and ready to be further
     * used to build Synoptic/Dynoptic DAG structures. <br/>
     * <br/>
     * This function also sets the numProcesses field to the number of processes
     * that have been observed in the log.
     * 
     * @param parser
     *            parser to use for parsing the log string
     * @param log
     *            log string to parse
     * @return
     * @throws Exception
     */
    public List<EventNode> parseEventsFromString(TraceParser parser, String log)
            throws Exception {
        assert parser != null;

        List<EventNode> parsedEvents = parser
                .parseTraceString(log, "trace", -1);

        if (parser.logTimeTypeIsTotallyOrdered()) {
            throw new OptionException(
                    "Dynoptic expects a log that is partially ordered.");
        }

        postParseEvents(parsedEvents);
        return parsedEvents;
    }

    /**
     * Further parsers the EventNodes -- setting up data structures internal to
     * the DistEventType, that is the EventNode.event.etype instance. This
     * function also determines and records the number of processes in the
     * system.
     */
    private void postParseEvents(List<EventNode> parsedEvents) throws Exception {

        if (parsedEvents.size() == 0) {
            throw new OptionException(
                    "Did not parse any events from the input log files. Stopping.");
        }

        // //////////////////
        // Parse the parsed events further (as distributed
        // events that capture message send/receives). And determine the number
        // of processes in the system.

        Set<ChannelId> usedChannelIds = new LinkedHashSet<ChannelId>();
        Set<Integer> usedPids = new LinkedHashSet<Integer>();

        for (EventNode eNode : parsedEvents) {
            synoptic.model.event.EventType synEType = eNode.getEType();
            if (!(synEType instanceof DistEventType)) {
                throw new InternalSynopticException(
                        "Expected a DistEvenType, instead got "
                                + synEType.getClass());
            }
            DistEventType distEType = ((DistEventType) synEType);

            String err = distEType.interpretEType(channelIds);
            if (err != null) {
                throw new OptionException(err);
            }

            // Record the pid and channelId corresponding to this eType.
            usedPids.add(distEType.getPid());
            if (distEType.isCommEvent()) {
                usedChannelIds.add(distEType.getChannelId());
            }
        }

        if (usedChannelIds.size() != channelIds.size()) {
            throw new OptionException(
                    "Some specified channelIds are not referenced in the log.");
        }

        // Find the max pid referenced in the log. This will determine the
        // number of processes in the system.
        int maxPid = 0;
        // Use sum of pids to check that all PIDs are referenced.
        int pidSum = 0;
        for (Integer pid : usedPids) {
            pidSum += pid;
            if (maxPid < pid) {
                maxPid = pid;
            }
        }
        numProcesses = maxPid + 1;
        if (pidSum != ((maxPid * (maxPid + 1)) / 2) || !usedPids.contains(0)) {
            throw new OptionException("Process ID range for the log has gaps: "
                    + usedPids.toString());
        }
    }

    /**
     * Uses Synoptic event nodes and ordering constraints between these to
     * generate ObsFSMStates (anonymous states), obsDAGNodes (to contain
     * obsFSMStates and encode dependencies between them), and an ObsDag per
     * execution parsed from the log. Then, this function converts each ObsDag
     * into an observed FifoSys. The list of these observed FifoSys instances is
     * then returned.
     * 
     * @param traceGraph
     * @param numProcesses
     * @param parsedEvents
     * @return
     */
    public List<ObsFifoSys> synTraceGraphToDynObsFifoSys(
            DAGsTraceGraph traceGraph) {
        assert numProcesses != -1;

        List<ObsFifoSys> traces = new ArrayList<ObsFifoSys>();

        // Maps an observed event to the generated ObsDAGNode that emits the
        // event in the Dynoptic DAG.
        Map<Event, ObsDAGNode> preEventNodesMap = new LinkedHashMap<Event, ObsDAGNode>();

        // Build a Dynoptic ObsDAG for each Synoptic trace DAG.
        for (int traceId = 0; traceId < traceGraph.getNumTraces(); traceId++) {
            logger.info("Processing trace " + traceId);
            preEventNodesMap.clear();

            // These contain the initial and terminal configurations in terms of
            // process states. These are used to construct the ObsDAG.
            List<ObsDAGNode> initDagCfg = Arrays
                    .asList(new ObsDAGNode[numProcesses]);

            List<ObsDAGNode> termDagCfg = Arrays
                    .asList(new ObsDAGNode[numProcesses]);

            // Maps a pid to the first event node for that pid.
            List<EventNode> pidInitialNodes = Arrays
                    .asList(new EventNode[numProcesses]);

            // Populate the pidInitialNodes list.
            logger.info("Populating initial nodes list");
            for (EventNode eNode : traceGraph.getNodes()) {
                // Skip nodes from other traces.
                if (eNode.getTraceID() != traceId) {
                    continue;
                }

                // Skip special nodes.
                if (eNode.isInitial() || eNode.isTerminal()) {
                    continue;
                }

                Event e = eNode.getEvent();
                int ePid = ((DistEventType) e.getEType()).getPid();

                if (pidInitialNodes.get(ePid) == null
                        || eNode.getTime().lessThan(
                                pidInitialNodes.get(ePid).getTime())) {
                    pidInitialNodes.set(ePid, eNode);
                }
            }

            // Walk the per-process chain starting at the initial node, and
            // create the corresponding Dynoptic states (without remote
            // dependencies).
            for (int pid = 0; pid < numProcesses; pid++) {
                logger.info("Walking process[" + pid
                        + "] chain to create ObsFSMState instances.");

                EventNode eNode = pidInitialNodes.get(pid);
                assert eNode != null;

                ObsFSMState obsState = ObsFSMState.ObservedInitialFSMState(pid);
                ObsDAGNode prevNode = new ObsDAGNode(obsState);

                initDagCfg.set(pid, prevNode);

                while (eNode != null) {
                    Event e = eNode.getEvent();

                    obsState = ObsFSMState.ObservedIntermediateFSMState(pid);
                    ObsDAGNode nextNode = new ObsDAGNode(obsState);

                    prevNode.addTransition(e, nextNode);
                    preEventNodesMap.put(e, prevNode);

                    prevNode = nextNode;
                    eNode = eNode.getProcessLocalSuccessor();
                }
                termDagCfg.set(pid, prevNode);
                obsState.markTerm();
            }

            // Walk the same chains as above, but now record the remote
            // dependencies between events as dependencies between states.
            for (int pid = 0; pid < numProcesses; pid++) {
                logger.info("Walking process[" + pid
                        + "] chain to record remote dependencies");

                EventNode eNode = pidInitialNodes.get(pid);

                while (eNode != null) {
                    Event e = eNode.getEvent();

                    // Record remote dependencies.
                    for (EventNode eNodeSucc : eNode.getAllSuccessors()) {
                        if (eNodeSucc.isTerminal()) {
                            continue;
                        }
                        Event eSucc = eNodeSucc.getEvent();
                        int eSuccPid = ((DistEventType) eSucc.getEType())
                                .getPid();

                        if (eSuccPid != pid) {
                            assert preEventNodesMap.containsKey(e);
                            if (!preEventNodesMap.containsKey(eSucc)) {

                                assert preEventNodesMap.containsKey(eSucc);

                            }
                            // post-state of eSucc depends on the post-state of
                            // e having occurred.
                            ObsDAGNode eSuccPost = preEventNodesMap.get(eSucc)
                                    .getNextState();
                            ObsDAGNode ePost = preEventNodesMap.get(e)
                                    .getNextState();
                            eSuccPost.addRemoteDependency(ePost);
                        }
                    }

                    eNode = eNode.getProcessLocalSuccessor();
                }
            }

            logger.info("Generating ObsDAG.");
            ObsDAG dag = new ObsDAG(initDagCfg, termDagCfg, channelIds);
            logger.info("Generating ObsFifoSys.");
            ObsFifoSys fifoSys = dag.getObsFifoSys();
            traces.add(fifoSys);
        }
        return traces;
    }

    /** Converts a set of Synoptic invariants into a set of Dynoptic invariants. */
    public static List<BinaryInvariant> synInvsToDynInvs(
            TemporalInvariantSet minedInvs) {
        List<BinaryInvariant> dynInvs = new ArrayList<BinaryInvariant>();

        BinaryInvariant dynInv = null;

        DistEventType dynETypeFirst, dynETypeSecond;
        for (ITemporalInvariant inv : minedInvs) {
            assert (inv instanceof synoptic.invariants.BinaryInvariant);

            synoptic.invariants.BinaryInvariant binv = (synoptic.invariants.BinaryInvariant) inv;

            if (!(binv.getFirst() instanceof DistEventType)) {
                assert (binv.getFirst() instanceof StringEventType);
                assert (inv instanceof AlwaysFollowedInvariant);
                assert binv.getFirst().isInitialEventType();
            }

            assert (binv.getSecond() instanceof DistEventType);

            if (binv.getFirst().isInitialEventType()) {
                // Special case for INITIAL event type since it does not appear
                // in the traces and is therefore not recorded in the eTypesMap.
                dynETypeFirst = DistEventType.INITIALEventType;
            } else {
                dynETypeFirst = ((DistEventType) binv.getFirst());
            }
            dynETypeSecond = ((DistEventType) binv.getSecond());

            if (inv instanceof AlwaysFollowedInvariant) {
                if (dynETypeFirst == DistEventType.INITIALEventType) {
                    dynInv = new EventuallyHappens(dynETypeSecond);
                } else {
                    dynInv = new AlwaysFollowedBy(dynETypeFirst, dynETypeSecond);
                }
            } else if (inv instanceof NeverFollowedInvariant) {
                assert dynETypeFirst != DistEventType.INITIALEventType;
                dynInv = new NeverFollowedBy(dynETypeFirst, dynETypeSecond);
            } else if (inv instanceof AlwaysPrecedesInvariant) {
                assert dynETypeFirst != DistEventType.INITIALEventType;
                dynInv = new AlwaysPrecedes(dynETypeFirst, dynETypeSecond);
            }
            if (dynInv != null) {
                dynInvs.add(dynInv);
                dynInv = null;
            }
        }

        return dynInvs;
    }

    /**
     * Implements the (model check - refine loop). Check each invariant in
     * dynInvs in the pGraph model, and refine pGraph as needed until all
     * invariants are satisfied.
     * 
     * @param invsToSatisfy
     *            Dynoptic invariants to check and satisfy in pGraph
     * @param pGraph
     *            The GFSM model that will be checked and refined to satisfy all
     *            of the invariants in invsTocheck
     * @throws Exception
     * @throws IOException
     * @throws InterruptedException
     */
    public void checkInvsRefineGFSM(List<BinaryInvariant> invsToSatisfy,
            GFSM pGraph) throws Exception, IOException, InterruptedException {
        assert pGraph != null;
        assert invsToSatisfy != null;
        assert invsToSatisfy.size() > 0;

        Set<BinaryInvariant> timedOutInvs = new LinkedHashSet<BinaryInvariant>();
        Set<BinaryInvariant> satisfiedInvs = new LinkedHashSet<BinaryInvariant>();
        // curInv will _always_ refer to the 0th element of invsToSatisfy.
        BinaryInvariant curInv = invsToSatisfy.get(0);

        int totalInvs = invsToSatisfy.size();
        int invsCounter = 1;

        // ////// Additive and memory-less timeout value adaptation.
        // Initial McScM invocation timeout in seconds.
        int baseTimeout = 30;
        // Current timeout value to use.
        int curTimeout = baseTimeout;
        // How much we increment curTimeout by, when we timeout on checking all
        // invariants.
        int timeoutDelta = 30;
        // Max curTimeout value (300s = 5min).
        int maxTimeout = 300;

        logger.info("Model checking " + curInv.toString() + " : " + invsCounter
                + " / " + totalInvs);

        // This counts the number of times we've refined the gfsm.
        int gfsmCounter = 0;
        String gfsmPrefixFilename = opts.outputPathPrefix;
        String dotFilename = gfsmPrefixFilename + ".gfsm.0.dot";
        GraphExporter.exportGFSM(dotFilename, pGraph);
        GraphExporter.generatePngFileFromDotFile(dotFilename);

        // Export intermediate CFSM:
        CFSM cfsm = pGraph.getCFSM();

        dotFilename = gfsmPrefixFilename + ".cfsm-no-inv." + gfsmCounter
                + ".dot";
        GraphExporter.exportCFSM(dotFilename, cfsm);
        GraphExporter.generatePngFileFromDotFile(dotFilename);

        cfsm.augmentWithInvTracing(curInv);
        dotFilename = gfsmPrefixFilename + ".cfsm." + gfsmCounter + ".dot";
        GraphExporter.exportCFSM(dotFilename, cfsm);
        GraphExporter.generatePngFileFromDotFile(dotFilename);

        while (true) {
            assert invsCounter <= totalInvs;
            assert curInv == invsToSatisfy.get(0);
            assert timedOutInvs.size() + satisfiedInvs.size()
                    + invsToSatisfy.size() == totalInvs;

            if (gfsmCounter == 2) {
                assert true;
            }

            // Get the CFSM corresponding to the partition graph.
            cfsm = pGraph.getCFSM();
            // Augment the CFSM with synthetic states/events to check curInv.
            cfsm.augmentWithInvTracing(curInv);

            // Model check the CFSM using the McScM model checker.
            String cStr = cfsm.toScmString("checking_"
                    + curInv.getConnectorString());

            // logger.info(cStr);

            // TODO: if we are re-checking an invariant, indicate how many times
            // we have rechecked it (since the last time it timed-out, if that
            // ever happened).

            logger.info("*******************************************************");

            logger.info("Checking ... " + curInv.toString() + ". Inv "
                    + invsCounter + " / " + totalInvs
                    + ", refinements so far: " + gfsmCounter);
            logger.info("*******************************************************");

            try {
                mcscm.verify(cStr, curTimeout);
            } catch (InterruptedException e) {
                // The model checker timed out. First, record the timed-out
                // invariant so that we are not stuck re-checking it.
                invsToSatisfy.remove(0);
                timedOutInvs.add(curInv);

                logger.info("Timed out in checking invariant: "
                        + curInv.toString());

                // No invariants are left to try -- increase the timeout value,
                // unless we are the timeout limit, in which case we throw an
                // exception.
                if (invsToSatisfy.size() == 0) {
                    logger.info("Timed out in checking these invariants with timeout value "
                            + curTimeout + " :" + timedOutInvs.toString());

                    curTimeout += timeoutDelta;

                    if (curTimeout > maxTimeout) {
                        throw new Exception(
                                "McScM timed-out on all invariants. Cannot continue.");
                    }

                    // Append all of the previously timed out invariants back to
                    // invsToSatisfy.
                    invsToSatisfy.addAll(timedOutInvs);
                    timedOutInvs.clear();
                }

                // Try the first invariant (perhaps again, but with a higher
                // timeout value).
                curInv = invsToSatisfy.get(0);
                continue;
            }

            // We did not time-out on checking curInv. Therefore, reset
            // curTimeout to base value.
            curTimeout = baseTimeout;

            VerifyResult result = mcscm.getVerifyResult(cfsm.getChannelIds());
            logger.info(result.toRawString());
            logger.info(result.toString());

            // If there is no counter-example, then the invariant holds true.
            if (result.getCExample() == null) {
                // Remove the current invariant from the invsToSatisfy list.
                invsToSatisfy.remove(0);
                satisfiedInvs.add(curInv);

                if (invsToSatisfy.size() == 0) {
                    // No more invariants to check. We are done.
                    logger.info("Finished checking " + invsCounter + " / "
                            + totalInvs + " invariants.");
                    break;
                }

                // Grab and start checking the next invariant.
                curInv = invsToSatisfy.get(0);
                invsCounter += 1;
                logger.info("Model checking " + curInv.toString() + " : "
                        + invsCounter + " / " + totalInvs);
            } else {
                // Refine the pGraph to eliminate cExample.

                // Match the sequence of events in the counter-example to
                // paths of corresponding GFSM states.
                List<CompleteGFSMCExample> paths = pGraph
                        .getCExamplePaths(result.getCExample());

                if (paths == null || paths.isEmpty()) {
                    cfsm = pGraph.getCFSM();

                    // A complete counter-example path does not exist. This may
                    // occur when the GFSM->CFSM conversion does not produce
                    // identical models (the CFSM model can be more general,
                    // i.e., accept more behavior). In this case, we refine a
                    // partial counter-example path with is the longest
                    // partitions path that matches the McScM counter-example.
                    PartialGFSMCExample partialPath = pGraph
                            .getLongestPartialCExamplePath(result.getCExample());

                    logger.info("Found partial path: " + partialPath.toString());

                    CompleteGFSMCExample completePath = partialPath
                            .extendToCompletePath(curInv);

                    assert completePath != null;

                    logger.info("Extended partial path to complete path: "
                            + completePath.toString());

                    logger.info("Resolving " + completePath.toString());
                    completePath.resolve(pGraph);

                    // partialPath.resolve(pGraph);

                } else {
                    // Resolve all of the complete counter-example paths.
                    for (CompleteGFSMCExample path : paths) {
                        logger.info("Resolving " + path.toString());

                        // TODO 2: if the paths overlap then it might be
                        // possible to resolve multiple paths with a single
                        // refinement. Implement this optimization.

                        // Check if path is still feasible before attempting to
                        // resolve it (resolving prior paths might have made it
                        // infeasible)
                        if (!pGraph.feasible(path)) {
                            logger.info("Path no longer feasible");
                            continue;
                        }

                        path.resolve(curInv, pGraph);

                        if (!path.isResolved()) {
                            throw new Exception("Cannot resolve "
                                    + path.toString() + " for "
                                    + curInv.toString());
                        }
                    }
                }

                // Increment the number of refinements:
                gfsmCounter += 1;

                // Export intermediate GFSM:
                dotFilename = gfsmPrefixFilename + ".gfsm." + gfsmCounter
                        + ".dot";
                GraphExporter.exportGFSM(dotFilename, pGraph);
                GraphExporter.generatePngFileFromDotFile(dotFilename);

                if (gfsmCounter == 1) {
                    logger.info("");
                    assert true;
                }

                // Export intermediate CFSM:
                cfsm = pGraph.getCFSM();

                dotFilename = gfsmPrefixFilename + ".cfsm-no-inv."
                        + gfsmCounter + ".dot";
                GraphExporter.exportCFSM(dotFilename, cfsm);
                GraphExporter.generatePngFileFromDotFile(dotFilename);

                cfsm.augmentWithInvTracing(curInv);
                dotFilename = gfsmPrefixFilename + ".cfsm." + gfsmCounter
                        + ".dot";
                GraphExporter.exportCFSM(dotFilename, cfsm);
                GraphExporter.generatePngFileFromDotFile(dotFilename);

                // Model changed through refinement. Therefore, forget any
                // invariants that might have timed out previously,
                // and add all of them back to invsToSatisfy.
                if (timedOutInvs.size() > 0) {
                    // Append all of the previously timed out invariants back to
                    // invsToSatisfy.
                    invsToSatisfy.addAll(timedOutInvs);
                    timedOutInvs.clear();
                }

            }
        }
    }

}
