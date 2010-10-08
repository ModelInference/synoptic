package tests;

import algorithms.bisim.Bisimulation;
import junit.framework.TestCase;
import model.PartitionGraph;
import model.input.GraphBuilder;
import model.scalability.ScalableGraph;

public class PerformanceTests extends TestCase {
	int iterations = 3;
	
	public void testPerf() {
		//runPerformanceVaryn(800, 10);
		//runPerformanceVaryM(10, 2);
		try {
			runTestBisim(1, 1000, 10, 50, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void runPerformanceVaryM(int r, int n) {
		System.out.println("*** Varying M parameter ****");
	/*	for (int M = 100; M < 1000; M+= 100) {
			runTestTrivialGkTail(1, M, n, r, 1, false);
		}
		for (int M = 100; M < 1000; M+= 100) {
			runTestScalableGkTail(1, M, n, r, 1, false);
		}*/
		for (int M = 100; M < 1000; M+= 100) {
			try {
				runTestBisim(1, M, n, r, false);
			} catch (Exception exn) {
				throw new RuntimeException(exn);
			}
		}
	}
	
	public void runPerformanceVaryn(int M, int r) {
		System.out.println("*** Varying n parameter ****");
		int[] n_values = new int[]{1,2,4,5,10};
		/*for (int i = 0; i < n_values.length; i++) {
			runTestTrivialGkTail(1, M, n_values[i], r, 1, false);
		}
		for (int i = 0; i < n_values.length; i++) {
			runTestScalableGkTail(1, M, n_values[i], r, 1, false);
		}*/
		for (int i = 0; i < n_values.length; i++) {
			try {
				runTestBisim(1, M, n_values[i], r, false);
			} catch (Exception exn) {
				exn.printStackTrace();
				throw new RuntimeException(exn);
			}
		}
	}
	
	private void runTestBisim(int traceType, int M, int n, int r,
			boolean invariants) throws Exception {
		long total_delta = 0;
		System.out.print("Bisimulation Test Parameters ( Trace Type=" + traceType + " M=" + M
				+ " n=" + n + " r=" + r + " invariants="
				+ invariants + ")");
		for (int iter = 0; iter < iterations; iter++) {
	
			String[][] traces = (traceType == 1) ? partitionTrace(structure1Trace(
					M, r), n) : partitionTrace(structure1Trace(M, r), n);
			PartitionGraph g = GraphBuilder.buildGraph(traces);
	
			long startTime = System.currentTimeMillis();
			Bisimulation.refinePartitions(g);
			total_delta  += System.currentTimeMillis() - startTime;
		}
		long delta = total_delta / iterations;
		System.out.println(" ==> TIME: " + delta + " ms (average over " + iterations + " iterations)");
	}
	

	private void runTestTrivialGkTail(int traceType, int M, int n, int r, int k,
			boolean invariants) {
		long total_delta = 0;
		System.out.print("Trivial GK Tail Test Parameters ( Trace Type=" + traceType + " M=" + M
				+ " n=" + n + " r=" + r + " k=" + k + " invariants="
				+ invariants + ")");
		for (int iter = 0; iter < iterations; iter++) {
	
			String[][] traces = (traceType == 1) ? partitionTrace(structure1Trace(
					M, r), n) : partitionTrace(structure1Trace(M, r), n);
			PartitionGraph g = GraphBuilder.buildGraph(traces);
	
			long startTime = System.currentTimeMillis();
			// TODO: call our k-Tail
			// KTail.kReduce(g, k, true, invariants);
			total_delta  += System.currentTimeMillis() - startTime;
		}
		long delta = total_delta / iterations;
		System.out.println(" ==> TIME: " + delta + " ms (average over " + iterations + " iterations)");
	}
	
	private void runTestScalableGkTail(int traceType, int M, int n, int r, int k,
			boolean invariants) {
		long total_delta = 0;
		System.out.print("Scalable GK Tail Test Parameters ( Trace Type=" + traceType + " M=" + M
				+ " n=" + n + " r=" + r + " k=" + k + " invariants="
				+ invariants + ")");
		for (int iter = 0; iter < iterations; iter++) {
			
			String[][] traces = (traceType == 1) ? partitionTrace(structure1Trace(
					M, r), n) : partitionTrace(structure1Trace(M, r), n);
			ScalableGraph sg = new ScalableGraph();
			for (String[] trace : traces) {
				sg.addGraph(GraphBuilder.buildGraph(trace));
			}
			long startTime = System.currentTimeMillis();
			PartitionGraph g  = sg.kReduce(k, true, invariants);
			total_delta = System.currentTimeMillis() - startTime;
		}
		long delta = total_delta / iterations;
		System.out.println(" ==> TIME: " + delta + " ms (average over " + iterations + " iterations)");
	}

	private String[] structure1Trace(int M, int r) {
		String[] trace = new String[M];
		for (int i = 0; i < M; i++) {
			trace[i] = "msg" + (i % r);
		}
		return trace;
	}

	private String[][] partitionTrace(String[] trace, int n) {
		if (trace.length % n != 0)
			throw new IllegalArgumentException(
					"Cannot evenly divide trace into partitions");

		String[][] partitions = new String[n][];
		for (int i = 0; i < n; i++) {
			int partitionLength = trace.length / n;
			int partitionOffset = partitionLength * i;
			String[] partition = new String[partitionLength];
			for (int j = 0; j < partitionLength; j++) {
				partition[j] = trace[j + partitionOffset];
			}
			partitions[i] = partition;
		}
		return partitions;
	}

}
