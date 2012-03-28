package main;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;
import model.SynopticModel;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TOInitialTerminalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.main.Options;
import synoptic.main.SynopticOptions;
import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.PartitionGraph;
import synoptic.model.StringEventType;
import synoptic.model.export.DotExportFormatter;
import synoptic.model.interfaces.ITransition;
import synoptic.util.BriefLogFormatter;

/**
 * InvariMint accepts a log file and regular expression arguments and uses
 * Synoptic to parse the log and mine invariants. Implicit NIFby invariants
 * along with an Initial/Terminal invariant are used to construct an initial DFA
 * model. The initial model is then intersected with each of Synoptic's mined
 * invariants to construct and export a final model.
 * 
 * @author Jenny
 */
public class InvariMintMain {
    public static Logger logger = null;

    public static void setUpLogging(InvariMintOptions opts) {
        // Get the top Logger instance
        logger = Logger.getLogger("InvariMintMain");

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

        // TODO: add options to InvariMintOptions to control verbosity.
        //
        // Set the logger's log level based on command line arguments
        // if (opts.logLvlQuiet) {
        // logger.setLevel(Level.WARNING);
        // } else if (opts.logLvlVerbose) {
        // logger.setLevel(Level.FINE);
        // } else if (opts.logLvlExtraVerbose) {
        // logger.setLevel(Level.FINEST);
        // } else {
        // logger.setLevel(Level.INFO);
        // }

        logger.setLevel(Level.INFO);

        consoleHandler.setFormatter(new BriefLogFormatter());
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
    }

    public static ChainsTraceGraph setUpSynoptic(InvariMintOptions opts)
            throws Exception {

        // Set up options in Synoptic Main that are used by the library.
        Main.options = new SynopticOptions();
        Main.setUpLogging();
        Main.options.logLvlExtraVerbose = false;
        Main.options.logLvlExtraVerbose = true;
        Main.options.internCommonStrings = true;
        Main.options.recoverFromParseErrors = opts.recoverFromParseErrors;
        Main.options.debugParse = opts.debugParse;
        Main.options.ignoreNonMatchingLines = opts.ignoreNonMatchingLines;

        Main.setUpLogging();
        Main.random = new Random(Main.options.randomSeed);
        Main.graphExportFormatter = new DotExportFormatter();

        // Instantiate the parser and parse the log lines.
        TraceParser parser = new TraceParser(opts.regExps,
                opts.partitionRegExp, opts.separatorRegExp);

        List<EventNode> parsedEvents = Main.parseEvents(parser,
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
     * Main entrance into the application. Application arguments (args) are
     * processed using Synoptic's build-in argument parser, extended with a few
     * InvDFAMinimization-specific arguments. For more information, see
     * InvDFAMinimizationOptions class.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Set up Synoptic.
        InvariMintOptions opts = new InvariMintOptions(args);
        setUpLogging(opts);
        handleOptions(opts);
        ChainsTraceGraph inputGraph = setUpSynoptic(opts);

        TemporalInvariantSet minedInvs = Main.mineTOInvariants(false,
                inputGraph);

        PartitionGraph initialModel = new PartitionGraph(inputGraph, true,
                minedInvs);

        // Construct initial DFA from NIFby invariants.
        TemporalInvariantSet NIFbys = initialModel.getNIFbyInvariants();
        Set<EventType> allEvents = new HashSet<EventType>(
                initialModel.getEventTypes());

        EventTypeEncodings encodings = new EventTypeEncodings(allEvents);
        InvsModel dfa = getIntersectedModelFromInvs(NIFbys, encodings, true);

        applyInitialTerminalCondition(dfa, encodings);

        // Intersect with mined invariants.
        dfa.intersectWith(getIntersectedModelFromInvs(minedInvs, encodings,
                true));
        dfa.minimize();

        // removeSpuriousEdges(dfa, initialModel.getTraceGraph(), encodings,
        // initialEvent, terminalEvent);

        // Export final model.
        dfa.exportDotAndPng(opts.outputPathPrefix + ".invarimintDFA.dot");

        // compareTranslatedSynopticModel(initialModel, encodings, dfa);
    }

    public static void applyInitialTerminalCondition(EncodedAutomaton dfa,
            EventTypeEncodings encodings) {
        // Intersect with initial/terminal InvModel.
        EventType initialEvent = StringEventType.newInitialStringEventType();
        EventType terminalEvent = StringEventType.newTerminalStringEventType();
        InvModel initialTerminalInv = new InvModel(
                new TOInitialTerminalInvariant(initialEvent, terminalEvent,
                        TraceParser.defaultRelation), encodings);
        dfa.intersectWith(initialTerminalInv);
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
            boolean minimize) {
        // Initial model will accept all Strings.
        InvsModel model = new InvsModel(encodings);

        // Intersect provided invariants.
        for (ITemporalInvariant invariant : invariants) {
            InvModel invDFA = new InvModel(invariant, encodings);
            // if (invariant instanceof KTailInvariant) {
            // try {
            // invDFA.exportDotAndPng("/Users/ivan/synoptic/InvariMint/test-output/inv.dot");
            // } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // }
            model.intersectWith(invDFA);
        }

        if (minimize) {
            // Optimize by minimizing the model.
            model.minimize();
        }

        return model;
    }

    // TODO: just build the new dfa -- no seen map
    private static void removeSpuriousEdges(EncodedAutomaton dfa,
            ChainsTraceGraph g, EventTypeEncodings encodings,
            EventType initialEvent, EventType terminalEvent) {

        Map<Pair, Set<Character>> seenTransitions = new HashMap<Pair, Set<Character>>();
        EventNode initNode = g.getDummyInitialNode(TraceParser.defaultRelation);

        // Iterate through all the traces -- each transition from the INITIAL
        // node connects\holds a single trace.
        for (ITransition<EventNode> initTrans : initNode.getTransitions()) {
            EventNode curNode = initTrans.getTarget();

            // Set curState to the state immediately following the INITIAL
            // transition.
            State curState = dfa.getInitialState();
            curState = fetchDestination(curState, encodings, initialEvent,
                    seenTransitions);

            while (curNode.getTransitions().size() != 0) {

                curState = fetchDestination(curState, encodings,
                        curNode.getEType(), seenTransitions);

                if (curState == null) {
                    throw new IllegalStateException(
                            "Something bad has happened");
                }

                // Move on to the next node in the trace.
                curNode = curNode.getTransitions().get(0).getTarget();
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
    private static State replicate(Map<Pair, Set<Character>> seenTransitions,
            State current, Map<State, State> visited,
            EventTypeEncodings encodings) {

        if (visited.containsKey(current)) {
            return visited.get(current);
        }

        State replica = new State();
        replica.setAccept(current.isAccept());
        visited.put(current, replica);

        for (Transition t : current.getTransitions()) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                Pair curTransition = new Pair(current, t.getDest());
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
            Map<Pair, Set<Character>> seenTransitions) {

        for (Transition t : source.getTransitions()) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                if (currentEvent.toString().equals(encodings.getString(c))) {
                    Pair curTransition = new Pair(source, t.getDest());
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
    private static void compareTranslatedSynopticModel(
            PartitionGraph initialModel, EventTypeEncodings encodings,
            EncodedAutomaton dfa) throws IOException {

        // To compare, we'll translate and export the Synoptic model.
        // First run synoptic on the initial model, initial model becomes final
        // model.
        Bisimulation.splitPartitions(initialModel);
        Bisimulation.mergePartitions(initialModel);

        SynopticModel convertedDfa = new SynopticModel(initialModel, encodings);

        // This minimization step will first determinize the model -- from the
        // dk brics documentation.
        convertedDfa.minimize();

        // Print whether the language accepted by dfa is a subset of the
        // language accepted by synDfa and vice versa.
        System.out
                .println("Translated Synoptic DFA language a subset of DFAmin language: "
                        + convertedDfa.subsetOf(dfa));
        System.out
                .println("DFAmin language a subset of translated Synoptic DFA language: "
                        + dfa.subsetOf(convertedDfa));
    }
}