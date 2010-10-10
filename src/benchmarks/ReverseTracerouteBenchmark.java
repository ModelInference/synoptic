package benchmarks;

import java.util.ArrayList;

import model.MessageEvent;
import model.PartitionGraph;
import model.interfaces.IGraph;
import tests.ReverseTraceroute;
import algorithms.bisim.Bisimulation;
import benchmarks.Benchmark.ConfigureableBenchmark;

/**
 * A benchmark for reverse traceroute.
 * @author Sigurd Schneider
 *
 */
public class ReverseTracerouteBenchmark {
	static private final int REPETITIONS = 2;
	static private  ConfigureableBenchmark<Integer> benchmarkFactory = new ConfigureableBenchmark<Integer>() {

		@Override
		protected void run(Integer val) {
			IGraph<MessageEvent> raw = ReverseTraceroute.readOverkill(val);
			PartitionGraph g = new PartitionGraph(raw, true);
			Bisimulation.refinePartitions(g);
			//Bisimulation.mergePartitions(g);
		}
		
	};
	static public void main(String[] args) {
		Benchmark<Integer> benchmark = new Benchmark<Integer>(REPETITIONS, benchmarkFactory);
		ArrayList<Integer> arguments = new ArrayList<Integer>();
		for (int i = 1; i < 10; i++) {
			arguments.add(i);
		}
		benchmark.run(arguments);
	}
}
