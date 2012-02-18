package synopticgwt.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.GWTSynOpts;
import synopticgwt.shared.LogLine;

/**
 * Interface specification for the Synoptic service. The server must implement
 * this interface.
 * 
 * <pre>
 * NOTE: This interface must match the interface specified by ISynopticServiceAsync.
 * </pre>
 */
@RemoteServiceRelativePath("synoptic")
public interface ISynopticService extends RemoteService {
    /**
     * Parses the input log, and sets up and stores Synoptic session state for
     * refinement\coarsening.
     * 
     * @param synOpts
     *            Synoptic processing options.
     * @return A pair of the mined invariants and the initial model. NOTE that
     *         if the input log is a PO log then the returned model is null (PO
     *         models are not supported yet).
     * @throws Exception
     */
    GWTPair<GWTInvariantSet, GWTGraph> parseLog(GWTSynOpts synOpts)
            throws Exception;

    /**
     * Reads a log file located in server where path specified by saved session
     * state assignment. Parses the input log contained in file,and sets up and
     * stores Synoptic session state for refinement\coarsening.
     * 
     * @return
     * @throws Exception
     */
    GWTPair<GWTInvariantSet, GWTGraph> parseUploadedLog(GWTSynOpts synOpts)
            throws Exception;

    /**
     * Performs a single step of refinement on the cached model.
     * 
     * @return
     * @throws Exception
     */
    GWTGraphDelta refineOneStep() throws Exception;

    /**
     * Performs coarsening of the completely refined model in one step.
     * 
     * @return
     * @throws Exception
     */
    GWTGraph coarsenCompletely() throws Exception;

    /**
     * Completes any refinement left to be done and then coarsens the graph into
     * a final model.
     * 
     * @return
     * @throws Exception
     */
    GWTGraph getFinalModel() throws Exception;

    /**
     * Find the requested partition and returns a list of log lines, each in the
     * form [line #, line, filename]
     * 
     * @param nodeID
     * @return
     * @throws Exception
     */
    List<LogLine> handleLogRequest(int nodeID) throws Exception;

    /**
     * Sets the set of activate invariants.
     * 
     * @param activeInvsHashes
     *            The hash codes for the invariants that are to be active.
     * @throws Exception
     */
    GWTGraph commitInvariants(Set<Integer> activeInvsHashes) throws Exception;

    /**
     * Exports the current model as a .dot file. Returns the filename/directory.
     * 
     * @return
     * @throws Exception
     */
    String exportDot() throws Exception;

    /**
     * Exports the current model as a .png file. Returns the filename/directory.
     * 
     * @return
     * @throws Exception
     */
    String exportPng() throws Exception;

    /**
     * Returns the set of all possible paths through a set of nodes.
     * 
     * @param selectedNodes
     * @return
     * @throws Exception
     */
    Map<List<GWTEdge>, Set<Integer>> getPathsThroughPartitionIDs(
            Set<Integer> selectedNodes) throws Exception;
}
