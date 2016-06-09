package synoptic.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import synoptic.main.options.AbstractOptions;
import synoptic.main.options.SynopticOptions;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.GenChainsTraceGraph;
import synoptic.model.PartitionGraph;
import synoptic.model.Relation;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.GenericEventType;
import synoptic.model.export.GenericExporter;
import synoptic.model.export.types.SynGraph;
import synoptic.model.interfaces.ISynType;
import synoptic.util.InternalSynopticException;
import synoptic.util.resource.ITotalResource;

/**
 * The library version of Synoptic. Most users should only need to run the
 * {@link #inferModel} method TODO:
 */
public class SynopticLib<T extends Comparable<T> & ISynType<T>>
        extends AbstractMain {

    private List<List<T>> rawTraces = null;

    // Maps a trace ID to events within that trace
    Map<Integer, List<EventNode>> traces = new LinkedHashMap<>();

    // EventNode -> Relation associated with this event node.
    Map<EventNode, Set<Relation>> allEventRelations = new HashMap<>();

    /**
     * Return the singleton instance of SynopticLib, first asserting that the
     * instance isn't null
     */
    public static SynopticLib getInstance() {
        assert (instance != null);
        assert (instance instanceof SynopticLib);
        return (SynopticLib) instance;
    }

    /**
     * Perform the Synoptic inference algorithm. See user documentation for an
     * explanation of the options
     */
    public static <T extends Comparable<T> & ISynType<T>> SynGraph<T> inferModel(
            SynopticOptions synOptions, List<List<T>> traces) throws Exception {
        // Construct main object
        AbstractOptions options = synOptions.toAbstractOptions();
        options.keepOrder = true;
        AbstractOptions.outputPathPrefix = null;
        SynopticLib<T> mainInstance = new SynopticLib<>(options);

        mainInstance.rawTraces = traces;

        try {
            Locale.setDefault(Locale.US);

            PartitionGraph pGraph = mainInstance.createInitialPartitionGraph();
            if (pGraph != null) {
                mainInstance.runSynoptic(pGraph);
            }
            SynGraph<T> synGraph = GenericExporter.makeSynGraph(pGraph);
            instance = null;
            return synGraph;
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw InternalSynopticException.wrap(e);
        }
    }

    /**
     * Constructor that simply stores parameters in fields and initializes the
     * pseudo RNG
     * 
     * @param opts
     *            Processed options from the command line
     */
    public SynopticLib(AbstractOptions opts) {
        setUpLogging(opts);

        if (AbstractMain.instance != null) {
            throw new RuntimeException(
                    "Cannot create multiple instances of singleton synoptic.main.AbstractMain");
        }
        this.options = opts;
        this.random = new Random(opts.randomSeed);
        logger.info("Using random seed: " + opts.randomSeed);
        traces = new HashMap<>();
        AbstractMain.instance = this;
    }

    /**
     * 
     */
    @Override
    protected ChainsTraceGraph makeTraceGraph() throws Exception {
        List<EventNode> allEvents = wrapTracesAsEvents();

        if (allEvents.size() == 0) {
            logger.severe("Input traces were empty or invalid. Stopping.");
            return null;
        }

        return makeChainsTraceGraph(allEvents);
    }

    /**
     * 
     */
    private List<EventNode> wrapTracesAsEvents() {
        long startTime = loggerInfoStart(
                "Converting traces to Synoptic events...");

        List<EventNode> allEvents = new ArrayList<EventNode>();
        int resourceVal = 0; // TODO: is this necessary?
        int traceId = 0;

        // Set up single relation that will be shared by all events
        Set<Relation> relations = new HashSet<>();
        Relation timeRelation = new Relation("time-relation",
                Event.defTimeRelationStr, false);
        relations.add(timeRelation);

        for (List<T> rawTrace : rawTraces) {
            // Each event
            for (T rawEvent : rawTrace) {

                EventType eType = new GenericEventType<T>(rawEvent);
                Event event = new Event(eType, "", "", 0);
                event.setTime(new ITotalResource(resourceVal++));

                // Add event to the current trace
                EventNode eventNode = addEventNodeToPartition(event, traceId);

                // We want to add event relations ONLY IF eventNode actually
                // represents an event, not a dummy state
                if (!eventNode.getEType().isSpecialEventType()) {
                    allEventRelations.put(eventNode, relations);
                }

                allEvents.add(eventNode);
            }
            traceId++;
        }

        loggerInfoEnd("Converting traces took ", startTime);
        return allEvents;
    }

    /**
     * 
     */
    private EventNode addEventNodeToPartition(Event event, int traceId) {
        EventNode eventNode = new EventNode(event);

        List<EventNode> events = traces.get(traceId);

        if (events == null) {
            events = new ArrayList<>();
            traces.put(traceId, events);
            logger.fine("Created trace '" + traceId + "'");
        }
        eventNode.setTraceID(traceId);

        // We want to add eventNode to partitions ONLY IF event actually
        // represents an event, not a dummy for state.
        if (!event.getEType().isSpecialEventType()) {
            events.add(eventNode);
        }
        return eventNode;
    }

    /**
     * 
     */
    private GenChainsTraceGraph makeChainsTraceGraph(
            List<EventNode> parsedEvents) throws ParseException {
        long startTime = loggerInfoStart(
                "Generating inter-event temporal relation...");
        GenChainsTraceGraph inputGraph = generateDirectTORelation(parsedEvents);
        loggerInfoEnd("Generating temporal relation took ", startTime);
        return inputGraph;
    }

    /**
     * 
     */
    private GenChainsTraceGraph generateDirectTORelation(
            List<EventNode> allEvents) throws ParseException {
        //
        GenChainsTraceGraph graph = new GenChainsTraceGraph(allEvents);
        for (List<EventNode> trace : traces.values()) {
            graph.addTrace(trace, allEventRelations);
        }
        return graph;
    }
}