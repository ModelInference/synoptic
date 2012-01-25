package synopticgwt.client.model;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.DOM;

import synopticgwt.client.util.AbstractErrorReportingAsyncCallback;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTEdge;

/**
 * Callback handler for the getPathsThroughPartitionIDs() service call.
 */
public class GetPathsThroughPartitionIDsAsyncCallback extends
        AbstractErrorReportingAsyncCallback<Map<Integer, Set<GWTEdge>>> {

    private final ModelTab modelTab;

    public GetPathsThroughPartitionIDsAsyncCallback(ProgressWheel pWheel,
            ModelTab modelPanel) {
        super(pWheel, "getPathsThroughPartitionIDs call");
        this.modelTab = modelPanel;
        initialize();
    }

    @Override
    public void onFailure(Throwable caught) {
        super.onFailure(caught);
    }

    @Override
    public void onSuccess(Map<Integer, Set<GWTEdge>> paths) {
        // TODO Show the traces and corresponding
        // paths in the client window.
        super.onSuccess(paths);

        // TODO Remove the debug code after the paths are properly displayed
        // (this is for verification. Any static method being called to
        // ModelGraphic
        // is debug code at the moment).
        if (paths.isEmpty()) {
            showError("No paths through selected partitions found.", "", "");
            ModelGraphic.printTraceID(-1);
        } else {
            for (Integer traceID : paths.keySet()) {
                Set<GWTEdge> edges = paths.get(traceID);
                ModelGraphic.printTraceID(traceID);
                for (GWTEdge edge : edges) {
                    ModelGraphic.printEdge(edge.getSrc().toString(), edge
                            .getDst().toString());
                }
            }
            
            modelTab.logInfoPanel.showPaths(paths);
        }
    }
}
