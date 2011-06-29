package synopticgwt.client;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.EventNode;
import synoptic.model.Graph;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariants;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.LogLine;

@RemoteServiceRelativePath("synoptic")
public interface ISynopticService extends RemoteService {
    GWTPair<GWTInvariants, GWTGraph> parseLog(String logLines,
            List<String> regExps, String partitionRegExp, String separatorRegExp)
            throws Exception;

    GWTGraphDelta refineOneStep() throws Exception;

    GWTGraph coarsenOneStep() throws Exception;

    GWTGraph getFinalModel() throws Exception;

    List<LogLine> handleLogRequest(int nodeID) throws Exception;

    GWTPair<GWTInvariants, GWTGraph> removeInvs(Set<Integer> hashes);
}
