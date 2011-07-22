package synoptic.model.export;

import synoptic.model.interfaces.INode;

/**
 * Implements a GraphViz exporter (DOT language) for graphs:
 * http://en.wikipedia.org/wiki/DOT_language
 */
public class DotExportFormatter extends GraphExportFormatter {

    @Override
    public String beginGraphString() {
        return new String("digraph {\n");
    }

    @Override
    public String endGraphString() {
        return new String("} // digraph {\n");
    }

    @Override
    public <T extends INode<T>> String nodeToString(int nodeId, T node,
            boolean isInitial, boolean isTerminal) {

        String attributes = "label=\"" + quote(node.getEType().toString())
                + "\"";
        if (isInitial) {
            attributes = attributes + ",shape=box";
        } else if (isTerminal) {
            attributes = attributes + ",shape=diamond";
        }

        return new String("  " + nodeId + " [" + attributes + "];\n");
    }

    private String edgeToString(boolean withProb, int nodeSrc, int nodeDst,
            double prob, String relation) {

        String s = new String(nodeSrc + "->" + nodeDst + " [");
        if (withProb) {
            String freqStr = quote(String.format("%.2f", prob));
            s += "label=\"" + freqStr + "\", weight=\"" + freqStr + "\",";
        }
        String color = relationColors.get(relation);
        if (!color.equals("")) {
            s += ",color=blue";
        }
        return s + "];" + "\n";
    }

    @Override
    public String edgeToStringWithProb(int nodeSrc, int nodeDst, double prob,
            String relation) {
        return edgeToString(true, nodeSrc, nodeDst, prob, relation);
    }

    @Override
    public String edgeToStringWithNoProb(int nodeSrc, int nodeDst,
            String relation) {
        return edgeToString(false, nodeSrc, nodeDst, 0.0, relation);
    }
}
