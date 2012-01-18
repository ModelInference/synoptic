package synopticgwt.client.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.Tab;
import synopticgwt.client.util.ErrorReportingAsyncCallback;
import synopticgwt.client.util.JsniUtil;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.client.util.TooltipListener;
import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTNode;
import synopticgwt.shared.LogLine;

/**
 * Represents the model tab, using which the user can refine/coarsen the model
 * and find out how the input log corresponds to paths and to nodes in the
 * model.
 */
public class ModelTab extends Tab<DockPanel> {
    // TODO: INITIAL_LABEL and TERMINAL_LABEL should not be hardcoded. Instead,
    // the EventType class should be ported to GWT, or a mirror type should be
    // created which would have the notion of initial/terminal based on
    // EventType.isInitialEventTyep and EventType.isTerminalEventType.

    /* Label of initial node, for layout purposes */
    private static final String INITIAL_LABEL = "INITIAL";

    /* Label of terminal node, for layout purposes */
    private static final String TERMINAL_LABEL = "TERMINAL";

    // Panels containing all relevant buttons.
    private final HorizontalPanel buttonsPanel = new HorizontalPanel();
    private final VerticalPanel controlsPanel = new VerticalPanel();

    // The set of node IDs that have been selected by the user in the model.
    private final Set<Integer> selectedNodes = new HashSet<Integer>();

    // Model tab widgets:
    private final Button modelRefineButton = new Button("Refine");
    private final Button modelCoarsenButton = new Button("Coarsen");
    private final Button modelGetFinalButton = new Button("Final Model");
    private final Button modelExportDotButton = new Button("Export DOT");
    private final Button modelExportPngButton = new Button("Export PNG");
    private final Button modelViewPathsButton = new Button("View Paths");
    private FlowPanel graphPanel;
    private LogLinesTable logLinesTable;
    private Label logLineLabel;

    // String representing the canvas div.
    private static final String canvasId = "canvasId";

    public ModelTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        super(synopticService, pWheel, "model-tab");
        panel = new DockPanel();

        buttonsPanel.add(modelRefineButton);
        buttonsPanel.add(modelCoarsenButton);
        buttonsPanel.add(modelGetFinalButton);
        buttonsPanel.setStyleName("buttonPanel");

        modelRefineButton.setWidth("100px");
        modelCoarsenButton.setWidth("100px");
        modelGetFinalButton.setWidth("100px");
        controlsPanel.add(buttonsPanel);

        HorizontalPanel buttonsPanelTwo = new HorizontalPanel();
        buttonsPanelTwo.add(modelExportDotButton);
        buttonsPanelTwo.add(modelExportPngButton);

        modelExportDotButton.setWidth("100px");
        modelExportPngButton.setWidth("100px");
        buttonsPanelTwo.setStyleName("buttonPanel");
        controlsPanel.add(buttonsPanelTwo);

        // Third button panel (so far, only contains the "view paths" button).
        HorizontalPanel viewPathsButtonPanel = new HorizontalPanel();
        viewPathsButtonPanel.add(modelViewPathsButton);

        modelViewPathsButton.setWidth("200px");
        viewPathsButtonPanel.setStyleName("buttonPanel");
        controlsPanel.add(viewPathsButtonPanel);

        VerticalPanel logPanel = new VerticalPanel();
        logPanel.setWidth("300px");

        // Header
        logLineLabel = new Label("Log Lines");
        DOM.setElementAttribute(logLineLabel.getElement(), "id",
                "log-line-label");

        // Add tool-tip to LogLineLabel
        TooltipListener tooltip = new TooltipListener(
                "Click on a node to view log lines.  Shift+Click to select multiple nodes.",
                5000, "tooltip");
        logLineLabel.addMouseOverHandler(tooltip);
        logLineLabel.addMouseOutHandler(tooltip);
        logPanel.add(logLineLabel);

        // Create and add a table with log lines.
        logLinesTable = new LogLinesTable();
        logPanel.add(logLinesTable);

        controlsPanel.add(logPanel);
        panel.add(controlsPanel, DockPanel.WEST);

        // Keep the view paths button disabled until
        // nodes have been selected.
        modelViewPathsButton.setEnabled(false);

        // Coarsening is disabled until refinement is completed.
        modelCoarsenButton.setEnabled(false);
        modelRefineButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refineButtonClick(event);
            }
        });
        modelCoarsenButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                coarsenModelButtonClick(event);
            }
        });
        modelGetFinalButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getFinalModelButtonClick(event);
            }
        });
        modelExportDotButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                exportDotButtonClick(event);
            }
        });
        modelExportPngButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                exportPngButtonClick(event);
            }
        });
        modelViewPathsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                viewSelectedPathsButtonClick(event);
            }
        });
    }

    /**
     * Shows the GWTGraph object on the screen in the modelPanel. NOTE: the
     * model tab MUST be made visible for showGraph to work.
     */
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
        logLinesTable.clear();

        graphPanel = new FlowPanel();
        graphPanel.getElement().setId(canvasId);
        graphPanel.setStylePrimaryName("modelCanvas");
        // modelPanel.addEast(graphPanel, 70);
        panel.add(graphPanel, DockPanel.CENTER);
        // Create the list of graph node labels and their Ids.
        HashSet<GWTNode> nodeSet = graph.getNodes();
        JavaScriptObject jsNodes = JavaScriptObject.createArray();
        for (GWTNode node : nodeSet) {
            JsniUtil.pushArray(jsNodes,
                    ((Integer) node.getPartitionNodeHashCode()).toString());
            JsniUtil.pushArray(jsNodes, node.toString());
        }

        // Create the list of edges, where two consecutive node Ids is an edge.
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        for (GWTEdge edge : graph.getEdges()) {
            JsniUtil.pushArray(jsEdges, ((Integer) edge.getSrc()
                    .getPartitionNodeHashCode()).toString());
            JsniUtil.pushArray(jsEdges, ((Integer) edge.getDst()
                    .getPartitionNodeHashCode()).toString());

            // This contains the edge's weight.
            JsniUtil.pushArray(jsEdges, ((Double) edge.getWeight()).toString());
        }

        // Determine the size of the graphic.
        int width = getModelGraphicWidth();
        int height = getModelGraphicHeight();
        graphPanel.setPixelSize(width, height);
        ModelGraphic.createGraph(this, jsNodes, jsEdges, width, height,
                canvasId, INITIAL_LABEL, TERMINAL_LABEL);
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
    public void showChangingGraph(GWTGraph graph, GWTNode refinedNode) {
        HashSet<GWTNode> nodeSet = graph.getNodes();
        JavaScriptObject jsNodes = JavaScriptObject.createArray();
        for (GWTNode node : nodeSet) {
            JsniUtil.pushArray(jsNodes,
                    ((Integer) node.getPartitionNodeHashCode()).toString());
            JsniUtil.pushArray(jsNodes, node.toString());
        }

        // Create the list of edges, where two consecutive node Ids is an edge.
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        List<GWTEdge> edgeList = graph.getEdges();
        for (GWTEdge edge : edgeList) {
            JsniUtil.pushArray(jsEdges, ((Integer) edge.getSrc()
                    .getPartitionNodeHashCode()).toString());
            JsniUtil.pushArray(jsEdges, ((Integer) edge.getDst()
                    .getPartitionNodeHashCode()).toString());
            JsniUtil.pushArray(jsEdges, ((Double) edge.getWeight()).toString());
        }

        ModelGraphic.createChangingGraph(jsNodes, jsEdges,
                refinedNode.getPartitionNodeHashCode(), canvasId);
    }

    /**
     * Requests the log lines for the Partition with the given nodeID. This
     * method is called from JavaScript when model nodes are double clicked.
     */
    public void handleLogRequest(int nodeID) throws Exception {
        // ////////////////////// Call to remote service.
        synopticService.handleLogRequest(nodeID,
                new ErrorReportingAsyncCallback<List<LogLine>>(pWheel,
                        "handleLogRequest call") {

                    @SuppressWarnings("synthetic-access")
                    @Override
                    public void onFailure(Throwable caught) {
                        super.onFailure(caught);
                        // TODO: differentiate between clicks on
                        // initial/terminal nodes and
                        // other nodes.

                        // This is expected whenever the user double clicks on
                        // an initial or
                        // terminal node, so we'll ignore it
                        logLinesTable.clear();
                    }

                    @SuppressWarnings("synthetic-access")
                    @Override
                    public void onSuccess(List<LogLine> result) {
                        super.onSuccess(result);
                        logLinesTable.showLines(result);
                    }
                });
        // //////////////////////
    }

    /** Returns the correct width for the model graphic in the model tab. */
    public int getModelGraphicWidth() {
        // TODO: make this more robust -- perhaps, by hard-coding the percentage
        // area that the model can take up.
        return Window.getClientWidth() - (logLineLabel.getOffsetWidth() + 100);
    }

    /** Returns the correct height for the model graphic in the model tab. */
    public int getModelGraphicHeight() {
        // TODO: make this more robust -- perhaps, by hard-coding the percentage
        // area that the model can take up.
        /* The 200 offset represents the top Synoptic header */
        return Window.getClientHeight() - 200;
    }

    /**
     * Updates the graph panel's canvas, and animates the model the fill the new
     * canvas.
     */
    public void updateGraphPanel() {
        int width = getModelGraphicWidth();
        int height = getModelGraphicHeight();

        graphPanel.setPixelSize(width, height);
        ModelGraphic.resizeGraph(width, height);
    }

    /**
     * Generates a call to Synoptic service to refine the model by a single
     * step.
     */
    public void refineButtonClick(ClickEvent event) {
        modelRefineButton.setEnabled(false);

        // ////////////////////// Call to remote service.
        try {
            synopticService
                    .refineOneStep(new ErrorReportingAsyncCallback<GWTGraphDelta>(
                            pWheel, "refineOneStep call") {
                        @SuppressWarnings("synthetic-access")
                        @Override
                        public void onFailure(Throwable caught) {
                            super.onFailure(caught);
                            modelRefineButton.setEnabled(true);
                        }

                        @Override
                        public void onSuccess(GWTGraphDelta graph) {
                            super.onSuccess(graph);
                            refineOneStepSuccess(graph);
                        }
                    });
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        // //////////////////////
    }

    /** Called when a refinement call to the service succeeded. */
    public void refineOneStepSuccess(GWTGraphDelta graph) {
        if (graph == null) {
            // Graph is null when no refinement occurred.
            modelCoarsenButton.setEnabled(true);
            return;
        }

        // Show an animation of refinement.
        showChangingGraph(graph.getGraph(), graph.getRefinedNode());

        if (graph.getUnsatInvs().invs.size() == 0) {
            // No further refinement is possible: disable refinement, enable
            // coarsening.
            modelRefineButton.setEnabled(false);
            modelCoarsenButton.setEnabled(true);
        } else {
            // Refinement still possible -- re-enable refinement.
            modelRefineButton.setEnabled(true);
        }
    }

    /** Generates a call to Synoptic service to coarsen the model. */
    public void coarsenModelButtonClick(ClickEvent event) {
        // ////////////////////// Call to remote service.
        modelCoarsenButton.setEnabled(false);
        try {
            synopticService
                    .coarsenOneStep(new ErrorReportingAsyncCallback<GWTGraph>(
                            pWheel, "coarsenOneStep call") {
                        @SuppressWarnings("synthetic-access")
                        @Override
                        public void onSuccess(GWTGraph graph) {
                            super.onSuccess(graph);
                            showGraph(graph);
                            modelRefineButton.setEnabled(false);
                            modelCoarsenButton.setEnabled(false);
                            modelGetFinalButton.setEnabled(false);
                        }
                    });
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        // //////////////////////
    }

    /** Generates a call to Synoptic service to retrieve the final model. */
    public void getFinalModelButtonClick(ClickEvent event) {
        modelRefineButton.setEnabled(false);
        modelCoarsenButton.setEnabled(false);
        modelGetFinalButton.setEnabled(false);

        // ////////////////////// Call to remote service.
        try {
            synopticService
                    .getFinalModel(new ErrorReportingAsyncCallback<GWTGraph>(
                            pWheel, "getFinalModel call") {

                        @Override
                        public void onSuccess(GWTGraph graph) {
                            super.onSuccess(graph);
                            getFinalModelSuccess(graph);
                        }
                    });
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        // //////////////////////
    }

    /**
     * Generates a call to the Synoptic server which then gets all of the paths
     * through the nodes which the user has selected.
     * 
     * @param event
     */
    public void viewSelectedPathsButtonClick(ClickEvent event) {
        try {
            // Query the server for paths through the selected
            // nodes.
            synopticService
                    .getPathsThroughSelectedNodes(
                            selectedNodes,
                            new ErrorReportingAsyncCallback<Map<Integer, Set<GWTEdge>>>(
                                    pWheel, "get paths call") {
                                @Override
                                public void onSuccess(
                                        Map<Integer, Set<GWTEdge>> paths) {
                                    // TODO Show the traces and corresponding
                                    // paths in the client window.
                                    super.onSuccess(paths);

                                    // TODO Remove this debug code.
                                    for (Integer traceID : paths.keySet()) {
                                        Set<GWTEdge> edges = paths.get(traceID);
                                        ModelGraphic.printTraceID(traceID);
                                        for (GWTEdge edge : edges) {
                                            ModelGraphic.printEdge(edge.getSrc().toString(), edge.getDst().toString());
                                        }   
                                    }
                                }
                            });
        } catch (Exception ex) {
            // TODO Inform the user of this error if it happens.
        }
    }

    /** Called when the call to retrieve final model succeeded. */
    public void getFinalModelSuccess(GWTGraph graph) {
        showGraph(graph);
        modelRefineButton.setEnabled(false);
        modelCoarsenButton.setEnabled(false);
        modelGetFinalButton.setEnabled(false);
    }

    /** Calls the Synoptic service to export the DOT file for the model. */
    public void exportDotButtonClick(ClickEvent event) {
        // ////////////////////// Call to remote service.
        try {
            synopticService.exportDot(new ErrorReportingAsyncCallback<String>(
                    pWheel, "exportDot call") {
                @Override
                public void onSuccess(String fileString) {
                    super.onSuccess(fileString);
                    Window.open("../" + fileString, "DOT file", "Enabled");
                }
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // //////////////////////
    }

    /** Calls the Synoptic service to export the PNG file for the model. */
    public void exportPngButtonClick(ClickEvent event) {
        // ////////////////////// Call to remote service.
        try {
            synopticService.exportPng(new ErrorReportingAsyncCallback<String>(
                    pWheel, "exportPng call") {

                @Override
                public void onSuccess(String fileString) {
                    super.onSuccess(fileString);
                    Window.open("../" + fileString, "PNG file", "Enabled");
                }
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // //////////////////////
    }

    /**
     * Adds a node as being "selected" to the model tab.
     * 
     * @param nodeID
     *            The ID of the selected event node.
     */
    public void addSelectedNode(int nodeID) {
        // Add the selected node to the list of all selecetd
        // nodes.
        selectedNodes.add(nodeID);
        toggleViewPathsButton();
    }

    /**
     * Removes a node as being "selected" from the model tab.
     * 
     * @param nodeID
     *            The ID of the selected event node.
     */
    public void removeSelectedNode(int nodeID) {
        // Add the selected node to the list of all selecetd
        // nodes.
        selectedNodes.remove(nodeID);
        toggleViewPathsButton();
    }

    /**
     * A simple method that checks to see if one or more nodes have been
     * selected. If so, the button for viewing paths is activated. If not, this
     * button is deactivated.
     */
    private void toggleViewPathsButton() {
        if (selectedNodes.size() > 0)
            modelViewPathsButton.setEnabled(true);
        else
            modelViewPathsButton.setEnabled(false);
    }

    /**
     * Manual control is translated to making refine/coarsen/final model buttons
     * visible.
     */
    public void setManualMode(boolean manualRefineCoarsen) {
        buttonsPanel.setVisible(manualRefineCoarsen);
    }
}
