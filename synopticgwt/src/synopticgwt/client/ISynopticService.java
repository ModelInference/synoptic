package synopticgwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariants;
import synopticgwt.shared.GWTPair;

@RemoteServiceRelativePath("synoptic")
public interface ISynopticService extends RemoteService {
    GWTPair<GWTInvariants, GWTGraph> parseLog(String logLines,
            List<String> regExps, String partitionRegExp, String separatorRegExp)
            throws Exception;

    GWTGraph refineOneStep() throws Exception;

    GWTGraph coarsenOneStep() throws Exception;

    GWTGraph getFinalModel() throws Exception;
}
