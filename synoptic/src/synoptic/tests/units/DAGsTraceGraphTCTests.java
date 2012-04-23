package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.algorithms.TransitiveClosure;
import synoptic.model.EventNode;
import synoptic.tests.DAGsTraceGraphBaseTest;

@RunWith(value = Parameterized.class)
public class DAGsTraceGraphTCTests extends DAGsTraceGraphBaseTest {
    boolean useFloysWarshall;

    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { true }, { false } };
        return Arrays.asList(data);
    }

    public DAGsTraceGraphTCTests(boolean useFloysWarshall) {
        this.useFloysWarshall = useFloysWarshall;
    }

    public void addToTC(Map<EventNode, Set<EventNode>> tc, EventNode e1,
            EventNode e2) {
        if (!tc.containsKey(e1)) {
            tc.put(e1, new LinkedHashSet<EventNode>());
        }
        tc.get(e1).add(e2);
    }

    /*******************************************************************/

    /**
     * Tests the TC of a DAG that looks like:
     * 
     * <pre>
     * a -> b -> d
     *  \-> c
     * </pre>
     */
    @Test
    public void forkGraphTCTest() {
        dag = buildForkDAG();
        TransitiveClosure tc = dag.getTransitiveClosure("followed by",
                useFloysWarshall);

        Map<EventNode, Set<EventNode>> tc2 = new LinkedHashMap<EventNode, Set<EventNode>>();
        tc2.put(a,
                new HashSet<EventNode>(Arrays
                        .asList(new EventNode[] { b, c, d })));

        tc2.put(b, new HashSet<EventNode>(Arrays.asList(new EventNode[] { d })));

        assertTrue(tc2.equals(tc.getTC()));
    }

    /**
     * Tests the TC of a DAG that looks like:
     * 
     * <pre>
     * a -> b ---> d -> e
     *  \-> c -/    \-> f
     * </pre>
     */
    @Test
    public void complexForkGraphTCTest() {
        dag = buildComplexForkDAG();
        TransitiveClosure tc = dag.getTransitiveClosure("followed by",
                useFloysWarshall);

        Map<EventNode, Set<EventNode>> tc2 = new LinkedHashMap<EventNode, Set<EventNode>>();

        tc2.put(a,
                new HashSet<EventNode>(Arrays.asList(new EventNode[] { b, c, d,
                        e, f })));

        tc2.put(b,
                new HashSet<EventNode>(Arrays
                        .asList(new EventNode[] { d, e, f })));

        tc2.put(c,
                new HashSet<EventNode>(Arrays
                        .asList(new EventNode[] { d, e, f })));

        tc2.put(d,
                new HashSet<EventNode>(Arrays.asList(new EventNode[] { e, f })));

        assertTrue(tc2.equals(tc.getTC()));
    }

    /**
     * Tests the TC of a DAG that looks like:
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
        TransitiveClosure tc = dag.getTransitiveClosure("followed by",
                useFloysWarshall);

        Map<EventNode, Set<EventNode>> tc2 = new LinkedHashMap<EventNode, Set<EventNode>>();
        tc2.put(a,
                new HashSet<EventNode>(Arrays.asList(new EventNode[] { c, d })));

        tc2.put(b,
                new HashSet<EventNode>(Arrays.asList(new EventNode[] { c, d })));

        tc2.put(c, new HashSet<EventNode>(Arrays.asList(new EventNode[] { d })));

        assertTrue(tc2.equals(tc.getTC()));
    }

    /**
     * Tests the TC of a DAG that looks like:
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
        TransitiveClosure tc = dag.getTransitiveClosure("followed by",
                useFloysWarshall);

        Map<EventNode, Set<EventNode>> tc2 = new LinkedHashMap<EventNode, Set<EventNode>>();

        tc2.put(a,
                new HashSet<EventNode>(Arrays
                        .asList(new EventNode[] { c, d, f })));

        tc2.put(b,
                new HashSet<EventNode>(Arrays
                        .asList(new EventNode[] { d, e, f })));

        tc2.put(c, new HashSet<EventNode>(Arrays.asList(new EventNode[] { f })));

        tc2.put(d, new HashSet<EventNode>(Arrays.asList(new EventNode[] { f })));

        assertTrue(tc2.equals(tc.getTC()));
    }

}
