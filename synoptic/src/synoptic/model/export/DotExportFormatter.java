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

    private String edgeToString(int nodeSrc, int nodeDst, String attributes,
            String relation) {
        assert (attributes != null);

        String s = new String(nodeSrc + "->" + nodeDst + " [");
        if (!attributes.equals("")) {
            s += attributes + ",";
        }
        s += "color=" + getRelationColor(relation);
        return s + "];" + "\n";
    }

    @Override
    public String edgeToStringWithTraceId(int nodeSrc, int nodeDst,
            int traceId, String relation) {
        String attributes = "label=\"" + traceId + "t\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relation);
    }

    @Override
    public String edgeToStringWithProb(int nodeSrc, int nodeDst, double prob,
            String relation) {
        String freqStr = quote(String.format("%.2f", prob));
        String attributes = "label=\"" + freqStr + "\", weight=\"" + freqStr
                + "\"";
        return edgeToString(nodeSrc, nodeDst, relation, attributes);
    }

    @Override
    public String edgeToStringWithNoProb(int nodeSrc, int nodeDst,
            String relation) {
        return edgeToString(nodeSrc, nodeDst, "", relation);
    }

}
