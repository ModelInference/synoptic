package algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import main.InvariMintOptions;
import model.EventTypeEncodings;
import model.InvsModel;

import synoptic.main.SynopticMain;
import synoptic.main.options.SynopticOptions;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.EventType;
import synoptic.model.export.DotExportFormatter;

/**
 * Provides an initial, all-accepting InvariMint model, and ChainsTraceGraph
 * creation for use in mining TO invariants.
 */
public class InvariMintInitializer {

    private ChainsTraceGraph traceGraph;

    private Set<EventType> allEvents;

    public static Logger logger;

    private InvariMintOptions opts;

    /**
     * Initializes the ChainsTraceGraph as well as the set of all event types
     * from the given log.
     * 
     * @param opts
     *            InvariMintOptions to create model with
     * @throws Exception
     */
    public InvariMintInitializer(InvariMintOptions opts) throws Exception {
        this.opts = opts;
        allEvents = new HashSet<EventType>();
        logger = Logger.getLogger("InvMintInitializer");

        // generates and initializes traceGraph field
        getSynopticChainsTraceGraph();

        // get all nodes from the traceGraph in order to add all events
        Set<EventNode> allNodes = traceGraph.getNodes();
        for (EventNode node : allNodes) {
            allEvents.add(node.getEType());
        }

    }

    /** Generate the traceGraph from input log files. */
    private void getSynopticChainsTraceGraph() throws Exception {
        // Set up options in Synoptic Main that are used by the library.
        SynopticOptions options = new SynopticOptions();
        options.logLvlExtraVerbose = true;
        options.internCommonStrings = true;
        options.recoverFromParseErrors = opts.recoverFromParseErrors;
        options.debugParse = opts.debugParse;
        options.ignoreNonMatchingLines = opts.ignoreNonMatchingLines;

        SynopticMain synMain = SynopticMain.getInstance();
        if (synMain == null) {
            synMain = new SynopticMain(options, new DotExportFormatter());
        }

        // Instantiate the parser and parse the log lines.
        TraceParser parser = new TraceParser(opts.regExps,
                opts.partitionRegExp, opts.separatorRegExp);

        List<EventNode> parsedEvents = SynopticMain.parseEvents(parser,
                opts.logFilenames);

        String errMsg = null;
        if (opts.debugParse) {
            // Terminate since the user is interested in debugging the parser.
            errMsg = "Terminating. To continue further, re-run without the debugParse option.";
        }

        if (!parser.logTimeTypeIsTotallyOrdered()) {
            errMsg = "Partially ordered log input detected. Stopping.";
        }

        if (parsedEvents.size() == 0) {
            errMsg = "Did not parse any events from the input log files. Stopping.";
        }

        if (errMsg != null) {
            logger.severe(errMsg);
            throw new Exception(errMsg);
        }

        // //////////////////
        traceGraph = parser.generateDirectTORelation(parsedEvents);
        // //////////////////

    }

    /**
     * Creates an all-accepting InvsModel from the events in the
     * ChainsTraceGraph
     * 
     * @return all-accepting model
     */
    public InvsModel createAllAcceptingModel() {
        logger.fine("Creating EventType encoding.");
        EventTypeEncodings encodings = new EventTypeEncodings(allEvents);
        logger.fine("Creating an initial, all-accepting, model.");
        return new InvsModel(encodings);

    }

    public ChainsTraceGraph getChainsTraceGraph() {
        return traceGraph;
    }

}
