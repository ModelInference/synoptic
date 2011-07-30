package synopticgwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
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
     * @param logLines
     * @param regExps
     * @param partitionRegExp
     * @param separatorRegExp
     * @return
     * @throws Exception
     */
    GWTPair<GWTInvariantSet, GWTGraph> parseLog(String logLines,
            List<String> regExps, String partitionRegExp, String separatorRegExp)
            throws Exception;

    /**
     * Performs a single step of refinement on the cached model.
     * 
     * @return
     * @throws Exception
     */
    GWTGraphDelta refineOneStep() throws Exception;

    /**
     * Performs coarsening of the completely refined model in one single step.
     * 
     * @return
     * @throws Exception
     */
    GWTGraph coarsenOneStep() throws Exception;

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
     * Deactivates an invariants.
     * 
     * @param invHash
     *            The hash code for the invariant to deactivate.
     * @return
     * @return
     * @throws Exception
     */
    Integer deactivateInvariant(Integer invHash) throws Exception;

    /**
     * Activates an invariants.
     * 
     * @param invHash
     *            The hash codes for the invariants to be removed.
     * @throws Exception
     */
    Integer activateInvariant(Integer invHash) throws Exception;

    /**
     * Commits the activated invariants, and re-creates the partition graph
     * based on this invariants set. Basically, this is a restart of refinement.
     * 
     * @return
     * @throws Exception
     */
    GWTGraph commitInvariants() throws Exception;

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
}
