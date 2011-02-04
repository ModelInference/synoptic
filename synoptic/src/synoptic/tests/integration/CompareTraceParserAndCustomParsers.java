package synoptic.tests.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import synoptic.benchmarks.TimedTask;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.input.GraphBuilder;
import synoptic.model.input.PetersonReader;
import synoptic.model.input.ReverseTracertParser;
import synoptic.model.input.StenningReader;
import synoptic.util.InternalSynopticException;
import synoptic.util.Predicate;

/**
 * Compares ITraceParser against the older hard-coded parsers
 * 
 * @author mgsloan
 */
public class CompareTraceParserAndCustomParsers {

    @Test
    public void testReverseTracert() throws ParseException,
            InternalSynopticException {
        TimedTask oldTimer = new TimedTask("old");
        ReverseTracertParser parser = new ReverseTracertParser();
        String file = "traces/ReverseTraceroute/rt_parsed_rich/Internet-Partners,-Inc.-AS10248-2_revtr.err";
        Graph<LogEvent> g1 = parser.parseTraceFile(file, 10000, 0);
        oldTimer.stop();

        TimedTask newTimer = new TimedTask("new");
        TraceParser m = new TraceParser();

        m.addSeparator("--------");
        m.addRegex("^\\s*$(?<HIDE=true>)");
        m.addRegex("(?<TYPE>.*)");

        Graph<LogEvent> g2 = m.readGraph(file, 10000, true);
        newTimer.stop();

        checkGraphs("reversetraceroute", g1, g2);
        System.out.println(oldTimer + " vs " + newTimer);
    }

    private static List<LogEvent> readGraphSet(TraceParser m, String baseName,
            int count, int linesToRead) throws ParseException,
            InternalSynopticException {
        List<LogEvent> results = new ArrayList<LogEvent>();
        for (int i = 1; i <= count; ++i) {
            results.addAll(m.parseTraceFile(new File(baseName.replace("?", ""
                    + i)), linesToRead));
        }
        return results;
    }

    @Test
    public void testPeterson() throws IOException, ParseException,
            InternalSynopticException {
        TimedTask oldTimer = new TimedTask("old");
        GraphBuilder b = new GraphBuilder();
        PetersonReader<LogEvent> parser = new PetersonReader<LogEvent>(b);
        String file = "../traces/PetersonLeaderElection/generated_traces/peterson_trace-n5-1-s?.txt";
        parser.readGraphSet(file, 2);
        Graph<LogEvent> g1 = b.getGraph();
        oldTimer.stop();

        TimedTask newTimer = new TimedTask("new");
        TraceParser m = new TraceParser();
        m.addRegex("^(?:#.*|\\s*|.*round-done.*)(?<HIDE=>true)$");
        m
                .addRegex("(?<nodename>)(?<TIME>)(?<TYPE>)(?:(?<mtype>)(?:(?<roundId>)(?:(?<payload>)(?:(?<id>))?)?)?)?");
        m.setPartitioner("\\k<FILE>\\k<nodename>");
        List<LogEvent> occs = readGraphSet(m, file, 2, -1);
        Graph<LogEvent> g2 = m.generateDirectTemporalRelation(occs, true);
        newTimer.stop();

        checkGraphs("peterson", g1, g2);
        System.out.println(oldTimer + " vs " + newTimer);
    }

    // TODO: fix word boundary whitespace

    @Test
    public void testStenning() throws IOException, ParseException,
            InternalSynopticException {
        TimedTask oldTimer = new TimedTask("old");
        GraphBuilder b = new GraphBuilder();
        StenningReader<LogEvent> r = new StenningReader<LogEvent>(b);
        String file = "traces/StenningDataLink/generated_traces/t-10-0.5-0-s1.txt";
        r.readGraphDirect(file);
        Graph<LogEvent> g1 = b.getGraph();
        oldTimer.stop();

        TimedTask newTimer = new TimedTask("new");
        TraceParser m = new TraceParser();
        m.addRegex("#.*(?<HIDE=true>)");
        m.addRegex("(?<role>)(?<fragment>)(?<TYPE>.*)");
        m.setPartitioner("\\k<FILE>");
        Graph<LogEvent> g2 = m.readGraph(file, -1, false);
        newTimer.stop();

        checkGraphs("stenning", g1, g2);
        System.out.println(oldTimer + " vs " + newTimer);
    }

    // Comparison of messages based on label. Passed into Graph.equalsWith()
    // below
    @Ignore("HelperClass")
    public class MessageEquality implements
            Predicate.IBinary<LogEvent, LogEvent> {
        public boolean eval(LogEvent a, LogEvent b) {
            return a.getAction().getLabel().equals(b.getAction().getLabel());
        }
    }

    /**
     * Checks if the two graphs are equal
     */
    private void checkGraphs(String prefix, Graph<LogEvent> g1,
            Graph<LogEvent> g2) {
        // if (!g1.equalsWith(g2, new MessageEquality())) {
        // System.out.println("Error: " + prefix +
        // " input graphs are different.");
        // System.out.println("sizes: " + g1.getNodes().size() + " vs " +
        // g2.getNodes().size());
        // System.out.println("head-count: " + g1.getInitialNodes().size() +
        // " vs " + g2.getInitialNodes().size());
        // }
        assertTrue(g1.equalsWith(g2, new MessageEquality()));
    }
}
