package synoptic.tests.units;

import static org.junit.Assert.fail;

import org.junit.Test;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.PartitionGraph;

public class SplitTest extends SynopticUnitTest {

    @Test
    public void test() throws Exception {
        Action a = new Action("a");
        Action A = new Action("A");
        Action B = new Action("B");
        String t = "t";
        LogEvent a1 = new LogEvent(a);
        LogEvent a2 = new LogEvent(a);
        LogEvent a3 = new LogEvent(a);
        LogEvent a4 = new LogEvent(a);
        LogEvent A1 = new LogEvent(A);
        LogEvent B1 = new LogEvent(B);
        a1.addTransition(A1, t);
        a2.addTransition(A1, t);
        a2.addTransition(B1, t);
        B1.addTransition(a3, t);
        a4.addTransition(B1, t);
        Graph<LogEvent> g = new Graph<LogEvent>();
        g.add(a1);
        g.tagInitial(a1, t);
        g.add(a2);
        g.tagInitial(a2, t);
        g.add(a3);
        g.tagInitial(a3, t);
        g.add(a4);
        g.tagInitial(a4, t);
        g.add(A1);
        g.add(B1);
        PartitionGraph pg = new PartitionGraph(g, true);
        Bisimulation.splitPartitions(pg);
        // TODO: test the resulting graph
        fail("TODO");
    }
}
