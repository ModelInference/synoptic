package synoptic.model.export;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.json.simple.JSONValue;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.EventNode;
import synoptic.model.PartitionGraph;
import synoptic.model.event.EventType;
import synoptic.model.export.types.EvBasedEdge;
import synoptic.model.export.types.EvBasedGraph;
import synoptic.model.export.types.EvBasedNode;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

/**
 * Outputs a partition graph as a JSON object. Uses the JSON-simple library,
 * licensed under Apache 2.0 (the same license as Synoptic and its
 * sub-projects), available at https://code.google.com/p/json-simple/.
 */
public class JsonExporter {

    // ID variable for the entire class
    private static int globalID = 0;
    private static final String initial = "INITIAL";
    private static final String terminal = "TERMINAL";
    // Map of nodes and their respective global ID's
    private static Map<EvBasedNode, Object> nodesIDMap = null;
    // Map of edges and their respective global ID's
    private static Map<EvBasedEdge, Integer> edgesIDMap = null;
    // Map of displayables and their respective global ID's
    private static Map<String, Integer> displayablesIDMap = null;
    // Map of event types and their respective global ID's
    private static Map<String, Integer> eventTypesIDMap = null;
    // Map of events and their respective global ID's
    private static Map<EventNode, Integer> eventsIDMap = null;
    // Map of events and their respective global ID's
    private static Map<String, Integer> invariantTypesIDMap = null;
    // Map of log statements and their respective global ID's
    private static Map<String, Integer> logStatementsIDMap = null;

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

        EvBasedGraph evGraph = new EvBasedGraph(pGraph);

        Map<String, Object> finalModelMap = new LinkedHashMap<>();

        // Add displayables to final model map
        List<Map<String, Object>> displayablesList = makeDisplayablesJSON(
                evGraph);
        finalModelMap.put("displayables", displayablesList);

        // Add nodes to the final model map
        List<Map<String, Object>> nodesList = makeNodesJSON(evGraph);
        finalModelMap.put("nodes", nodesList);

        // Add edges to final model map
        List<Map<String, Object>> edgesList = makeEdgesJSON(evGraph);
        finalModelMap.put("edges", edgesList);

        // Add event types to final model map
        List<Map<String, Object>> eventTypesList = makeEventTypesJSON(evGraph);
        finalModelMap.put("eventTypes", eventTypesList);

        // Add events to final model map
        List<Map<String, Object>> eventsList = makeEventsJSON(evGraph);
        finalModelMap.put("events", eventsList);

        // Add invariant types to final model map
        List<Map<String, Object>> invariantTypesList = makeInvariantTypesJSON(
                pGraph);
        finalModelMap.put("invariantTypes", invariantTypesList);

        // Add invariants to final model map
        List<Map<String, Object>> invariantsList = makeInvariantsJSON(pGraph);
        finalModelMap.put("invariants", invariantsList);

        // Add log statements to final model map
        List<Map<String, Object>> logStatementsList = makeLogStatementsJSON(
                evGraph);
        finalModelMap.put("logStatements", logStatementsList);

        // Add links to final model map
        List<Map<String, Object>> linksList = makeLinksJSON(evGraph);
        finalModelMap.put("links", linksList);

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


    public static <T extends INode<T>> void exportJsonObject(IGraph<T> graph) {

        // The graph must be a partition graph
        assert graph instanceof PartitionGraph;
        PartitionGraph pGraph = (PartitionGraph) graph;

        EvBasedGraph evGraph = new EvBasedGraph(pGraph);

        Map<String, Object> finalModelMap = new LinkedHashMap<>();

        // Add displayables to final model map
        List<Map<String, Object>> displayablesList = makeDisplayablesJSON(
                evGraph);
        finalModelMap.put("displayables", displayablesList);

        // Add nodes to the final model map
        List<Map<String, Object>> nodesList = makeNodesJSON(evGraph);
        finalModelMap.put("nodes", nodesList);

        // Add edges to final model map
        List<Map<String, Object>> edgesList = makeEdgesJSON(evGraph);
        finalModelMap.put("edges", edgesList);

        // Add event types to final model map
        List<Map<String, Object>> eventTypesList = makeEventTypesJSON(evGraph);
        finalModelMap.put("eventTypes", eventTypesList);

        // Add events to final model map
        List<Map<String, Object>> eventsList = makeEventsJSON(evGraph);
        finalModelMap.put("events", eventsList);

        // Add invariant types to final model map
        List<Map<String, Object>> invariantTypesList = makeInvariantTypesJSON(
                pGraph);
        finalModelMap.put("invariantTypes", invariantTypesList);

        // Add invariants to final model map
        List<Map<String, Object>> invariantsList = makeInvariantsJSON(pGraph);
        finalModelMap.put("invariants", invariantsList);

        // Add log statements to final model map
        List<Map<String, Object>> logStatementsList = makeLogStatementsJSON(
                evGraph);
        finalModelMap.put("logStatements", logStatementsList);

        // Add links to final model map
        List<Map<String, Object>> linksList = makeLinksJSON(evGraph);
        finalModelMap.put("links", linksList);

    }

    /**
     * Creates the 'nodes' of the JSON object: a list of nodes within this
     * EvBasedGraph
     * 
     * @param evGraph
     *            The EvBasedGraph whose nodes we're outputting
     */

    public static List<Map<String, Object>> makeNodesJSON(
            EvBasedGraph evGraph) {

        nodesIDMap = new LinkedHashMap<>();

        assert (displayablesIDMap != null) : "displayablesIDMap is null";

        // List of nodes to go into the JSON object
        List<Map<String, Object>> nodeList = new LinkedList<>();

        // Add the initial node first
        Map<String, Object> initNodeMap = makeNode(evGraph, evGraph.getInitialNode());
        nodeList.add(initNodeMap);

        // Get all the nodes in the evGraph
        for (EvBasedNode node : evGraph.getNodes()) {

            if (node != evGraph.getInitialNode() && node != evGraph.getTerminalNode()) {

                Map<String, Object> nodeMap = makeNode(evGraph, node);
                nodeList.add(nodeMap);
             }
        }

        // Add the terminal node last
        Map<String, Object> termNodeMap = makeNode(evGraph,
                evGraph.getTerminalNode());
        nodeList.add(termNodeMap);

        return nodeList;
    }

    /**
     * Processes nodes for makeNodesJSON by adding their ID and displayable
     * value if applicable
     * 
     * @param evGraph
     *            The EvBasedGraph whose nodes we're processing
     * @param node
     *            The particular node being processed
     */
    public static Map<String, Object> makeNode(EvBasedGraph evGraph,
            EvBasedNode node) {
        Map<String, Object> nodeMap = new LinkedHashMap<>();
        nodeMap.put("id", globalID);
        nodesIDMap.put(node, globalID);
        if (node == evGraph.getInitialNode()) {
            List<Integer> displayableList = new LinkedList<>();
            displayableList.add(displayablesIDMap.get(initial));
            nodeMap.put("displayableIDs", displayableList);
        }
        if (node == evGraph.getTerminalNode()) {
            List<Integer> displayableList = new LinkedList<>();
            displayableList.add(displayablesIDMap.get(terminal));
            nodeMap.put("displayableIDs", displayableList);
        }
        globalID++;
        return nodeMap;
    }

    /**
     * Creates the 'edges' of the JSON object: a list of edges within this
     * EvBasedGraph
     * 
     * @param evGraph
     *            The EvBasedGraph whose edges we're outputting
     */
    public static List<Map<String, Object>> makeEdgesJSON(
            EvBasedGraph evGraph) {

        edgesIDMap = new LinkedHashMap<>();

        assert (nodesIDMap != null
                && displayablesIDMap != null) : "Either one or both of nodesIDMap and displayablesIDMap is null";

        // List of edges and info to go into the JSON Object
        List<Map<String, Object>> edgesList = new LinkedList<>();

        // Contains all edges used to avoid duplicates
        Set<EvBasedEdge> usedEdges = new HashSet<>();

        for (EvBasedNode node : evGraph.getNodes()) {

            for (EvBasedEdge edge : node.outEdges) {

                Map<String, Object> edgesMap = new LinkedHashMap<>();

                // Check to see if it is a duplicate
                if (!usedEdges.contains(edge)) {

                    edgesMap.put("id", globalID);
                    edgesMap.put("srcNodeID", nodesIDMap.get(edge.srcNode));
                    edgesMap.put("destNodeID", nodesIDMap.get(edge.destNode));

                    if (edge.eType.getETypeLabel() != initial
                            && edge.eType.getETypeLabel() != terminal) {

                        String displayableValue;

                        if (edge.resMin == null && edge.resMax == null) {
                            displayableValue = edge.eType.getETypeLabel();
                        } else {
                            displayableValue = edge.eType.getETypeLabel() + " ["
                                    + edge.resMin + ", " + edge.resMax + "]";
                        }
                        List<Integer> displayableList = new LinkedList<>();
                        displayableList.add(displayablesIDMap.get(displayableValue));
                        edgesMap.put("displayableIDs", displayableList);
                    }

                    edgesIDMap.put(edge, globalID);
                    edgesList.add(edgesMap);
                    usedEdges.add(edge);

                    globalID++;
                }
            }
        }
        return edgesList;
    }

    /**
     * Creates the 'displayables' of the JSON object: a list of displayables
     * within this EvBasedGraph
     * 
     * @param evGraph
     *            The EvBasedGraph whose displayables we're outputting
     */
    public static List<Map<String, Object>> makeDisplayablesJSON(
            EvBasedGraph evGraph) {

        displayablesIDMap = new LinkedHashMap<>();

        // List of edges and info to go into the JSON Object
        List<Map<String, Object>> displayablesList = new LinkedList<>();

        // Contains all the edges to avoid duplicates
        Set<EvBasedEdge> usedEdges = new HashSet<>();

        for (EvBasedNode node : evGraph.getNodes()) {

            for (EvBasedEdge edge : node.outEdges) {

                Map<String, Object> displayablesMap = new LinkedHashMap<>();

                // Check for duplicate
                if (!usedEdges.contains(edge)) {

                    // Make the string to go into displayableValue
                    String displayableValue = edge.eType.getETypeLabel() + " ["
                            + edge.resMin + ", " + edge.resMax + "]";
                    if (edge.resMin == null && edge.resMax == null) {
                        if (displayablesIDMap.containsKey(initial)
                                && edge.eType.getETypeLabel() == initial) {
                            displayableValue = terminal;
                        } else {
                            displayableValue = edge.eType.getETypeLabel();
                        }
                    }

                    displayablesMap.put("id", globalID);
                    displayablesMap.put("displayableValue", displayableValue);
                    displayablesIDMap.put(displayableValue, globalID);
                    displayablesList.add(displayablesMap);
                    usedEdges.add(edge);
                    globalID++;
                }
            }
        }

        if(!displayablesIDMap.containsKey(terminal)) {
            Map<String, Object> displayablesMap = new LinkedHashMap<>();
            displayablesMap.put("id", globalID);
            displayablesMap.put("displayableValue", terminal);
            displayablesIDMap.put(terminal, globalID);
            displayablesList.add(displayablesMap);
            globalID++;
        }

        return displayablesList;
    }

    /**
     * Creates the 'eventTypes' of the JSON object: a list of event types within
     * this EvBasedGraph
     * 
     * @param evGraph
     *            The EvBasedGraph whose event types we're outputting
     */
    public static List<Map<String, Object>> makeEventTypesJSON(
            EvBasedGraph evGraph) {

        eventTypesIDMap = new LinkedHashMap<>();

        // The list of event types to go into the JSON object
        List<Map<String, Object>> eventTypesList = new LinkedList<>();

        for (EvBasedNode node : evGraph.getNodes()) {

            for (EvBasedEdge edge : node.outEdges) {

                Map<String, Object> eventTypesMap = new LinkedHashMap<>();

                if (!eventTypesIDMap.containsKey(edge.eType.toString())
                        && !edge.eType.isSpecialEventType()) {
                    eventTypesMap.put("id", globalID);
                    eventTypesMap.put("label", edge.eType.toString());
                    eventTypesList.add(eventTypesMap);
                    eventTypesIDMap.put(edge.eType.toString(), globalID);
                    globalID++;
                }
            }
        }

        return eventTypesList;
    }

    /**
     * Creates the 'events' of the JSON object: a list of events within this
     * EvBasedGraph
     * 
     * @param evGraph
     *            The EvBasedGraph whose events we're outputting
     */
    public static List<Map<String, Object>> makeEventsJSON(
            EvBasedGraph evGraph) {

        eventsIDMap = new LinkedHashMap<>();

        assert (eventTypesIDMap != null) : "eventTypesIDMap is null";

        // The events to go into the JSON object
        List<Map<String, Object>> eventsList = new LinkedList<>();

        EvBasedNode initialNode = evGraph.getInitialNode();
        Set<EvBasedEdge> iniOutEdges = initialNode.outEdges;
        // Set<EventNode> iniOutEvents = iniOutEdges.events;

        int traceID = 0;
        for (EventNode startingEvent : iniOutEdges.iterator().next().events
                .iterator().next().getAllSuccessors()) {
            int traceIndex = 0;

            for (EventNode event = startingEvent; !event
                    .isTerminal(); event = event.getAllSuccessors().iterator()
                            .next()) {

                Map<String, Object> singleEventMap = new LinkedHashMap<>();

                singleEventMap.put("id", globalID);
                singleEventMap.put("traceID", traceID);
                singleEventMap.put("traceIndex", traceIndex);
                EventType evType = event.getEType();
                singleEventMap.put("eventTypeID",
                        eventTypesIDMap.get(evType.toString()));
                eventsIDMap.put(event, globalID);

                // Populate this event's time if it's not INITIAL or TERMINAL
                // if (!evType.isSpecialEventType()) {
                //     singleEventMap.put("timestamp", event.getTime());
                // }
                eventsList.add(singleEventMap);
                // eventMap.put(event, new EventInstance(traceID,
                // traceIndex++));
                traceIndex++;
                globalID++;
            }

            traceID++;
        }
        return eventsList;
    }

    /**
     * Creates the 'invariantTypes' of the JSON object: a list of invariant
     * types within this EvBasedGraph
     * 
     * @param evGraph
     *            The EvBasedGraph whose invariant types we're outputting
     */
    public static List<Map<String, Object>> makeInvariantTypesJSON(
            PartitionGraph pGraph) {

        invariantTypesIDMap = new LinkedHashMap<>();

        // The list of invariant types to go into the JSON object
        List<Map<String, Object>> invariantTypesList = new LinkedList<>();

        // Get all invariants in the partition graph
        TemporalInvariantSet allInvariants = pGraph.getInvariants();

        for (ITemporalInvariant inv : allInvariants) {
            // One invariant contains type, predicates, constraint, and bounds
            Map<String, Object> invariantMap = new LinkedHashMap<>();

            // To avoid duplicates, check if an invariant with the same name has
            // already been mapped
            if (!invariantTypesIDMap.containsKey(inv.getLongName())) {
                // Store the invariant type ID
                invariantMap.put("id", globalID);
                // Store the invariant type
                invariantMap.put("type", inv.getLongName());
                // Store the invariant type short name
                if (inv.getLongName() == "AlwaysPrecedes") {
                    invariantMap.put("shortName", "&larr;");
                }
                if (inv.getLongName() == "AlwaysFollowedBy") {
                    invariantMap.put("shortName", "&rarr;");
                }
                if (inv.getLongName() == "NeverFollowedBy") {
                    invariantMap.put("shortName", "&#8603;");
                }
                if (inv.getLongName() == "InterruptedBy") {
                    invariantMap.put("shortName", "&#8699;");
                }
                // Add it to invariant type map for global usage
                invariantTypesIDMap.put(inv.getLongName(), globalID);
                // Put the invariant map into the list of invariant types
                invariantTypesList.add(invariantMap);
                // Increment global ID
                globalID++;
            }
        }
        return invariantTypesList;
    }

    /**
     * Creates the 'invariants' of the JSON object: a list of invariants within
     * this EvBasedGraph
     * 
     * @param evGraph
     *            The EvBasedGraph whose invariants we're outputting
     */
    public static List<Map<String, Object>> makeInvariantsJSON(
            PartitionGraph pGraph) {

         assert (invariantTypesIDMap != null) : "invariantTypesIDMap is null";
         boolean skip = false;

        // The list of invariants to go into the JSON object
        List<Map<String, Object>> invariantsList = new LinkedList<>();
        List<Map<String, Object>> resourceBoundsArray = new LinkedList<>();
        Map<String, Object> resourceBoundsMap = new LinkedHashMap<>();


        // Get all invariants in the partition graph
        TemporalInvariantSet allInvariants = pGraph.getInvariants();
        // One invariant, contains type, predicates, constraint, and bounds
        Map<String, Object> invariantMap = new LinkedHashMap<>();
        for (ITemporalInvariant inv : allInvariants) {
            

            if(skip == false) {
                resourceBoundsMap = new LinkedHashMap<>();
                invariantMap = new LinkedHashMap<>();
                // Store the invariant ID
                invariantMap.put("id", globalID);
                // Get the invariant type ID and store it
                invariantMap.put("invariantTypeID",
                        invariantTypesIDMap.get(inv.getLongName()));
                // Get invariant predicates
                List<Integer> predicateList = new LinkedList<>();
                for (EventType evType : inv.getPredicates()) {
                    predicateList.add(eventTypesIDMap.get(evType.toString()));
                }
                invariantMap.put("predicates", predicateList);
                if (inv instanceof TempConstrainedInvariant) {
                    TempConstrainedInvariant<?> constInv = (TempConstrainedInvariant<?>) inv;
                    resourceBoundsArray = new LinkedList<>();
                    String constraint = constInv.getConstraint().toString();
                    // Extract the bound type and number from the constraint string
                    if (constraint.contains("lowerbound = ")) {
                        String boundType = "lower";
                        resourceBoundsMap.put("type", boundType);
                        constraint = constraint.replaceAll("[^\\.0123456789]", "");
                        resourceBoundsMap.put("bound", constraint);
                        resourceBoundsArray.add(resourceBoundsMap);
                        // invariantMap.put("resourceBounds", resourceBoundsArray);
                        skip = true;
                        //invariantsList.add(invariantMap);

                    } 
                }
                else {
                    invariantsList.add(invariantMap);
                    globalID++;
                }
            }
            if(skip == true) {
                TempConstrainedInvariant<?> constInv = (TempConstrainedInvariant<?>) inv;
                String constraint = constInv.getConstraint().toString();
                    resourceBoundsMap = new LinkedHashMap<>();
                    if (constraint.contains("upperbound = ")) {
                        String boundType = "upper";
                        resourceBoundsMap.put("type", boundType);
                        constraint = constraint.replaceAll("[^\\.0123456789]", "");
                        resourceBoundsMap.put("bound", constraint);
                        resourceBoundsArray.add(resourceBoundsMap);
                        invariantMap.put("resourceBounds", resourceBoundsArray);
                        skip = false;
                        globalID++;
                                    invariantsList.add(invariantMap);

                    }
            }


            // Put the invariant map into the list of invariants
            // invariantsList.add(invariantMap);
        }

        return invariantsList;
    }

    /**
     * Creates the 'logStatements' of the JSON object: a list of log statements
     * within this EvBasedGraph
     * 
     * @param evGraph
     *            The EvBasedGraph whose logStatements we're outputting
     */

    public static List<Map<String, Object>> makeLogStatementsJSON(
            EvBasedGraph evGraph) {

        logStatementsIDMap = new LinkedHashMap<>();

        // The log statements to go into the JSON Object
        List<Map<String, Object>> logStatementsList = new LinkedList<>();

        // Contains all edges used to avoid duplicates
        Set<EventNode> usedEvents = new HashSet<>();

        for (EvBasedNode node : evGraph.getNodes()) {

            for (EvBasedEdge edge : node.outEdges) {

                for (EventNode event : edge.events) {

                    Map<String, Object> logStatementsMap = new LinkedHashMap<>();
                    if (!usedEvents.contains(event)) {
                        if (event.getLine() != null) {

                            logStatementsMap.put("id", globalID);
                            logStatementsMap.put("text", event.getLine());
                            logStatementsMap.put("logPosition",
                                    event.getLineNum());

                            logStatementsList.add(logStatementsMap);
                            logStatementsIDMap.put(event.getLine(), globalID);
                            usedEvents.add(event);
                            globalID++;
                        }
                    }
                }
            }
        }

        return logStatementsList;
    }

    /**
     * Creates the 'nodes' of the JSON object: a list of nodes within this
     * EvBasedGraph
     * 
     * @param evGraph
     *            The EvBasedGraph whose nodes we're outputting
     */
    public static List<Map<String, Object>> makeLinksJSON(
            EvBasedGraph evGraph) {

        assert (eventsIDMap != null && logStatementsIDMap != null
                && edgesIDMap != null) : "Either one, two or all three of eventsIDMap, logStatementsIDMap, or edgesIDMap is null";

        List<Map<String, Object>> linksList = new LinkedList<>();

        // Contains all edges used to avoid duplicates
        Set<EventNode> usedEvents = new HashSet<>();

        for (EvBasedNode node : evGraph.getNodes()) {

            for (EvBasedEdge edge : node.outEdges) {

                for (EventNode event : edge.events) {

                    if (!usedEvents.contains(event)) {
                        if (logStatementsIDMap.get(event.getLine()) != null) {
                            Map<String, Object> eventLinksMap = new LinkedHashMap<>();
                            eventLinksMap.put("id1", eventsIDMap.get(event));
                            eventLinksMap.put("id2",
                                    logStatementsIDMap.get(event.getLine()));
                            linksList.add(eventLinksMap);
                            usedEvents.add(event);
                            Map<String, Object> edgeLinksMap = new LinkedHashMap<>();
                            edgeLinksMap.put("id1", edgesIDMap.get(edge));
                            edgeLinksMap.put("id2",
                                    logStatementsIDMap.get(event.getLine()));
                            linksList.add(edgeLinksMap);
                        }
                    }
                }

            }
        }

        return linksList;

    }

}