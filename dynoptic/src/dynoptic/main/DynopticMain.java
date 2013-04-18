package dynoptic.main;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mcscm.McScM;
import mcscm.McScMCExample;
import mcscm.VerifyResult;
import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.invariants.EventuallyHappens;
import dynoptic.invariants.NeverFollowedBy;
import dynoptic.model.export.GraphExporter;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.gfsm.GFSM;
import dynoptic.model.fifosys.gfsm.GFSMPath;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;
import dynoptic.util.Util;

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
        //
        assert (assertsOn = true) == true;
        // assertsOn = false;
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
        if (channelIds.isEmpty()) {
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

        if (opts.logFilenames.isEmpty()) {
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

        // Export a visualization of the traceGraph
        String dotFilename = opts.outputPathPrefix + ".trace-graph.dot";
        synoptic.model.export.GraphExporter.exportGraph(dotFilename,
                traceGraph, true);
        // synoptic.model.export.GraphExporter
        // .generatePngFileFromDotFile(dotFilename);

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

        // ///////////////////
        // Convert Synoptic invariants into Dynoptic invariants.
        logger.info("Converting Synoptic invariants to Dynoptic invariants...");
        List<BinaryInvariant> dynInvs = synInvsToDynInvs(minedInvs);

        logger.info(minedInvs.numInvariants() + " Synoptic invs --> "
                + dynInvs.size() + " Dynoptic invs.");

        if (dynInvs.isEmpty()) {
            logger.info("Mined 0 Dynoptic invariants. Stopping.");
            return;
        }

        // //////////////////
        // Use Synoptic event nodes and ordering constraints
        // between these to generate ObsFSMStates (anonymous states),
        // obsDAGNodes (to contain obsFSMStates and encode dependencies between
        // them), and an ObsDag per execution parsed from the log.
        logger.info("Generating ObsFifoSys from DAGsTraceGraph...");
        List<ObsFifoSys> traces = ObsFifoSys.synTraceGraphToDynObsFifoSys(
                traceGraph, numProcesses, channelIds, opts.consistentInitState);

        assert !traces.isEmpty();

        // Export (just the first!) Observed FIFO System instance:
        dotFilename = opts.outputPathPrefix + ".obsfifosys.tid1.dot";
        GraphExporter.exportObsFifoSys(dotFilename, traces.get(0));
        // GraphExporter.generatePngFileFromDotFile(dotFilename);

        // //////////////////
        // If assume consistent per-process initial state, check that
        // only one ObsFifoSys is created.
        //
        // Also, this option allows stitchings between traces, which may lead to
        // invariant violations. This post-processing step finds invariants that
        // violate such stitchings.
        if (opts.consistentInitState) {
            assert traces.size() == 1;

            logger.info("Finding invalidated invariants in the observed fifo system.");
            Set<BinaryInvariant> faultyInvs = traces.get(0)
                    .findInvalidatedInvariants(dynInvs);
            if (!faultyInvs.isEmpty()) {
                logger.warning("Input traces are incomplete --- some mined invariants cannot be satisfied: "
                        + faultyInvs.toString());

                dynInvs.removeAll(faultyInvs);
                logger.info("Ignoring faulty invariant and continuing. New invariants set: "
                        + dynInvs.toString());
            }
        }

        // ///////////////////
        // Create a partition graph (GFSM instance) of the ObsFifoSys instances
        // we've created above. Use the default initial partitioning strategy,
        // based on head of all of the queues of each ObsFifoSysState.
        logger.info("Generating the initial partition graph (GFSM)...");
        GFSM pGraph = new GFSM(traces);

        // Order dynInvs so that the eventually invariants are at the front (the
        // assumption is that they are faster to model check).
        logger.info("Reordering invariants to place \"eventually\" invariants at the front.");
        for (int i = 0; i < dynInvs.size(); i++) {
            if (dynInvs.get(i) instanceof EventuallyHappens) {
                BinaryInvariant inv = dynInvs.remove(i);
                dynInvs.add(0, inv);
            }
        }

        // ///////////////////
        // Model check, refine loop. Check each invariant in the model, and
        // refine the model as needed until all invariants hold.
        checkInvsRefineGFSM(dynInvs, pGraph);

        // ///////////////////
        // Output the final CFSM model (corresponding to pGraph) using GraphViz
        // (dot-format).

        logger.info("Final scm model:");
        logger.info(pGraph.getCFSM(opts.minimize).toScmString("final model"));

        CFSM cfsm = pGraph.getCFSM(opts.minimize);
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
        assert !logFilenames.isEmpty();

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

        if (parsedEvents.isEmpty()) {
            throw new OptionException(
                    "Did not parse any events from the input log files. Stopping.");
        }

        // //////////////////
        // Parse the parsed events further (as distributed
        // events that capture message send/receives). And determine the number
        // of processes in the system.

        Set<ChannelId> usedChannelIds = Util.newSet();
        Set<Integer> usedPids = Util.newSet();

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
        logger.info("Detected " + numProcesses + " processes in the log.");

        if (pidSum != ((maxPid * (maxPid + 1)) / 2) || !usedPids.contains(0)) {
            throw new OptionException("Process ID range for the log has gaps: "
                    + usedPids.toString());
        }

        // Make sure that we have observed at least one event for each process
        // associated with a used channel.
        for (ChannelId chId : channelIds) {
            if (chId.getSrcPid() > maxPid) {
                throw new OptionException(
                        "Did not observed any events for process "
                                + chId.getSrcPid()
                                + " that is part of channel " + chId.toString());
            } else if (chId.getDstPid() > maxPid) {
                throw new OptionException(
                        "Did not observed any events for process "
                                + chId.getDstPid()
                                + " that is part of channel " + chId.toString());
            }
        }

    }

    /**
     * Converts a set of Synoptic invariants into a set of Dynoptic invariants.
     * Currently we ignore all \parallel and \nparallel invariants, as well as
     * all "x NFby y" invariants where x and y occur at different processes (see
     * Issue 271)
     */
    public static List<BinaryInvariant> synInvsToDynInvs(
            TemporalInvariantSet minedInvs) {
        List<BinaryInvariant> dynInvs = Util.newList();

        BinaryInvariant dynInv = null;

        DistEventType first, second;
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
                first = DistEventType.INITIALEventType;
            } else {
                first = ((DistEventType) binv.getFirst());
            }
            second = ((DistEventType) binv.getSecond());

            if (inv instanceof AlwaysFollowedInvariant) {
                if (first == DistEventType.INITIALEventType) {
                    dynInv = new EventuallyHappens(second);
                } else {
                    dynInv = new AlwaysFollowedBy(first, second);
                }
            } else if (inv instanceof NeverFollowedInvariant) {
                assert first != DistEventType.INITIALEventType;

                // Ignore x NFby y if x and y occur at different processes
                // (Issue 271).
                if (first.getPid() == second.getPid()) {
                    dynInv = new NeverFollowedBy(first, second);
                }

            } else if (inv instanceof AlwaysPrecedesInvariant) {
                assert first != DistEventType.INITIALEventType;
                dynInv = new AlwaysPrecedes(first, second);
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
    public void checkInvsRefineGFSM(List<BinaryInvariant> invs, GFSM pGraph)
            throws Exception, IOException, InterruptedException {
        assert pGraph != null;
        assert invs != null;
        assert !invs.isEmpty();

        // Make a copy of invs, as we'll be modifying the list (removing
        // invariants once they are satisfied by the model).
        List<BinaryInvariant> invsToSatisfy = Util.newList(invs);

        // The set of invariants that have timed-out so far. This set is reset
        // whenever we successfully check/refine an invariant.
        Set<BinaryInvariant> timedOutInvs = Util.newSet();

        // The set of invariants (subset of original invsToSatisfy) that the
        // model satisfies.
        Set<BinaryInvariant> satisfiedInvs = Util.newSet();

        // curInv will _always_ refer to the 0th element of invsToSatisfy.
        BinaryInvariant curInv = invsToSatisfy.get(0);

        int totalInvs = invsToSatisfy.size();
        int invsCounter = 1;

        // ////// Additive and memory-less timeout value adaptation.
        // Initial McScM invocation timeout in seconds.
        int baseTimeout = opts.baseTimeout;

        // How much we increment curTimeout by, when we timeout on checking all
        // invariants.
        int timeoutDelta = opts.timeoutDelta;

        // At what point to we stop the incrementing the timeout and terminate
        // with a failure.
        int maxTimeout = opts.maxTimeout;

        // Current timeout value to use.
        int curTimeout = baseTimeout;

        if (maxTimeout < baseTimeout) {
            throw new Exception(
                    "maxTimeout value must be greater than baseTimeout value");
        }

        logger.info("Model checking " + curInv.toString() + " : " + invsCounter
                + " / " + totalInvs);

        // This counts the number of times we've refined the gfsm.
        int gfsmCounter = 0;

        String gfsmPrefixFilename = opts.outputPathPrefix;

        exportIntermediateModels(pGraph, curInv, gfsmCounter,
                gfsmPrefixFilename);

        while (true) {
            assert invsCounter <= totalInvs;
            assert curInv == invsToSatisfy.get(0);
            assert timedOutInvs.size() + satisfiedInvs.size()
                    + invsToSatisfy.size() == totalInvs;

            // Get the CFSM corresponding to the partition graph.
            CFSM cfsm = pGraph.getCFSM(opts.minimize);

            // Augment the CFSM with synthetic states/events to check curInv.
            cfsm.augmentWithInvTracing(curInv);

            // Model check the CFSM using the McScM model checker.
            String cStr = cfsm.toScmString("checking_"
                    + curInv.getConnectorString());

            logger.info("*******************************************************");
            logger.info("Checking ... " + curInv.toString() + ". Inv "
                    + invsCounter + " / " + totalInvs
                    + ", refinements so far: " + gfsmCounter + ". Timeout = "
                    + curTimeout + ".");
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
                // unless we reached the timeout limit, in which case we throw
                // an exception.
                if (invsToSatisfy.isEmpty()) {
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

            if (result.modelIsSafe()) {
                // Remove the current invariant from the invsToSatisfy list.
                BinaryInvariant curInvCheck = invsToSatisfy.remove(0);
                assert curInvCheck == curInv;
                satisfiedInvs.add(curInv);

                if (invsToSatisfy.isEmpty()) {
                    // No more invariants to check. We are done.
                    logger.info("Finished checking " + invsCounter + " / "
                            + totalInvs + " invariants.");
                    return;
                }

                // Grab and start checking the next invariant.
                curInv = invsToSatisfy.get(0);
                invsCounter += 1;
                logger.info("Model checking " + curInv.toString() + " : "
                        + invsCounter + " / " + totalInvs);
            } else {
                // Refine the pGraph in an attempt to eliminate the counter
                // example.
                refineCExample(pGraph, result.getCExample());

                // Increment the number of refinements:
                gfsmCounter += 1;

                exportIntermediateModels(pGraph, curInv, gfsmCounter,
                        gfsmPrefixFilename);

                // Model changed through refinement. Therefore, forget any
                // invariants that might have timed out previously,
                // and add all of them back to invsToSatisfy.
                if (!timedOutInvs.isEmpty()) {
                    // Append all of the previously timed out invariants back to
                    // invsToSatisfy.
                    invsToSatisfy.addAll(timedOutInvs);
                    timedOutInvs.clear();
                }
            }
        }
    }

    /**
     * Matches the sequence of events in the counter-example to paths of
     * corresponding GFSM states for each process. Then, refines each process'
     * paths until all paths for some process are successfully refined.
     * 
     * @throws Exception
     *             if we were not able to eliminate the counter-example
     */
    private void refineCExample(GFSM pGraph, McScMCExample cexample)
            throws Exception {

        // Resolve all of the complete counter-example paths by:
        // refining all possible stitching partitions for pid 0.
        // However, if not all of these can be refined then try pid 1, and
        // so on. By construction we are guaranteed to be able to
        // eliminate this execution, so we are making progress as long
        // as we refine at each step.
        for (int i = 0; i < this.getNumProcesses(); i++) {
            // Get set of GFSM paths for process i that generate the
            // sub-sequence of process i events in the counter-example.
            logger.info("Computing process " + i + " paths...");
            Set<GFSMPath> processPaths = pGraph.getCExamplePaths(cexample, i);
            if (processPaths == null) {
                logger.info("No matching paths for process " + i
                        + " exist, continuing.");
                // Treat this as if we refined all of the paths for the process
                // -- none exist!
                return;
            }

            // logger.info("Process " + i + " paths: " +
            // processPaths.toString());

            // Attempt to refine stitching partitions along these paths.

            // TODO: attempt to find partitions that over all of the paths in
            // processPaths. Then, refine these partitions, for a fewer number
            // of total refinements.

            boolean refinedAll = true;
            for (GFSMPath path : processPaths) {
                logger.info("Attempting to resolve process " + i + " path: "
                        + path.toString());
                // If path is no longer a valid path then it was refined and
                // eliminated previously.
                if (!GFSMPath.checkPathCompleteness(path)) {
                    continue;
                }

                // Otherwise, the path still needs to be refined.
                if (!path.refine(pGraph)) {
                    refinedAll = false;
                    break;
                }
            }

            // We were able to refine all paths for process i. Therefore, the
            // abstract counter-example path cannot exist.
            if (refinedAll) {
                return;
            }

        }
        if (DynopticMain.assertsOn) {
            assert false;
        }
        throw new Exception(
                "Unable to eliminate CFSM counter-example from GFSM.");
    }

    /**
     * Exports the GFSM model, as well as the corresponding CFSM, and CFSM
     * augmented with the invariant we are currently checking.
     * 
     * @param pGraph
     * @param curInv
     * @param gfsmCounter
     * @param gfsmPrefixFilename
     * @throws IOException
     * @throws Exception
     */
    private void exportIntermediateModels(GFSM pGraph, BinaryInvariant curInv,
            int gfsmCounter, String gfsmPrefixFilename) throws IOException,
            Exception {

        // Export GFSM:
        String dotFilename = gfsmPrefixFilename + ".gfsm." + gfsmCounter
                + ".dot";
        GraphExporter.exportGFSM(dotFilename, pGraph);
        // GraphExporter.generatePngFileFromDotFile(dotFilename);

        // Export CFSM:
        CFSM cfsm = pGraph.getCFSM(opts.minimize);

        dotFilename = gfsmPrefixFilename + ".cfsm-no-inv." + gfsmCounter
                + ".dot";
        GraphExporter.exportCFSM(dotFilename, cfsm);
        // GraphExporter.generatePngFileFromDotFile(dotFilename);

        // Export CFSM, augmented with curInv:
        cfsm.augmentWithInvTracing(curInv);
        dotFilename = gfsmPrefixFilename + ".cfsm." + gfsmCounter + ".dot";
        GraphExporter.exportCFSM(dotFilename, cfsm);
        // GraphExporter.generatePngFileFromDotFile(dotFilename);
    }

}
