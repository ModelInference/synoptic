package synoptic.main;

import java.util.List;

import synoptic.main.options.AbstractOptions;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;

public abstract class CommandlineMain extends AbstractMain {

    @Override
    protected ChainsTraceGraph makeTraceGraph() throws Exception {
        TraceParser parser = new TraceParser(options.regExps,
                AbstractOptions.partitionRegExp,
                AbstractOptions.separatorRegExp, options.dateFormat);
        List<EventNode> parsedEvents;
        try {
            parsedEvents = parseEvents(parser,
                    AbstractOptions.plumeOpts.logFilenames);
        } catch (ParseException e) {
            logger.severe(
                    "Caught ParseException -- unable to continue, exiting. Try cmd line option:\n\t"
                            + AbstractOptions.plumeOpts.getOptDesc("help"));
            logger.severe(e.toString());
            return null;
        }

        if (options.debugParse) {
            // Terminate since the user is interested in debugging the parser.
            logger.info(
                    "Terminating. To continue further, re-run without the debugParse option.");
            return null;
        }

        // PO Logs are processed differently.
        if (!parser.logTimeTypeIsTotallyOrdered()) {
            logger.warning(
                    "Partially ordered log input detected. Only mining invariants since refinement/coarsening is not yet supported.");
            processPOLog(parser, parsedEvents);
            return null;
        }

        if (parsedEvents.size() == 0) {
            logger.severe(
                    "Did not parse any events from the input log files. Stopping.");
            return null;
        }

        return genChainsTraceGraph(parser, parsedEvents);
    }
}
