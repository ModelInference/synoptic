package synopticgwt.client.model;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.Tab;
import synopticgwt.client.util.JsniUtil;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.client.util.TooltipListener;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTTriplet;
import synopticgwt.shared.LogLine;

/**
 * Represents the model tab, using which the user can refine/coarsen the model
 * and find out how the input log corresponds to paths and to nodes in the
 * model.
 */
public class ModelTab extends Tab<DockPanel> {

    // TODO: INITIAL_LABEL and TERMINAL_LABEL should not be hardcoded. Instead,
    // these should be dynamically set based on server defaults -- the server
    // can access EventType.initialNodeLabel and EventType.terminalNodeLabel to
    // detect what these should be.

    /* Label of initial node, for layout purposes */
    private static final String INITIAL_LABEL = "INITIAL";

    /* Label of terminal node, for layout purposes */
    private static final String TERMINAL_LABEL = "TERMINAL";

    // Model tab widgets:
    private final Button modelRefineButton = new Button("Refine");
    private final Button modelCoarsenButton = new Button("Coarsen");
    private final Button modelGetFinalButton = new Button("Final Model");
    private final Button modelExportDotButton = new Button("Export DOT");
    private final Button modelExportPngButton = new Button("Export PNG");
    private FlowPanel graphPanel;
    private FlexTable logLineTable;

    public ModelTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        super(synopticService, pWheel);
        panel = new DockPanel();

        VerticalPanel controlsPanel = new VerticalPanel();

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.add(modelRefineButton);
        buttonsPanel.add(modelCoarsenButton);
        buttonsPanel.add(modelGetFinalButton);
        modelRefineButton.setWidth("100px");
        modelCoarsenButton.setWidth("100px");
        modelGetFinalButton.setWidth("100px");
        buttonsPanel.setStyleName("buttonPanel");
        controlsPanel.add(buttonsPanel);

        HorizontalPanel buttonsPanelTwo = new HorizontalPanel();
        buttonsPanelTwo.add(modelExportDotButton);
        buttonsPanelTwo.add(modelExportPngButton);
        modelExportDotButton.setWidth("100px");
        modelExportPngButton.setWidth("100px");
        buttonsPanelTwo.setStyleName("buttonPanel");
        controlsPanel.add(buttonsPanelTwo);

        VerticalPanel logPanel = new VerticalPanel();
        logPanel.setWidth("300px");

        // Header
        Label logLineLabel = new Label("Log Lines");
        DOM.setElementAttribute(logLineLabel.getElement(), "id",
                "log-line-label");

        // Add tooltip to LogLineLabel
        TooltipListener tooltip = new TooltipListener(
                "Double-click on a node to view log lines", 5000, "tooltip");
        logLineLabel.addMouseOverHandler(tooltip);
        logLineLabel.addMouseOutHandler(tooltip);
        logPanel.add(logLineLabel);

        // Add log lines display table
        logLineTable = new FlexTable();
        logLineTable.setText(0, 0, "Line #");
        logLineTable.setText(0, 1, "Line");
        logLineTable.setText(0, 2, "Filename");
        logPanel.add(logLineTable);

        // Style table
        logLineTable.addStyleName("FlexTable");
        HTMLTable.RowFormatter rf = logLineTable.getRowFormatter();
        rf.addStyleName(0, "TableHeader");
        HTMLTable.ColumnFormatter cf = logLineTable.getColumnFormatter();
        cf.addStyleName(0, "LineNumCol");
        cf.addStyleName(1, "LineCol");
        cf.addStyleName(2, "FilenameCol");

        controlsPanel.add(logPanel);
        panel.add(controlsPanel, DockPanel.WEST);

        // Coarsening is disabled until refinement is completed.
        modelCoarsenButton.setEnabled(false);
        modelRefineButton.addClickHandler(new RefineModelHandler());
        modelCoarsenButton.addClickHandler(new CoarsenModelHandler());
        modelGetFinalButton.addClickHandler(new GetFinalModelHandler());
        modelExportDotButton.addClickHandler(new ExportDotHandler());
        modelExportPngButton.addClickHandler(new ExportPngHandler());
    }

    /** Shows the GWTGraph object on the screen in the modelPanel */
    public void showGraph(GWTGraph graph) {
        modelRefineButton.setEnabled(true);
        modelCoarsenButton.setEnabled(false);
        modelGetFinalButton.setEnabled(true);

        // Clear the second (non-button ) widget model
        // panel.
        if (panel.getWidgetCount() > 1) {
            panel.remove(panel.getWidget(1));
            assert (panel.getWidgetCount() == 1);
        }

        // Clear the log line table.
        clearLogTable();

        String canvasId = "canvasId";

        graphPanel = new FlowPanel();
        graphPanel.getElement().setId(canvasId);
        graphPanel.setStylePrimaryName("modelCanvas");
        // modelPanel.addEast(graphPanel, 70);
        panel.add(graphPanel, DockPanel.CENTER);
        // Create the list of graph node labels and their Ids.
        HashMap<Integer, String> nodes = graph.getNodes();
        JavaScriptObject jsNodes = JavaScriptObject.createArray();
        for (Integer key : nodes.keySet()) {
            JsniUtil.pushArray(jsNodes, key.toString());
            JsniUtil.pushArray(jsNodes, nodes.get(key));
        }

        // Create the list of edges, where two consecutive node Ids is an edge.
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        List<GWTTriplet<Integer, Integer, Double>> edges = graph.getEdges();
        for (GWTTriplet<Integer, Integer, Double> edge : edges) {
            JsniUtil.pushArray(jsEdges, edge.getLeft().toString());
            JsniUtil.pushArray(jsEdges, edge.getMiddle().toString());

            // This contains the edge's weight.
            JsniUtil.pushArray(jsEdges, edge.getRight().toString());
        }

        // Determine the size of the graphic -- make it depend on the current
        // window size.
        // TODO: make sizing more robust, and allow users to resize the graphic
        int width = Math.max(Window.getClientWidth() - 600, 300);
        int height = Math.max(Window.getClientHeight() - 300, 300);
        graphPanel.setPixelSize(width, height);
        ModelGraphic mGraphic = new ModelGraphic();
        mGraphic.createGraph(jsNodes, jsEdges, width, height, canvasId,
                INITIAL_LABEL, TERMINAL_LABEL);
    }

    /**
     * Shows the refined GWTGraph object on the screen in the modelPanel,
     * animating transition to new positions
     * 
     * @param graph
     *            the updated graph to display
     * @param refinedNode
     *            the refined node's id
     */
    public void showChangingGraph(GWTGraph graph, int refinedNode) {
        String canvasId = "canvasId";
        HashMap<Integer, String> nodes = graph.getNodes();
        JavaScriptObject jsNodes = JavaScriptObject.createArray();
        for (Integer key : nodes.keySet()) {
            JsniUtil.pushArray(jsNodes, key.toString());
            JsniUtil.pushArray(jsNodes, nodes.get(key));
        }

        // Create the list of edges, where two consecutive node Ids is an edge.
        // JavaScriptObject newEdges = JavaScriptObject.createArray();
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        List<GWTTriplet<Integer, Integer, Double>> edges = graph.getEdges();
        for (GWTTriplet<Integer, Integer, Double> edge : edges) {
            JsniUtil.pushArray(jsEdges, edge.getLeft().toString());
            JsniUtil.pushArray(jsEdges, edge.getMiddle().toString());
        }

        // Determine the size of the graphic -- make it depend on the current
        // window size.
        // TODO: make sizing more robust, and allow users to resize the graphic
        int width = Math.max(Window.getClientWidth() - 600, 300);
        int height = Math.max(Window.getClientHeight() - 300, 300);
        graphPanel.setPixelSize(width, height);

        ModelGraphic.createChangingGraph(jsNodes, jsEdges, refinedNode,
                canvasId);
    }

    /**
     * Requests the log lines for the Partition with the given nodeID.
     */
    public void LogLineRequestHandler(int nodeID) throws Exception {
        // ////////////////////// Call to remote service.
        synopticService
                .handleLogRequest(nodeID, new ViewLogLineAsyncCallback());
        // //////////////////////
    }

    /** Removes currently displayed log lines from the log line table. */
    private void clearLogTable() {
        for (int i = 1; i < logLineTable.getRowCount(); i++) {
            logLineTable.removeRow(i);
        }
    }

    /**
     * Displays the returned log lines from a LogLineRequest.
     */
    class ViewLogLineAsyncCallback implements AsyncCallback<List<LogLine>> {

        @Override
        public void onFailure(Throwable caught) {
            // This is expected whenever the user double clicks on an initial or
            // terminal
            // node, so we'll ignore it
            clearLogTable();
        }

        @Override
        public void onSuccess(List<LogLine> result) {
            clearLogTable();
            int row = 1;
            for (LogLine log : result) {
                logLineTable.setText(row, 0, log.getLineNum() + "");
                logLineTable.setText(row, 1, log.getLine());
                logLineTable.setText(row, 2, log.getFilename());
                row++;
            }
        }
    }

    /**
     * Used for handling Refine button clicks.
     */
    class RefineModelHandler implements ClickHandler {
        /**
         * Fired when the user clicks on the Refine.
         */
        @Override
        public void onClick(ClickEvent event) {
            pWheel.startAnimation();
            modelRefineButton.setEnabled(false);

            // ////////////////////// Call to remote service.
            try {
                synopticService.refineOneStep(new RefineOneStepAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // //////////////////////
        }
    }

    /**
     * Callback handler for refineOneStep().
     **/
    class RefineOneStepAsyncCallback implements AsyncCallback<GWTGraphDelta> {
        @Override
        public void onFailure(Throwable caught) {
            pWheel.stopAnimation();
            displayRPCErrorMessage("Remote Procedure Call Failure while refining");
            modelRefineButton.setEnabled(true);
        }

        @Override
        public void onSuccess(GWTGraphDelta graph) {
            pWheel.stopAnimation();
            if (graph == null) {
                modelCoarsenButton.setEnabled(true);
                return;
            }
            modelRefineButton.setEnabled(true);
            // Do we want to surprise the user and switch their view for them?
            // tabPanel.selectTab(2);
            showChangingGraph(graph.getGraph(), graph.getRefinedNode());

        }
    }

    /**
     * Handles coarsen button clicks.
     */
    class CoarsenModelHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            pWheel.startAnimation();

            // ////////////////////// Call to remote service.
            modelCoarsenButton.setEnabled(false);
            try {
                synopticService
                        .coarsenOneStep(new CoarsenOneStepAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // //////////////////////
        }
    }

    /**
     * Callback handler for coarsenOneStep().
     **/
    class CoarsenOneStepAsyncCallback implements AsyncCallback<GWTGraph> {
        @Override
        public void onFailure(Throwable caught) {
            pWheel.stopAnimation();
            displayRPCErrorMessage("Remote Procedure Call Failure while coarsening");
        }

        @Override
        public void onSuccess(GWTGraph graph) {
            pWheel.stopAnimation();
            // Do we want to surprise the user and switch their view for them?
            // tabPanel.selectTab(2);
            showGraph(graph);
        }
    }

    /**
     * Handles Final Model button clicks.
     */
    class GetFinalModelHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            pWheel.startAnimation();
            modelRefineButton.setEnabled(false);
            modelCoarsenButton.setEnabled(false);
            modelGetFinalButton.setEnabled(false);

            // ////////////////////// Call to remote service.
            try {
                synopticService.getFinalModel(new GetFinalModelAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // //////////////////////
        }
    }

    /**
     * Callback handler for coarsenOneStep()
     **/
    class GetFinalModelAsyncCallback implements AsyncCallback<GWTGraph> {
        @Override
        public void onFailure(Throwable caught) {
            pWheel.stopAnimation();
            displayRPCErrorMessage("Remote Procedure Call Failure while fetching final model");
        }

        @Override
        public void onSuccess(GWTGraph graph) {
            pWheel.stopAnimation();
            // Do we want to surprise the user and switch their view for them?
            // tabPanel.selectTab(2);
            showGraph(graph);
        }
    }

    /**
     * Handles Export DOT button clicks
     */
    class ExportDotHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            // ////////////////////// Call to remote service.
            try {
                synopticService.exportDot(new ExportDotAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // //////////////////////
        }
    }

    /**
     * Callback handler for exportDot().
     */
    class ExportDotAsyncCallback implements AsyncCallback<String> {
        @Override
        public void onFailure(Throwable caught) {
            displayRPCErrorMessage("Remote Procedure Call Failure while exporting current model");
        }

        @Override
        public void onSuccess(String fileString) {
            Window.open("../" + fileString, "DOT file", "Enabled");
        }
    }

    /**
     * Handles Export PNG button clicks
     */
    class ExportPngHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            // ////////////////////// Call to remote service.
            try {
                synopticService.exportPng(new ExportPngAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // //////////////////////
        }
    }

    /**
     * Callback handler for exportPng().
     */
    class ExportPngAsyncCallback implements AsyncCallback<String> {
        @Override
        public void onFailure(Throwable caught) {
            displayRPCErrorMessage("Remote Procedure Call Failure while exporting current model");
        }

        @Override
        public void onSuccess(String fileString) {
            Window.open("../" + fileString, "PNG file", "Enabled");
        }
    }

}
