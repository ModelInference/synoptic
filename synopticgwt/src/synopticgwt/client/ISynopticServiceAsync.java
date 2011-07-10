package synopticgwt.client;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.LogLine;

public interface ISynopticServiceAsync {
    void parseLog(String logLines, List<String> regExps,
            String partitionRegExp, String separatorRegExp,
            AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> callback);

    void refineOneStep(AsyncCallback<GWTGraphDelta> callback) throws Exception;

    void coarsenOneStep(AsyncCallback<GWTGraph> callback) throws Exception;

    void getFinalModel(AsyncCallback<GWTGraph> callback) throws Exception;

    void handleLogRequest(int nodeID, AsyncCallback<List<LogLine>> callback)
            throws Exception;

    void removeInvs(Set<Integer> hashes,
    		AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> callback) throws Exception;

    void exportModel(AsyncCallback<String> callback) throws Exception;
}
