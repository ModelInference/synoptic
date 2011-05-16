package synopticgwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariants;
import synopticgwt.shared.GWTPair;

public interface ISynopticServiceAsync {
    void parseLog(String logLines, List<String> regExps,
            String partitionRegExp, String separatorRegExp,
            AsyncCallback<GWTPair<GWTInvariants, GWTGraph>> callback);

    void refineOneStep(AsyncCallback<GWTGraph> callback) throws Exception;

    void coarsenOneStep(AsyncCallback<GWTGraph> callback) throws Exception;

    void getFinalModel(AsyncCallback<GWTGraph> callback) throws Exception;
}
