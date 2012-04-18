package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import synoptic.model.EventNode;
import synoptic.tests.DAGsTraceGraphBaseTest;

/**
 * Tests for the DAGsTraceGraph.
 */
public class DAGsTraceGraphTests extends DAGsTraceGraphBaseTest {

    /**
     * Tests the topological order of a DAG that looks like:
     * 
     * <pre>
     * a -> b -> d
     *  \-> c
     * </pre>
     */
    @Test
    public void forkDAGTopoOrderTest() {
        dag = buildForkDAG();
        List<EventNode> order = dag.computeTopologicalOrder(0,
                FollowedByRelationsSet);

        List<List<EventNode>> trueOrders = new LinkedList<List<EventNode>>();
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, c, d }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, c, b, d }));

        assertTrue(trueOrders.contains(order));
    }

    /**
     * Tests the topological order of a DAG that looks like:
     * 
     * <pre>
     * a -> b ---> d -> e
     *  \-> c -/    \-> f
     * </pre>
     */
    @Test
    public void complexForkDAGTopoOrderTest() {
        dag = buildComplexForkDAG();
        List<EventNode> order = dag.computeTopologicalOrder(0,
                FollowedByRelationsSet);

        List<List<EventNode>> trueOrders = new LinkedList<List<EventNode>>();
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, c, d, e, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, c, d, f, e }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, c, b, d, e, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, c, b, d, f, e }));

        assertTrue(trueOrders.contains(order));
    }

    /**
     * Tests the topological order of a DAG that looks like:
     * 
     * <pre>
     * a
     *  \
     *   --> c --> d
     *  /
     * b
     * </pre>
     */
    @Test
    public void twoSourcesDAGTopoOrderTest() {
        dag = buildTwoSourcesDAG();
        List<EventNode> order = dag.computeTopologicalOrder(0,
                FollowedByRelationsSet);
        List<List<EventNode>> trueOrders = new LinkedList<List<EventNode>>();
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, c, d }));
        assertTrue(trueOrders.contains(order));
    }

    /**
     * Tests the topological order of a DAG that looks like:
     * 
     * <pre>
     * a --> c --
     *  \        \
     *   --> d --> f
     *  /
     * b --> e
     * </pre>
     */
    @Test
    public void twoSourcesComplexDAGTopoOrderTest() {
        dag = buildTwoSourcesComplexDAG();
        List<EventNode> order = dag.computeTopologicalOrder(0,
                FollowedByRelationsSet);

        List<List<EventNode>> trueOrders = new LinkedList<List<EventNode>>();
        // a, b
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, c, d, e, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, c, d, f, e }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, c, e, d, f }));

        trueOrders.add(Arrays.asList(new EventNode[] { a, b, e, c, d, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, e, d, c, f }));

        trueOrders.add(Arrays.asList(new EventNode[] { a, b, d, e, c, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, d, c, e, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, b, d, c, f, e }));

        // a, c
        trueOrders.add(Arrays.asList(new EventNode[] { a, c, b, e, d, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, c, b, d, e, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, c, b, d, f, e }));

        trueOrders.add(Arrays.asList(new EventNode[] { a, c, d, f, b, e }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, c, d, b, f, e }));
        trueOrders.add(Arrays.asList(new EventNode[] { a, c, d, b, e, f }));

        // b, a
        trueOrders.add(Arrays.asList(new EventNode[] { b, a, c, d, e, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { b, a, c, d, f, e }));
        trueOrders.add(Arrays.asList(new EventNode[] { b, a, c, e, d, f }));

        trueOrders.add(Arrays.asList(new EventNode[] { b, a, e, c, d, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { b, a, e, d, c, f }));

        trueOrders.add(Arrays.asList(new EventNode[] { b, a, d, e, c, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { b, a, d, c, e, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { b, a, d, c, f, e }));

        trueOrders.add(Arrays.asList(new EventNode[] { b, e, a, c, d, f }));
        trueOrders.add(Arrays.asList(new EventNode[] { b, e, a, d, c, f }));

        assertTrue(trueOrders.contains(order));
    }

}
