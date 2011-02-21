package synoptic.tests.integration;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.PartitionGraph;
import synoptic.model.scalability.ScalableGraph;
import synoptic.tests.SynopticTest;

@RunWith(value = Parameterized.class)
public class PerformanceTests extends SynopticTest {
    int iterations = 3;

    TraceParser parser = null;

    int traceType;
    int M;
    int n;
    int r;
    boolean invariants;

    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { 1, 1000, 10, 50, false } };
        return Arrays.asList(data);
    }

    public PerformanceTests(int traceType, int M, int n, int r,
            boolean invariants) {
        this.traceType = traceType;
        this.M = M;
        this.n = n;
        this.r = r;
        this.invariants = invariants;
        Main.logLvlExtraVerbose = false;
        Main.logLvlQuiet = true;
    }

    @Override
    public void setUp() throws ParseException {
        super.setUp();
        parser = new TraceParser();
        parser.addRegex("^(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
    }

    // public void testPerf() {
    // // runPerformanceVaryn(800, 10);
    // // runPerformanceVaryM(10, 2);
    // try {
    // runTestBisim(1, 1000, 10, 50, false);
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

    // ///////////////////////////////////////////////////
    // TODO: have this method parametrize the test-case.

    // public void runPerformanceVaryM(int r, int n) {
    // System.out.println("*** Varying M parameter ****");
    // /*
    // * for (int M = 100; M < 1000; M+= 100) { runTestTrivialGkTail(1, M, n,
    // * r, 1, false); } for (int M = 100; M < 1000; M+= 100) {
    // * runTestScalableGkTail(1, M, n, r, 1, false); }
    // */
    // for (int M = 100; M < 1000; M += 100) {
    // try {
    // runTestBisim(1, M, n, r, false);
    // } catch (Exception e) {
    // throw InternalSynopticException.Wrap(e);
    // }
    // }
    // }

    // ///////////////////////////////////////////////////
    // TODO: have this method parametrize the test-case.

    // public void runPerformanceVaryn(int M, int r) {
    // System.out.println("*** Varying n parameter ****");
    // int[] n_values = new int[] { 1, 2, 4, 5, 10 };
    // /*
    // * for (int i = 0; i < n_values.length; i++) { runTestTrivialGkTail(1,
    // * M, n_values[i], r, 1, false); } for (int i = 0; i < n_values.length;
    // * i++) { runTestScalableGkTail(1, M, n_values[i], r, 1, false); }
    // */
    // for (int i = 0; i < n_values.length; i++) {
    // try {
    // runTestBisim(1, M, n_values[i], r, false);
    // } catch (Exception e) {
    // throw InternalSynopticException.Wrap(e);
    // }
    // }
    // }

    private PartitionGraph buildGraph(String[] traces) throws Exception {
        return genInitialPartitionGraph(traces, parser);
    }

    @Test
    public void runTestBisim() throws Exception {
        long total_delta = 0;
        System.out.print("Bisimulation Test Parameters ( Trace Type="
                + traceType + " M=" + M + " n=" + n + " r=" + r
                + " synoptic.invariants=" + invariants + ")");
        for (int iter = 0; iter < iterations; iter++) {

            String[] traces = partitionTrace(structure1Trace(M, r), n);
            PartitionGraph g = buildGraph(traces);

            long startTime = System.currentTimeMillis();
            Bisimulation.splitPartitions(g);
            total_delta += System.currentTimeMillis() - startTime;
        }
        long delta = total_delta / iterations;
        System.out.println("\n\n==> TIME: " + delta + " ms (average over "
                + iterations + " iterations)");
    }

    public void runTestTrivialGkTail(int traceType, int M, int n, int r, int k,
            boolean invariants) throws Exception {
        long total_delta = 0;
        System.out.print("Trivial GK Tail Test Parameters ( Trace Type="
                + traceType + " M=" + M + " n=" + n + " r=" + r + " k=" + k
                + " synoptic.invariants=" + invariants + ")");
        for (int iter = 0; iter < iterations; iter++) {

            String[] traces = partitionTrace(structure1Trace(M, r), n);
            PartitionGraph g = buildGraph(traces);

            long startTime = System.currentTimeMillis();
            // TODO: call our k-Tail
            // KTail.kReduce(g, k, true, synoptic.invariants);
            total_delta += System.currentTimeMillis() - startTime;
        }
        long delta = total_delta / iterations;
        System.out.println(" ==> TIME: " + delta + " ms (average over "
                + iterations + " iterations)");
    }

    public void runTestScalableGkTail(int traceType, int M, int n, int r,
            int k, boolean invariants) {
        long total_delta = 0;
        System.out.print("Scalable GK Tail Test Parameters ( Trace Type="
                + traceType + " M=" + M + " n=" + n + " r=" + r + " k=" + k
                + " synoptic.invariants=" + invariants + ")");
        for (int iter = 0; iter < iterations; iter++) {

            String[] traces = partitionTrace(structure1Trace(M, r), n);
            ScalableGraph sg = new ScalableGraph();
            // TODO
            // for (String[] trace : traces) {
            // sg.addGraph(buildGraph(trace));
            // }
            long startTime = System.currentTimeMillis();
            PartitionGraph g = sg.kReduce(k, true, invariants);
            total_delta = System.currentTimeMillis() - startTime;
        }
        long delta = total_delta / iterations;
        System.out.println(" ==> TIME: " + delta + " ms (average over "
                + iterations + " iterations)");
    }

    private String[] structure1Trace(int M, int r) {
        String[] trace = new String[M];
        for (int i = 0; i < M; i++) {
            trace[i] = "" + i % r;
        }
        return trace;
    }

    private String[] partitionTrace(String[] trace, int n) {
        if (trace.length % n != 0) {
            throw new IllegalArgumentException(
                    "Cannot evenly divide trace into partitions");
        }

        int perPartition = trace.length / n;
        String[] partitioned = new String[trace.length + n - 1];

        int inPartCnt = 0;
        int j = 0;
        for (int i = 0; i < trace.length; i++) {
            partitioned[j] = trace[i];
            if (inPartCnt == perPartition) {
                partitioned[j + 1] = "--";
                j += 2;
                inPartCnt = 0;
                continue;
            }
            j++;
            inPartCnt += 1;
        }
        return partitioned;
    }

}
