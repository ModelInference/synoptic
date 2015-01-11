import java.io.File;
import java.io.FileInputStream;

import benchmarks.TimedTask;

import algorithms.bisim.Bisimulation;

import MessageTrace.TraceSet;
import model.PartitionGraph;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;

public class CreateTwitGraph {
    public static void main(String[] args) throws Exception {
        GraphVizExporter exporter = new GraphVizExporter();
        TraceSet tr = TraceSet.parseFrom(new FileInputStream(
                "src/trace/twitter/TwitTrace2.trace"));
        PartitionGraph orgBisim = GraphBuilder.buildGraph(tr, true);

        Bisimulation.refinePartitions(orgBisim);
        Bisimulation.mergePartitions(orgBisim);

        File file2 = new File("output/TwitTrace2-bisim.dot");
        exporter.export(file2, orgBisim);
        exporter.exportPng(file2);

        // File file = new File("output/TwitTrace2.dot");
        // exporter.export(file, orgGK);
        // exporter.exportPng(file);
        int acc = 0;
        int TIMES = 5;
        for (int i = 0; i < TIMES; ++i) {
            TimedTask t = new TimedTask("time");
            PartitionGraph orgGK = GraphBuilder.buildGraph(tr, true);

            // Bisimulation.mergePartitions(orgGK, orgGK.getInvariants(), 0);
            Bisimulation.refinePartitions(orgGK);
            Bisimulation.mergePartitions(orgGK);
            t.stop();
            acc += t.getTime();
        }
        System.out.println(acc / TIMES);
        // File file3 = new File("output/TwitTrace2-gktail.dot");
        // exporter.export(file3, orgGK);
        // exporter.exportPng(file3);
    }

}
