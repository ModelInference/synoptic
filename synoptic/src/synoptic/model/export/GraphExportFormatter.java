package synoptic.model.export;

import java.util.LinkedHashMap;
import java.util.Map;

import synoptic.model.interfaces.INode;

/**
 * Base class representing possible exported/serializing formats for graphs.
 */
public abstract class GraphExportFormatter {
    /**
     * Maps commonly used relations to the edge colors used in dot output.
     */
    static final Map<String, String> relationColors;

    // Initialize relationColors
    // TODO: this needs to be made dynamic, instead of being hardcoded.
    static {
        relationColors = new LinkedHashMap<String, String>();
        relationColors.put("t", "");
        relationColors.put("i", "blue");
    }

    /**
     * Returns a string that begins a new graph.
     */
    public abstract String beginGraphString();

    /**
     * Returns a string that terminates a graph.
     */
    public abstract String endGraphString();

    /**
     * Serializes a single node in a graph to a string.
     * 
     * @param <T>
     * @param nodeId
     *            a unique node identifier
     * @param node
     *            INode node, whose event type will be used as a label
     * @param isInitial
     *            whether or not node is initial
     * @param isTerminal
     *            whether or not node is terminal
     * @param relation
     *            a string representing the relation (e.g., "t")
     */
    public abstract <T extends INode<T>> String nodeToString(int nodeId,
            T node, boolean isInitial, boolean isTerminal, String relation);

    /**
     * Serializes a single node edge in a graph to a string.
     * 
     * @param outputEdgeLabels
     *            whether or not to output edge labels
     * @param nodeSrc
     *            the unique identifier for the source node
     * @param nodeDst
     *            the unique identifier for the target node
     * @param freq
     *            the frequency value to be used as a label on the edge
     * @param relation
     *            a string representing the relation (e.g., "t")
     * @return
     */
    public abstract String edgeToString(boolean outputEdgeLabels, int nodeSrc,
            int nodeDst, double freq, String relation);

    /**
     * Returns a string with escaped forward slashes and double quotes.
     */
    protected static String quote(String string) {
        final StringBuilder sb = new StringBuilder(string.length() + 2);
        for (int i = 0; i < string.length(); ++i) {
            final char c = string.charAt(i);
            switch (c) {
            case '\\':
                sb.append("\\\\");
                break;
            case '"':
                sb.append("\\\"");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }
}
