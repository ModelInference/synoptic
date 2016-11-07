package synoptic.model.export.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import synoptic.main.AbstractMain;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.ITransition;
import synoptic.util.resource.AbstractResource;

/**
 * An event-based FSM version of a Synoptic model, specifically of a
 * {@link PartitionGraph}
 */
public class EvBasedGraph {
    private EvBasedNode initialNode;
    private EvBasedNode terminalNode;
    private final Set<EvBasedNode> nodes = new HashSet<>();
    private int nextID = 0;

    public EvBasedNode getInitialNode() {
        return initialNode;
    }

    public EvBasedNode getTerminalNode() {
        return terminalNode;
    }

    public Set<EvBasedNode> getNodes() {
        return nodes;
    }

    /**
     * Construct the event-based FSM
     */
    public EvBasedGraph(PartitionGraph pGraph) {
        // Map of partitions to event-based FSM nodes
        Map<Partition, EvBasedNode> partToNode = new HashMap<>();

        // Populate partition/node map, and find INITIAL and TERMINAL nodes
        for (Partition part : pGraph.getNodes()) {
            EvBasedNode node = new EvBasedNode(nextID++);

            if (part.isInitial()) {
                initialNode = node;
            } else if (part.isTerminal()) {
                terminalNode = node;
            }

            nodes.add(node);
            partToNode.put(part, node);
        }

        // For each partition, add all original outgoing edges as new
        // event-based FSM edges to the partition's corresponding node
        for (Partition part : pGraph.getNodes()) {
            for (ITransition<Partition> pTrans : part
                    .getWeightedTransitions()) {
                EvBasedEdge edge = makeEdge(partToNode, pTrans);
                EvBasedNode srcNode = partToNode.get(pTrans.getSource());
                srcNode.addOutEdge(edge);
            }
        }
    }

    /**
     * Create and return a new event-based FSM edge
     */
    private EvBasedEdge makeEdge(Map<Partition, EvBasedNode> partToNode,
            ITransition<Partition> pTrans) {
        Partition srcPart = pTrans.getSource();

        // Get the edge's endpoint nodes and contained events
        EvBasedNode srcNode = partToNode.get(srcPart);
        EvBasedNode destNode = partToNode.get(pTrans.getTarget());
        Set<EventNode> events = srcPart.getEventNodes();
        EventType eType = srcPart.getEType();

        AbstractResource resMin = null;
        AbstractResource resMax = null;
        // AbstractResource timeMedian = null;

        // For Perfume, get the min, max, and median resource deltas
        if (AbstractMain.getInstance().options.usePerformanceInfo
                && pTrans.getDeltaSeries() != null) {
            resMin = pTrans.getDeltaSeries().computeMin();
            resMax = pTrans.getDeltaSeries().computeMax();
            // timeMedian = pTrans.getDeltaSeries().computeMed();
        }

        Double prob = pTrans.getProbability();

        return new EvBasedEdge(nextID++, srcNode, destNode, events, eType,
                resMin, resMax, prob);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (EvBasedNode node : nodes) {
            sb.append("\n").append(node.nodeID);
            for (EvBasedEdge edge : node.outEdges) {
                sb.append(String.format("\n  %s.%d[%s,%s](p=%f) -> %d",
                        edge.eType, edge.edgeID, edge.resMin, edge.resMax,
                        edge.prob, edge.destNode.nodeID));
            }
        }

        return sb.toString();
    }
}
