package synoptic.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.algorithms.graph.StronglyConnectedComponents;
import synoptic.benchmarks.TimedTask;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.input.GraphBuilder;
import synoptic.model.input.PetersonReader;
import synoptic.model.nets.PetriEvent;
import synoptic.model.nets.Net;

public class PetersonTest {
    static final String prefix = "/Users/ivan/synoptic/trunk/";

    public static void main(String[] args) throws Exception {
        TimedTask load = new TimedTask("load files");
        TimedTask total = new TimedTask("total");
        GraphBuilder gBuilder = new GraphBuilder();
        PetersonReader<LogEvent> petersonReader = new PetersonReader<LogEvent>(
                gBuilder);
        GraphVizExporter e = new GraphVizExporter();

        // r.readGraphSet("traces/PetersonLeaderElection/generated_traces/5node1seed_withid.trace",
        // 5);

        petersonReader.readGraphDirect(prefix + "afby-bug-peterson.log");
        // petersonReader.readGraphSet(prefix +
        // "traces/PetersonLeaderElection/generated_traces/peterson_trace-n5-1-s?.txt",
        // 5);

        // r.readGraphSet("traces/TapioExampleTrace/trace.txt",
        // 1);
        /*
         * r .readGraphSet(
         * "traces/PetersonLeaderElection/generated_traces/peterson_trace-rounds-0-s?.txt"
         * , 5);
         */
        Main.logLvlVerbose = true;
        Main.SetUpLogging();

        Graph<LogEvent> g = gBuilder.getGraph();
        int nodes = g.getNodes().size();
        System.out.println("Nodes: " + g.getNodes().size());
        load.stop();
        System.out.println(load);
        TimedTask invariants = new TimedTask("synoptic.invariants");
        // mineSplitInvariants(g, e);
        System.out.println("Computing Invariants...");
        e.exportAsDotAndPng(prefix + "output/peterson/initial.dot", g);
        System.out.println("Creating Partition Graph...");
        PartitionGraph pg = new PartitionGraph(g, true);
        TemporalInvariantSet s = pg.getInvariants();
        System.out.println("InvSize: " + s.numInvariants());
        invariants.stop();
        e.exportAsDotAndPng(prefix + "output/peterson/synoptic.invariants.dot",
                s.getInvariantGraph(null));
        e.exportAsDotAndPng(prefix
                + "output/peterson/synoptic.invariants-AP.dot", s
                .getInvariantGraph("AP"));
        e.exportAsDotAndPng(prefix
                + "output/peterson/synoptic.invariants-AFby.dot", s
                .getInvariantGraph("AFby"));
        e.exportAsDotAndPng(prefix
                + "output/peterson/synoptic.invariants-NFby.dot", s
                .getInvariantGraph("NFby"));
        System.out.println(s);
        TimedTask refinement = new TimedTask("refinement");
        // e.exportAsDotAndPngFast("output/peterson/initial-pg.dot", pg);
        System.out.println("Refining Partitions...");
        Bisimulation.splitPartitions(pg);
        System.out.println("Refined to " + pg.getNodes().size() + " nodes.");
        // e.exportAsDotAndPngFast("output/peterson/output-pg.dot", pg);
        refinement.stop();
        pg.checkSanity();
        TimedTask coarsening = new TimedTask("coarsening");
        System.out.println("Merging Partitions...");
        Bisimulation.mergePartitions(pg);
        coarsening.stop();
        total.stop();
        System.out.println("Merge done.");
        e.exportAsDotAndPngFast(
                prefix + "output/peterson/output-pg-merged.dot", pg);
        // exportSCCsWithInvariants(e, pg);
        // Bisimulation.mergePartitions(pg);
        // NetBuilder netBuilder = new NetBuilder();
        // GraphUtil.copyTo(pg, netBuilder);
        // Net net = netBuilder.getNet();

        // HashMap<PetriEvent, ArrayList<PetriEvent>> entries = getEventSequences(net);
        // e.exportAsDotAndPng("output/peterson/output-net.dot", net);
        // for (ArrayList<PetriEvent> seq : entries.values()) {
        // if (seq.size() > 1) {
        // System.out.println(seq);
        // net.replace(seq, conciseName(seq));
        // }
        // }

        // e.exportAsDotAndPng("output/peterson/output-net-condensed.dot", net);

        System.out.println(nodes + " Nodes");
        System.out.println(load);
        System.out.println(invariants);
        System.out.println(refinement);
        System.out.println(coarsening);
        System.out.println(total);
    }

    private static void exportSCCsWithInvariants(GraphVizExporter e,
            PartitionGraph pg) throws Exception {
        StronglyConnectedComponents<Partition> sccs = new StronglyConnectedComponents<Partition>(
                pg);
        int partN = 0;
        for (Set<Partition> scc : sccs) {
            Graph<Partition> graph = new Graph<Partition>();
            Graph<LogEvent> messageGraph = new Graph<LogEvent>();
            for (Partition p : scc) {
                graph.add(p);
                for (LogEvent m : p.getMessages()) {
                    messageGraph.add(m);
                }
            }
            e.exportAsDotAndPngFast(
                    prefix + "output/peterson/messageGraph.dot", messageGraph);
            e.exportAsDotAndPngFast(prefix + "output/peterson/partition-"
                    + partN + ".dot", graph);
            System.out.println(scc);
            TemporalInvariantSet.generateStructuralInvariants = true;
            TemporalInvariantSet s2 = TemporalInvariantSet
                    .computeInvariants(messageGraph);
            e.exportAsDotAndPngFast(prefix + "output/peterson/partition-"
                    + partN + "-synoptic.invariants.dot", s2
                    .getInvariantGraph("AP"));
            TemporalInvariantSet.generateStructuralInvariants = false;
            partN++;
        }
    }

    private static void mineSplitInvariants(Graph<LogEvent> g,
            GraphVizExporter exporter) throws Exception {
        HashMap<String, HashMap<String, Set<LogEvent>>> buckets = new HashMap<String, HashMap<String, Set<LogEvent>>>();
        for (LogEvent e : g.getNodes()) {
            if (!buckets.containsKey(e.getStringArgument("nodeName"))) {
                buckets.put(e.getStringArgument("nodeName"),
                        new HashMap<String, Set<LogEvent>>());
            }
            if (!buckets.get(e.getStringArgument("nodeName")).containsKey(
                    e.getStringArgument("localRoundId"))) {
                buckets.get(e.getStringArgument("nodeName")).put(
                        e.getStringArgument("localRoundId"),
                        new HashSet<LogEvent>());
            }
            buckets.get(e.getStringArgument("nodeName")).get(
                    e.getStringArgument("localRoundId")).add(e);
        }
        for (String key : buckets.keySet()) {
            for (Entry<String, Set<LogEvent>> e : buckets.get(key)
                    .entrySet()) {
                Graph<LogEvent> sg = new Graph<LogEvent>();
                for (LogEvent ev : e.getValue()) {
                    sg.add(ev);
                }
                TemporalInvariantSet inv = TemporalInvariantSet
                        .computeInvariants(sg);
                System.out.println(e.getKey() + ": " + inv);
                exporter.exportAsDotAndPng(prefix
                        + "output/peterson/synoptic.invariants-node" + key
                        + "-round" + e.getKey() + ".dot", inv
                        .getInvariantGraph(null));
            }
        }
    }

    private static HashMap<PetriEvent, ArrayList<PetriEvent>> getEventSequences(Net net) {
        HashMap<PetriEvent, ArrayList<PetriEvent>> entries = new HashMap<PetriEvent, ArrayList<PetriEvent>>();
        HashSet<PetriEvent> seen = new HashSet<PetriEvent>();
        for (PetriEvent event : net.getEvents()) {
            if (!seen.add(event)) {
                continue;
            }
            Set<PetriEvent> post = event.getPostEvents();
            if (post.size() != 1) {
                continue;
            }
            if (net.getPreEvents(event).size() > 1) {
                continue;
            }
            entries.put(event, new ArrayList<PetriEvent>(Collections
                    .singleton(event)));
            Iterator<PetriEvent> iter = post.iterator();
            while (iter.hasNext()) {
                PetriEvent next = iter.next();
                seen.add(next);
                if (entries.get(event).contains(next)) {
                    break;
                }
                if (net.getPreEvents(next).size() > 1) {
                    break;
                }
                Set<PetriEvent> post2 = next.getPostEvents();
                if (post2.size() > 1) {
                    break;
                }
                if (entries.containsKey(next)) {
                    for (PetriEvent old : entries.get(next)) {
                        if (entries.get(event).contains(old)) {
                            break;
                        }
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

    private static String conciseName(ArrayList<PetriEvent> seq) {
        ArrayList<String> names = new ArrayList<String>();
        for (PetriEvent e : seq) {
            names.add(e.getName().charAt(0) + ""
                    + e.getName().charAt(e.getName().length() - 1));
        }
        Collections.sort(names);
        return names.toString();
    }
}
