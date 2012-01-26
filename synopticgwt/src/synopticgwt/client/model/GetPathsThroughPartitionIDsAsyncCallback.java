package synopticgwt.client.model;

import java.util.Map;
import java.util.Set;

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

    /**
     * After acquiring the paths through the selected partitions,
     * send the information to the model tab so that it can be properly
     * displayed.
     */
    @Override
    public void onSuccess(Map<Integer, Set<GWTEdge>> paths) {
        super.onSuccess(paths);

        if (paths.isEmpty()) {
            showError("No paths through selected partitions found.", "", "");
        } else {
            modelTab.logInfoPanel.showPaths(paths);
        }
    }
}
