package dynoptic.model.export;

import java.util.Set;

import dynoptic.model.AbsFSMState;

import synoptic.model.event.DistEventType;

/**
 * Implements a GraphViz exporter (DOT language) for graphs:
 * http://en.wikipedia.org/wiki/DOT_language
 */
public class DotExportFormatter extends GraphExportFormatter {

    private int startStateCount;

    public DotExportFormatter() {
        startStateCount = 0;
    }

    @Override
    public String beginGraphString() {
        return "digraph {\n";
    }

    @Override
    public String endGraphString() {
        return "} // digraph {\n";
    }

    @Override
    public <State extends AbsFSMState<State>> String nodeToString(int nodeId,
            State node, boolean isInitial, boolean isTerminal) {
        String attributes = "label=\"" + quote(Integer.toString(nodeId))
                + "\",shape=circle";
        String extra = "";

        if (isInitial) {
            String startId = "start_" + startStateCount;
            extra = "  " + startId + " [label=\"start\",shape=plaintext];\n";
            extra += "  " + startId + "->" + nodeId + ";\n";
            startStateCount++;
        }

        if (isTerminal) {
            attributes += ",shape=doublecircle";
        }

        return "  " + nodeId + " [" + attributes + "];\n" + extra;
    }

    @Override
    public String edgeToStringWithDistEvent(int nodeSrc, int nodeDst,
            DistEventType event, Set<String> relations) {
        String attributes = "label=\"" + quote(event.toDotString()) + "\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    private String edgeToString(int nodeSrc, int nodeDst, String attributes,
            Set<String> relations) {
        assert (attributes != null);

        String s = nodeSrc + "->" + nodeDst + " [";
        if (!attributes.isEmpty()) {
            s += attributes + ",";
        }
        // s += "color=" + getRelationColor(relations);
        return s + "];" + "\n";
    }
}
