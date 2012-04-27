package main;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;
import model.SynopticModel;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;

import synoptic.algorithms.Bisimulation;
import synoptic.algorithms.KTails;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverImmediatelyFollowedInvariant;
import synoptic.invariants.TOInitialTerminalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ImmediateInvariantMiner;
import synoptic.invariants.miners.KTailInvariantMiner;
import synoptic.invariants.miners.TOInvariantMiner;
import synoptic.main.Options;
import synoptic.main.SynopticMain;
import synoptic.main.SynopticOptions;
import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.PartitionGraph;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.export.DotExportFormatter;
import synoptic.model.export.GraphExporter;

/**
 * InvariMint accepts a log file and regular expression arguments and constructs
 * a DFA model of the system which generated the input log. InvariMint relies on
 * Synoptic for log parsing and invariant mining. Default behavior is to mine
 * traditional Synoptic invariants, though the --performKTails and --
 * kTailLength options allow users to specify that kTail invariants should be
 * mined instead. In either case, implicit NIFby invariants along with an
 * Initial/Terminal invariant are used to construct an initial DFA model. The
 * initial model is then intersected with each of the mined invariants to
 * construct and export a final model.
 * 
 * @author Jenny
 */
public class InvariMintMain {
    public static Logger logger = null;

    /**
     * Main entrance into the application. Application arguments (args) are
     * processed using Synoptic's build-in argument parser, extended with a few
     * InvDFAMinimization-specific arguments. For more information, see
     * InvDFAMinimizationOptions class.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        InvariMintOptions opts = new InvariMintOptions(args);
        EncodedAutomaton dfa = runInvariMint(opts);

        // Export final model.
        dfa.exportDotAndPng(opts.outputPathPrefix + ".invarimintDFA.dot");
    }

    /**
     * Performs InvariMint with the given set of options, returns the final dfa.
     */
    public static EncodedAutomaton runInvariMint(InvariMintOptions opts)
            throws Exception {

        // Set up Synoptic.
        setUpLogging(opts);
        handleOptions(opts);
        ChainsTraceGraph inputGraph = setUpSynoptic(opts);

        // Mine invariants -- will be Synoptic invariants or kTail invariants
        // depending on opts.
        TemporalInvariantSet minedInvs = mineInvariants(opts, inputGraph);
        logger.fine("Mined " + minedInvs.numInvariants()
                + (opts.performKTails ? " KTail" : " Synoptic")
                + " invariant(s).");
        logger.fine(minedInvs.toPrettyString());

        // Construct initial DFA from NIFby invariants.
        ImmediateInvariantMiner miner = new ImmediateInvariantMiner(inputGraph);
        TemporalInvariantSet NIFbys = miner.getNIFbyInvariants();
        Set<EventType> allEvents = new HashSet<EventType>(miner.getEventTypes());

        logger.fine("Mined " + NIFbys.numInvariants() + " NIFby invariant(s).");
        logger.fine(NIFbys.toPrettyString());

        EventTypeEncodings encodings = new EventTypeEncodings(allEvents);
        EncodedAutomaton dfa = null;

        dfa = getIntersectedModelFromInvs(NIFbys, encodings, opts);

        // Apply initial/terminal condition
        EventType initialEvent = StringEventType.newInitialStringEventType();
        EventType terminalEvent = StringEventType.newTerminalStringEventType();
        InvModel initialTerminalInv = new InvModel(
                new TOInitialTerminalInvariant(initialEvent, terminalEvent,
                        Event.defaultTimeRelationString), encodings);
        dfa.intersectWith(initialTerminalInv);

        // Intersect with mined invariants.
        dfa.intersectWith(getIntersectedModelFromInvs(minedInvs, encodings,
                opts));
        dfa.minimize();

        // Remove paths from the model not found in any input trace
        if (opts.removeSpuriousEdges) {
            logger.info("Removing spurious edges");
            removeSpuriousEdges(dfa, inputGraph, encodings, initialEvent,
                    terminalEvent);
        }

        // Run Synoptic to compare models
        if (opts.runSynoptic) {
            logger.info("Running Synoptic");
            PartitionGraph pGraph = null;
            if (opts.performKTails) {
                pGraph = KTails.performKTails(inputGraph, opts.kTailLength);
            } else {
                pGraph = new PartitionGraph(inputGraph, true, minedInvs);
                Bisimulation.splitUntilAllInvsSatisfied(pGraph);
                Bisimulation.mergePartitions(pGraph);
            }

            logger.info("Comparing Synoptic and InvariMint models");
            compareTranslatedModel(pGraph, encodings, dfa, opts);
        }

        return dfa;
    }

    public static void setUpLogging(InvariMintOptions opts) {
        // Get the top Logger instance
        logger = Logger.getLogger("InvariMintMain");

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

    public static void handleOptions(InvariMintOptions opts) {
        // Display help for all option groups, including unpublicized ones
        if (opts.allHelp) {
            opts.printLongHelp();
            System.exit(0);
        }

        // Display help just for the 'publicized' option groups
        if (opts.help) {
            opts.printShortHelp();
            System.exit(0);
        }

        if (opts.outputPathPrefix == null) {
            logger.warning("Cannot output initial graph. Specify output path prefix using:\n\t"
                    + Options.getOptDesc("outputPathPrefix"));
            System.exit(0);
        }

        if (opts.logFilenames.size() == 0) {
            logger.severe("No log filenames specified, exiting. Try cmd line option:\n\t"
                    + synoptic.main.Options.getOptDesc("help"));
            System.exit(0);
        }

        if (!opts.runSynoptic
                && (opts.exportSynopticNFA || opts.exportSynopticDFA)) {
            logger.severe("Will not export Synoptic models since --runSynoptic is false");
        }
    }

    public static ChainsTraceGraph setUpSynoptic(InvariMintOptions opts)
            throws Exception {

        // Set up options in Synoptic Main that are used by the library.
        SynopticOptions options = new SynopticOptions();
        options.logLvlExtraVerbose = true;
        options.internCommonStrings = true;
        options.recoverFromParseErrors = opts.recoverFromParseErrors;
        options.debugParse = opts.debugParse;
        options.ignoreNonMatchingLines = opts.ignoreNonMatchingLines;

        SynopticMain synMain = new SynopticMain(options,
                new DotExportFormatter());

        // Instantiate the parser and parse the log lines.
        TraceParser parser = new TraceParser(opts.regExps,
                opts.partitionRegExp, opts.separatorRegExp);

        List<EventNode> parsedEvents = SynopticMain.parseEvents(parser,
                opts.logFilenames);

        if (opts.debugParse) {
            // Terminate since the user is interested in debugging the parser.
            logger.info("Terminating. To continue further, re-run without the debugParse option.");
            System.exit(0);
        }

        if (!parser.logTimeTypeIsTotallyOrdered()) {
            logger.severe("Partially ordered log input detected. Stopping");
            System.exit(0);
        }

        if (parsedEvents.size() == 0) {
            logger.severe("Did not parse any events from the input log files. Stopping.");
            System.exit(0);
        }
        // //////////////////
        ChainsTraceGraph inputGraph = parser
                .generateDirectTORelation(parsedEvents);
        // //////////////////

        return inputGraph;
    }

    /**
     * Mines invariants from the given input graph. These are either Synoptic or
     * kTail invariants depending on command line options stored in opts.
     */
    private static TemporalInvariantSet mineInvariants(InvariMintOptions opts,
            ChainsTraceGraph inputGraph) {
        TOInvariantMiner miner;
        if (opts.performKTails) {
            miner = new KTailInvariantMiner(opts.kTailLength);
        } else {
            miner = new ChainWalkingTOInvMiner();
        }

        logger.info("Mining invariants [" + miner.getClass().getName() + "]..");
        long startTime = System.currentTimeMillis();

        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph,
                false);

        long endTime = System.currentTimeMillis();
        logger.info("Mining took " + (endTime - startTime));

        logger.fine("Mined " + minedInvs.numInvariants()
                + " kTail invariant(s).");
        logger.fine(minedInvs.toPrettyString());

        return minedInvs;
    }

    /**
     * Constructs an InvsModel by intersecting InvModels for each of the given
     * temporal invariants.
     * 
     * @param invariants
     *            a set of TemporalInvariants
     * @param minimize
     *            whether or not to minimize the model before returning.
     * @return the intersected InvsModel
     */
    public static InvsModel getIntersectedModelFromInvs(
            TemporalInvariantSet invariants, EventTypeEncodings encodings,
            InvariMintOptions opts) {
        // Initial model will accept all Strings.
        InvsModel model = new InvsModel(encodings);

        // Intersect provided invariants.
        for (ITemporalInvariant invariant : invariants) {
            InvModel invDFA = new InvModel(invariant, encodings);

            model.intersectWith(invDFA);

            if (opts.exportMinedInvariantDFAs
                    && !(invariant instanceof NeverImmediatelyFollowedInvariant)) {
                try {
                    invDFA.exportDotAndPng(opts.outputPathPrefix + ".InvDFA"
                            + opts.invIDCounter + ".dot");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                opts.invIDCounter++;
            }
        }

        if (opts.minimizeIntersections) {
            // Optimize by minimizing the model.
            model.minimize();
        }

        return model;
    }

    /**
     * Removes edges from the provided dfa that cannot be mapped to any trace in
     * the input trace graph g.
     */
    private static void removeSpuriousEdges(EncodedAutomaton dfa,
            ChainsTraceGraph g, EventTypeEncodings encodings,
            EventType initialEvent, EventType terminalEvent) {
        // TODO: just build the new dfa -- no seen map

        Map<StatePair, Set<Character>> seenTransitions = new HashMap<StatePair, Set<Character>>();
        EventNode initNode = g.getDummyInitialNode();

        // Iterate through all the traces -- each transition from the INITIAL
        // node holds a single trace.
        for (EventNode curNode : initNode.getAllSuccessors()) {
            // Set curState to the state immediately following the INITIAL
            // transition.
            State curState = dfa.getInitialState();
            curState = fetchDestination(curState, encodings, initialEvent,
                    seenTransitions);

            while (curNode.getAllTransitions().size() != 0) {

                curState = fetchDestination(curState, encodings,
                        curNode.getEType(), seenTransitions);

                if (curState == null) {
                    throw new IllegalStateException(
                            "Unable to fetch valid destination for ");
                }

                // Move on to the next node in the trace.
                assert (curNode.getAllTransitions().size() > 0);
                curNode = curNode.getAllTransitions().get(0).getTarget();
            }

            fetchDestination(curState, encodings, terminalEvent,
                    seenTransitions);
        }

        dfa.setInitialState(replicate(seenTransitions, dfa.getInitialState(),
                new HashMap<State, State>(), encodings));

        dfa.minimize();
    }

    /**
     * Recursively replicates the given automata starting from the current state
     * but eliminates transitions that were not 'seen'.
     */
    private static State replicate(
            Map<StatePair, Set<Character>> seenTransitions, State current,
            Map<State, State> visited, EventTypeEncodings encodings) {

        if (visited.containsKey(current)) {
            return visited.get(current);
        }

        State replica = new State();
        replica.setAccept(current.isAccept());
        visited.put(current, replica);

        for (Transition t : current.getTransitions()) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                StatePair curTransition = new StatePair(current, t.getDest());
                if (seenTransitions.containsKey(curTransition)
                        && seenTransitions.get(curTransition).contains(
                                new Character(c))) {
                    replica.addTransition(new Transition(c, replicate(
                            seenTransitions, t.getDest(), visited, encodings)));
                }
            }
        }
        return replica;
    }

    /**
     * Given a State and an EventType, returns the State to which the source
     * state would transition given the Event if such a state exists. Also
     * updates the seenTransitions map with the transition used to find the
     * destination.
     */
    private static State fetchDestination(State source,
            EventTypeEncodings encodings, EventType currentEvent,
            Map<StatePair, Set<Character>> seenTransitions) {

        for (Transition t : source.getTransitions()) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                if (currentEvent.toString().equals(encodings.getString(c))) {
                    StatePair curTransition = new StatePair(source, t.getDest());
                    if (!seenTransitions.containsKey(curTransition)) {
                        seenTransitions.put(curTransition,
                                new HashSet<Character>());
                    }
                    seenTransitions.get(curTransition).add(new Character(c));
                    return t.getDest();
                }
            }
        }
        return null;
    }

    /**
     * Runs Synoptic on the initial model then translates the final Synoptic
     * model to a DFA. The Synoptic DFA is exported and then compared with the
     * final DFAmin model.
     * 
     * @throws IOException
     */
    private static void compareTranslatedModel(PartitionGraph pGraph,
            EventTypeEncodings encodings, EncodedAutomaton dfa,
            InvariMintOptions opts) throws IOException {

        if (opts.exportSynopticNFA) {
            GraphExporter.exportGraph(opts.outputPathPrefix
                    + ".synopticNFA.dot", pGraph, false);
            GraphExporter.generatePngFileFromDotFile(opts.outputPathPrefix
                    + ".synopticNFA.dot");
        }

        SynopticModel convertedDfa = new SynopticModel(pGraph, encodings);

        if (opts.exportSynopticDFA) {
            convertedDfa.exportDotAndPng(opts.outputPathPrefix
                    + ".synopticDFA.dot");
        }

        // Print whether the language accepted by dfa is a subset of the
        // language accepted by synDfa and vice versa.
        logger.info("Translated Synoptic DFA language a subset of DFAmin language: "
                + convertedDfa.subsetOf(dfa));
        logger.info("DFAmin language a subset of translated Synoptic DFA language: "
                + dfa.subsetOf(convertedDfa));
    }
}