package tests;

import java.util.ArrayList;
import java.util.List;

import benchmarks.TimedTask;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.input.PetersonReader;
import model.input.ReverseTracertParser;
import model.input.StenningReader;
import model.*;
import main.*;

import algorithms.ktail.InputEquivalence;

// Compares the behavior of functionality found in Main with the hardcoded tests.
public class MainTest {
	public static void main(String[] args) throws Exception {
		testReverseTracert();
		testPeterson();
		testStenning();
	}

	public static void testReverseTracert() throws Exception {
		TimedTask oldTimer = new TimedTask("old");
		ReverseTracertParser parser = new ReverseTracertParser();
		String file = "traces/ReverseTraceroute/rt_parsed_rich/Internet-Partners,-Inc.-AS10248-2_revtr.err";
		model.Graph<MessageEvent> g1 = parser.parseTraceFile(file, 10000, 0);
		oldTimer.stop();
		
		TimedTask newTimer = new TimedTask("new");
		TraceParser m = new TraceParser();
		
		m.addSeperator("--------");
		//Equivalent to:
		//m.addRegex("--------(?<section++>)");
		//m.setPartitioner("\\k<section>");
		m.addRegex("^\\s*$(?<HIDE=true>)");
		m.addRegex("(?<TYPE>.*)");
		
		model.Graph<MessageEvent> g2 = m.readGraph(file, 10000, true);
		newTimer.stop();
		
		checkGraphs("reversetraceroute", g1, g2);
		System.out.println(oldTimer + " vs " + newTimer);
	}
	
	public static List<TraceParser.Occurrence> readGraphSet(TraceParser m, String baseName, int count, int linesToRead) {
		List<TraceParser.Occurrence> results = new ArrayList<TraceParser.Occurrence>();
		for (int i = 1; i <= count; ++i) {
			results.addAll(m.parseTraceFile(baseName.replace("?", "" + i), linesToRead));
		}
		return results;
	}
	
	//TODO: all caps special fields
	//TODO: set filter -> setPartitionMap ?
	
	public static void testPeterson() throws Exception {
		TimedTask oldTimer = new TimedTask("old");
		GraphBuilder b = new GraphBuilder();
		PetersonReader<MessageEvent> parser = new PetersonReader<MessageEvent>(b);
		String file = "traces/PetersonLeaderElection/generated_traces/peterson_trace-n5-1-s?.txt";
		parser.readGraphSet(file, 2);
		model.Graph<MessageEvent> g1 = b.getRawGraph();
		oldTimer.stop();
		
		TimedTask newTimer = new TimedTask("new");
		TraceParser m = new TraceParser();
		m.addRegex("^(?:#.*|\\s*|.*round-done.*)(?<HIDE=true>)$");
		m.addRegex("(?<nodename>)(?<TIME>)(?<TYPE>)(?:(?<mtype>)(?:(?<roundId>)(?:(?<payload>)(?:(?<id>))?)?)?)?");
		m.setPartitioner("\\k<FILE>\\k<nodename>");
		m.builder = new GraphBuilder();
		List<TraceParser.Occurrence> occs = readGraphSet(m, file, 2, -1);
		m.generateDirectTemporalRelation(occs, true);
		model.Graph<MessageEvent> g2 = ((GraphBuilder)m.builder).getRawGraph();
		newTimer.stop();
		
		checkGraphs("peterson", g1, g2);
		System.out.println(oldTimer + " vs " + newTimer);
	}
	
	//TODO: fix word boundary whitespace
	
	public static void testStenning() throws Exception {
		TimedTask oldTimer = new TimedTask("old");
		GraphBuilder b = new GraphBuilder();
		StenningReader<MessageEvent> r = new StenningReader<MessageEvent>(b);
		String file = "traces/StenningDataLink/generated_traces/t-10-0.5-0-s1.txt";
		r.readGraphDirect(file);
		model.Graph<MessageEvent> g1 = b.getRawGraph();
		oldTimer.stop();

		TimedTask newTimer = new TimedTask("new");
		TraceParser m = new TraceParser();
		m.addRegex("#.*(?<HIDE=true>)");
		m.addRegex("(?<role>)(?<fragment>)(?<TYPE>.*)");
		m.setPartitioner("\\k<FILE>");
		model.Graph<MessageEvent> g2 = m.readGraph(file, -1, false);
		newTimer.stop();

		checkGraphs("stenning", g1, g2);
		System.out.println(oldTimer + " vs " + newTimer);
	}

	// Comparison of messages based on label.  Passed into Graph.equalsWith.
	public static class MessageEquality implements model.Graph.I2Predicate<MessageEvent> {
		public boolean func(MessageEvent a, MessageEvent b) {
			return a.getAction().getLabel().equals(b.getAction().getLabel());
		}
	}
	
	// Exports the provided graphs, and checks if they are equal.
	public static void checkGraphs(String prefix, Graph<MessageEvent> g1, Graph<MessageEvent> g2) {
		GraphVizExporter export = new GraphVizExporter();
		export.edgeLabels = false;
		try {
			export.exportAsDotAndPngFast(prefix + "_input1.dot", g1);
			export.exportAsDotAndPngFast(prefix + "_input2.dot", g2);
		} catch (Exception e) {
			System.err.println("Couldn't export graphs because: " + e.toString());
		}
		
		if (!g1.equalsWith(g2, new MessageEquality())) {
			System.out.println("Error: " + prefix + " input graphs are different.");
			System.out.println("sizes: " + g1.getNodes().size() + " vs " + g2.getNodes().size());
			System.out.println("headcount: " + g1.getInitialNodes().size() + " vs " + g2.getInitialNodes().size());
		} else {
			System.out.println(prefix + " yielded same results.");
		}
/*
		int count = 0;
		for (MessageEvent e1 : g1.getInitialNodes()) {
		    for (MessageEvent e2 : g2.getInitialNodes()) {
				if (InputEquivalence.isInputEquivalent(e1, e2)) {
				    count++;
				}
		    }
		}
		System.out.printf("%d of the trace pairs found to be equivalent.", count); */
	}
}
