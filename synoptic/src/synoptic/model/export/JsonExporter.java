package synoptic.model.export;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

/**
 * Outputs a partition graph as a JSON object. Uses the JSON-simple library,
 * licensed under Apache 2.0 (the same license as Synoptic and its
 * sub-projects), available at https://code.google.com/p/json-simple/.
 * 
 */
public class JsonExporter {

    /**
     * Each event mapped to its relevant JSON information, the trace ID and its
     * index within the trace
     */
    private static Map<EventNode, EventInstance> eventMap = new HashMap<EventNode, EventInstance>();

    /**
     * Simple pair of a trace ID and an event index within the trace to uniquely
     * identify a specific event instance/node
     */
    private static class EventInstance {
        public int traceID;
        public int eventIndexWithinTrace;

        public EventInstance(int traceID, int eventIndexWithinTrace) {
            this.traceID = traceID;
            this.eventIndexWithinTrace = eventIndexWithinTrace;
        }
    }

    /**
     * Export the JSON object representation of the partition graph pGraph to
     * the filename specified
     * 
     * @param baseFilename
     *            The filename to which the JSON object should be written sans
     *            file extension
     * @param graph
     *            The partition graph to output
     */
    public static <T extends INode<T>> void exportJsonObject(
            String baseFilename, IGraph<T> graph) {

        // The graph must be a partition graph
        assert graph instanceof PartitionGraph;
        PartitionGraph pGraph = (PartitionGraph) graph;

        Map<String, Object> finalModelMap = new LinkedHashMap<String, Object>();

        // Add log to final model map
        List<Map<String, Object>> logListOfTraces = makeLogJSON(pGraph);
        finalModelMap.put("log", logListOfTraces);

        // Add partitions to final model map
        List<Map<String, Object>> partitionList = makePartitionsJSON(pGraph);
        finalModelMap.put("partitions", partitionList);

        // Add invariants to final model map
        List<Map<String, Object>> invariantList = makeInvariantsJSON(pGraph);
        finalModelMap.put("invariants", invariantList);

        // Output the final model map as a JSON object
        try {
            PrintWriter output = new PrintWriter(baseFilename + ".json");
            JSONValue.writeJSONString(finalModelMap, output);
            output.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the 'log' of the JSON object: a list of traces within the log of
     * this partition graph
     * 
     * @param pGraph
     *            The partition graph whose log we're outputting
     */
    private static List<Map<String, Object>> makeLogJSON(PartitionGraph pGraph) {
        // The log (list of traces) to go into the JSON object
        List<Map<String, Object>> logListOfTraces = new LinkedList<Map<String, Object>>();

        // Get all partitions in the partition graph
        Set<Partition> allPartitions = pGraph.getNodes();

        // Get the INITIAL partition, which will be used to retrieve all traces
        // and their events
        Partition initialPart = null;
        for (Partition part : allPartitions) {
            if (part.isInitial()) {
                initialPart = part;
                break;
            }
        }

        // There must have been an INITIAL partition found
        assert initialPart != null;
        if (initialPart == null) {
            return null;
        }

        // Follow all traces and store them in the log list of traces
        int traceID = 0;
        for (EventNode startingEvent : initialPart.getEventNodes().iterator()
                .next().getAllSuccessors()) {
            // One trace, contains the trace number and a list of events
            Map<String, Object> singleTraceMap = new LinkedHashMap<String, Object>();
            // List of events
            List<Map<String, Object>> singleTraceEventsList = new LinkedList<Map<String, Object>>();

            int eventIndexWithinTrace = 0;
            for (EventNode event = startingEvent; !event.isTerminal(); event = event
                    .getAllSuccessors().iterator().next()) {
                // One event, contains event index, event type, and timestamp
                Map<String, Object> singleEventMap = new LinkedHashMap<String, Object>();

                // Populate this event's index within the trace and its type
                singleEventMap.put("eventIndex", eventIndexWithinTrace);
                EventType evType = event.getEType();
                singleEventMap.put("eventType", evType.toString());

                // Populate this event's time if it's not INITIAL or TERMINAL
                if (!evType.isSpecialEventType()) {
                    singleEventMap.put("timestamp", event.getTime());
                }

                // Add this event to this trace's list of events
                singleTraceEventsList.add(singleEventMap);

                // Record this event's event instance information to ease the
                // creation of the partition part of the JSON later
                eventMap.put(event, new EventInstance(traceID,
                        eventIndexWithinTrace++));
            }

            // Populate the single trace
            singleTraceMap.put("traceID", traceID++);
            singleTraceMap.put("events", singleTraceEventsList);

            // Put the trace into the log's list of traces
            logListOfTraces.add(singleTraceMap);
        }

        return logListOfTraces;
    }

    /**
     * Creates the 'partitions' of the JSON object: a list of partitions within
     * this partition graph
     * 
     * @param pGraph
     *            The partition graph whose partitions we're outputting
     */
    private static List<Map<String, Object>> makePartitionsJSON(
            PartitionGraph pGraph) {
        // The list of partitions to go into the JSON object
        List<Map<String, Object>> partitionsList = new LinkedList<Map<String, Object>>();

        // Get all partitions in the partition graph
        Set<Partition> allPartitions = pGraph.getNodes();

        PartitionLoop:
        for (Partition partition : allPartitions) {
            // One partition, contains event type and list of events
            Map<String, Object> singlePartitionMap = new LinkedHashMap<String, Object>();

            // This partition's list of events it contains
            List<Map<String, Object>> singlePartitionEventList = new LinkedList<Map<String, Object>>();

            // Populate this partition's event type
            EventType evType = partition.getEType();
            singlePartitionMap.put("eventType", evType.toString());

            for (EventNode event : partition.getEventNodes()) {
                // One event, contains trace ID and index within the trace
                Map<String, Object> singleEventMap = new LinkedHashMap<String, Object>();

                // Get the event instance info required to identify this event
                // within the JSON object
                EventInstance evInstance = eventMap.get(event);

                if (evType.isSpecialEventType()) {
                    continue PartitionLoop;
                }

                // Populate this event's trace ID and index within the trace
                singleEventMap.put("traceID", evInstance.traceID);
                singleEventMap.put("eventIndex",
                        evInstance.eventIndexWithinTrace);

                // Put the event into the partition's list of events
                singlePartitionEventList.add(singleEventMap);
            }

            // Store this partition's list of events
            singlePartitionMap.put("events", singlePartitionEventList);

            // Put the partition into the full list of partitions
            partitionsList.add(singlePartitionMap);
        }

        return partitionsList;
    }

    /**
     * Creates the 'invariants' of the JSON object: a list of the invariants
     * used to construct the partition graph
     * 
     * @param pGraph
     *            The partition graph made using the invariants we're outputting
     */
    private static List<Map<String, Object>> makeInvariantsJSON(
            PartitionGraph pGraph) {
        // The list of invariants to go into the JSON object
        List<Map<String, Object>> invariantsList = new LinkedList<Map<String, Object>>();

        // Get all invariants in the partition graph
        TemporalInvariantSet allInvariants = pGraph.getInvariants();

        for (ITemporalInvariant inv : allInvariants) {
            // One invariant, contains type, predicates, constraint, and bounds
            Map<String, Object> singleInvariantMap = new LinkedHashMap<String, Object>();

            // Store the invariant type
            singleInvariantMap.put("invariantType", inv.getLongName());

            // Store the predicates
            List<String> predicateList = new LinkedList<String>();
            for (EventType evType : inv.getPredicates()) {
                predicateList.add(evType.toString());
            }
            singleInvariantMap.put("predicates", predicateList);

            if (inv instanceof TempConstrainedInvariant) {
                TempConstrainedInvariant<?> constInv = (TempConstrainedInvariant<?>) inv;

                // Store the constraints with bounds
                List<String> constraintBoundList = new LinkedList<String>();
                constraintBoundList.add(constInv.getConstraint().toString());
                singleInvariantMap.put("constraints", constraintBoundList);
            }

            // Put the invariant map into the list of invariants
            invariantsList.add(singleInvariantMap);
        }

        return invariantsList;
    }
}
