package model.nets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import algorithms.bisim.Bisimulation;
import algorithms.graph.GraphUtil;

import invariants.TemporalInvariantSet;
import model.Action;
import model.Graph;
import model.MessageEvent;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;
import model.input.NetBuilder;
import model.input.PetersonReader;

public class NetTest {
	static public void main(String[] args) throws Exception {
		GraphBuilder graphBuilder = new GraphBuilder();
		PetersonReader<MessageEvent> r = new PetersonReader<MessageEvent>(graphBuilder);
		GraphVizExporter e = new GraphVizExporter();
		r
				.readGraphSet(
						"traces/PetersonLeaderElection/generated_traces/5process_trace-5-?.txt",
						5);
		/*peterson_trace-n5-s?*/
		Graph<MessageEvent> g = graphBuilder.getRawGraph();
		e.exportAsDotAndPng("output/petri/raw.dot", g);
		TemporalInvariantSet s = TemporalInvariantSet.computeInvariants(g);
		Graph<MessageEvent> igAP = s.getInvariantGraph("AP");
		Graph<MessageEvent> igNFby = s.getInvariantGraph("NFby");
		Graph<MessageEvent> igAFby = s.getInvariantGraph("AFby");
		e.exportAsDotAndPng("output/petri/invariants-AP.dot", igAP);
		e.exportAsDotAndPng("output/petri/invariants-NFby.dot", igNFby);
		e.exportAsDotAndPng("output/petri/invariants-AFby.dot", igAFby);
		GraphUtil.heuristicTransitiveReduction(igAP, "AP");
		NetBuilder b = new NetBuilder(true);
		GraphUtil.copyTo(igAP, b);
		Net n = b.getNet();

		e.exportAsDotAndPng("output/petri/initial-wo-end.dot", n);

		/*
		 * Place finalPlace = n.createPlace(); for (Event evt :
		 * n.getTerminalEvents()) { n.connect(evt, finalPlace); }
		 */
		n.mergeTerminalPlaces();

		HashMap<String, Event> eventMap = new HashMap<String, Event>();
		for (Event evt : n.getEvents()) {
			eventMap.put(evt.getName(), evt);
		}

		e.exportAsDotAndPng("output/petri/initial.dot", n);

		// mutex
		// Action NFbyRelation = new Action("NFby");
		// for (Message m1 : igNFby.getNodes()) {
		// for (Message m2 : igNFby.getNodes()) {
		// if (m1 != m2 && m1.getTransition(m2, NFbyRelation) != null
		// && m2.getTransition(m1, NFbyRelation) != null) {
		// Event lub = getLUB(eventMap.get(m1.getLabel()),
		// eventMap.get(m2.getLabel()), n);
		//
		// System.out.println("exclusive " + m1 + " " + m2 + " at "
		// + lub);
		// n.makeExclusive(eventMap.get(m1.getLabel()), eventMap
		// .get(m2.getLabel()), lub);
		// }
		// }
		// }

		e.exportAsDotAndPng("output/petri/initial-mutex.dot", n);

		SimulationChecker simc = new SimulationChecker(n);
		ArrayList<String> trace = new ArrayList<String>();
		trace.addAll(Arrays.asList("active", "send1", "recv1", "send2",
				"recv2", "relay"));
		for (String t : trace) {
			if (!simc.tryPerform(t))
				System.out.println("Could not perform " + t);
		}
		if (!simc.isFinished())
			System.out.println("not run till end");

		NetBuilder netBuilder = new NetBuilder(true);
		GraphUtil.copyTo(g, netBuilder);
		Net net = netBuilder.getNet();
		e.exportAsDotAndPng("output/petri/net-init.dot", net);
		for (Event initialEvent : net.getInitalEvents()) {
			Event work = initialEvent;
			while (work != null) {
				ArrayList<Event> seq = new ArrayList<Event>();
				Event myWork = work;
				Iterator<Event> iter2 = myWork.getPostEvents().iterator();
				if (iter2.hasNext()) {
					work = iter2.next();
				} else
					work = null;
				SimulationChecker sc = new SimulationChecker(n);
				if (!sc.tryPerform(myWork.getName())) {
					//System.out.println("could not start with " + myWork);
					continue;
				}
				seq.add(myWork);
				Iterator<Event> iter = myWork.getPostEvents().iterator();
				Event next = null;
				while (iter.hasNext()) {
					next = iter.next();
					if (!sc.tryPerform(next.getName())) {
						break;
					}
					seq.add(next);
					iter = next.getPostEvents().iterator();
				}
				Set<String> reason = sc.isFinishedReason();
				if (reason.size() > 0) {
					if (reason.size() >= 1 && reason.size() <= 2 
							&& (reason.iterator().next().contains("leader") || reason
									.iterator().next().contains("relay"))) {
						System.out.println("Simulated (incomplete): " + seq);
						work = next;
					} else {
						System.out.println("could not simulate sequence: "
								+ seq + " " +next);
						continue;
					}
				} else
					System.out.println("Simulated: " + seq);
				net.replace(seq, "Net");
			}
		}
		e.exportAsDotAndPng("output/petri/net-final.dot", net);
		GraphBuilder gBuilder = new GraphBuilder(); 
		GraphUtil.copyNetTo(net, gBuilder); 
		Graph<MessageEvent> condensedGraph = gBuilder.getRawGraph();
		e.exportAsDotAndPng("output/petri/condensed-input.dot", condensedGraph);
		PartitionGraph pgCondensed = new PartitionGraph(condensedGraph, true);
		e.exportAsDotAndPng("output/petri/condensed-merged.dot", pgCondensed);
		Bisimulation.refinePartitions(pgCondensed);
		e.exportAsDotAndPng("output/petri/condensed-refined.dot", pgCondensed);
	}

	static public Event getLUB(Event e1, Event e2, Net n) {
		Set<Event> preM1 = new HashSet<Event>();
		preM1.add(e1);
		Set<Event> preM2 = new HashSet<Event>();
		preM2.add(e2);
		for (;;) {
			for (Event ev1 : preM1)
				for (Event ev2 : preM2)
					if (ev1 == ev2)
						return ev1;

			Set<Event> add1 = new HashSet<Event>();
			for (Event ev1 : preM1)
				add1.addAll(n.getPreEvents(ev1));
			for (Event ev1 : add1)
				for (Event ev2 : preM2)
					if (ev1 == ev2)
						return ev1;

			Set<Event> add2 = new HashSet<Event>();
			for (Event ev1 : preM2)
				add2.addAll(n.getPreEvents(ev1));
			for (Event ev1 : preM1)
				for (Event ev2 : add2)
					if (ev1 == ev2)
						return ev1;
			if (!preM1.addAll(add1) && !preM2.addAll(add2))
				break;
		}
		return null;
	}
}
