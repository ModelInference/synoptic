package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import synoptic.main.AbstractMain;
import synoptic.main.PerfumeMain;
import synoptic.main.options.PerfumeOptions;
import synoptic.model.PartitionGraph;
import synoptic.model.export.DotExportFormatter;
import synoptic.model.export.JsonExporter;
import synoptic.model.export.types.EvBasedGraph;
import synoptic.tests.SynopticTest;

public class JsonExporterTests {

    private PartitionGraph pGraph = null;

    @Before
    public void setUp() throws Exception {
        PerfumeMain main = null;
        // Set up Perfume options
        PerfumeOptions perfOpts = new PerfumeOptions();
        perfOpts.regExps = Arrays.asList(
                new String[] { "(?<ip>.+), (?<TYPE>.+), (?<DTIME>.+)" });
        perfOpts.partitionRegExp = "\\k<ip>";
        String pathToLogFileName = SynopticTest.getTestPath(
                "traces/abstract/perfume-survey/browser-caching-traces.txt");
        perfOpts.logFilenames.add(pathToLogFileName);

        // Create a Perfume instance
        AbstractMain.instance = null;
        main = new PerfumeMain(perfOpts.toAbstractOptions(),
                new DotExportFormatter());

        // Run Perfume
        pGraph = main.createInitialPartitionGraph();
        main.runSynoptic(pGraph);
    }

    /**
     * Creates an evBasedGraph for testing purposes
     */
    public EvBasedGraph createEvGraph() {
        JsonExporter.exportJsonObject(pGraph);
        return new EvBasedGraph(pGraph);
    }

    /**
     * Parses and creates a JSONArray from expected-perfume-model.json for a
     * specific field
     * 
     * @param field
     *            The field for which the JSONArray will be created
     */
    public JSONArray parseJSONArray(String field) throws Exception {

        JSONParser parser = new JSONParser();
        Object expectedJSON = parser
                .parse(new FileReader("expected-perfume-model.json"));
        JSONObject expectedJSONObject = (JSONObject) expectedJSON;

        return (JSONArray) expectedJSONObject.get(field);
    }

    /*
     * Tests for the makeDisplayablesJSON method - Checks that each displayable
     * has an ID and displayableValue and that these values are not null
     */
    @Test
    public void displayableTest() throws Exception {

        EvBasedGraph evGraph = createEvGraph();
        List<Map<String, Object>> generatedDisplayablesList = JsonExporter
                .makeDisplayablesJSON(evGraph);
        Set<String> generatedDisplayables = new HashSet<>();
        int idCount = 0;
        int dVCount = 0;

        for (Map<String, Object> genDisplayable : generatedDisplayablesList) {

            if (genDisplayable.get("displayableValue") != null) {
                generatedDisplayables
                        .add(genDisplayable.get("displayableValue").toString());
            }

            if (genDisplayable.containsKey("id")) {
                if (genDisplayable.get("id") == null) {
                    Assert.fail("Displayable contains null id.");
                }
                idCount++;
            }
            if (genDisplayable.containsKey("displayableValue")) {
                if (genDisplayable.get("displayableValue") == null) {
                    Assert.fail("DisplayableValue contains null value.");
                }
                dVCount++;
            }
        }

        JSONArray displayables = parseJSONArray("displayables");
        Set<String> expectedDisplayables = new HashSet<>();

        for (Object expDisplayable : displayables) {

            JSONObject dispJSONObj = (JSONObject) expDisplayable;
            String displayableValue = dispJSONObj.get("displayableValue")
                    .toString();
            expectedDisplayables.add(displayableValue);
        }

        assertTrue(expectedDisplayables.equals(generatedDisplayables));
        assertTrue(idCount == generatedDisplayablesList.size());
        assertTrue(idCount == dVCount);
    }

    /*
     * Tests for the makeNodesJSON method - Checks that each node has an
     * associated ID and its ID is not null, if the node has an associated
     * displayableID, check that it is not null
     */
    @Test
    public void nodesTest() throws Exception {

        EvBasedGraph evGraph = createEvGraph();
        List<Map<String, Object>> generatedNodesList = JsonExporter
                .makeNodesJSON(evGraph);
        int idCount = 0;

        for (Map<String, Object> genNode : generatedNodesList) {

            if (genNode.containsKey("id")) {
                if (genNode.get("id") == null) {
                    Assert.fail("Node contains null id.");
                }
                idCount++;
            }
            if (genNode.containsKey("displayableIDs")
                    && genNode.get("displayableIDs") == null) {
                Assert.fail("DisplayableIDs is null.");
            }
        }

        assertTrue(idCount == generatedNodesList.size());
    }

    /*
     * Tests for the makeEdgesJSON method - Checks that each edge has an
     * associated ID, source node ID, destination node ID and these values are
     * not null, if the edge has an associated displayableID, check that it is
     * not null
     */
    @Test
    public void edgesTest() throws Exception {

        EvBasedGraph evGraph = createEvGraph();
        List<Map<String, Object>> t = JsonExporter.makeNodesJSON(evGraph);
        List<Map<String, Object>> generatedEdgesList = JsonExporter
                .makeEdgesJSON(evGraph);
        int idCount = 0;
        int srcNodeCount = 0;
        int destNodeCount = 0;

        for (Map<String, Object> genEdge : generatedEdgesList) {

            if (genEdge.containsKey("id")) {
                if (genEdge.get("id") == null) {
                    Assert.fail("Edge contains null id.");
                }
                idCount++;
            }
            if (genEdge.containsKey("srcNodeID")) {
                if (genEdge.get("srcNodeID") == null) {
                    Assert.fail("Edge contains null srcNodeID.");
                }
                srcNodeCount++;
            }
            if (genEdge.containsKey("destNodeID")) {
                if (genEdge.get("destNodeID") == null) {
                    Assert.fail("Edge contains null destNodeID.");
                }
                destNodeCount++;
            }
            if (genEdge.containsKey("displayableIDs")
                    && genEdge.get("displayableIDs") == null) {
                Assert.fail("DisplayableIDs is null.");
            }
        }

        assertTrue(idCount == srcNodeCount);
        assertTrue(idCount == destNodeCount);
        assertTrue(idCount == generatedEdgesList.size());
    }

    /*
     * Tests for the makeEventTypesJSON method - Checks that each eventType has
     * an associated ID and label and that these values are not null
     */
    @Test
    public void eventTypesTest() throws Exception {

        EvBasedGraph evGraph = createEvGraph();
        List<Map<String, Object>> generatedEventTypesList = JsonExporter
                .makeEventTypesJSON(evGraph);
        Set<String> generatedEventTypes = new HashSet<>();
        int idCount = 0;
        int labelCount = 0;

        for (Map<String, Object> genEventType : generatedEventTypesList) {

            if (genEventType.get("label") != null) {
                generatedEventTypes.add(genEventType.get("label").toString());
            }
            if (genEventType.containsKey("id")) {
                if (genEventType.get("id") == null) {
                    Assert.fail("EventTypes contains null id.");
                }
                idCount++;
            }
            if (genEventType.containsKey("label")) {
                if (genEventType.get("label") == null) {
                    Assert.fail("EventTypes contains null label.");
                }
                labelCount++;
            }
        }

        JSONArray eventTypes = parseJSONArray("eventTypes");
        Set<String> expectedEventTypes = new HashSet<>();

        for (Object expEventType : eventTypes) {

            JSONObject eventTypeJSONObj = (JSONObject) expEventType;
            String label = eventTypeJSONObj.get("label").toString();
            expectedEventTypes.add(label);
        }

        assertTrue(expectedEventTypes.equals(generatedEventTypes));
        assertTrue(idCount == generatedEventTypesList.size());
        assertTrue(idCount == labelCount);
    }

    /*
     * Tests for the makeEventsJSON method - Checks that each event has an
     * associated ID, traceID, traceIndex, eventTypeID and timestamp and these
     * values are not null
     */
    @Test
    public void eventsTest() throws Exception {

        EvBasedGraph evGraph = createEvGraph();
        List<Map<String, Object>> generatedEventsList = JsonExporter
                .makeEventsJSON(evGraph);
        int idCount = 0;
        int traceIDCount = 0;
        int traceIndexCount = 0;
        int eventTypeIDCount = 0;
        int timestampCount = 0;

        for (Map<String, Object> genEvent : generatedEventsList) {

            if (genEvent.containsKey("id")) {
                if (genEvent.get("id") == null) {
                    Assert.fail("Events contains null id.");
                }
                idCount++;
            }
            if (genEvent.containsKey("traceID")) {
                if (genEvent.get("traceID") == null) {
                    Assert.fail("Events contains null traceID.");
                }
                traceIDCount++;
            }
            if (genEvent.containsKey("traceIndex")) {
                if (genEvent.get("traceIndex") == null) {
                    Assert.fail("Events contains null traceIndex.");
                }
                traceIndexCount++;
            }
            if (genEvent.containsKey("eventTypeID")) {
                if (genEvent.get("eventTypeID") == null) {
                    Assert.fail("Events contains null eventTypeID.");
                }
                eventTypeIDCount++;
            }
            if (genEvent.containsKey("timestamp")) {
                if (genEvent.get("timestamp") == null) {
                    Assert.fail("Events contains null timestamp.");
                }
                timestampCount++;
            }
        }

        assertTrue(idCount == generatedEventsList.size());
        assertTrue(idCount == traceIDCount);
        assertTrue(idCount == traceIndexCount);
        assertTrue(idCount == eventTypeIDCount);
        assertTrue(idCount == timestampCount);
    }

    /*
     * Tests for the makeInvariantTypesJSON method - Checks that each
     * invariantType has an associated ID, type, and short name and that these
     * values are not null
     */
    @Test
    public void invariantTypesTest() throws Exception {

        List<Map<String, Object>> generatedInvariantTypesList = JsonExporter
                .makeInvariantTypesJSON(pGraph);
        Set<String> generatedInvariantTypes = new HashSet<>();
        int idCount = 0;
        int typeCount = 0;
        int shortNameCount = 0;

        for (Map<String, Object> genInvariantType : generatedInvariantTypesList) {

            if (genInvariantType.get("type") != null) {
                generatedInvariantTypes
                        .add(genInvariantType.get("type").toString());
            }
            if (genInvariantType.containsKey("id")) {
                if (genInvariantType.get("id") == null) {
                    Assert.fail("InvariantTypes contains a null id.");
                }
                idCount++;
            }
            if (genInvariantType.containsKey("type")) {
                if (genInvariantType.get("type") == null) {
                    Assert.fail("InvariantTypes contains a null type.");
                }
                typeCount++;
            }
            if (genInvariantType.containsKey("shortName")) {
                if (genInvariantType.get("shortName") == null) {
                    Assert.fail("InvariantTypes contains a null shortName.");
                }
                shortNameCount++;
            }
        }

        JSONArray invariantTypes = parseJSONArray("invariantTypes");
        Set<String> expectedInvariantTypes = new HashSet<>();

        for (Object expInvariantType : invariantTypes) {

            JSONObject invariantTypeJSONObj = (JSONObject) expInvariantType;
            String type = invariantTypeJSONObj.get("type").toString();
            expectedInvariantTypes.add(type);
        }

        assertTrue(expectedInvariantTypes.equals(generatedInvariantTypes));
        assertTrue(idCount == generatedInvariantTypesList.size());
        assertTrue(idCount == typeCount);
        assertTrue(idCount == shortNameCount);
    }

    /*
     * Tests for the makeInvariantsJSON method Checks - that each invariant has
     * an associated ID and invariantTypeID and that these values are not null,
     * if there is an associated predicate or resourceBoound, check that there
     * values are not null
     */
    @Test
    public void invariantsTest() throws Exception {

        List<Map<String, Object>> generatedInvariantsList = JsonExporter
                .makeInvariantsJSON(pGraph);
        int idCount = 0;
        int invariantTypeIDCount = 0;

        for (Map<String, Object> genInvariant : generatedInvariantsList) {

            if (genInvariant.containsKey("id")) {
                if (genInvariant.get("id") == null) {
                    Assert.fail("Invariants contains a null id.");
                }
                idCount++;
            }
            if (genInvariant.containsKey("invariantTypeID")) {
                if (genInvariant.get("invariantTypeID") == null) {
                    Assert.fail("Invariants contains a null invariantTypeID.");
                }
                invariantTypeIDCount++;
            }
            if (genInvariant.containsKey("predicates")
                    && genInvariant.get("predicates") == null) {
                Assert.fail("Invariants contains null predicates.");
            }
            if (genInvariant.containsKey("resourceBounds")
                    && genInvariant.get("resourceBounds") == null) {
                Assert.fail("Invariants contains null resourceBounds.");
            }
        }

        assertTrue(idCount == generatedInvariantsList.size());
        assertTrue(idCount == invariantTypeIDCount);
    }

    /*
     * Tests for the makeLogStatementsJSON method - Checks that each
     * logStatement has an associated ID, text, and log position and that these
     * values are not null
     */
    @Test
    public void logStatementsTest() throws Exception {

        EvBasedGraph evGraph = createEvGraph();
        List<Map<String, Object>> generatedLogStatementsList = JsonExporter
                .makeLogStatementsJSON(evGraph);
        Set<String> generatedLogStatements = new HashSet<>();
        int idCount = 0;
        int textCount = 0;
        int logPositionCount = 0;

        for (Map<String, Object> genLogStatement : generatedLogStatementsList) {

            if (genLogStatement.get("text") != null) {
                generatedLogStatements
                        .add(genLogStatement.get("text").toString());
            }
            if (genLogStatement.containsKey("id")) {
                if (genLogStatement.get("id") == null) {
                    Assert.fail("LogStatements contains a null id.");
                }
                idCount++;
            }
            if (genLogStatement.containsKey("text")) {
                if (genLogStatement.get("text") == null) {
                    Assert.fail("LogStatements contains a null text.");
                }
                textCount++;
            }
            if (genLogStatement.containsKey("logPosition")) {
                if (genLogStatement.get("logPosition") == null) {
                    Assert.fail("LogStatements contains a null logPosition.");
                }
                logPositionCount++;
            }
        }

        JSONArray logStatements = parseJSONArray("logStatements");
        Set<String> expectedLogStatements = new HashSet<>();

        for (Object expLogStatement : logStatements) {
            JSONObject logStatementJSONObj = (JSONObject) expLogStatement;
            String type = logStatementJSONObj.get("text").toString();
            expectedLogStatements.add(type);
        }

        assertTrue(expectedLogStatements.equals(generatedLogStatements));
        assertTrue(idCount == generatedLogStatementsList.size());
        assertTrue(idCount == textCount);
        assertTrue(idCount == logPositionCount);
    }

    /*
     * Tests for the makeLinksJSON method - Checks that each link has an
     * associated id1 and id2 and that these values are not null
     */
    @Test
    public void linksTest() throws Exception {

        EvBasedGraph evGraph = createEvGraph();
        List<Map<String, Object>> generatedEdgesList = JsonExporter
                .makeEdgesJSON(evGraph);
        List<Map<String, Object>> generatedLinksList = JsonExporter
                .makeLinksJSON(evGraph);
        int idCount1 = 0;
        int idCount2 = 0;

        for (Map<String, Object> genLink : generatedLinksList) {

            if (genLink.containsKey("id1")) {
                if (genLink.get("id1") == null) {
                    Assert.fail("Links contains a null id1.");
                }
                idCount1++;
            }
            if (genLink.containsKey("id2")) {
                if (genLink.get("id2") == null) {
                    Assert.fail("Links contains a null id2.");
                }
                idCount2++;
            }
        }

        assertTrue(idCount1 == generatedLinksList.size());
        assertTrue(idCount1 == idCount2);
    }
}