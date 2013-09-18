package algorithms;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import main.InvariMintOptions;
import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.InvsModel;
import model.PartitionGraphAutomaton;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.main.SynopticMain;
import synoptic.main.options.SynopticOptions;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.PartitionGraph;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.export.DotExportFormatter;
import synoptic.model.export.GraphExporter;

/**
 * Represent an InvariMint algorithm that models a PartitionGraph-based
 * algorithm, such as kTails and Synoptic.
 */
public abstract class PGraphInvariMint {
    String stdAlgName;
    String invMintAlgName;

    public static Logger logger;
    InvariMintOptions opts;
    ChainsTraceGraph traceGraph;

    // The final partition graph derived with the standard algorithm.
    PartitionGraph stdAlgPGraph;

    PartitionGraphAutomaton stdAlgDFA;
    EventTypeEncodings encodings;
    InvsModel invMintModel;
    TemporalInvariantSet minedInvs;
    EventType initialEvent;
    EventType terminalEvent;

    public PGraphInvariMint(InvariMintOptions opts, String stdAlgName)
            throws Exception {
        this.stdAlgName = stdAlgName;
        this.invMintAlgName = "InvMint" + stdAlgName;
        logger = Logger.getLogger(invMintAlgName);

        initialEvent = StringEventType.newInitialStringEventType();
        terminalEvent = StringEventType.newTerminalStringEventType();

        this.opts = opts;
        traceGraph = getSynopticChainsTraceGraph();
        assert traceGraph != null;
    }

    // ///////////////////////////// Public methods.

    /**
     * Executes the InvariMint algorithm. Implemented by sub-classes.
     */
    public abstract InvsModel runInvariMint() throws Exception;

    public String getInvMintAlgName() {
        return invMintAlgName;
    }

    public String getStdAlgName() {
        return stdAlgName;
    }

    /** Removes spurious edges from the model. */
    public void removeSpuriousEdges() {
        assert initialEvent != null;
        assert terminalEvent != null;

        logger.info("Removing spurious edges");
        TraceFiltering.removeSpuriousEdges(invMintModel, traceGraph, encodings,
                initialEvent, terminalEvent);
    }

    /** Exports the partition graph inferred by the standard algorithm. */
    public void exportStdAlgPGraph() throws IOException {
        if (stdAlgPGraph == null) {
            runStdAlg();
        }
        assert stdAlgPGraph != null;

        String exportPrefix = opts.outputPathPrefix + "." + stdAlgName
                + ".pGraph-final.dot";
        GraphExporter.exportGraph(exportPrefix, stdAlgPGraph, false);
        GraphExporter.generatePngFileFromDotFile(exportPrefix);
    }

    /** Translates the standard algorithm partition graph into a DFA. */
    public void exportStdAlgDFA() throws Exception {
        stdAlgpGraphToDFA();
        assert stdAlgDFA != null;

        String exportPrefix = opts.outputPathPrefix + "." + stdAlgName
                + ".dfa.dot";
        stdAlgDFA.exportDotAndPng(exportPrefix);
    }

    /**
     * Compares the InvariMint-derived model to the model derived using the
     * Standard algorithm.
     */
    public boolean compareToStandardAlg() {
        stdAlgpGraphToDFA();
        assert stdAlgDFA != null;

        boolean stdSubset = stdAlgDFA.subsetOf(invMintModel);
        boolean invSubset = invMintModel.subsetOf(stdAlgDFA);

        logger.info("L(stdAlgDFA) subsetOf L(invMintDFA): " + stdSubset);
        logger.info("L(invMintDFA) subsetOf L(stdAlgDFA): " + invSubset);

        EncodedAutomaton modelDiff = null;
        String exportDiffFname = "";
        if (stdSubset && !invSubset) {
            // Output traces in invMintDFA that are not in stdAlgDFA:
            modelDiff = invMintModel.differenceWith(stdAlgDFA);
            exportDiffFname = opts.outputPathPrefix + "." + "InvMint-Std"
                    + ".dfa.dot";
        } else if (!stdSubset && invSubset) {
            // Output traces in stdAlgDFA that are not in invMintDFA:
            modelDiff = invMintModel.differenceWith(stdAlgDFA);
            exportDiffFname = opts.outputPathPrefix + "." + "Std-InvMint"
                    + ".dfa.dot";
        }

        if (modelDiff != null) {
            assert (exportDiffFname != "");
            try {
                modelDiff.exportDotAndPng(exportDiffFname);
            } catch (IOException e) {
                logger.info("Unable to export model difference.");
            }
        }

        return stdSubset && invSubset;
    }

    /**
     * Mine invariants from the given input graph using a specific miner.
     */
    protected TemporalInvariantSet mineInvariants(ITOInvariantMiner miner) {
        long startTime = System.currentTimeMillis();
        logger.info("Mining invariants [" + miner.getClass().getName() + "]..");

        TemporalInvariantSet invs = miner.computeInvariants(traceGraph, false);

        long endTime = System.currentTimeMillis();
        logger.info("Mining took " + (endTime - startTime) + "ms");

        logger.fine("Mined " + invs.numInvariants() + " invariant(s).");
        // logger.fine(invs.toPrettyString());

        return invs;
    }

    // ///////////////////////////// Protected methods.

    /** Executes the standard algorithm and sets stdAlgPGraph. */
    public abstract void runStdAlg();

    // ///////////////////////////// Private methods.

    /**
     * If this hasn't been done before, convert and cache the DFA-version of the
     * partition graph inferred by the standard algorithm.
     */
    private void stdAlgpGraphToDFA() {
        if (stdAlgPGraph == null) {
            runStdAlg();
        }
        assert stdAlgPGraph != null;

        if (stdAlgDFA == null) {
            stdAlgDFA = new PartitionGraphAutomaton(stdAlgPGraph, encodings);
        }
    }

    /** Generate the traceGraph from input log files. */
    private ChainsTraceGraph getSynopticChainsTraceGraph() throws Exception {
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

        return traceGraph;
    }
}
