package synopticgwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariants;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.LogLine;

public interface ISynopticServiceAsync {
    void parseLog(String logLines, List<String> regExps,
            String partitionRegExp, String separatorRegExp,
            AsyncCallback<GWTPair<GWTInvariants, GWTGraph>> callback);

    void refineOneStep(AsyncCallback<GWTGraphDelta> callback) throws Exception;

    void coarsenOneStep(AsyncCallback<GWTGraph> callback) throws Exception;

    void getFinalModel(AsyncCallback<GWTGraph> callback) throws Exception;
    
    void handleLogRequest(int nodeID, AsyncCallback<List<LogLine>> callback) throws Exception;

}
