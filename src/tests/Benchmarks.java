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

public class Benchmarks {
	private static final int REPETITIONS = 10;

	public static void main(String[] args) throws Exception {
		GraphVizExporter e = new GraphVizExporter();
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 15; i < 150; i += 15) {
			list.add(i);
		}
		ArrayList<HashMap<String,Long>> resList = new ArrayList<HashMap<String,Long>>();
		for (int count : list) {
			HashMap<String, Long> res = new HashMap<String, Long>();
			for (int i = 0; i < REPETITIONS; ++i) {
				TimedTask total = new TimedTask("total", 1);
				TimedTask load = new TimedTask("load", 1);
				GraphBuilder b = new GraphBuilder();
				PetersonReader<MessageEvent> r = new PetersonReader<MessageEvent>(
						b);
				r
						.readGraphSet(
								"traces/PetersonLeaderElection/generated_traces/peterson_trace-n5-1-s?.txt",
								count);
				Graph<MessageEvent> g = b.getRawGraph();
				load.stop();
				TimedTask invariants = new TimedTask("invariants", 1);
				PartitionGraph pg = new PartitionGraph(g, true);
				invariants.stop();

				TimedTask refinement = new TimedTask("refinement", 1);
				Bisimulation.refinePartitionsSmart(pg);
				refinement.stop();

				TimedTask coarsening = new TimedTask("coarsening", 1);
				Bisimulation.mergePartitions(pg);
				coarsening.stop();
				total.stop();
				record(res, load);
				record(res, invariants);
				record(res, refinement);
				record(res, coarsening);
				record(res, total);
				if (!res.containsKey("nodes"))
					res.put("nodes", 0L);
				res.put("nodes", res.get("nodes")+(long)g.getNodes().size());
			}
			for (Entry<String, Long> entry : res.entrySet()) {
				System.out.println(entry.getKey() + "\t" + entry.getValue() / REPETITIONS);
				res.put(entry.getKey(), entry.getValue()/REPETITIONS);
			}
			System.out.println();
			resList.add(res);
		}
		for (HashMap<String, Long> map : resList) {
			System.out.println(map.get("nodes") + " " +map.get("load") + " " +map.get("refinement") + " " +map.get("coarsening") + " " +map.get("invariants") + " " +map.get("total"));
		}
	}

	private static void record(HashMap<String, Long> res, TimedTask load) {
		if (!res.containsKey(load.getTask()))
			res.put(load.getTask(), 0L);
		res.put(load.getTask(), load.getTime()+res.get(load.getTask()));
	}
}