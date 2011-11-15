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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.Tab;
import synopticgwt.client.util.JsniUtil;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.client.util.TooltipListener;
import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
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

	// Model tab widgets:
	private final Button modelRefineButton = new Button("Refine");
	private final Button modelCoarsenButton = new Button("Coarsen");
	private final Button modelGetFinalButton = new Button("Final Model");
	private final Button modelExportDotButton = new Button("Export DOT");
	private final Button modelExportPngButton = new Button("Export PNG");
	private FlowPanel graphPanel;
	private LogLinesTable logLinesTable;
	
	// String representing the canvas div.
	private static final String canvasId = "canvasId";

	public ModelTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
		super(synopticService, pWheel);
		panel = new DockPanel();

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

		// Create and add a table with log lines.
		logLinesTable = new LogLinesTable();
		logPanel.add(logLinesTable);

		controlsPanel.add(logPanel);
		panel.add(controlsPanel, DockPanel.WEST);

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
		HashMap<Integer, String> nodes = graph.getNodes();
		JavaScriptObject jsNodes = JavaScriptObject.createArray();
		for (Integer key : nodes.keySet()) {
			JsniUtil.pushArray(jsNodes, key.toString());
			JsniUtil.pushArray(jsNodes, nodes.get(key));
		}

		// Create the list of edges, where two consecutive node Ids is an edge.
		JavaScriptObject jsEdges = JavaScriptObject.createArray();
		for (GWTEdge edge : graph.getEdges()) {
			JsniUtil.pushArray(jsEdges, ((Integer) edge.getSrc()).toString());
			JsniUtil.pushArray(jsEdges, ((Integer) edge.getDst()).toString());

			// This contains the edge's weight.
			JsniUtil.pushArray(jsEdges, ((Double) edge.getWeight()).toString());
		}

		// Determine the size of the graphic -- make it depend on the current
		// window size.
		// TODO: Currently the +50 with buttonsPanel's width is to account
		// for a small offset, and needs a better way to be consistently
		// calculated.
		// The minus 100 as also arbitrary and needs a better way to be
		// calculated.
		// TODO: make sizing more robust, and allow users to resize the graphic
		int width = Window.getClientWidth()
				- (buttonsPanel.getOffsetWidth() + 50);
		int height = Window.getClientHeight() - 100;
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
	public void showChangingGraph(GWTGraph graph, int refinedNode) {
		HashMap<Integer, String> nodes = graph.getNodes();
		JavaScriptObject jsNodes = JavaScriptObject.createArray();
		for (Integer key : nodes.keySet()) {
			JsniUtil.pushArray(jsNodes, key.toString());
			JsniUtil.pushArray(jsNodes, nodes.get(key));
		}

		// Create the list of edges, where two consecutive node Ids is an edge.
		JavaScriptObject jsEdges = JavaScriptObject.createArray();
		List<GWTEdge> edges = graph.getEdges();
		for (GWTEdge edge : edges) {
			JsniUtil.pushArray(jsEdges, ((Integer) edge.getSrc()).toString());
			JsniUtil.pushArray(jsEdges, ((Integer) edge.getDst()).toString());
			JsniUtil.pushArray(jsEdges, ((Double) edge.getWeight()).toString());
		}

		ModelGraphic.createChangingGraph(jsNodes, jsEdges, refinedNode,
				canvasId);
	}

	/** Called when the request to get log lines for a partition failed. */
	public void viewLogLineFailure(Throwable caught) {
		// TODO: differentiate between clicks on initial/terminal nodes and
		// other nodes.

		// This is expected whenever the user double clicks on an initial or
		// terminal node, so we'll ignore it
		logLinesTable.clear();
	}

	/** Called when the request to get log lines for a partition succeeded. */
	public void viewLogLineSuccess(List<LogLine> result) {
		logLinesTable.showLines(result);
	}

	/**
	 * Requests the log lines for the Partition with the given nodeID. This
	 * method is called from JavaScript when model nodes are double clicked.
	 */
	public void handleLogRequest(int nodeID) throws Exception {
		// ////////////////////// Call to remote service.
		synopticService.handleLogRequest(nodeID,
				new AsyncCallback<List<LogLine>>() {

					@Override
					public void onFailure(Throwable caught) {
						viewLogLineFailure(caught);
					}

					@Override
					public void onSuccess(List<LogLine> result) {
						viewLogLineSuccess(result);
					}
				});
		// //////////////////////
	}
	
	/**
	 * Updates the graph panel's canvas,
	 * and animates the model the fill the new canvas.
	 */
	public void updateGraphPanel() {
		int width = Window.getClientWidth()
				- (buttonsPanel.getOffsetWidth() + 50);
		int height = Window.getClientHeight() - 100;
		graphPanel.setPixelSize(width, height);
		ModelGraphic.resizeGraph(width, height);
	}

	/**
	 * Generates a call to Synoptic service to refine the model by a single
	 * step.
	 */
	public void refineButtonClick(ClickEvent event) {
		pWheel.startAnimation();
		modelRefineButton.setEnabled(false);

		// ////////////////////// Call to remote service.
		try {
			synopticService.refineOneStep(new AsyncCallback<GWTGraphDelta>() {
				@Override
				public void onFailure(Throwable caught) {
					refineOneStepFailure(caught);
				}

				@Override
				public void onSuccess(GWTGraphDelta graph) {
					refineOneStepSuccess(graph);
				}
			});
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		// //////////////////////
	}

	/** Called when a refinement call to the service failed. */
	public void refineOneStepFailure(Throwable caught) {
		pWheel.stopAnimation();
		displayRPCErrorMessage("Remote Procedure Call Failure while refining: "
				+ caught.toString());
		modelRefineButton.setEnabled(true);
	}

	/** Called when a refinement call to the service succeeded. */
	public void refineOneStepSuccess(GWTGraphDelta graph) {
		pWheel.stopAnimation();

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
		// Do we want to surprise the user and switch their view for them?
		// tabPanel.selectTab(2);
	}

	/** Generates a call to Synoptic service to coarsen the model. */
	public void coarsenModelButtonClick(ClickEvent event) {
		pWheel.startAnimation();

		// ////////////////////// Call to remote service.
		modelCoarsenButton.setEnabled(false);
		try {
			synopticService.coarsenOneStep(new AsyncCallback<GWTGraph>() {
				@Override
				public void onFailure(Throwable caught) {
					coarsenOneStepFailure(caught);
				}

				@Override
				public void onSuccess(GWTGraph graph) {
					coarsenOneStepSuccess(graph);
				}
			});
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		// //////////////////////
	}

	/** Called when the coarsening call to the service failed. */
	public void coarsenOneStepFailure(Throwable caught) {
		pWheel.stopAnimation();
		displayRPCErrorMessage("Remote Procedure Call Failure while coarsening");

	}

	/** Called when the coarsening call to the service succeeded. */
	public void coarsenOneStepSuccess(GWTGraph graph) {
		pWheel.stopAnimation();
		// Do we want to surprise the user and switch their view for them?
		// tabPanel.selectTab(2);
		showGraph(graph);

		modelRefineButton.setEnabled(false);
		modelCoarsenButton.setEnabled(false);
		modelGetFinalButton.setEnabled(false);
	}

	/** Generates a call to Synoptic service to retrieve the final model. */
	public void getFinalModelButtonClick(ClickEvent event) {
		pWheel.startAnimation();
		modelRefineButton.setEnabled(false);
		modelCoarsenButton.setEnabled(false);
		modelGetFinalButton.setEnabled(false);

		// ////////////////////// Call to remote service.
		try {
			synopticService.getFinalModel(new AsyncCallback<GWTGraph>() {
				@Override
				public void onFailure(Throwable caught) {
					getFinalModelFailure(caught);
				}

				@Override
				public void onSuccess(GWTGraph graph) {
					getFinalModelSuccess(graph);
				}
			});
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		// //////////////////////
	}

	/** Called when the call to retrieve final model failed. */
	public void getFinalModelFailure(Throwable caught) {
		pWheel.stopAnimation();
		displayRPCErrorMessage("Remote Procedure Call Failure while fetching final model");

	}

	/** Called when the call to retrieve final model succeeded. */
	public void getFinalModelSuccess(GWTGraph graph) {
		pWheel.stopAnimation();
		// Do we want to surprise the user and switch their view for them?
		// tabPanel.selectTab(2);
		showGraph(graph);
		modelRefineButton.setEnabled(false);
		modelCoarsenButton.setEnabled(false);
		modelGetFinalButton.setEnabled(false);

	}

	/** Calls the Synoptic service to export the DOT file for the model. */
	public void exportDotButtonClick(ClickEvent event) {
		// ////////////////////// Call to remote service.
		try {
			synopticService.exportDot(new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					exportDotFailure(caught);
				}

				@Override
				public void onSuccess(String fileString) {
					exportDotSuccess(fileString);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// //////////////////////
	}

	/** Called when export DOT failed. */
	public void exportDotFailure(Throwable caught) {
		displayRPCErrorMessage("Remote Procedure Call Failure while exporting current model");
	}

	/** Called when export DOT succeeded. */
	public void exportDotSuccess(String fileString) {
		Window.open("../" + fileString, "DOT file", "Enabled");
	}

	/** Calls the Synoptic service to export the PNG file for the model. */
	public void exportPngButtonClick(ClickEvent event) {
		// ////////////////////// Call to remote service.
		try {
			synopticService.exportPng(new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					exportPngFailure(caught);
				}

				@Override
				public void onSuccess(String fileString) {
					exportPngSuccess(fileString);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// //////////////////////
	}

	/** Called when export PNg failed. */
	public void exportPngFailure(Throwable caught) {
		displayRPCErrorMessage("Remote Procedure Call Failure while exporting current model");
	}

	/** Called when export PNG succeeded. */
	public void exportPngSuccess(String fileString) {
		Window.open("../" + fileString, "PNG file", "Enabled");
	}
}
