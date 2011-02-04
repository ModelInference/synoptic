package synoptic.tests;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.input.GraphBuilder;
import synoptic.model.input.StenningReader;

public class StenningDataLinkTest {
    public static void main(String[] args) throws Exception {
        GraphBuilder b = new GraphBuilder();
        StenningReader<LogEvent> r = new StenningReader<LogEvent>(b);
        GraphVizExporter e = new GraphVizExporter();
        // r.readGraphSet("traces/PetersonLeaderElection/generated_traces/5node1seed_withid.trace",
        // 5);
        // r.readGraphSet("traces/PetersonLeaderElection/generated_traces/peterson_trace-more-n5-1-s?.txt",
        // 5);
        r.readGraphSet(
                "./traces/StenningDataLink/generated_traces/t-10-0.5-0-s?.txt",
                5);
        Graph<LogEvent> g = b.getGraph();
        e.exportAsDotAndPng("output/stenning/initial.dot", g);
        TemporalInvariantSet s = TemporalInvariantSet.computeInvariants(g);
        e.exportAsDotAndPng("output/stenning/synoptic.invariants.dot", s
                .getInvariantGraph(null));
        e.exportAsDotAndPng("output/stenning/synoptic.invariants-AP.dot", s
                .getInvariantGraph("AP"));
        e.exportAsDotAndPng("output/stenning/synoptic.invariants-AFby.dot", s
                .getInvariantGraph("AFby"));
        e.exportAsDotAndPng("output/stenning/synoptic.invariants-NFby.dot", s
                .getInvariantGraph("NFby"));
        System.out.println(s);
        PartitionGraph pg = new PartitionGraph(g, true);
        e.exportAsDotAndPng("output/stenning/initial-pg.dot", pg);
        Bisimulation.splitPartitions(pg);
        e.exportAsDotAndPng("output/stenning/output-pg.dot", pg);
        Bisimulation.mergePartitions(pg);
        System.out.println("Merge done.");
        e.exportAsDotAndPng("output/stenning/output-pg-merged.dot", pg);
    }
}
