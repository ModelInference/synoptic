package synoptic.tests.integration;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.algorithms.Bisimulation;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.main.AbstractMain;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.PartitionGraph;
import synoptic.model.scalability.ScalableGraph;
import synoptic.tests.SynopticTest;

@RunWith(value = Parameterized.class)
public class PerformanceTests extends SynopticTest {

    int numIterations;
    int traceType;
    int totalEvents;
    int numPartitions;
    int numEventTypes;
    boolean withInvariants;
    boolean useFSMChecker;

    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { true, 3, 1, 1000, 10, 50, true },
                { false, 3, 1, 1000, 10, 50, true } };
        return Arrays.asList(data);
    }

    public PerformanceTests(boolean useFSMChecker, int numIterations,
            int traceType, int totalEvents, int numPartitions,
            int numEventTypes, boolean withInvariants) {
        this.useFSMChecker = useFSMChecker;
        this.numIterations = numIterations;
        this.traceType = traceType;
        this.totalEvents = totalEvents;
        this.numPartitions = numPartitions;
        this.numEventTypes = numEventTypes;
        this.withInvariants = withInvariants;
    }

    @Before
    public void setUp() throws ParseException {
        super.setUp();
        AbstractMain main = AbstractMain.getInstance();
        main.options.logLvlExtraVerbose = false;
        main.options.logLvlQuiet = true;
    }

    public void reportTime(long msTime) {
        System.out.println(testName.getMethodName() + ":" + "\n\tType "
                + traceType + "\n\ttotalEvents " + totalEvents
                + "\n\tnumPartitions " + numPartitions + "\n\tnumEventTypes "
                + numEventTypes + "\n\twithInvariants " + withInvariants
                + "\n\t==> TIME: " + msTime + "ms (averaged over "
                + numIterations + " iterations)\n");
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

    public TraceParser genParser() throws ParseException {
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
        return parser;
    }

    @Test
    public void bisimPerfTest() throws Exception {
        long total_delta = 0;
        for (int iter = 0; iter < numIterations; iter++) {
            TraceParser parser = genParser();

            String[] traces = partitionTrace(structure1Trace());
            PartitionGraph g = genInitialPartitionGraph(traces, parser,
                    new ChainWalkingTOInvMiner(), false);

            long startTime = System.currentTimeMillis();
            Bisimulation.splitUntilAllInvsSatisfied(g);
            total_delta += System.currentTimeMillis() - startTime;
            // exportTestGraph(g, 1);
        }
        long delta = total_delta / numIterations;
        reportTime(delta);
    }

    public void trivialGkTailPerfTest() throws Exception {
        long total_delta = 0;
        System.out.print("Trivial GK Tail Test");
        for (int iter = 0; iter < numIterations; iter++) {
            TraceParser parser = genParser();

            String[] traces = partitionTrace(structure1Trace());
            genInitialPartitionGraph(traces, parser,
                    new ChainWalkingTOInvMiner(), false);

            long startTime = System.currentTimeMillis();
            // TODO: call our k-Tail
            // KTail.kReduce(g, k, true, synoptic.invariants);
            total_delta += System.currentTimeMillis() - startTime;
        }
        long delta = total_delta / numIterations;
        reportTime(delta);
    }

    public void scalableGkTailPerfTest() {
        long total_delta = 0;
        System.out.print("Scalable GK Tail Test");
        // TODO: vary k
        int k = 0;
        for (int iter = 0; iter < numIterations; iter++) {
            @SuppressWarnings("unused")
            String[] traces = partitionTrace(structure1Trace());
            ScalableGraph sg = new ScalableGraph();
            // TODO
            // for (String[] trace : traces) {
            // sg.addGraph(buildGraph(trace));
            // }
            long startTime = System.currentTimeMillis();
            sg.kReduce(k, true, withInvariants);
            total_delta = System.currentTimeMillis() - startTime;
        }
        long delta = total_delta / numIterations;
        reportTime(delta);
    }

    private String[] structure1Trace() {
        String[] trace = new String[totalEvents];
        for (int i = 0; i < totalEvents; i++) {
            trace[i] = "" + i % numEventTypes;
        }
        return trace;
    }

    private String[] partitionTrace(String[] trace) {
        if (trace.length % numPartitions != 0) {
            throw new IllegalArgumentException(
                    "Cannot evenly divide trace into partitions");
        }

        int perPartition = trace.length / numPartitions;
        String[] partitioned = new String[trace.length + numPartitions - 1];

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
