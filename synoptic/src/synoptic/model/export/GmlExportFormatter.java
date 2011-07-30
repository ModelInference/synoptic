package synoptic.model.export;

import synoptic.model.interfaces.INode;

/**
 * Implements a GML (graph modeling language) export for graphs:
 * http://en.wikipedia.org/wiki/Graph_Modelling_Language
 */
public class GmlExportFormatter extends GraphExportFormatter {

    @Override
    public String beginGraphString() {
        return new String("graph [\n");
    }

    @Override
    public String endGraphString() {
        return new String("]\n");
    }

    @Override
    public <T extends INode<T>> String nodeToString(int nodeId, T node,
            boolean isInitial, boolean isTerminal) {
        String nodeGraphics = new String("graphics [\n");
        if (isInitial) {
            nodeGraphics += "  type \"rectangle\"\n]\n";
        } else if (isTerminal) {
            nodeGraphics += "  type \"rhombus\"\n]\n";
        } else {
            nodeGraphics += "  type \"oval\"\n]\n";
        }

        return new String("node\n[\n  id " + nodeId + "\n  label " + "\""
                + quote(node.getEType().toString()) + "\"" + "\n"
                + nodeGraphics + "]\n");
    }

    private String edgeToString(int nodeSrc, int nodeDst, String attributes,
            String relation) {
        assert (attributes != null);

        String s = new String("edge\n[\n  ");
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
            int traceId, String relation) {
        String attributes = "  label \"" + quote(String.format("%d", traceId))
                + "\"\n";
        return edgeToString(nodeSrc, nodeDst, attributes, relation);
    }

    @Override
    public String edgeToStringWithProb(int nodeSrc, int nodeDst, double prob,
            String relation) {
        String attributes = "  label \"" + quote(String.format("%.2f", prob))
                + "\"\n";
        return edgeToString(nodeSrc, nodeDst, attributes, relation);
    }

    @Override
    public String edgeToStringWithNoProb(int nodeSrc, int nodeDst,
            String relation) {
        return edgeToString(nodeSrc, nodeDst, "", relation);
    }
}
