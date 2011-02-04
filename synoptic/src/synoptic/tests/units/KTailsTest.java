package synoptic.tests.units;

import static org.junit.Assert.fail;

import org.junit.Test;

import synoptic.model.PartitionGraph;
import synoptic.model.input.GraphBuilder;

public class KTailsTest {

    @Test
    public void Test() {
        int LOOPS = 20;

        GraphBuilder gb = new GraphBuilder();
        String[] trace1 = new String[] { "p", "p", "c", "c", "txc", "txc", };
        String[] trace2 = new String[] { "p", "p", "c", "a", "txa", "txa", };
        String[] trace3 = new String[] { "p", "p", "a", "c", "txa", "txa", };
        String[] trace4 = new String[] { "p", "p", "a", "a", "txa", "txa", };

        // the graph will contain each trace as separate component
        gb.buildGraphLocal(new String[][] { trace1, trace2, trace3, trace4 });

        PartitionGraph pg = new PartitionGraph(gb.getGraph(), true);

        // int k = 0;
        // Set<Partition> allPartitions = pg.getInitialNodes();
        // INode<Partition> n1 = allPartitions.
        // INode<Partition> n2 = null;

        // KTails.kEquals(n1, n2, k, false);

        // TODO: test the resulting graph
        fail("TODO");
    }
}
