package synoptic.model.export;

import java.util.Set;

import daikonizer.DaikonInvariants;

import synoptic.model.interfaces.INode;
import synoptic.util.time.ITime;

/**
 * Implements a GML (graph modeling language) export for graphs:
 * http://en.wikipedia.org/wiki/Graph_Modelling_Language
 */
public class GmlExportFormatter extends GraphExportFormatter {

    @Override
    public String beginGraphString() {
        return "graph [\n";
    }

    @Override
    public String endGraphString() {
        return "]\n";
    }

    @Override
    public <T extends INode<T>> String nodeToString(int nodeId, T node,
            boolean isInitial, boolean isTerminal) {
        String nodeGraphics = "graphics [\n";
        if (isInitial) {
            nodeGraphics += "  type \"rectangle\"\n]\n";
        } else if (isTerminal) {
            nodeGraphics += "  type \"rhombus\"\n]\n";
        } else {
            nodeGraphics += "  type \"oval\"\n]\n";
        }

        return "node\n[\n  id " + nodeId + "\n  label " + "\""
                + quote(node.getEType().toString()) + "\"" + "\n"
                + nodeGraphics + "]\n";
    }

    private String edgeToString(int nodeSrc, int nodeDst, String attributes,
            Set<String> relations) {
        assert (attributes != null);

        String s = "edge\n[\n  ";
        s += "source " + nodeSrc + "\n  target " + nodeDst + "\n";

        if (!attributes.equals("")) {
            s += attributes + ",";
        }

        // TODO: use edge color that corresponds to the relation via:
        // String color = relationColors.get(relation);

        return s + "]" + "\n";
    }

    @Override
    public String edgeToStringWithTraceId(int nodeSrc, int nodeDst,
            int traceId, Set<String> relations) {
        String attributes = "  label \"" + quote(String.format("%d", traceId))
                + "\"\n";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    @Override
    public String edgeToStringWithProb(int nodeSrc, int nodeDst, double prob,
            Set<String> relations) {
        String attributes = "  label \"" + quote(probToString(prob)) + "\"\n";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    @Override
    public String edgeToStringWithITimes(int nodeSrc, int nodeDst,
            ITime timeMin, ITime timeMax, Set<String> relations) {

        // Make time string
        String timeStr;
        if (timeMin != null && timeMax != null) {

            // String is range if min != max time or else just the single time
            // if they are equal
            if (!timeMin.equals(timeMax)) {
                timeStr = "[" + quote(timeMin.toString()) + ","
                        + quote(timeMax.toString()) + "]";
            } else {
                timeStr = quote(timeMin.toString());
            }
        } else {
            timeStr = "";
        }

        String attributes = "  label \"" + timeStr + "\"\n";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    @Override
    public String edgeToStringWithNoProb(int nodeSrc, int nodeDst,
            Set<String> relations) {
        return edgeToString(nodeSrc, nodeDst, "", relations);
    }

    @Override
    public String edgeToStringWithDaikonInvs(int nodeSrc, int nodeDst,
            DaikonInvariants daikonInvs, Set<String> relations) {
        String attributes = "  label \"" + quote(daikonInvs.toString())
                + "\"\n";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }
}
