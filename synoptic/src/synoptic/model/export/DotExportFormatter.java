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
            boolean isInitial, boolean isTerminal, String relation) {

        String attributes = "label=\"" + quote(node.getEType().toString())
                + "\"";
        if (isInitial) {
            attributes = attributes + ",shape=box";
        } else if (isTerminal) {
            attributes = attributes + ",shape=diamond";
        }

        String color = relationColors.get(relation);
        if (!color.equals("")) {
            attributes = attributes + ",color=" + color;
        }

        return new String("  " + nodeId + " [" + attributes + "];\n");
    }

    @Override
    public String edgeToString(boolean outputEdgeLabels, int nodeSrc,
            int nodeDst, double freq, String relation) {
        String freqStr = quote(String.format("%.2f", freq));

        String s = new String(nodeSrc + "->" + nodeDst + " [");
        if (outputEdgeLabels) {
            s += "label=\"" + freqStr + "\", weight=\"" + freqStr + "\",";
        }
        String color = relationColors.get(relation);
        if (!color.equals("")) {
            s += ",color=blue";
        }
        return s + "];" + "\n";
    }
}
