package dynoptic.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mcscm.CounterExample;
import mcscm.McScM;
import mcscm.VerifyResult;
import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.EventuallyHappens;
import dynoptic.invariants.NeverFollowedBy;
import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.gfsm.GFSM;
import dynoptic.model.fifosys.gfsm.GFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.dag.ObsDAG;
import dynoptic.model.fifosys.gfsm.observed.dag.ObsDAGNode;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
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
import synoptic.model.export.DotExportFormatter;
import synoptic.util.InternalSynopticException;
import synoptic.util.Pair;

/**
 * <p>
 * This class wraps everything together to provide a command-line interface, as
 * well as an API, to run Dynoptic programatically.
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

    static Logger logger = null;

    private DynopticOptions opts = null;

    // The Java McScM model checker bridge instance that interfaces with the
    // McScM verify binary.
    private McScM mcscm = null;

    // The channels associated with this Dynoptic execution. These are parsed in
    // checkOptions().
    private List<ChannelId> channelIds = null;

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

    // //////////////////////////////////////////////////////////////////

    /** Prepares a new DynopticMain instance based on opts. */
    public DynopticMain(DynopticOptions opts) throws Exception {
        this.opts = opts;
        setUpLogging(opts);
        checkOptions(opts);
        mcscm = new McScM(opts.mcPath);
    }

    public List<ChannelId> getChannelIds() {
        return channelIds;
    }

    /**
     * Runs Dynoptic based on setting in opts, but uses the pre-created GFSM
     * with observations and a set of invariants invs. Satisfies all of the
     * invariants invs in g.
     * 
     * @param g
     *            A fifosys model that partitions concrete observations into
     *            abstract states.
     * @param invs
     *            Invariants to satisfy in g.
     */
    public void run(GFSM g, TemporalInvariantSet invs) {
        for (ITemporalInvariant inv : invs) {
            // TODO
            // 1. Iterate over invs.
        }
    }

    /**
     * Converts a temporal invariant inv into a set S of pairs of GFSMStates.
     * This set S satisfies the condition that:
     * 
     * <pre>
     * inv is true iff \forall <p,q> \in S there is no path from p to q in g.
     * </pre>
     * 
     * @param g
     * @return
     */
    public Set<Pair<GFSMState, GFSMState>> invToBadStates(GFSM g,
            ITemporalInvariant inv) {
        // TODO: assert that inv is composed of events that are in g's alphabet
        // assert g.getAlphabet().contains(
        Set<Pair<GFSMState, GFSMState>> ret = new LinkedHashSet<Pair<GFSMState, GFSMState>>();

        // The basic strategy, regardless of invariant, is to create a
        // separate FIFO queue that will be used to record the sequence of
        // executed events that are relevant to the invariant.
        //
        // For instance, for a AFby b invariant, create a queue Q_ab. Modify any
        // state p that has an outgoing "a" transition, add a synthetic state
        // p_synth, and redirect the "a" transition from p to p_synth. Then, add
        // just one outgoing transition on "Q_ab ! a" from p_synth to the
        // original state target of "a" in state p. That is, whenever "a"
        // occurs, we will add "a" to Q_ab. Do the same for event "b".
        //
        // For a AFby b bad state pairs within the modified GFSM (per above
        // procedure) are all initial state and all states where all queues
        // except Q_ab are empty, and where Q_ab = [*a], and where the process
        // states are terminal. In a sense, we've added Q_ab to track "a" and
        // "b" executions, and not interfere with the normal execution of the
        // FIFO system.
        //
        // For a AP b, the procedure is identical, but the second bad state in
        // every pair would have Q_ab = [b*]. For a NFby b, Q_ab = [*a*b*]. In a
        // sense, we've expressed LTL properties as regular expressions of Q_ab
        // queue contents.

        // TODO:

        return ret;
    }

    /**
     * <p>
     * Refines g until there is no path from badStates.left to badStates.right
     * in g. Throws an exception if (1) no such refinement is possible, or (2)
     * if the model checker that checks the abstract model corresponding to g
     * (i.e., the CFSM derived from g) has exceeded an execution time-bound.
     * </p>
     * <p>
     * NOTE: this method mutates g.
     * </p>
     * 
     * @param g
     * @param badStates
     */
    public void run(GFSM g, Pair<GFSMState, GFSMState> badStates) {
        // TODO
    }

    /**
     * Runs Dynoptic based on setting in opts, but uses the log from the passed
     * in String, and not from the logFilenames defined in opts.
     * 
     * @param log
     */
    public void run(String log) {
        // TODO
    }

    /**
     * Runs the Dynoptic process based on the settings in opts. In particular,
     * we expect that the logFilenames are specified in opts.
     * 
     * @throws Exception
     */
    public void run() throws Exception {
        // ////////////////// Parse the input log files into _Synoptic_
        // structures.

        if (opts.logFilenames.size() == 0) {
            String err = "No log filenames specified, exiting. Specify log files at the end of the command line.";
            throw new OptionException(err);
        }

        SynopticOptions synOpts = new SynopticOptions();
        synOpts.ignoreNonMatchingLines = opts.ignoreNonMatchingLines;
        synOpts.recoverFromParseErrors = opts.recoverFromParseErrors;
        synOpts.debugParse = opts.debugParse;
        SynopticMain synMain = new SynopticMain(synOpts,
                new DotExportFormatter());

        TraceParser parser = new TraceParser(opts.regExps,
                opts.partitionRegExp, opts.separatorRegExp);
        List<EventNode> parsedEvents;

        parsedEvents = SynopticMain.parseEvents(parser, opts.logFilenames);
        if (parsedEvents.size() == 0) {
            throw new OptionException(
                    "Did not parse any events from the input log files. Stopping.");
        }

        if (parser.logTimeTypeIsTotallyOrdered()) {
            throw new OptionException(
                    "Dynoptic expects a log that is partially ordered.");
        }

        // ////////////////// Generate the Synoptic DAG from parsed events
        DAGsTraceGraph traceGraph = SynopticMain.genDAGsTraceGraph(parser,
                parsedEvents);

        // Parser can be garbage-collected.
        parser = null;

        // ////////////////// Mine Synoptic invariants
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

        // ////////////////// Generate an alphabet of Dynoptic EventTypes based
        // on the events parsed by Synoptic.

        FSMAlphabet alphabet = new FSMAlphabet();

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
            usedPids.add(distEType.getEventPid());
            if (distEType.isCommEvent()) {
                usedChannelIds.add(distEType.getChannelId());
            }
            alphabet.add(distEType);
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
        int numProcesses = maxPid;
        if (pidSum != ((maxPid * (maxPid + 1)) / 2) || !usedPids.contains(0)) {
            throw new OptionException("Process ID range for the log has gaps: "
                    + usedPids.toString());
        }

        // ////////////////// Use Synoptic event nodes and ordering constraints
        // between these to generate ObsFSMStates (anonymous states),
        // obsDAGNodes (to contain obsFSMStates and encode dependencies between
        // them), and an ObsDag per execution parsed from the log.

        // TODO

        int pid = 0;
        ObsDAGNode node, nextNode = null, prevNode = null;
        ObsFSMState s;
        Event obsEvent = null;

        List<ObsFifoSys> traces = new ArrayList<ObsFifoSys>();

        // A map of trace id to the set of initial nodes in the trace.
        Map<Integer, Set<EventNode>> traceIdToInitNodes = traceGraph
                .getTraceIdToInitNodes();

        for (int traceId = 0; traceId < traceGraph.getNumTraces(); traceId++) {
            assert traceIdToInitNodes.containsKey(traceId);

            Set<EventNode> initSynNodes = traceIdToInitNodes.get(traceId);

            /*
             * TODO: build a Dynoptic DAG from the Synoptic DAG.
             * 
             * s = ObsFSMState.ObservedInitialFSMState(pid);
             * ObsFSMState.ObservedIntermediateFSMState(pid);
             * ObsFSMState.ObservedInitialTerminalFSMState(pid);
             * 
             * node = new ObsDAGNode(s); node.addDependency(prevNode);
             * node.addTransition(obsEvent, nextNode);
             */
            List<ObsDAGNode> initDagConfig = new ArrayList<ObsDAGNode>();
            List<ObsDAGNode> termDagConfig = new ArrayList<ObsDAGNode>();

            ObsDAG dag = new ObsDAG(initDagConfig, termDagConfig, channelIds);

            ObsFifoSys fifoSys = dag.getObsFifoSys();
            traces.add(fifoSys);
        }

        // /////////////////// Express Synoptic invariants as Dynoptic
        // invariants.

        Set<dynoptic.invariants.BinaryInvariant> dynInvs = new LinkedHashSet<dynoptic.invariants.BinaryInvariant>();
        dynoptic.invariants.BinaryInvariant dynInv = null;
        DistEventType dynETypeFirst, dynETypeSecond;
        for (ITemporalInvariant inv : minedInvs) {
            BinaryInvariant binv = (BinaryInvariant) inv;

            assert (binv.getFirst() instanceof DistEventType);
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

        if (dynInvs.size() == 0) {
            logger.info("Mined 0 Dynoptic invariants. Stopping.");
            return;
        }

        // /////////////////// Create a partition graph (GFSM instance) of the
        // ObsFifoSys instances we've created above. And check each invariant in
        // the model, and refine the model as needed until all invariants hold.

        // Create the initial partition graph using the default partitioning
        // strategy, based on head of all of the queues of each
        // ObsFifoSysState.
        GFSM pGraph = new GFSM(traces);

        Iterator<dynoptic.invariants.BinaryInvariant> invIter = dynInvs
                .iterator();
        // We checked above that the number of mined Dynoptic invariants is
        // non-zero.
        dynoptic.invariants.BinaryInvariant curInv = invIter.next();
        while (true) {
            // Get the CFSM corresponding to the partition graph.
            CFSM cfsm = pGraph.getCFSM();
            // Augment the CFSM with synthetic states/events to check curInv.
            cfsm.augmentWithInvTracing(curInv);

            // Model check the CFSM using the McScM model checker.
            String cStr = cfsm.toScmString();
            mcscm.verify(cStr);
            logger.info(cStr);

            VerifyResult result = mcscm.getVerifyResult(cfsm.getChannelIds());
            logger.info(result.toRawString());
            logger.info(result.toString());

            // If there is no counter-example, then the invariant holds true.
            if (result.getCExample() == null) {
                if (!invIter.hasNext()) {
                    // No more invariants to check. We are done.
                    break;
                }
                // Check the next invariant.
                curInv = invIter.next();
            } else {
                CounterExample cExample = result.getCExample();

                // TODO: refine the pGraph to eliminate cExample.

            }
        }

        // ///////////////////
        // Output the final CFSM model using GraphViz (dot-format).

        // TODO.
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Interprets a Synoptic DistEventType and returns a corresponding Dynoptic
     * EventType.
     */
    // private EventType parseEventType(EventNode eNode) {

    // }

    /** Finds an returns a ChannelId by name. Returns null if none is found. */
    private ChannelId getChIdByName(String name) {
        for (ChannelId cid : channelIds) {
            if (cid.getName().equals(name)) {
                return cid;
            }
        }
        return null;
    }

    /**
     * Checks the input Dynoptic options for consistency and omissions.
     * 
     * @param optns
     * @throws Exception
     */
    private void checkOptions(DynopticOptions optns) throws Exception {
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

        channelIds = parseChannelSpec(opts.channelSpec);
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

    // //////////////////////////////////////////////////

    /**
     * Parses the channelSpec command line option value and returns a list of
     * corresponding channelId instances.
     */
    public static List<ChannelId> parseChannelSpec(String channelSpec) {
        // Parse the channelSpec option value into channelId instances.
        int scmId = 0, srcPid, dstPid;
        String chName;

        // This pattern matcher will match strings like "M:0->1;A:1->0", which
        // defines two channels -- 'M' and 'A'. The M channel has pid 0 as
        // sender and pid 1 as receiver, and the A channel has pid 1 as sender
        // and pid 0 as receiver.
        Pattern pattern = Pattern.compile("(.*?):(\\d+)\\-\\>(\\d+);*");
        Matcher matcher = pattern.matcher(channelSpec);

        ChannelId cid;
        List<ChannelId> cids = new ArrayList<ChannelId>();
        Set<String> chNames = new LinkedHashSet<String>();
        int lastEnd = 0;
        while (matcher.find()) {
            // logger.info("Found text" + " \"" + matcher.group()
            // + "\" starting at " + "index " + matcher.start()
            // + "  and ending at index " + matcher.end());
            chName = matcher.group(1);
            srcPid = Integer.parseInt(matcher.group(2));
            dstPid = Integer.parseInt(matcher.group(3));
            lastEnd = matcher.end();
            if (chNames.contains(chName)) {
                // Channel names should be unique since in the log channels are
                // identified solely by the channel name.
                throw new OptionException(
                        "Channel spec contains multiple entries for channel '"
                                + chName + "'.");
            }
            chNames.add(chName);

            cid = new ChannelId(srcPid, dstPid, scmId, chName);
            logger.info("Parsed ChannelId : " + cid.toString());
            cids.add(cid);
            scmId += 1;
        }

        if (lastEnd != channelSpec.length()) {
            throw new OptionException(
                    "Failed to completely parse the channel spec. Parsed up to char position "
                            + lastEnd);
        }
        return cids;
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

}
