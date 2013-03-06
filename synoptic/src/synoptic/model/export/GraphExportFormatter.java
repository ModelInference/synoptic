package synoptic.model.export;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.model.event.Event;
import synoptic.model.interfaces.INode;
import synoptic.util.time.ITime;

/**
 * Base class representing possible exported/serializing formats for graphs.
 */
public abstract class GraphExportFormatter {
    public static Logger logger = Logger.getLogger("GraphExportFormatter");

    /**
     * Possible colors (X11 scheme) for differentiating edges of different
     * relations (one color per relation).
     */
    List<String> possibleColors;

    /**
     * Maps commonly observed relations to the edge colors from the list above.
     */
    Map<String, String> relationColors;

    /** Color used for the "t" relation and when we run out of colors. */
    static final String defaultRelationColor = "black";

    /**
     * Rounds off edge probability to two decimal places. Used by all edge
     * formatters. NOTE: if this method is modified, make sure to update the
     * synopticgwt.client.model.ModelTab.probToString() method.
     */
    public static String probToString(double prob) {
        return String.format("%.2f", Math.round(prob * 100.0) / 100.0);
    }

    /**
     * Whether or not we've printed a message stating that we've run out of
     * colors.
     */
    boolean reportedColorsDeficiency = false;

    public GraphExportFormatter() {
        // This colors subset is taken from:
        // http://www.graphviz.org/doc/info/colors.html
        possibleColors = new LinkedList<String>(Arrays.asList("darkorange1",
                "goldenrod", "darkseagreen", "green3", "gray66", "indianred",
                "indigo", "yellow1", "thistle"));

        relationColors = new LinkedHashMap<String, String>();
        relationColors.put(Event.defTimeRelationStr,
                defaultRelationColor);
    }

    /**
     * Returns a color for the relation if one was assigned previously. If not,
     * grabs a new unused color and assigns it to this relation. If all colors
     * are used, prints a warning (once per instance of this class) and returns
     * the defaultRelationColor.
     */
    public String getRelationColor(String relation) {
        if (relationColors.containsKey(relation)) {
            return relationColors.get(relation);
        }
        if (possibleColors.size() == 0) {
            if (!reportedColorsDeficiency) {
                logger.severe("Ran out of colors for relations when exporting graph. Using the default color ("
                        + defaultRelationColor
                        + ") for the remaining relations.");
            }
            reportedColorsDeficiency = true;
        }
        if (possibleColors.size() == 0) {
            return defaultRelationColor;
        }
        String color = possibleColors.remove(0);
        relationColors.put(relation, color);
        return color;
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
     */
    public abstract <T extends INode<T>> String nodeToString(int nodeId,
            T node, boolean isInitial, boolean isTerminal);

    /**
     * Serializes a single node edge in a graph to a string that represents this
     * edge, along with the probability of the edge transition.
     * 
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
    public abstract String edgeToStringWithProb(int nodeSrc, int nodeDst,
            double freq, Set<String> relation);

    /**
     * Serializes a single node edge in a graph to a string that represents this
     * edge, with NO probability of the edge transition.
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
    public abstract String edgeToStringWithNoProb(int nodeSrc, int nodeDst,
            Set<String> relations);

    /**
     * Serializes a single node edge in a graph to a string. Also, outputs a
     * trace identifier as the edge label.
     * 
     * @param nodeSrc
     *            the unique identifier for the source node
     * @param nodeDst
     *            the unique identifier for the target node
     * @param freq
     *            the frequency value to be used as a label on the edge
     * @param traceId
     *            the identifier of the trace that this edge belongs to.
     * @param relation
     *            a string representing the relation (e.g., "t")
     * @return
     */
    public abstract String edgeToStringWithTraceId(int nodeSrc, int nodeDst,
            int traceId, Set<String> relations);

    public abstract String edgeToStringWithITime(int nodeSrc, int nodeDst,
            ITime time, Set<String> relations);

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
