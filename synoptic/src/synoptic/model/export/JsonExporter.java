package synoptic.model.export;

import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

/**
 * Outputs a partition graph as a JSON object.
 * 
 * @author Tony Ohmann (ohmann@cs.umass.edu)
 * @param <T>
 *            The node type of the partition graph.
 */
public class JsonExporter {

    /**
     * Export the JSON object representation of the partition graph pGraph to
     * the filename specified
     * 
     * @param baseFilename
     *            The filename to which the JSON object should be written sans
     *            file extension
     * @param pGraph
     *            The partition graph to output
     */
    public static <T extends INode<T>> void exportJsonObject(
            String baseFilename, IGraph<T> pGraph) {
        //
    }
}
