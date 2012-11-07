package dynoptic.model.export;

import java.util.Set;

import dynoptic.model.AbsFSMState;

import synoptic.model.event.IDistEventType;

/**
 * Base class representing possible exported/serializing formats for graphs.
 */
public abstract class GraphExportFormatter {

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
     * @param nodeId
     *            a unique node identifier
     * @param node
     *            FSMState node, whose scm id will be used as a label
     * @param isInitial
     *            whether or not node is initial
     * @param isTerminal
     *            whether or not node is terminal
     */
    public abstract <State extends AbsFSMState<State, TxnEType>, TxnEType extends IDistEventType> String nodeToString(
            int nodeId, State node, boolean isInitial, boolean isTerminal);

    /**
     * Serializes a single node edge in a graph to a string that represents this
     * edge.
     * 
     * @param nodeSrc
     *            the unique identifier for the source node
     * @param nodeDst
     *            the unique identifier for the target node
     * @param event
     *            the transitioning event to be used as a label on the edge
     * @param relation
     *            a string representing the relation (e.g., "t")
     * @return
     */
    public abstract String edgeToStringWithDistEvent(int nodeSrc, int nodeDst,
            IDistEventType event, Set<String> relations);

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
