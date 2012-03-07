package synopticgwt.client.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.Tab;
import synopticgwt.client.model.JSGraph.EdgeLabelType;
import synopticgwt.client.util.ErrorReportingAsyncCallback;
import synopticgwt.client.util.FlowLayoutPanel;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.client.util.TooltipListener;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.LogLine;

/**
 * Represents the model tab, using which the user can refine/coarsen the model
 * and find out how the input log corresponds to paths and to nodes in the
 * model.
 */
public class ModelTab extends Tab<DockLayoutPanel> {

    // CSS Attributes of the log info label
    public static final String LOG_INFO_PATHS_CLASS = "log-info-displaying-paths";
    public static final String LOG_INFO_LINES_CLASS = "log-info-displaying-log-lines";
    public static final String LOG_INFO_LABEL_ID = "log-info-label";

    // Panels containing all relevant buttons.
    private final HorizontalPanel manualControlButtonsPanel = new HorizontalPanel();
    private final FlowLayoutPanel controlsPanel = new FlowLayoutPanel();

    protected final LogInfoPanel logInfoPanel;

    public static final String TOOLTIP_URL = "http://code.google.com/p/synoptic/wiki/DocsWebAppTutorial#Invariants_Tab";

    // Model tab widgets:
    private final Button modelRefineButton = new Button("Refine");
    private final Button modelCoarsenButton = new Button("Coarsen");
    private final Button modelGetFinalButton = new Button("Final Model");
    private final Button modelExportDotButton = new Button("Export DOT");
    private final Button modelExportPngButton = new Button("Export PNG");
    private final Button modelViewPathsButton = new Button("View Paths");

    // Model options widgets
    // Whether or not to show edge transition counts (true) or transition
    // probabilities (false).
    private boolean showEdgeTraceCounts = false;
    private final DisclosurePanel modelOpts = new DisclosurePanel(
            "Model options");
    private final RadioButton probEdgesRadioButton = new RadioButton(
            "edgeLabelsRadioGroup", "Show probabilities on edges");
    private final RadioButton countEdgesRadioButton = new RadioButton(
            "edgeLabelsRadioGroup", "Show counts on edges");

    // Panel containing the model graphic.
    private FlowPanel graphPanel = null;

    // String representing the canvas div.
    private static final String canvasId = "canvasId";

    // The JS representation of the GWTGraph. Handles events on the graph,
    // and maintains the graphical state.
    private JSGraph jsGraph;

    HorizontalPanel exportButtonsPanel;
    HorizontalPanel viewPathsButtonPanel;

    public ModelTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        super(synopticService, pWheel, "model-tab");
        panel = new DockLayoutPanel(Unit.PX);
        panel.setHeight(this.getModelGraphicHeight() + "px");

        // Set up the buttons for controlling Synoptic manually.
        manualControlButtonsPanel.add(modelRefineButton);
        manualControlButtonsPanel.add(modelCoarsenButton);
        manualControlButtonsPanel.add(modelGetFinalButton);
        manualControlButtonsPanel.setStyleName("buttonPanel");

        controlsPanel.setSize("300px", "100%");

        modelRefineButton.setWidth("100px");
        modelCoarsenButton.setWidth("100px");
        modelGetFinalButton.setWidth("100px");
        controlsPanel.add(manualControlButtonsPanel);

        // Set up buttons for exporting models.
        exportButtonsPanel = new HorizontalPanel();
        exportButtonsPanel.add(modelExportDotButton);
        exportButtonsPanel.add(modelExportPngButton);

        modelExportDotButton.setWidth("100px");
        modelExportPngButton.setWidth("100px");
        exportButtonsPanel.setStyleName("buttonPanel");
        controlsPanel.add(exportButtonsPanel);

        // Set up the buttons for retrieving paths through selected nodes.
        viewPathsButtonPanel = new HorizontalPanel();
        viewPathsButtonPanel.add(modelViewPathsButton);

        modelViewPathsButton.setWidth("200px");
        viewPathsButtonPanel.setStyleName("buttonPanel");
        controlsPanel.add(viewPathsButtonPanel);

        // Add a model options panel.
        TooltipListener
                .setTooltip(
                        probEdgesRadioButton,
                        "Annotate edges with probabilities, which indicate the fraction of traces that pass along an edge.",
                        TOOLTIP_URL);
        EdgeViewChangeHandler edgeOptsHandler = new EdgeViewChangeHandler(this);
        probEdgesRadioButton.addValueChangeHandler(edgeOptsHandler);

        TooltipListener
                .setTooltip(
                        countEdgesRadioButton,
                        "Annotate edges with trace counts, which indicate the number of traces that pass along an edge",
                        TOOLTIP_URL);
        countEdgesRadioButton.addValueChangeHandler(edgeOptsHandler);

        Grid modelOptsGrid = new Grid(2, 1);
        modelOptsGrid.setCellSpacing(6);
        modelOptsGrid.setWidget(0, 0, countEdgesRadioButton);
        modelOptsGrid.setWidget(1, 0, probEdgesRadioButton);
        modelOpts.setContent(modelOptsGrid);
        modelOpts.setAnimationEnabled(true);
        modelOpts.setStyleName("SpecialOptions");
        controlsPanel.add(modelOpts);

        // Add log info panel.
        logInfoPanel = new LogInfoPanel("300px", this);
        logInfoPanel.setSize("300px",
                Math.max(this.getModelGraphicHeight() - 200, 400) + "px");
        this.logInfoPanel.getLogLinesTable().setHeight("100%");
        controlsPanel.add(logInfoPanel);

        panel.addWest(controlsPanel, 300);

        TooltipListener
                .setTooltip(
                        modelRefineButton,
                        "Refine the model by splitting nodes to eliminate paths that violate invariants.",
                        TOOLTIP_URL);

        modelRefineButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refineButtonClick(event);
            }
        });

        TooltipListener
                .setTooltip(
                        modelCoarsenButton,
                        "Coarsen the model by merging nodes. Coarsening is disabled until refinement is completed.",
                        TOOLTIP_URL);
        modelCoarsenButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                coarsenModelButtonClick(event);
            }
        });

        TooltipListener
                .setTooltip(
                        modelGetFinalButton,
                        "Perform all the necessary refinement/coarsening and retrieve the final model.",
                        TOOLTIP_URL);
        modelGetFinalButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getFinalModelButtonClick(event);
            }
        });

        TooltipListener.setTooltip(modelExportDotButton,
                "Export the model in Graphviz DOT format.", TOOLTIP_URL);
        modelExportDotButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                exportDotButtonClick(event);
            }
        });

        TooltipListener.setTooltip(modelExportPngButton,
                "Export the model as an PNG image file.", TOOLTIP_URL);
        modelExportPngButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                exportPngButtonClick(event);
            }
        });
        modelViewPathsButton.addClickHandler(new ViewPathsClickHandler());
        TooltipListener
                .setTooltip(
                        modelViewPathsButton,
                        "Shift+Click to select multiple nodes and then use this button to view all paths through the selected nodes.",
                        TOOLTIP_URL);

        initializeTabState();
    }

    public JSGraph getJSGraph() {
        return this.jsGraph;
    }

    /**
     * Changes model edges to displays counts or probabilities.
     */
    class EdgeViewChangeHandler implements ValueChangeHandler<Boolean> {
        ModelTab modelTab;

        public EdgeViewChangeHandler(ModelTab modelTab) {
            this.modelTab = modelTab;
        }

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            modelTab.showEdgeTraceCounts = modelTab.countEdgesRadioButton
                    .getValue();

            if (modelTab.showEdgeTraceCounts) {
                jsGraph.setEdgeLabelType(EdgeLabelType.COUNT);
            } else {
                jsGraph.setEdgeLabelType(EdgeLabelType.WEIGHT);
            }
        }
    }

    public void setShowEdgeCounts(boolean showCounts) {
        this.showEdgeTraceCounts = showCounts;
    }

    public boolean getShowEdgeCounts() {
        return this.showEdgeTraceCounts;
    }

    /**
     * Initialize model tab state, run whenever a new log is parsed.
     */
    public void initializeTabState() {
        // TODO: Is this a bug? Need to check if we can refine.
        modelRefineButton.setEnabled(true);
        // Coarsening is disabled until refinement is completed.
        modelCoarsenButton.setEnabled(false);
        // TODO: Is this a bug? Need to check if initial mode != final model.
        modelGetFinalButton.setEnabled(true);

        // Keep the view paths button disabled until nodes have been selected.
        modelViewPathsButton.setEnabled(false);

        // Set the initial model opts settings.
        probEdgesRadioButton.setValue(true);
        countEdgesRadioButton.setValue(false);
        showEdgeTraceCounts = false;

        logInfoPanel.clear();
    }

    /**
     * Shows the GWTGraph object on the screen in the modelPanel.
     * 
     * <pre>
     * NOTE: the model tab MUST be first made visible for this method to work.
     * </pre>
     */
    public void showGraph(GWTGraph graph) {
        initializeTabState();

        // Remove and re-create the graph panel widget.
        if (graphPanel != null) {
            panel.remove(graphPanel);
        }

        // Create and add the a flow panel that will contain the actual model
        // graphic.
        graphPanel = new FlowPanel();
        graphPanel.getElement().setId(canvasId);
        graphPanel.setStylePrimaryName("modelCanvas");
        panel.add(graphPanel);

        // Determine the size of the graphic.
        int width = getModelGraphicWidth();
        int height = getModelGraphicHeight();
        graphPanel.setPixelSize(width, height);

        this.jsGraph = new JSGraph(this, graph, width, height, canvasId);
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
                        logInfoPanel.clear();
                    }

                    @SuppressWarnings("synthetic-access")
                    @Override
                    public void onSuccess(List<LogLine> result) {
                        super.onSuccess(result);
                        logInfoPanel.showLogLines(result);
                    }
                });
        // //////////////////////
    }

    /** Returns the correct width for the model graphic in the model tab. */
    public int getModelGraphicWidth() {
        // TODO: make this more robust -- perhaps, by hard-coding the percentage
        // area that the model can take up.
        return Math.max(
                Window.getClientWidth() - controlsPanel.getOffsetWidth() + 100,
                400);
    }

    /** Returns the correct height for the model graphic in the model tab. */
    public int getModelGraphicHeight() {
        // TODO: make this more robust -- perhaps, by hard-coding the percentage
        // area that the model can take up.
        /* The 200 offset represents the top Synoptic header */
        return Math.max(Window.getClientHeight() - 200, 400);
    }

    /**
     * Updates the graph panel's canvas, and animates the model to fill the new
     * canvas.
     */
    public void updateGraphPanel() {
        if (graphPanel == null) {
            // This occurs when the graphPanel is first shown -- the graphic is
            // not yet displayed, so we skip the update in this case.
            return;
        }
        int width = getModelGraphicWidth();
        int height = getModelGraphicHeight();

        graphPanel.setPixelSize(width, height);
        this.jsGraph.resize(width, height);

        logInfoPanel
                .setHeight(Math.max(this.getModelGraphicHeight() - 200, 400)
                        + "px");
        panel.setHeight(height + "px");
        this.panel.onResize();
        this.controlsPanel.onResize();
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
    public void refineOneStepSuccess(GWTGraphDelta deltaGraph) {
        if (deltaGraph == null) {
            // Graph is null when no refinement occurred.
            modelCoarsenButton.setEnabled(true);
            return;
        }

        // Set the log lines display to default and clear
        // any information.
        logInfoPanel.clear();

        // Shows the refined GWTGraph object on the screen in the modelPanel,
        // animating transition to new positions.
        this.jsGraph.refineOneStep(deltaGraph);

        if (deltaGraph.getUnsatInvs().invs.size() == 0) {
            // No further refinement is possible: disable refinement, enable
            // coarsening.
            modelRefineButton.setEnabled(false);
            modelCoarsenButton.setEnabled(true);
        } else {
            // Refinement still possible -- re-enable refinement.
            modelRefineButton.setEnabled(true);
        }
    }

    /**
     * Generates a call to Synoptic service to coarsen the model.
     * 
     * <pre>
     * TODO: What we really need is a coarsenOneStep counter-part to refineOneStep.
     * </pre>
     */
    public void coarsenModelButtonClick(ClickEvent event) {
        // ////////////////////// Call to remote service.
        modelCoarsenButton.setEnabled(false);
        try {
            synopticService
                    .coarsenCompletely(new ErrorReportingAsyncCallback<GWTGraph>(
                            pWheel, "coarsenOneStep call") {
                        @SuppressWarnings("synthetic-access")
                        @Override
                        public void onSuccess(GWTGraph graph) {
                            super.onSuccess(graph);
                            showGraph(graph);
                            disableManualControlButtons();
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
        disableManualControlButtons();

        // ////////////////////// Call to remote service.
        try {
            synopticService
                    .getFinalModel(new ErrorReportingAsyncCallback<GWTGraph>(
                            pWheel, "getFinalModel call") {

                        @Override
                        public void onSuccess(GWTGraph graph) {
                            super.onSuccess(graph);
                            showGraph(graph);
                            disableManualControlButtons();
                        }
                    });
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        // //////////////////////
    }

    /** Called when the call to retrieve final model succeeded. */
    public void disableManualControlButtons() {
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
                public void onSuccess(String dotString) {
                    super.onSuccess(dotString);
                    ModelDotPopUp popUp = new ModelDotPopUp(dotString);
                    popUp.setGlassEnabled(true);
                    popUp.center();
                    popUp.show();
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
                    ModelPngPopUp popUp = new ModelPngPopUp(fileString);
                    popUp.setGlassEnabled(true);
                    popUp.center();
                    popUp.show();
                }
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // //////////////////////
    }

    /**
     * A simple method that checks to see if one or more nodes have been
     * selected. If so, the button for viewing paths is activated. If not, this
     * button is deactivated.
     */
    public void updateViewPathsButton() {
        if (jsGraph.getSelectedNodeIDs().size() > 0) {
            modelViewPathsButton.setEnabled(true);
        } else {
            modelViewPathsButton.setEnabled(false);
        }
    }

    /**
     * Manual control is translated to making refine/coarsen/final model buttons
     * visible.
     */
    public void setManualMode(boolean manualRefineCoarsen) {
        manualControlButtonsPanel.setVisible(manualRefineCoarsen);
    }

    /**
     * Class for viewing paths through selected partitions.
     */
    class ViewPathsClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            try {
                synopticService.getPathsThroughPartitionIDs(jsGraph
                        .getSelectedNodeIDs(),
                        new GetPathsThroughPartitionIDsAsyncCallback(pWheel,
                                ModelTab.this.logInfoPanel));

                jsGraph.setPathHighlightViewState();
            } catch (Exception e) {
                // TODO: Do something about the exception
            }
        }
    }
}
