package dynoptic.model.export;

import java.util.Set;

import dynoptic.model.AbsFSMState;

import synoptic.model.event.DistEventType;

/**
 * Implements a GraphViz exporter (DOT language) for graphs:
 * http://en.wikipedia.org/wiki/DOT_language
 */
public class DotExportFormatter extends GraphExportFormatter {

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
		String attributes = "label=\"" + quote(node.toString()) + "\"";
		String inArrow = "";
		
		if (isInitial) {
			inArrow = "  none [shape=none];\n";
			inArrow = "  none->" + nodeId + ";\n";
		} else if (isTerminal) {
			attributes = attributes + ",shape=doublecircle";
		}
		
		return "  " + nodeId + " [" + attributes + "];\n" + inArrow;
	}

	@Override
	public String edgeToStringWithDistEvent(int nodeSrc, int nodeDst,
			DistEventType event, Set<String> relations) {
		String attributes = "label=\"" + quote(event.toString()) + "\"";
        return edgeToString(nodeSrc, nodeDst, attributes, relations);
	}
	
	private String edgeToString(int nodeSrc, int nodeDst, String attributes,
            Set<String> relations) {
        assert (attributes != null);

        String s = nodeSrc + "->" + nodeDst + " [";
        if (!attributes.equals("")) {
            s += attributes + ",";
        }
        // s += "color=" + getRelationColor(relations);
        return s + "];" + "\n";
    }
}
