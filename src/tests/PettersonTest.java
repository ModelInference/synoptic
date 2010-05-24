package tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import algorithms.bisim.Bisimulation;
import algorithms.graph.GraphUtil;
import invariants.TemporalInvariantSet;
import model.Graph;
import model.MessageEvent;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.input.NetBuilder;
import model.input.PetersonReader;
import model.nets.Event;
import model.nets.Net;

public class PettersonTest {
	public static void main(String[] args) throws Exception {
		GraphBuilder b = new GraphBuilder();
		PetersonReader<MessageEvent> r = new PetersonReader<MessageEvent>(b);
		GraphVizExporter e = new GraphVizExporter();

		// r.readGraphSet("traces/PetersonLeaderElection/generated_traces/5node1seed_withid.trace",
		// 5);
		// r.readGraphSet("traces/PetersonLeaderElection/generated_traces/peterson_trace-more-n5-1-s?.txt",
		// 5);
		r
				.readGraphSet(
						"traces/PetersonLeaderElection/generated_traces/peterson_trace-rounds-0-s?.txt",
						5);
		Graph<MessageEvent> g = b.getRawGraph();
		mineSplitInvariants(g, e);
		e.exportAsDotAndPng("output/peterson/initial.dot", g);
		TemporalInvariantSet s = TemporalInvariantSet.computeInvariants(g);
		e.exportAsDotAndPng("output/peterson/invariants.dot", s
				.getInvariantGraph(null));
		e.exportAsDotAndPng("output/peterson/invariants-AP.dot", s
				.getInvariantGraph("AP"));
		e.exportAsDotAndPng("output/peterson/invariants-AFby.dot", s
				.getInvariantGraph("AFby"));
		e.exportAsDotAndPng("output/peterson/invariants-NFby.dot", s
				.getInvariantGraph("NFby"));
		System.out.println(s);
		PartitionGraph pg = new PartitionGraph(g, true);
		e.exportAsDotAndPngFast("output/peterson/initial-pg.dot", pg);
		Bisimulation.refinePartitionsSmart(pg);
		e.exportAsDotAndPngFast("output/peterson/output-pg.dot", pg);
		Bisimulation.mergePartitions(pg);
		System.out.println("Merge done.");
		// e.exportAsDotAndPng("output/peterson/output-pg-merged.dot", pg);
		// Bisimulation.mergePartitions(pg);
		// e.exportAsDotAndPng("output/peterson/output-pg-merged.dot", pg);
		NetBuilder netBuilder = new NetBuilder();
		GraphUtil.copyTo(pg, netBuilder);
		Net net = netBuilder.getNet();

		HashMap<Event, ArrayList<Event>> entries = getEventSequences(net);
		e.exportAsDotAndPng("output/peterson/output-net.dot", net);
		for (ArrayList<Event> seq : entries.values()) {
			if (seq.size() > 1) {
				System.out.println(seq);
				net.replace(seq, conciseName(seq));
			}
		}

		e.exportAsDotAndPng("output/peterson/output-net-condensed.dot", net);
	}

	private static void mineSplitInvariants(Graph<MessageEvent> g,
			GraphVizExporter exporter) throws Exception {
		HashMap<String, HashMap<String, Set<MessageEvent>>> buckets = new HashMap<String, HashMap<String, Set<MessageEvent>>>();
		for (MessageEvent e : g.getNodes()) {
			if (!buckets.containsKey(e.getStringArgument("nodeName")))
				buckets.put(e.getStringArgument("nodeName"),
						new HashMap<String, Set<MessageEvent>>());
			if (!buckets.get(e.getStringArgument("nodeName")).containsKey(
					e.getStringArgument("localRoundId")))
				buckets.get(e.getStringArgument("nodeName")).put(
						e.getStringArgument("localRoundId"),
						new HashSet<MessageEvent>());
			buckets.get(e.getStringArgument("nodeName")).get(
					e.getStringArgument("localRoundId")).add(e);
		}
		for (String key : buckets.keySet()) {
			for (Entry<String, Set<MessageEvent>> e : buckets.get(key)
					.entrySet()) {
				Graph<MessageEvent> sg = new Graph<MessageEvent>();
				for (MessageEvent ev : e.getValue())
					sg.add(ev);
				TemporalInvariantSet inv = TemporalInvariantSet
						.computeInvariants(sg);
				System.out.println(e.getKey() + ": " + inv);
				exporter.exportAsDotAndPng("output/peterson/invariants-node"+key+"-round"
						+ e.getKey() + ".dot", inv.getInvariantGraph(null));
			}
		}
	}

	private static HashMap<Event, ArrayList<Event>> getEventSequences(Net net) {
		HashMap<Event, ArrayList<Event>> entries = new HashMap<Event, ArrayList<Event>>();
		HashSet<Event> seen = new HashSet<Event>();
		for (Event event : net.getEvents()) {
			if (!seen.add(event))
				continue;
			Set<Event> post = event.getPostEvents();
			if (post.size() != 1)
				continue;
			if (net.getPreEvents(event).size() > 1)
				continue;
			entries.put(event, new ArrayList<Event>(Collections
					.singleton(event)));
			Iterator<Event> iter = post.iterator();
			while (iter.hasNext()) {
				Event next = iter.next();
				seen.add(next);
				if (entries.get(event).contains(next))
					break;
				if (net.getPreEvents(next).size() > 1)
					break;
				Set<Event> post2 = next.getPostEvents();
				if (post2.size() > 1)
					break;
				if (entries.containsKey(next)) {
					for (Event old : entries.get(next)) {
						if (entries.get(event).contains(old))
							break;
						entries.get(event).add(old);
					}
					entries.remove(next);
					break;
				}
				entries.get(event).add(next);
				if (post2.size() == 0) {
					break;
				}
				// at this point we know post.size() == 1
				iter = post2.iterator();
			}
		}
		return entries;
	}

	private static String conciseName(ArrayList<Event> seq) {
		ArrayList<String> names = new ArrayList<String>();
		for (Event e : seq)
			names.add(e.getName().charAt(0) + ""
					+ e.getName().charAt(e.getName().length() - 1));
		Collections.sort(names);
		return names.toString();
	}
}
