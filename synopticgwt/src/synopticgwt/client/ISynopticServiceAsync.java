package synopticgwt.client;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.GWTSynOpts;
import synopticgwt.shared.LogLine;

/**
 * Asynchronous interface specification for the Synoptic service. The browser
 * connects to the server and expects the server to implement this interface.
 * This interface is asynchronous -- when invoked, a method returns immediately.
 * Each method takes a callback, which will be executed when the method finishes
 * execution. For detailed comments about each of the methods, consult
 * ISynopticService.
 * 
 * <pre>
 * NOTE: This interface must match the interface specified by ISynopticService.
 * </pre>
 */
public interface ISynopticServiceAsync {
    void parseLog(GWTSynOpts synOpts,
            AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> callback);

    void parseUploadedLog(GWTSynOpts synOpts,
            AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> callback);

    void refineOneStep(AsyncCallback<GWTGraphDelta> callback) throws Exception;

    void coarsenOneStep(AsyncCallback<GWTGraph> callback) throws Exception;

    void getFinalModel(AsyncCallback<GWTGraph> callback) throws Exception;

    void handleLogRequest(int nodeID, AsyncCallback<List<LogLine>> callback)
            throws Exception;

    void commitInvariants(Set<Integer> activeInvsHashes,
            AsyncCallback<GWTGraph> callback) throws Exception;

    void exportDot(AsyncCallback<String> callback) throws Exception;

    void exportPng(AsyncCallback<String> callback) throws Exception;

}
