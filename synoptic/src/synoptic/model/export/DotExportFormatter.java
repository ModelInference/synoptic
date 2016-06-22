package synoptic.model.export;

import java.util.Set;

import synoptic.model.interfaces.INode;
import synoptic.util.resource.AbstractResource;

import daikonizer.DaikonInvariants;

/**
 * Implements a GraphViz exporter (DOT language) for graphs:
 * http://en.wikipedia.org/wiki/DOT_language
 */
public class DotExportFormatter extends GraphExportFormatter {

    @Override
    public String beginGraphString() {
        return "digraph G {\n";
    }

    @Override
    public String endGraphString() {
        return "}\n";
    }

    @Override
    public <T extends INode<T>> String nodeToString(int nodeId, T node, boolean isInitial, boolean isTerminal) {

        String attributes = "label=\"" + quote(node.getEType().toString()) + "\"";
        if (isInitial) {
            attributes = attributes + ",shape=box";
        } else if (isTerminal) {
            attributes = attributes + ",shape=diamond";
        }

        return "  " + nodeId + " [" + attributes + "];\n";
    }

    private String edgeToString(int nodeSrc, int nodeDst, String attributes, Set<String> relations) {
        assert (attributes != null);

        String s = nodeSrc + "->" + nodeDst + " [";
        if (!attributes.equals("")) {
            s += attributes;
        }
        // s += "color=" + getRelationColor(relations);
        return s + "];" + "\n";
    }

    @Override
    public String edgeToStringWithTraceId(int nodeSrc, int nodeDst, int traceId, Set<String> relations) {
        String attributes = "label=\"" + traceId + "t\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    @Override
    public String edgeToStringWithProb(int nodeSrc, int nodeDst, double prob, Set<String> relations) {
        String probStr = quote(probToString(prob));
        String attributes = "label=\"P: " + probStr + "\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    @Override
    public String edgeToStringWithCnt(int nodeSrc, int nodeDst, int cnt, Set<String> relations) {
        String cntStr = quote(Integer.toString(cnt));
        String attributes = "label=\"Cnt: " + cntStr + "\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    @Override
    public String edgeToStringWithProbCnt(int nodeSrc, int nodeDst, double prob, int cnt, Set<String> relations) {
        String probStr = quote(probToString(prob));
        String cntStr = quote(Integer.toString(cnt));
        String attributes = "label=\"P: " + probStr + ", Cnt: " + cntStr + "\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    @Override
    public String edgeToStringWithITimes(int nodeSrc, int nodeDst, AbstractResource timeMin, AbstractResource timeMax,
            AbstractResource timeMedian, Set<String> relations) {

        // Make time string
        int sigDigits = 3;
        String timeStr = getITimeString(timeMin, timeMax, timeMedian, sigDigits);

        String attributes = "label=\"" + timeStr + "\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    @Override
    public String edgeToStringWithITimesAndProb(int nodeSrc, int nodeDst, AbstractResource timeMin,
            AbstractResource timeMax, AbstractResource timeMedian, double prob, Set<String> relations) {

        // Make time and probability strings
        int sigDigits = 3;
        String timeStr = getITimeString(timeMin, timeMax, timeMedian, sigDigits);
        String probStr = quote(probToString(prob));

        String attributes = "label=\"" + timeStr + " " + probStr + "\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

    @Override
    public String edgeToStringWithNoProb(int nodeSrc, int nodeDst, Set<String> relations) {
        return edgeToString(nodeSrc, nodeDst, "", relations);
    }

    @Override
    public String edgeToStringWithDaikonInvs(int nodeSrc, int nodeDst, DaikonInvariants daikonInvs,
            Set<String> relations) {
        String invStr = quote(daikonInvs.toString());
        String attributes = "label=\"" + invStr + "\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
    }

}
