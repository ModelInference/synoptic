package synopticgwt.client;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.LogLine;

@RemoteServiceRelativePath("synoptic")
public interface ISynopticService extends RemoteService {
    GWTPair<GWTInvariantSet, GWTGraph> parseLog(String logLines,
            List<String> regExps, String partitionRegExp, String separatorRegExp)
            throws Exception;

    GWTGraphDelta refineOneStep() throws Exception;

    GWTGraph coarsenOneStep() throws Exception;

    GWTGraph getFinalModel() throws Exception;

    List<LogLine> handleLogRequest(int nodeID) throws Exception;

    GWTPair<GWTInvariantSet, GWTGraph> removeInvs(Set<Integer> hashes) throws Exception;

    String exportDot() throws Exception;

    String exportPng() throws Exception;
}
