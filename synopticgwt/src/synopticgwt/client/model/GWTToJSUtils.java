package synopticgwt.client.model;

import java.util.Collection;

import synopticgwt.client.util.JsniUtil;
import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTNode;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.NumberFormat;

public class GWTToJSUtils {

    /**
     * @param nodeSet
     *            The collection of nodes whose information is to be mutated
     *            into easily manageable data for graphhandler.js
     * 
     * @return A JavaScriptObject array where each GWTNode's information is
     *         pushed onto the array in the following way: the node's id (the
     *         hash code of the corresponding partition that the GWTNode
     *         represents in the Synoptic model), followed by the node's event
     *         label.
     */
    public static JavaScriptObject createJSArrayFromGWTNodes(
            Collection<GWTNode> nodeSet) {
        JavaScriptObject jsNodes = JavaScriptObject.createArray();
        for (GWTNode node : nodeSet) {
            JsniUtil.pushArray(jsNodes,
                    ((Integer) node.getPartitionNodeHashCode()).toString());
            JsniUtil.pushArray(jsNodes, node.toString());
        }

        return jsNodes;
    }

    /**
     * Takes a collection of GWTEdges and returns a JavaScriptObject array
     * containing information necessary to transfer the GWT data to
     * graphhandler.js
     * 
     * The current format is simply to push all information about each edge in
     * the following order: edge source id, edge destination id, edge weight,
     * and edge count.
     * 
     * @param edgeSet
     * @return A JavaScriptObject array containing information about all edges
     *         as described above.
     */
    public static JavaScriptObject createJSArrayFromGWTEdges(
            Collection<GWTEdge> edgeSet) {
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        for (GWTEdge edge : edgeSet) {
            JsniUtil.pushArray(jsEdges, ((Integer) edge.getSrc()
                    .getPartitionNodeHashCode()).toString());
            JsniUtil.pushArray(jsEdges, ((Integer) edge.getDst()
                    .getPartitionNodeHashCode()).toString());

            // This contains the edge's weight.
            JsniUtil.pushArray(jsEdges, probToString(edge.getWeight()));
            JsniUtil.pushArray(jsEdges, ((Integer) edge.getCount()).toString());
        }

        return jsEdges;
    }

    /**
     * <pre>
     * NOTE: This method is a copy of
     * synoptic.model.export.GraphExportFormatter.probToString()
     * 
     * Unfortunately, there is no way to unify these two methods without passing
     * probabilities as both doubles and strings from the server, or as strings
     * and then converting them to doubles. Both of alternatives are ugly enough
     * to make this duplication ok in this case.
     * </pre>
     */
    public static String probToString(double prob) {
        return NumberFormat.getFormat("0.00").format(
                Math.round(prob * 100.0) / 100.0);
    }
}
