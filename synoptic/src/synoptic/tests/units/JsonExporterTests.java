package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                "../traces/abstract/perfume-survey/browser-caching-traces.txt");
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
    // public JSONArray parseJSONArray(String field) {

    // Object expectedJSON = null;
    // JSONObject expectedJSONObject = null;
    // JSONParser parser = new JSONParser();

    // try {
    // expectedJSON = parser.parse(new
    // FileReader("expected-perfume-model.json"));
    // expectedJSONObject = (JSONObject)expectedJSON;

    // }
    // catch (FileNotFoundException e) {
    // e.printStackTrace();
    // }
    // catch (IOException e) {
    // e.printStackTrace();
    // }
    // catch (ParseException e) {
    // e.printStackTrace();
    // }

    // return (JSONArray)expectedJSONObject.get(field);
    // }

    /*
     * Tests for the makeDisplayablesJSON method - Checks that each displayable
     * has an ID and displayableValue and that these values are not null
     */
    @Test
    public void displayableTest() throws Exception {

        // JSONArray displayables = parseJSONArray("displayables");

        // ArrayList<String> parsedDisplayables = new ArrayList<String>();
        // for(int a = 0; a < displayables.size(); a++) {
        // JSONObject entireEntry = (JSONObject)displayables.get(a);
        // String displayableValue =
        // entireEntry.get("displayableValue").toString();
        // parsedDisplayables.add(displayableValue);
        // }

        // List<Map<String, Object>> displayablesTest =
        // JsonExporter.makeDisplayablesJSON(evGraph);
        // Map<String, Object> m = new HashMap<>();
        // ArrayList<Object> generatedDisplayables = new ArrayList<Object>();
        // for(int b = 0; b < displayablesTest.size(); b++) {
        // m = displayablesTest.get(b);
        // if(m.get("displayableValue") != null) {
        // generatedDisplayables.add(m.get("displayableValue"));
        // }
        // }

        // assertTrue(displayables.size() == displayablesTest.size());
        // assertTrue(parsedDisplayables.size() == displayablesTest.size());
        // assertTrue(parsedDisplayables.containsAll(generatedDisplayables));

        EvBasedGraph evGraph = createEvGraph();
        List<Map<String, Object>> generatedDisplayablesList = JsonExporter
                .makeDisplayablesJSON(evGraph);

        int idCount = 0;
        int dVCount = 0;
        boolean res = true;

        Map<String, Object> m = new LinkedHashMap<>();

        for (int c = 0; c < generatedDisplayablesList.size(); c++) {
            m = generatedDisplayablesList.get(c);
            if (m.containsKey("id") && m.get("id") != null) {
                idCount++;
            } else if (m.containsKey("id") && m.get("id") == null) {
                res = false;
            }
            if (m.containsKey("displayableValue")
                    && m.get("displayableValue") != null) {
                dVCount++;
            } else if (m.containsKey("displayableValue")
                    && m.get("displayableValue") == null) {
                res = false;
            }
        }
        assertTrue(idCount == generatedDisplayablesList.size());
        assertTrue(idCount == dVCount);
        assertTrue(res);
    }

    /*
     * Tests for the makeNodesJSON method - Checks that each node has an
     * associated ID and its ID is not null, if the node has an associated
     * displayableID, check that it is not null
     */
    @Test
    public void nodesTest() throws Exception {

        EvBasedGraph evGraph = createEvGraph();
        List<Map<String, Integer>> generatedNodesList = JsonExporter
                .makeNodesJSON(evGraph);

        int idCount = 0;
        boolean res = true;

        Map<String, Integer> m = new HashMap<>();

        for (int a = 0; a < generatedNodesList.size(); a++) {
            m = generatedNodesList.get(a);
            if (m.containsKey("id") && m.get("id") != null) {
                idCount++;
            }
            if (m.containsKey("id") && m.get("id") == null) {
                res = false;
            }
            if (m.containsKey("displayableValue")
                    && m.get("displayableIDs") == null) {
                res = false;
            }
        }
        assertTrue(idCount == generatedNodesList.size());
        assertTrue(res);
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
        List<Map<String, Integer>> t = JsonExporter.makeNodesJSON(evGraph);
        List<Map<String, Integer>> generatedEdgesList = JsonExporter
                .makeEdgesJSON(evGraph);

        int idCount = 0;
        int srcNodeCount = 0;
        int destNodeCount = 0;
        boolean res = true;

        Map<String, Integer> m = new LinkedHashMap<>();

        for (int a = 0; a < generatedEdgesList.size(); a++) {

            m = generatedEdgesList.get(a);

            if (m.containsKey("id") && m.get("id") != null) {
                idCount++;
            } else if (m.containsKey("id") && m.get("id") == null) {
                res = false;
            }
            if (m.containsKey("srcNodeID") && m.get("srcNodeID") != null) {
                srcNodeCount++;
            } else if (m.containsKey("srcNodeID")
                    && m.get("srcNodeID") == null) {
                res = false;
            }
            if (m.containsKey("destNodeID") && m.get("destNodeID") != null) {
                destNodeCount++;
            } else if (m.containsKey("destNodeID")
                    && m.get("destNodeID") == null) {
                res = false;
            }
            if (m.containsKey("displayableIDs")
                    && m.get("displayableIDs") == null) {
                res = false;
            }
        }
        assertTrue(idCount == srcNodeCount);
        assertTrue(idCount == destNodeCount);
        assertTrue(idCount == generatedEdgesList.size());
        assertTrue(res);
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

        int idCount = 0;
        int labelCount = 0;
        boolean res = true;

        Map<String, Object> m = new LinkedHashMap<>();

        for (int a = 0; a < generatedEventTypesList.size(); a++) {
            m = generatedEventTypesList.get(a);
            if (m.containsKey("id") && m.get("id") != null) {
                idCount++;
            } else if (m.containsKey("id") && m.get("id") == null) {
                res = false;
            }
            if (m.containsKey("label") && m.get("label") != null) {
                labelCount++;
            } else if (m.containsKey("label") && m.get("label") == null) {
                res = false;
            }
        }
        assertTrue(idCount == generatedEventTypesList.size());
        assertTrue(idCount == labelCount);
        assertTrue(res);
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
        boolean res = true;

        Map<String, Object> m = new LinkedHashMap<>();

        for (int a = 0; a < generatedEventsList.size(); a++) {
            m = generatedEventsList.get(a);
            if (m.containsKey("id") && m.get("id") != null) {
                idCount++;
            } else if (m.containsKey("id") && m.get("id") == null) {
                res = false;
            }
            if (m.containsKey("traceID") && m.get("traceID") != null) {
                traceIDCount++;
            } else if (m.containsKey("traceIDCount")
                    && m.get("traceIDCount") == null) {
                res = false;
            }
            if (m.containsKey("traceIndex") && m.get("traceIndex") != null) {
                traceIndexCount++;
            } else if (m.containsKey("traceIndex")
                    && m.get("traceIndex") == null) {
                res = false;
            }
            if (m.containsKey("eventTypeID") && m.get("eventTypeID") != null) {
                eventTypeIDCount++;
            } else if (m.containsKey("eventTypeID")
                    && m.get("eventTypeID") == null) {
                res = false;
            }
            if (m.containsKey("timestamp") && m.get("timestamp") != null) {
                timestampCount++;
            } else if (m.containsKey("timestamp")
                    && m.get("timestamp") == null) {
                res = false;
            }
        }
        assertTrue(idCount == generatedEventsList.size());
        assertTrue(idCount == traceIDCount);
        assertTrue(idCount == traceIndexCount);
        assertTrue(idCount == eventTypeIDCount);
        assertTrue(idCount == timestampCount);
        assertTrue(res);
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

        int idCount = 0;
        int typeCount = 0;
        int shortNameCount = 0;
        boolean res = true;

        Map<String, Object> m = new LinkedHashMap<>();
        for (int a = 0; a < generatedInvariantTypesList.size(); a++) {
            m = generatedInvariantTypesList.get(a);
            if (m.containsKey("id") && m.get("id") != null) {
                idCount++;
            }
            if (m.containsKey("id") && m.get("id") == null) {
                res = false;
            }
            if (m.containsKey("type") && m.get("type") != null) {
                typeCount++;
            }
            if (m.containsKey("type") && m.get("type") == null) {
                res = false;
            }
            if (m.containsKey("shortName") && m.get("shortName") != null) {
                shortNameCount++;
            }
            if (m.containsKey("shortName") && m.get("shortName") == null) {
                res = false;
            }
        }
        assertTrue(idCount == generatedInvariantTypesList.size());
        assertTrue(idCount == typeCount);
        assertTrue(idCount == shortNameCount);
        assertTrue(res);
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
        boolean res = true;

        Map<String, Object> m = new LinkedHashMap<>();

        for (int a = 0; a < generatedInvariantsList.size(); a++) {
            m = generatedInvariantsList.get(a);
            if (m.containsKey("id") && m.get("id") != null) {
                idCount++;
            }
            if (m.containsKey("id") && m.get("id") == null) {
                res = false;
            }
            if (m.containsKey("invariantTypeID")
                    && m.get("invariantTypeID") != null) {
                invariantTypeIDCount++;
            }
            if (m.containsKey("invariantTypeID")
                    && m.get("invariantTypeID") == null) {
                res = false;
            }
            if (m.containsKey("predicates") && m.get("predicates") == null) {
                res = false;
            }
            if (m.containsKey("resourceBounds")
                    && m.get("resourceBounds") == null) {
                res = false;
            }
        }
        assertTrue(idCount == generatedInvariantsList.size());
        assertTrue(idCount == invariantTypeIDCount);
        assertTrue(res);
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

        int idCount = 0;
        int textCount = 0;
        int logPositionCount = 0;
        boolean res = true;

        Map<String, Object> m = new LinkedHashMap<>();

        for (int a = 0; a < generatedLogStatementsList.size(); a++) {
            m = generatedLogStatementsList.get(a);
            if (m.containsKey("id") && m.get("id") != null) {
                idCount++;
            } else if (m.containsKey("id") && m.get("id") == null) {
                res = false;
            }
            if (m.containsKey("text") && m.get("text") != null) {
                textCount++;
            } else if (m.containsKey("text") && m.get("text") == null) {
                res = false;
            }
            if (m.containsKey("logPosition") && m.get("logPosition") != null) {
                logPositionCount++;
            } else if (m.containsKey("logPosition")
                    && m.get("logPosition") == null) {
                res = false;
            }
        }
        assertTrue(idCount == generatedLogStatementsList.size());
        assertTrue(idCount == textCount);
        assertTrue(idCount == logPositionCount);
        assertTrue(res);
    }

    /*
     * Tests for the makeLinksJSON method - Checks that each link has an
     * associated id1 and id2 and that these values are not null
     */
    @Test
    public void linksTest() throws Exception {

        EvBasedGraph evGraph = createEvGraph();
        // List<Map<String,Integer>> generatedNodesList =
        // JsonExporter.makeNodesJSON(evGraph);
        List<Map<String, Integer>> generatedEdgesList = JsonExporter
                .makeEdgesJSON(evGraph);
        List<Map<String, Object>> generatedLinksList = JsonExporter
                .makeLinksJSON(evGraph);

        int idCount1 = 0;
        int idCount2 = 0;
        boolean res = true;

        Map<String, Object> m = new LinkedHashMap<>();

        for (int a = 0; a < generatedLinksList.size(); a++) {
            m = generatedLinksList.get(a);
            if (m.containsKey("id1") && m.get("id1") != null) {
                idCount1++;
            } else if (m.containsKey("id1") && m.get("id1") == null) {
                res = false;
            }
            if (m.containsKey("id2") && m.get("id2") != null) {
                idCount2++;
            } else if (m.containsKey("id2") && m.get("id2") == null) {
                res = false;
            }
        }
        assertTrue(idCount1 == generatedLinksList.size());
        assertTrue(idCount1 == idCount2);
        assertTrue(res);
    }
}