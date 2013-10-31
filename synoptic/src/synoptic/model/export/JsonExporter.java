package synoptic.model.export;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;

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
 * @author Tony Ohmann (ohmann@cs.umass.edu)
 * @param <T>
 *            The node type of the partition graph.
 */
public class JsonExporter {

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

        // Add invariants to final model map

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
     * Creates a JSON sub-object for the log containing a list of traces
     * 
     * @param pGraph
     *            The partition graph whose log we're outputting
     */
    private static List<Map<String, Object>> makeLogJSON(PartitionGraph pGraph) {
        // The log (list of traces) to go into the JSON object
        List<Map<String, Object>> logListOfTraces = new LinkedList<Map<String, Object>>();

        Set<Partition> allPart = pGraph.getNodes();
        Partition initialPart = null;
        for (Partition part : allPart) {
            if (part.isInitial()) {
                initialPart = part;
                break;
            }
        }

        int traceNum = 0;
        for (EventNode startingEvent : initialPart.getEventNodes().iterator()
                .next().getAllSuccessors()) {
            // One trace, contains the trace number and a list of events
            Map<String, Object> singleTraceMap = new LinkedHashMap<String, Object>();
            // List of events
            List<Map<String, Object>> singleTraceEventsList = new LinkedList<Map<String, Object>>();

            for (EventNode event = startingEvent; !event.isTerminal(); event = event
                    .getAllSuccessors().iterator().next()) {
                // One event, contains the event type and the timestamp
                Map<String, Object> singleEventMap = new LinkedHashMap<String, Object>();

                EventType evType = event.getEType();

                // Populate this event's type
                singleEventMap.put("eventType", evType.toString());

                // Populate this event's time if it's not INITIAL or TERMINAL
                if (!evType.isSpecialEventType()) {
                    singleEventMap.put("timestamp", event.getTime());
                }

                singleTraceEventsList.add(singleEventMap);
            }

            // Populate the single trace
            singleTraceMap.put("traceID", traceNum++);
            singleTraceMap.put("events", singleTraceEventsList);

            // Put the trace into the log's list of traces
            logListOfTraces.add(singleTraceMap);
        }

        return logListOfTraces;
    }
}
