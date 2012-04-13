package synoptic.algorithms.graph;

import java.util.HashSet;
import java.util.Set;

import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.interfaces.ITransition;

public class KTails {

    public static PartitionGraph performKTails(ChainsTraceGraph g, int k) {

        Set<Partition> partitions = new HashSet<Partition>();

        EventNode initNode = g.getDummyInitialNode(TraceParser.defaultRelation);
        Partition initial = new Partition(initNode);
        partitions.add(initial);

        // Iterate through all the traces -- each transition from the
        // INITIAL node connects/holds a single trace.
        EventNode curNode = null;
        for (ITransition<EventNode> initTrans : initNode.getTransitions()) {
            curNode = initTrans.getTarget();
            while (curNode.getTransitions().size() != 0) {
                Partition p = new Partition(curNode);
                partitions.add(p);
                curNode = curNode.getTransitions().get(0).getTarget();
            }
        }

        Partition terminal = new Partition(curNode);
        partitions.add(terminal);

        PartitionGraph pGraph = new PartitionGraph(g, initial, terminal,
                partitions);

        // Attempt to merge all pairs of partitions in the current graph.
        for (Partition p : partitions) {
            if (p.size() == 0) {
                continue;
            }

            for (Partition q : partitions) {
                // 1. Can't merge a partition with itself
                if (p == q) {
                    continue;
                }

                // 2. Can't merge with empty partitions
                if (q.size() == 0) {
                    continue;
                }

                // 3. Only merge partitions that are k-equivalent
                if (!synoptic.algorithms.bisim.KTails.kEquals(p, q, k, false)) {
                    continue;
                }
                pGraph.apply(new PartitionMerge(p, q));
            }
        }

        for (Partition p : partitions) {
            if (p.size() == 0) {
                pGraph.removePartition(p);
            }
        }

        return pGraph;
    }

}
