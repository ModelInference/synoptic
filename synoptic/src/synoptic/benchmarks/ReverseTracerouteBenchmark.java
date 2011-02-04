package synoptic.benchmarks;

import java.util.ArrayList;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.benchmarks.Benchmark.ConfigureableBenchmark;
import synoptic.model.LogEvent;
import synoptic.model.PartitionGraph;
import synoptic.model.interfaces.IGraph;

/**
 * A benchmark for reverse traceroute.
 * 
 * @author Sigurd Schneider
 */
public class ReverseTracerouteBenchmark {
    static private final int REPETITIONS = 1;
    static private ConfigureableBenchmark<Integer> benchmarkFactory = new ConfigureableBenchmark<Integer>() {

        @Override
        protected void run(Integer val) {
            IGraph<LogEvent> raw = ReverseTraceroute.readOverkill(val);
            PartitionGraph g = new PartitionGraph(raw, true);
            Bisimulation.splitPartitions(g);
            // Bisimulation.mergePartitions(g);
        }

    };

    static public void main(String[] args) {
        Benchmark<Integer> benchmark = new Benchmark<Integer>(REPETITIONS,
                benchmarkFactory);
        ArrayList<Integer> arguments = new ArrayList<Integer>();
        for (int i = 1; i < 30; i++) {
            arguments.add(i);
        }
        benchmark.run(arguments);
    }
}
