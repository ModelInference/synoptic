package synoptic.tests;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import synoptic.algorithms.graph.GraphUtil;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.MessageEvent;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.input.GraphBuilder;
import synoptic.model.input.NetBuilder;
import synoptic.model.input.PetersonReader;
import synoptic.model.nets.Event;
import synoptic.model.nets.Net;
import synoptic.model.nets.Place;
import synoptic.statistics.FrequencyMiner;


public class TraceCondenserTest {
	private static String igAPRel = "AP";

	public static void main(String[] args) throws Exception {
		GraphBuilder b = new GraphBuilder();
		PetersonReader<MessageEvent> r = new PetersonReader<MessageEvent>(b);
		GraphVizExporter exporter = new GraphVizExporter();
		r.readGraphSet(
						"traces/PetersonLeaderElection/generated_traces/no-rand.trace",
						/* 5process_trace-5-1 */
						1);
		Graph<MessageEvent> g = b.getRawGraph();
		exporter.exportAsDotAndPng("output/traceCondenser/initial.dot", g);
		FrequencyMiner<MessageEvent> miner = new FrequencyMiner<MessageEvent>(g
				.getNodes());
		System.out.println(miner);

		TemporalInvariantSet s = TemporalInvariantSet.computeInvariants(g);
		TemporalInvariantSet s2 = TemporalInvariantSet.computeInvariantsSplt(g, "relay");
		System.out.println(s2);
		Graph<MessageEvent> igAP = s.getInvariantGraph("AP");
		exportInvariants(exporter, s, "synoptic.invariants");
		exportInvariants(exporter, s2, "synoptic.invariants-splt");
		System.out.println(s);
		NetBuilder netBuilder = new NetBuilder();
		GraphUtil.copyTo(g, netBuilder);
		Net net = netBuilder.getNet();
		exporter.exportAsDotAndPng("output/traceCondenser/initial.dot", net);
		Net newNet = condense(igAP, net);
		exporter.exportAsDotAndPng(
				"output/traceCondenser/initial-condensed.dot", newNet);
	}

	private static Net condense(Graph<MessageEvent> igAP, Net net) {
		HashMap<String, MessageEvent> map = new HashMap<String, MessageEvent>();
		for (MessageEvent m : igAP.getNodes()) {
			map.put(m.getLabel(), m);
		}
		for (Place p : net.getInitalPlaces()) {
			Place current = p;
			while (current != null) {
				Set<Place> nexts = current.getPostPlaces();
				if (nexts.size() != 1) {
					System.out.println("not linear");
					break;
				}
				Place next = nexts.iterator().next();
				Set<Event> first = net.getPre(next);
				Set<Event> second = next.getPost();
				HashMap<Event, Set<Event>> related = new HashMap<Event, Set<Event>>();
				if (noneRelated(first, second, map, related)
						&& noneRelated(second, first, map, related)) {
					System.out.println("contracting " + current + " " + next);
					net.contract(current, next);
				} else
					System.out.println("related: " + related);
				current = next;
			}
		}
		return net;
	}

	private static void exportInvariants(GraphVizExporter exporter,
			TemporalInvariantSet s, String fileName) throws Exception {
		exporter.exportAsDotAndPng(
				"output/traceCondenser/" + fileName + ".dot", s
						.getInvariantGraph(null));
		exporter.exportAsDotAndPng("output/traceCondenser/" + fileName
				+ "-AP.dot", s.getInvariantGraph("AP"));
		exporter.exportAsDotAndPng("output/traceCondenser/" + fileName
				+ "-AFby.dot", s.getInvariantGraph("AFby"));
		exporter.exportAsDotAndPng("output/traceCondenser/" + fileName
				+ "-NFby.dot", s.getInvariantGraph("NFby"));
	}

	private static boolean noneRelated(Set<Event> first, Set<Event> second,
			HashMap<String, MessageEvent> map, HashMap<Event, Set<Event>> related) {
		boolean ret = true;
		for (Event e : first) {
			for (Event e2 : second) {
				if (map.get(e.getName()).getTransition(map.get(e2.getName()),
						igAPRel) != null) {
					ret = false;
					if (!map.containsKey(e))
						related.put(e, new HashSet<Event>());
					related.get(e).add(e2);
				}
			}
		}
		return ret;
	}

	private static Net condense2(Graph<MessageEvent> igAP, Net net) {
		HashMap<Event, Event> map2 = new HashMap<Event, Event>();
		HashMap<String, MessageEvent> map = new HashMap<String, MessageEvent>();
		for (MessageEvent m : igAP.getNodes()) {
			map.put(m.getLabel(), m);
		}
		NetBuilder nb = new NetBuilder();
		for (Event e : net.getInitalEvents()) {
			Event first = nb.insert(new Action(e.getName()));
			Event e2 = e;
			map2.put(e, first);
			nb.addInitial(first, "");

			while (e2.getPost().size() > 0) {
				Set<Event> post = e2.getPostEvents();
				if (post.size() == 0)
					break;
				if (post.size() > 1)
					throw new RuntimeException("not linear");
				e2 = post.iterator().next();
				Set<Event> related = getFirstRelatedPredecessor(net, e2, map);
				System.out.println(e2 + " related " + related);
				Set<Event> relatedTranslated = new HashSet<Event>();
				for (Event evt : related)
					relatedTranslated.add(map2.get(evt));
				Event newEvent = nb.insert(new Action(e2.getName()));
				map2.put(e2, newEvent);
				for (Event rel : relatedTranslated) {
					nb.connect(rel, newEvent, "");
				}
			}
		}
		return nb.getNet();
	}

	private static Set<Event> getFirstRelatedPredecessor(Net net, Event e2,
			HashMap<String, MessageEvent> map) {
		HashSet<Event> relatedEvents = new HashSet<Event>();
		Set<Event> pre = net.getPreEvents(e2);
		if (pre.size() > 1)
			throw new RuntimeException("not linear");
		Event current = pre.iterator().next();
		HashMap<Event, Set<Event>> related = new HashMap<Event, Set<Event>>();
		while (current != null) {
			if (!noneRelated(Collections.singleton(current), Collections
					.singleton(e2), map, related)
					|| !noneRelated(Collections.singleton(e2), Collections
							.singleton(current), map, related)) {
				relatedEvents.add(current);
			}
			Set<Event> pre2 = net.getPreEvents(current);
			if (pre2.size() == 0)
				return relatedEvents;
			if (pre2.size() > 1)
				throw new RuntimeException("not linear");
			current = pre2.iterator().next();
		}
		return relatedEvents;
	}
}
