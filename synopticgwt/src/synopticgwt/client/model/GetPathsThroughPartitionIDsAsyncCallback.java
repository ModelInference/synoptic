package synopticgwt.client.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import synopticgwt.client.util.AbstractErrorReportingAsyncCallback;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTEdge;

/**
 * Callback handler for the getPathsThroughPartitionIDs() service call.
 */
public class GetPathsThroughPartitionIDsAsyncCallback extends
        AbstractErrorReportingAsyncCallback<Map<List<GWTEdge>, Set<Integer>>> {

    private final LogInfoPanel infoPanel;

    public GetPathsThroughPartitionIDsAsyncCallback(ProgressWheel pWheel,
            LogInfoPanel infoPanel) {
        super(pWheel, "getPathsThroughPartitionIDs call");
        this.infoPanel = infoPanel;
        initialize();
    }

    @Override
    public void onFailure(Throwable caught) {
        super.onFailure(caught);
        infoPanel.clearAndShowPathsTable();
    }

    /**
     * After acquiring the paths through the selected partitions, send the
     * information to the model tab so that it can be properly displayed.
     */
    @Override
    public void onSuccess(Map<List<GWTEdge>, Set<Integer>> paths) {
        super.onSuccess(paths);

        if (paths.isEmpty()) {
            showError("No paths through selected partitions found.", "", "");
            infoPanel.clearAndShowPathsTable();
        } else {
            infoPanel.showPaths(paths);
        }
    }
}
