package tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;


import util.TimedTask;

import algorithms.bisim.Bisimulation;
import algorithms.graph.StronglyConnectedComponents;
import invariants.TemporalInvariantSet;
import model.Graph;
import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.input.PetersonReader;
import model.nets.Event;
import model.nets.Net;

public class PettersonTest {
	public static void main(String[] args) throws Exception {
		TimedTask load = new TimedTask("load files", 1);
		TimedTask total = new TimedTask("total", 1);
		GraphBuilder b = new GraphBuilder();
		PetersonReader<MessageEvent> r = new PetersonReader<MessageEvent>(b);
		GraphVizExporter e = new GraphVizExporter();

		// r.readGraphSet("traces/PetersonLeaderElection/generated_traces/5node1seed_withid.trace",
		// 5);
		
		r.readGraphSet("traces/PetersonLeaderElection/generated_traces/peterson_trace-n5-1-s?.txt",
		 75);
		
		//r.readGraphSet("traces/TapioExampleTrace/trace.txt",
		//		 1);
		/*r
				.readGraphSet(
						"traces/PetersonLeaderElection/generated_traces/peterson_trace-rounds-0-s?.txt",
						5);*/
		
		Graph<MessageEvent> g = b.getRawGraph();
		int nodes = g.getNodes().size();
		System.out.println("Nodes: " + g.getNodes().size());
		load.stop();
		System.out.println(load);
		TimedTask invariants = new TimedTask("invariants", 1);
		//mineSplitInvariants(g, e);
		System.out.println("Computing Invariants...");
		e.exportAsDotAndPng("output/peterson/initial.dot", g);
		System.out.println("Creating Partition Graph...");
		PartitionGraph pg = new PartitionGraph(g, true);
		TemporalInvariantSet s = pg.getInvariants();
		invariants.stop();
		e.exportAsDotAndPng("output/peterson/invariants.dot", s
				.getInvariantGraph(null));
		e.exportAsDotAndPng("output/peterson/invariants-AP.dot", s
				.getInvariantGraph("AP"));
		e.exportAsDotAndPng("output/peterson/invariants-AFby.dot", s
				.getInvariantGraph("AFby"));
		e.exportAsDotAndPng("output/peterson/invariants-NFby.dot", s
				.getInvariantGraph("NFby"));
		System.out.println(s);
		TimedTask refinement = new TimedTask("refinement", 1);
		//e.exportAsDotAndPngFast("output/peterson/initial-pg.dot", pg);
		System.out.println("Refining Partitions...");
		Bisimulation.refinePartitions(pg);
		System.out.println("Refined to " + pg.getNodes().size()+ " nodes.");
		//e.exportAsDotAndPngFast("output/peterson/output-pg.dot", pg);
		refinement.stop();
		pg.checkSanity();
		TimedTask coarsening = new TimedTask("coarsening", 1);
		System.out.println("Merging Partitions...");
		Bisimulation.mergePartitions(pg);
		coarsening.stop();
		total.stop();
		System.out.println("Merge done.");
		e.exportAsDotAndPngFast("output/peterson/output-pg-merged.dot", pg);
		//exportSCCsWithInvariants(e, pg);
		// Bisimulation.mergePartitions(pg);
		e.exportAsDotAndPng("output/peterson/output-pg-merged-S.dot", pg.getSystemStateGraph());
		//NetBuilder netBuilder = new NetBuilder();
		//GraphUtil.copyTo(pg, netBuilder);
		//Net net = netBuilder.getNet();

		//HashMap<Event, ArrayList<Event>> entries = getEventSequences(net);
		//e.exportAsDotAndPng("output/peterson/output-net.dot", net);
		//for (ArrayList<Event> seq : entries.values()) {
		//	if (seq.size() > 1) {
		//		System.out.println(seq);
		//		net.replace(seq, conciseName(seq));
		//	}
		//}

		//e.exportAsDotAndPng("output/peterson/output-net-condensed.dot", net);
		
		System.out.println(nodes + " Nodes");
		System.out.println(load);
		System.out.println(invariants);
		System.out.println(refinement);
		System.out.println(coarsening);
		System.out.println(total);
	}

	private static void exportSCCsWithInvariants(GraphVizExporter e,
			PartitionGraph pg) throws Exception {
		StronglyConnectedComponents<Partition> sccs = new StronglyConnectedComponents<Partition>(pg);
		int partN = 0;
		for (Set<Partition> scc : sccs) {
			Graph<Partition> graph = new Graph<Partition>();
			Graph<MessageEvent> messageGraph = new Graph<MessageEvent>();
			for (Partition p : scc) {
				graph.add(p);
				for (MessageEvent m : p.getMessages()) {
					messageGraph.add(m);
				}
			}
			e.exportAsDotAndPngFast("output/peterson/messageGraph.dot", messageGraph);
			e.exportAsDotAndPngFast("output/peterson/partition-"+partN+".dot", graph);
			System.out.println(scc);
			TemporalInvariantSet.generateStructuralInvariants = true;
			TemporalInvariantSet s2 = TemporalInvariantSet.computeInvariants(messageGraph);
			e.exportAsDotAndPngFast("output/peterson/partition-"+partN+"-invariants.dot", s2
					.getInvariantGraph("AP"));
			TemporalInvariantSet.generateStructuralInvariants = false;
			partN++;
		}
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
