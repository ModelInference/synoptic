package synoptic.model.export;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.export.types.SynGraph;
import synoptic.model.export.types.SynNode;
import synoptic.model.export.types.SynSubEdge;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

/**
 * Export the Synoptic final model as a generically-typed
 * {@link synoptic.model.export.types.SynGraph}. The main entry method is
 * {@link #exportLTS(String, IGraph)}.
 */
public class GenericExporter {
    /**
     * Return the {@link synoptic.model.export.types.SynGraph} representation of
     * a partition graph
     * 
     * @param graph
     *            The partition graph to output
     */
    public static <N extends INode<N>, T> SynGraph<T> exportLTS(
            IGraph<N> graph) {
        // The graph must be a partition graph
        assert graph instanceof PartitionGraph;
        PartitionGraph pGraph = (PartitionGraph) graph;

        return buildSynGraph(pGraph);
    }

    /**
     * Build and return a SynGraph representation of a partition graph
     * 
     * @param pGraph
     *            The partition graph to output
     */
    private static <T> SynGraph<T> buildSynGraph(PartitionGraph pGraph) {
        //
        SynGraph<T> synGraph = new SynGraph<>();

        Map<Partition, SynNode<T>> partToSNode = new HashMap<>();
        Partition initialPart = null;

        // Make a SynNode for each partition, and find the initial partition
        for (Partition part : pGraph.getNodes()) {
            // Skip terminal
            if (part.isTerminal()) {
                continue;
            }
            // Find initial
            if (part.isInitial()) {
                initialPart = part;
                continue;
            }

            //
            Collection<T> nodeElems = new LinkedList<>();
            for (EventNode eNode : part.getEventNodes()) {
                EventType eType = eNode.getEType().getETypeLabel();
                nodeElems.add((T) eType);
            }
            partToSNode.put(part, new SynNode<>(nodeElems));
            synGraph.addNode(nodeElems);
        }

        // Initialize fields for BFT (breadth-first traversal) over all
        // partitions
        LinkedBlockingQueue<Partition> bftQueue = new LinkedBlockingQueue<>();
        bftQueue.add(initialPart);
        HashSet<Partition> visited = new HashSet<>();

        // Perform a BFT over all partitions, adding all outgoing transitions as
        // SynGraph edges
        while (!bftQueue.isEmpty()) {
            // Get a partition
            Partition part = bftQueue.poll();

            SynNode<T> srcNode = partToSNode.get(part);

            // Loop over all outgoing transitions
            for (ITransition<Partition> pTrans : part
                    .getWeightedTransitions()) {
                //
                Partition nextPart = pTrans.getTarget();
                SynNode<T> destNode = partToSNode.get(nextPart);
                double prob = pTrans.getProbability();

                //
                if (part.isInitial()) {
                    synGraph.setNodeAsInitial(destNode, prob);
                    continue;
                }

                //
                if (nextPart.isTerminal()) {
                    srcNode.makeTerminal();
                    continue;
                }

                Collection<SynSubEdge<T>> subEdges = new LinkedList<>();

                //
                for (ITransition<EventNode> eTrans : part
                        .getEventTransitionsWithExactRelations(nextPart,
                                Event.defTimeRelationSet)) {
                    SynSubEdge<T> subEdge = new SynSubEdge<>();
                    subEdge.srcElem = eTrans.getSource().getEType()
                            .getETypeLabel();
                    subEdge.destElem = eTrans.getTarget().getEType()
                            .getETypeLabel();
                    subEdges.add(subEdge);
                }

                synGraph.addEdge(srcNode, destNode, subEdges, prob);

                // Standard BFT: ensure partitions are visited exactly once
                if (!visited.contains(nextPart)) {
                    visited.add(nextPart);
                    bftQueue.add(nextPart);
                }
            }
        }

        return synGraph;
    }
}
