package synoptic.tests.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.tests.SynopticTest;

@RunWith(value = Parameterized.class)
public class TOMiningPerformanceTests extends SynopticTest {

    ITOInvariantMiner miner = null;
    int numIterations;
    int totalEvents;
    int numPartitions;
    int numEventTypes;

    /**
     * Generates parameters for this unit test.
     * 
     * @return The set of parameters to pass to the constructor the unit test.
     */
    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
                { new TransitiveClosureInvMiner(false), 3, 1000, 10, 50 },
                { new TransitiveClosureInvMiner(true), 3, 1000, 10, 50 },
                { new ChainWalkingTOInvMiner(), 3, 10000, 10, 50 } };
        return Arrays.asList(data);
    }

    public TOMiningPerformanceTests(ITOInvariantMiner minerToUse,
            int numIterations, int totalEvents, int numPartitions,
            int numEventTypes) {
        miner = minerToUse;
        this.numIterations = numIterations;
        this.totalEvents = totalEvents;
        this.numPartitions = numPartitions;
        this.numEventTypes = numEventTypes;
    }

    @Before
    public void setUp() throws ParseException {
        super.setUp();
        SynopticMain syn = synoptic.main.SynopticMain
                .getInstanceWithExistenceCheck();
        syn.options.logLvlExtraVerbose = false;
        syn.options.logLvlQuiet = true;
    }

    public void reportTime(long msTime) {
        System.out.println(testName.getMethodName() + ":" + "\n\ttotalEvents "
                + totalEvents + "\n\tnumPartitions " + numPartitions
                + "\n\tnumEventTypes " + numEventTypes + "\n\t==> TIME: "
                + msTime + "ms (averaged over " + numIterations
                + " iterations)\n");
    }

    @Test
    public void mineInvariantsPerfTest() throws Exception {
        long total_delta = 0;
        long delta = 0;

        for (int iter = 0; iter < numIterations; iter++) {
            TraceParser parser = new TraceParser();
            parser.addRegex("^(?<TYPE>)$");
            parser.addPartitionsSeparator("^--$");

            // //////
            long startTime = System.currentTimeMillis();
            String[] traces = partitionTrace(structure1Trace());
            delta = System.currentTimeMillis() - startTime;
            // ////////
            System.out
                    .println("Done with generating trace in: " + delta + "ms");

            // //////
            startTime = System.currentTimeMillis();
            ArrayList<EventNode> parsedEvents = parseLogEvents(traces, parser);
            delta = System.currentTimeMillis() - startTime;
            // ////////
            System.out.println("Done with parsing trace in: " + delta + "ms");

            // //////
            startTime = System.currentTimeMillis();
            ChainsTraceGraph inputGraph = parser
                    .generateDirectTORelation(parsedEvents);
            delta = System.currentTimeMillis() - startTime;
            // ////////
            System.out.println("Done with generateDirectTemporalRelation in: "
                    + delta + "ms");

            System.out.println("Starting mining..");

            // /////////////
            startTime = System.currentTimeMillis();
            miner.computeInvariants(inputGraph, false);
            total_delta += System.currentTimeMillis() - startTime;
            // /////////////
        }
        delta = total_delta / numIterations;
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