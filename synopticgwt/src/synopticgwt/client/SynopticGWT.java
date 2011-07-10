package synopticgwt.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.GWTTriplet;
import synopticgwt.shared.LogLine;

/**
 * Implements the visual and interactive components of the SynopticGWT wep-app
 */
public class SynopticGWT implements EntryPoint {
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";

    /* Label of initial node, for layout purposes */
    private static final String INITIAL_LABEL = "INITIAL";

    /* Label of terminal node, for layout purposes */
    private static final String TERMINAL_LABEL = "TERMINAL";

    /**
     * Create an RPC proxy to talk to the Synoptic service
     */
    private final ISynopticServiceAsync synopticService = GWT
            .create(ISynopticService.class);

    TabPanel tabPanel = new TabPanel();

    // Inputs tab widgets:
    private final VerticalPanel inputsPanel = new VerticalPanel();
    private final Label parseErrorMsgLabel = new Label();
    private final TextArea logTextArea = new TextArea();
    private final TextArea regExpsTextArea = new TextArea();
    private final TextBox partitionRegExpTextBox = new TextBox();
    private final TextBox separatorRegExpTextBox = new TextBox();
    private final Button parseLogButton = new Button("Parse Log");

    // Invariants tab widgets:
    private final VerticalPanel invariantsPanel = new VerticalPanel();
    private final HorizontalPanel invariantsButtonPanel = new HorizontalPanel();

    // List of hash codes to be removed from the server's set of invariants.
    // Each hash code represents a temporal invariant.
    private final Set<Integer> invHashesForRemoval = new HashSet<Integer>();
    private final Button invRemoveButton = new Button("Remove Invariants");

    // Model tab widgets:
    private final DockPanel modelPanel = new DockPanel();
    private final Button modelRefineButton = new Button("Refine");
    private final Button modelCoarsenButton = new Button("Coarsen");
    private final Button modelGetFinalButton = new Button("Final Model");
    private final Button modelExportDownloadButton= new Button("Export/Download");
    private FlowPanel graphPanel;
    private FlexTable logLineTable;

    // //////////////////////////////////////////////////////////////////////////
    // JSNI methods -- JavaScript Native Interface methods. The method body of
    // this calls is pure JavaScript.

    /**
     * A JSNI method to create and display an invariants graphic.
     *
     * @param AFby
     *            associative array with AFby relations
     * @param NFby
     *            associative array with NFby relations
     * @param AP
     *            associative array with AP relations
     * @param eTypes
     *            array of all event types
     * @param width
     *            width of graphic
     * @param height
     *            height of graphic
     * @param lX
     *            the x value of the left most column
     * @param mX
     *            the x value of the middle column
     * @param rX
     *            the x value of the right most column
     * @param canvasId
     *            the div id where to draw the graphic
     */
    public static native void createInvariantsGraphic(JavaScriptObject AFby,
            JavaScriptObject NFby, JavaScriptObject AP,
            JavaScriptObject eTypes, int width, int height, int lX, int mX,
            int rX, String canvasId) /*-{

		var paper = $wnd.Raphael($doc.getElementById(canvasId), width, height);

		// Attribute to track the target node pointed to from the middle text-element.
		paper.customAttributes.dest = function(textElem) {
			return {
				dest : textElem
			};
		};

		// Attribute to track the highlighted color of the lines connected to the selected middle text-element.
		paper.customAttributes.highlight = function(color) {
			return {
				highlight : color
			};
		};

		var topMargin = 20;
		var dY = 50;

		var lines = new Array();

		// These will contain text labels in the middle/right/left columns:
		var tMiddlesArr = [];
		var tRightsArr = [];
		var tLeftsArr = [];

		var ypos = new Array();

		// Create the three columns of text labels.
		for ( var i = 0; i < eTypes.length; i++) {
			var eType = eTypes[i]

			var tMiddle = paper.text(mX, dY * i + topMargin, eType);
			tMiddlesArr.push(tMiddle);
			tMiddle.attr({
				'font-size' : "30px",
				fill : "grey"
			});

			// Remember the y position of every row of labels.
			ypos[eType] = dY * i + 10;

			// Do not create the INITIAL labels on the left/right
			if (eType == "I.INITIAL") {
				continue;
			}

			var tLeft = paper.text(lX, dY * i + topMargin, eType);
			tLeft.attr({
				'font-size' : "30px",
				fill : "grey"
			});
			tLeftsArr[eType] = tLeft;

			var tRight = paper.text(rX, dY * i + topMargin, eType);
			tRight.attr({
				'font-size' : "30px",
				fill : "grey"
			});
			tRightsArr[eType] = tRight;
		}

		// Create all the lines by iterating through labels in the middle column.
		for ( var i = 0; i < eTypes.length; i++) {
			var eType = eTypes[i]
			lines[eType] = []
		}

		for ( var i = 0; i < eTypes.length; i++) {
			var eType = eTypes[i]
			var tMiddle = tMiddlesArr[i];

			// AP:
			for ( var j in AP[eType]) {
				var line = paper.path(("M" + mX + " " + ypos[AP[eType][j]]
						+ "L" + lX + " " + ypos[eType]));
				line.attr({
					stroke : "grey",
					highlight : "blue",
					dest : tLeftsArr[eType]
				});
				// NOTE: we associate the middle label destination of the arrow, not the left label source.
				lines[AP[eType][j]].push(line);
			}

			// AFby:
			for ( var j in AFby[eType]) {
				var line = paper.path(("M" + mX + " " + ypos[eType] + "L" + rX
						+ " " + ypos[AFby[eType][j]]));
				line.attr({
					stroke : "grey",
					highlight : "blue",
					dest : tRightsArr[AFby[eType][j]]
				});
				lines[eType].push(line);
			}

			// NFby:
			for ( var j in NFby[eType]) {
				var line = paper.path(("M" + mX + " " + ypos[eType] + "L" + rX
						+ " " + ypos[NFby[eType][j]]));
				line.attr({
					stroke : "grey",
					highlight : "red",
					dest : tRightsArr[NFby[eType][j]]
				});
				lines[eType].push(line);
			}

			// Function to execute when the tMiddle label is pointed-to.
			tMiddle.mouseover(function(y) {
				return function(e) {
					// y is tMiddle
					for ( var line in lines[y.attr('text')]) {
						lines[y.attr('text')][line].attr({
							'stroke-width' : '3'
						});
						lines[y.attr('text')][line].attr({
							stroke : lines[y.attr('text')][line]
									.attr('highlight')
						});
						lines[y.attr('text')][line].attr('dest').attr({
							fill : "black"
						});
					}
					y.attr({
						fill : "black"
					});

				};
			}(tMiddle));

			// Function to execute when the tMiddle label is not pointed-to.
			tMiddle.mouseout(function(y) {
				return function(e) {
					for ( var line in lines[y.attr('text')]) {
						lines[y.attr('text')][line].attr({
							'stroke-width' : '1'
						});
						lines[y.attr('text')][line].attr({
							stroke : "grey"
						});
						lines[y.attr('text')][line].attr('dest').attr({
							fill : "grey"
						});
					}
					y.attr({
						fill : "grey"
					});
				};
			}(tMiddle));
		}
    }-*/;

    /**
     * A JSNI method to create and display a graph.
     *
     * @param nodes
     *            An array of nodes, each consecutive pair is a <id,label>
     * @param edges
     *            An array of edges, each consecutive pair is <node id, node id>
     * @param width
     *            width of the graph
     * @param height
     *            height of the graph
     * @param canvasId
     *            the div id with which to associate the resulting graph
     */
    public native void createGraph(JavaScriptObject nodes,
            JavaScriptObject edges, int width, int height, String canvasId,
            String initial, String terminal) /*-{

		// required to export this instance
		var _this = this;

		// export the LogLineRequestHandler globally
		$wnd.viewLogLines = function(id) {
			_this.@synopticgwt.client.SynopticGWT::LogLineRequestHandler(I)(id);
		};

		// create the graph
		var g = new $wnd.Graph();
		g.edgeFactory.template.style.directed = true;

		// add each node to graph
		for ( var i = 0; i < nodes.length; i += 2) {
			g.addNode(nodes[i], {
				label : nodes[i + 1],
				render : $wnd.GRAPH_HANDLER.render
			});
		}

		// add each edge to graph
		for ( var i = 0; i < edges.length; i += 3) {
			//first is source, second is target, third is weight for the label
			g.addEdge(edges[i], edges[i + 1], {label : edges[i + 2]});
		}

		// give stable layout to graph elements
		var layouter = new $wnd.Graph.Layout.Stable(g, initial, terminal);

		// render the graph
		var renderer = new $wnd.Graph.Renderer.Raphael(canvasId, g, width,
				height);

		// store graph state
		$wnd.GRAPH_HANDLER.initializeStableIDs(nodes, edges, renderer,
				layouter, g);
    }-*/;

    /**
     * A JSNI method to update and display a refined graph, animating the
     * transition to a new layout.
     *
     * @param nodes
     *            An array of nodes, each consecutive pair is a <id,label>
     * @param edges
     *            An array of edges, each consecutive pair is <node id, node id>
     * @param refinedNode
     *            the ID of the refined node
     * @param canvasId
     *            the div id with which to associate the resulting graph
     */
    public static native void createChangingGraph(JavaScriptObject nodes,
            JavaScriptObject edges, int refinedNode, String canvasId) /*-{

		// update graph and fetch array of new nodes
		var newNodes = $wnd.GRAPH_HANDLER.updateRefinedGraph(nodes, edges,
				refinedNode);

		// fetch the current layouter
		var layouter = $wnd.GRAPH_HANDLER.getLayouter();

		// update each graph element's position, re-assigning a position
		layouter.updateLayout($wnd.GRAPH_HANDLER.getGraph(), newNodes);

		// fetch the renderer
		var renderer = $wnd.GRAPH_HANDLER.getRenderer();

		// re-draw the graph, animating transitions from old to new position
		renderer.draw();
    }-*/;

    /**
     * A JSNI method for adding a String element to a java script array object.
     * (Yes, this is rather painful.)
     *
     * @param array
     *            Array object to add to
     * @param s
     *            element to add
     */
    private native static void pushArray(JavaScriptObject array, String s) /*-{
		array.push(s);
    }-*/;

    /**
     * A JSNI method for associating a key in an array to a value. (Yes, this is
     * rather painful.)
     *
     * @param array
     *            Array object to add to
     * @param key
     * @param val
     */
    private native static void addToKeyInArray(JavaScriptObject array,
            String key, String val) /*-{
		if (!(key in array)) {
			array[key] = [];
		}
		array[key].push(val);
    }-*/;

    /**
     * A JSNI method for adding a progress wheel to a div. (Yes, this is rather
     * painful.)
     *
     * @param radius
     *            size of the svg graphic / 2
     * @param r1
     *            (smaller) inner radius of wheel
     * @param r2
     *            (larger) outter radius of wheel
     */
    private native static void addProgressWheel(String divHolder, int radius,
            int r1, int r2) /*-{
		var r = $wnd.Raphael($doc.getElementById(divHolder), radius * 2,
				radius * 2);
		var sectorsCount = 12;
		var color = "#000";
		var width = 1;
		var cx = radius;
		var cy = radius;
		var sectors = [], opacity = [];
		var beta = 2 * $wnd.Math.PI / sectorsCount,

		pathParams = {
			stroke : color,
			"stroke-width" : width,
			"stroke-linecap" : "round"
		};

		for ( var i = 0; i < sectorsCount; i++) {
			var alpha = (beta * i);
			var cos = $wnd.Math.cos(alpha);
			var sin = $wnd.Math.sin(alpha);
			opacity[i] = 1 / sectorsCount * i;

			sectors[i] = r.path("M" + (cx + r1 * cos) + " " + (cy + r1 * sin)
					+ "L" + (cx + r2 * cos) + " " + (cy + r2 * sin));
			sectors[i].attr(pathParams);
		}
		(function ticker() {
			opacity.unshift(opacity.pop());
			for ( var i = 0; i < sectorsCount; i++) {
				sectors[i].attr("opacity", opacity[i]);
			}
			$wnd.setTimeout(ticker, 1000 / sectorsCount);
		})();
    }-*/;

    // </JSNI methods>
    // //////////////////////////////////////////////////////////////////////////

    /**
     * Shows the GWTGraph object on the screen in the modelPanel
     *
     * @param graph
     * @throws Exception
     */
    public void showGraph(GWTGraph graph) {
        // Clear the second (non-button ) widget model
        // panel
        if (modelPanel.getWidgetCount() > 1) {
            modelPanel.remove(modelPanel.getWidget(1));
            assert (modelPanel.getWidgetCount() == 1);
        }

        // clear the log line table
        clearLogTable();

        String canvasId = "canvasId";

        graphPanel = new FlowPanel();
        graphPanel.getElement().setId(canvasId);
        graphPanel.setStylePrimaryName("modelCanvas");
        // modelPanel.addEast(graphPanel, 70);
        modelPanel.add(graphPanel, DockPanel.CENTER);
        // Create the list of graph node labels and their Ids.
        HashMap<Integer, String> nodes = graph.getNodes();
        JavaScriptObject jsNodes = JavaScriptObject.createArray();
        for (Integer key : nodes.keySet()) {
            pushArray(jsNodes, key.toString());
            pushArray(jsNodes, nodes.get(key));
        }

        // Create the list of edges, where two consecutive node Ids is an edge.
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        List<GWTTriplet<Integer, Integer, Double>> edges = graph.getEdges();
        for (GWTTriplet<Integer, Integer, Double> edge : edges) {
            pushArray(jsEdges, edge.getLeft().toString());
            pushArray(jsEdges, edge.getMiddle().toString());

            //This contains the edge's weight
            pushArray(jsEdges, edge.getRight().toString());
        }

        // Determine the size of the graphic -- make it depend on the current
        // window size.
        // TODO: make sizing more robust, and allow users to resize the graphic
        int width = Math.max(Window.getClientWidth() - 600, 300);
        int height = Math.max(Window.getClientHeight() - 300, 300);
        graphPanel.setPixelSize(width, height);
        createGraph(jsNodes, jsEdges, width, height, canvasId, INITIAL_LABEL,
                TERMINAL_LABEL);
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
            pushArray(jsNodes, key.toString());
            pushArray(jsNodes, nodes.get(key));
        }

        // Create the list of edges, where two consecutive node Ids is an edge.
        // JavaScriptObject newEdges = JavaScriptObject.createArray();
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        List<GWTTriplet<Integer, Integer, Double>> edges = graph.getEdges();
        for (GWTTriplet<Integer, Integer, Double> edge : edges) {
            pushArray(jsEdges, edge.getLeft().toString());
            pushArray(jsEdges, edge.getMiddle().toString());
        }

        // Determine the size of the graphic -- make it depend on the current
        // window size.
        // TODO: make sizing more robust, and allow users to resize the graphic
        int width = Math.max(Window.getClientWidth() - 600, 300);
        int height = Math.max(Window.getClientHeight() - 300, 300);
        graphPanel.setPixelSize(width, height);

        createChangingGraph(jsNodes, jsEdges, refinedNode, canvasId);
    }

    /**
     * Shows the invariant graphic on the screen in the invariantsPanel
     *
     * @param graph
     */
    public void showInvariants(GWTInvariantSet gwtInvs) {
        // Clear the invariants panel of the non-button widget
    	// (the horizontal panel for the table and graphics).
    	if (invariantsPanel.getWidgetCount() > 1) {
    		invariantsPanel.remove(invariantsPanel.getWidget(1));
            assert (invariantsPanel.getWidgetCount() == 1);
        }

        // Create and populate the panel with the invariants table and the
        // invariants graphic.
        HorizontalPanel tableAndGraphicPanel = new HorizontalPanel();
        invariantsPanel.add(tableAndGraphicPanel);

        Set<String> invTypes = gwtInvs.getInvTypes();
        int eTypesCnt = 0;
        JavaScriptObject eventTypesJS = JavaScriptObject.createArray();
        JavaScriptObject AFbyJS = JavaScriptObject.createArray();
        JavaScriptObject NFbyJS = JavaScriptObject.createArray();
        JavaScriptObject APJS = JavaScriptObject.createArray();
        Set<String> eventTypes = new LinkedHashSet<String>();
        int longestEType = 0;

        // Iterate through all invariants to (1) add them to the grid / table,
        // and (2) to create the JS objects for drawing the invariants graphic.
        for (String invType : invTypes) {
            final List<GWTInvariant<String, String>> invs = gwtInvs.getInvs(invType);

            final Grid grid = new Grid(invs.size() + 1, 1);
            tableAndGraphicPanel.add(grid);

            grid.setWidget(0, 0, new Label(invType));
            grid.getCellFormatter().setStyleName(0, 0, "topTableCell");

            int i = 1;
            for (GWTInvariant<String, String> inv : invs) {
                if (!eventTypes.contains(inv.getSource())) {
                    pushArray(eventTypesJS, inv.getSource());
                    eventTypes.add(inv.getSource());
                    if (inv.getSource().length() > longestEType) {
                        longestEType = inv.getSource().length();
                    }
                    eTypesCnt++;
                }
                if (!eventTypes.contains(inv.getTarget())) {
                    pushArray(eventTypesJS, inv.getTarget());
                    eventTypes.add(inv.getTarget());
                    if (inv.getTarget().length() > longestEType) {
                        longestEType = inv.getTarget().length();
                    }
                    eTypesCnt++;
                }

                String x = inv.getSource();
                String y = inv.getTarget();
                if (invType.equals("AFby")) {
                    addToKeyInArray(AFbyJS, x, y);
                } else if (invType.equals("NFby")) {
                    addToKeyInArray(NFbyJS, x, y);
                } else if (invType.equals("AP")) {
                    addToKeyInArray(APJS, x, y);
                }

                grid.setWidget(i, 0,
                        new Label(inv.getSource() + ", " + inv.getTarget()));
                i += 1;
            }

            grid.setStyleName("invariantsGrid grid");
            for (i = 1; i < grid.getRowCount(); i++) {
                grid.getCellFormatter().setStyleName(i, 0, "tableButtonCell");
            }

            // Allow the user to toggle invariants on the grid.
            addInvariantToggleHandler(grid, invs);

        }

        // Show the invariant graphic.
        String invCanvasId = "invCanvasId";
        HorizontalPanel invGraphicId = new HorizontalPanel();
        invGraphicId.getElement().setId(invCanvasId);
        invGraphicId.setStylePrimaryName("modelCanvas");
        tableAndGraphicPanel.add(invGraphicId);

        // A little magic to size things right.
        int lX = (longestEType * 30) / 2 - 60;
        int mX = lX + (longestEType * 30);
        int rX = mX + (longestEType * 30);
        int width = rX + 50;

        createInvariantsGraphic(AFbyJS, NFbyJS, APJS, eventTypesJS, width,
                (eTypesCnt + 1) * 50, lX, mX, rX, invCanvasId);
    }

    /**
     * Requests the log lines for the Partition with the given nodeID
     */
    public void LogLineRequestHandler(int nodeID) throws Exception {
        synopticService
                .handleLogRequest(nodeID, new ViewLogLineAsyncCallback());
    }

    /* removes currently displayed log lines from the log line table */
    private void clearLogTable() {
        for (int i = 1; i < logLineTable.getRowCount(); i++) {
            logLineTable.removeRow(i);
        }
    }

    /**
     * Accesses the list of copies of server-side invariant hash codes and
     * uses them to remove their corresponding server-side invariants.
     * The client-side graph and invariants are then recalculated and redisplayed.
     */
    class RemoveInvariantsHandler implements ClickHandler {
    	@Override
    	public void onClick(ClickEvent event) {

    		// Keep the user from clicking the button multiple times.
    		invRemoveButton.setEnabled(false);

    		// Remove the invariants from the server based on
    		// the hash code copies, then recalculate the graph
    		// and invariants so they can be redrawn in their respective
    		// panels.
    		try {
    			synopticService.removeInvs(invHashesForRemoval,
    					new RemoveInvariantsAsyncCallback());
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		// Since the invariants have been removed, the queue should be emptied.
    		invHashesForRemoval.clear();
    	}
    }

    /**
     * Callback method for removing user-specified invariants.
     * Redraws the content in the model and invariants tab.
     */
    class RemoveInvariantsAsyncCallback
    	implements AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> {

    	/**
    	 * Handles any general problems that may arise.
    	 */
    	@Override
    	public void onFailure(Throwable caught) {
            injectRPCError("Remote Procedure Call Failure while removing invariants");
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
    	}

    	/**
    	 * Redraws the model and invariants tabs.
    	 */
    	@Override
    	public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> result) {

            // Reset appropriate buttons.
            parseLogButton.setEnabled(true);
            tabPanel.selectTab(2);
            modelRefineButton.setEnabled(true);
            modelCoarsenButton.setEnabled(false);
            modelGetFinalButton.setEnabled(true);
            modelExportDownloadButton.setEnabled(true);

            // Show the model graph.
            GWTGraph graph = result.getRight();
            showGraph(graph);

            // Show the invariants table and graphics.
            GWTInvariantSet gwtInvs = result.getLeft();
            showInvariants(gwtInvs);

            // An error occurs when the tabPanel stays
            // in something other than the model tab when the graph is drawn,
            // so the tab is switched to the graph for drawing, and then back to
            // the invariants tab for now.
            tabPanel.selectTab(1);
    	}
    }

    /**
     * Displays the returned log lines from a LogLineRequest
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
     * Used for handling Parse Log button clicks
     */
    class ParseLogHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            // Disallow the user from making concurrent Parse Log calls
            parseLogButton.setEnabled(false);

            // Reset the parse error msg
            parseErrorMsgLabel.setText("");

            // Extract arguments for parseLog call
            String logLines = logTextArea.getText();
            String regExpLines[] = regExpsTextArea.getText().split("\\r?\\n");
            List<String> regExps = Arrays.asList(regExpLines);

            String partitionRegExp = partitionRegExpTextBox.getText();
            if (partitionRegExp == "") {
                partitionRegExp = null;
            }
            String separatorRegExp = separatorRegExpTextBox.getText();
            if (separatorRegExp == "") {
                separatorRegExp = null;
            }

            // TODO: validate the args to parseLog()
            synopticService.parseLog(logLines, regExps, partitionRegExp,
                    separatorRegExp, new ParseLogAsyncCallback());
        }
    }

    /**
     * onSuccess\onFailure callback handler for parseLog()
     */
    class ParseLogAsyncCallback implements
            AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> {
        @Override
        public void onFailure(Throwable caught) {
            injectRPCError("Remote Procedure Call Failure while parsing log");
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
            parseLogButton.setEnabled(true);
        }

        @Override
        public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> result) {
            // Create new tabs.
            tabPanel.add(invariantsPanel, "Invariants");
            tabPanel.add(modelPanel, "Model");

            // Create buttons on the Model tab.
            parseLogButton.setEnabled(true);
            tabPanel.selectTab(2);
            modelRefineButton.setEnabled(true);
            modelCoarsenButton.setEnabled(false);
            modelGetFinalButton.setEnabled(true);
            modelExportDownloadButton.setEnabled(true);

            // Show the model graph.
            GWTGraph graph = result.getRight();
            showGraph(graph);

            // Show the invariants table and graphics.
            GWTInvariantSet gwtInvs = result.getLeft();
            showInvariants(gwtInvs);

        }
    }

    /**
     * Used for handling Refine button clicks
     */
    class RefineModelHandler implements ClickHandler {
        /**
         * Fired when the user clicks on the Refine.
         */
        @Override
        public void onClick(ClickEvent event) {
            // addProgressWheel("progressDiv", 10, 2, 7);
            modelRefineButton.setEnabled(false);
            modelExportDownloadButton.setEnabled(true);
            try {
                synopticService.refineOneStep(new RefineOneStepAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * onSuccess\onFailure callback handler for refineOneStep()
     **/
    class RefineOneStepAsyncCallback implements AsyncCallback<GWTGraphDelta> {
        @Override
        public void onFailure(Throwable caught) {
            injectRPCError("Remote Procedure Call Failure while refining");
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
            modelRefineButton.setEnabled(true);
        }

        @Override
        public void onSuccess(GWTGraphDelta graph) {
            if (graph == null) {
                modelCoarsenButton.setEnabled(true);
                return;
            }
            modelRefineButton.setEnabled(true);
            tabPanel.selectTab(2);
            showChangingGraph(graph.getGraph(), graph.getRefinedNode());

        }
    }

    /**
     * Used for handling coarsen button clicks
     */
    class CoarsenModelHandler implements ClickHandler {
        /**
         * Fired when the user clicks on the coarsen.
         */
        @Override
        public void onClick(ClickEvent event) {
            // Coarsening is a one-shot step at the moment.
            modelCoarsenButton.setEnabled(false);
            modelExportDownloadButton.setEnabled(true);
            try {
                synopticService
                        .coarsenOneStep(new CoarsenOneStepAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * onSuccess\onFailure callback handler for coarsenOneStep()
     **/
    class CoarsenOneStepAsyncCallback implements AsyncCallback<GWTGraph> {
        @Override
        public void onFailure(Throwable caught) {
            injectRPCError("Remote Procedure Call Failure while coarsening");
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
        }

        @Override
        public void onSuccess(GWTGraph graph) {
            tabPanel.selectTab(2);
            showGraph(graph);
        }
    }

    /**
     * Used for handling Final Model button clicks
     */
    class GetFinalModelHandler implements ClickHandler {
        /**
         * Fired when the user clicks on the button.
         */
        @Override
        public void onClick(ClickEvent event) {
            modelRefineButton.setEnabled(false);
            modelCoarsenButton.setEnabled(false);
            modelGetFinalButton.setEnabled(false);
            modelExportDownloadButton.setEnabled(true);

            try {
                synopticService.getFinalModel(new GetFinalModelAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    /**
	 * This makes the grid clickable, so that when clicked,
	 * the grid's cell data will be looked up in the client-side set of invariants.
	 * This client-side invariant then contains the server-side hashcode for the
	 * corresponding server-side invariant.  This hash code is then queued up so that
	 * each server-side hash code specifies a server-side invariant for removal.
	 *
	 * When a cell is "active," this means that it's corresponding
	 * invariant is queued up to be removed at the click of the removal button.  When one
	 * or more cells are active, then the removal button will also be activated.
	 *
	 * When a cell is deactivated, the corresponding invariant is removed from the queue.
	 * If all cells are not active, the removal button will also be deactivated.
	 *
	 * @param invs
	 * 	The set of client-side invariants
	 * @param grid
	 * 	The grid which will become clickable.
	 */
    private void addInvariantToggleHandler
    	(final Grid grid, final List<GWTInvariant<String, String>> invs) {

    	// Add the basic click handler to the graph.
    	grid.addClickHandler(new ClickHandler() {

    		// Add the aforementioned functionality to the click handler.
    		@Override
    		public void onClick(ClickEvent event) {

    			// Specify which cell was clicked.
        		HTMLTable.Cell cell = ((Grid)event.getSource()).getCellForEvent(event);

        		// Check to see (from the row index), whether the cell clicked
        		// is the top (zeroth) cell.  This shouldn't be activated, as it is the
        		// column title.
        		int cellRowIndex = cell.getRowIndex();
        		if (cellRowIndex > 0) {
        			// Extract the cell data from the grid's cell.
        			// TODO: This is likely an ineffective way of doing this,
        			// as the invariants on the left and right may not be separated by a
        			// comma.
        			String[] cellData = cell.getElement().getInnerText()
        				.split(", ", 2);

        			// Create an invariant to be looked up in the client-side list.
        			GWTInvariant<String, String> pairFromCell =
        					new GWTInvariant<String, String>(cellData[0],
        												cellData[1], "");
        			int matchingIndex = invs.indexOf(pairFromCell);

        			// Extract a copy of the server-side's invariant hash code.
        			int serverHash = invs.get(matchingIndex).hashCode();

        			// Check whether the cell is active (style of "tableButtonCell")
        			// or not (style of "tableCellSelected").
        			CellFormatter formatter = grid.getCellFormatter();
        			if (formatter.getStyleName(cellRowIndex, 0)
        					.equals("tableButtonCell")) {

        				// Activate the cell and queue up the hash code.
        				formatter.setStyleName(cellRowIndex, 0, "tableCellSelected");
        				invHashesForRemoval.add(serverHash);

        				// Activate the removal button
        				invRemoveButton.setEnabled(true);
        			} else {

        				// Deactivate the cell and remove the hash code from the queue.
        				formatter.setStyleName(cellRowIndex, 0, "tableButtonCell");
        				invHashesForRemoval.remove(serverHash);

        				// Deactivate the removal button if there are no invariants
        				// queued up.
        				if (invHashesForRemoval.isEmpty()) {
            				invRemoveButton.setEnabled(false);
            			}
        			}
        		}
        	}
    	});
    }

    /**
     * onSuccess\onFailure callback handler for coarsenOneStep()
     **/
    class GetFinalModelAsyncCallback implements AsyncCallback<GWTGraph> {
        @Override
        public void onFailure(Throwable caught) {
            injectRPCError("Remote Procedure Call Failure while fetching final model");
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
        }

        @Override
        public void onSuccess(GWTGraph graph) {
            tabPanel.selectTab(2);
            showGraph(graph);
        }
    }

    /**
     * Used for handling Export/Download button clicks
     * @author i3az0kimchi
     *
     */
    class ExportDownloadModelHandler implements ClickHandler {
    	@Override
    	public void onClick(ClickEvent event) {
    		try {
    			synopticService.exportModel(new ExportDownloadModelAsyncCallback());
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    }

    /**
     * onSuccess/onFailure callback handler for exportModel()
     * @author i3az0kimchi
     * Opens new windows for the .dot and .png file of the current model
     * if the operation was a success
     */
    class ExportDownloadModelAsyncCallback implements AsyncCallback<String> {
    	@Override
    	public void onFailure(Throwable caught) {
    		injectRPCError("Remote Procedure Call Failure while exporting current model");
    		parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
    	}

    	@Override
    	public void onSuccess(String filename) {
    		modelExportDownloadButton.setEnabled(false);
    		Window.open("../" + filename, "DOT file", "");
    		Window.open("../" + filename + ".png", "PNG file", "");
    	}
    }


    /**
     * Entry point method.
     */
    @Override
    public void onModuleLoad() {
        // Build the page layout.
        RootPanel.get("mainDiv").add(tabPanel);

        tabPanel.setWidth("100%");
        tabPanel.add(inputsPanel, "Inputs");
        tabPanel.selectTab(0);

        // Construct the inputs panel using a grid.
        inputsPanel.add(parseErrorMsgLabel);

        Grid grid = new Grid(5, 2);
        inputsPanel.add(grid);

        grid.setWidget(0, 0, new Label("Log lines"));
        grid.setWidget(0, 1, logTextArea);
        logTextArea.setCharacterWidth(80);
        logTextArea.setVisibleLines(10);

        grid.setWidget(1, 0, new Label("Regular expressions"));
        grid.setWidget(1, 1, regExpsTextArea);
        regExpsTextArea.setCharacterWidth(80);

        grid.setWidget(2, 0, new Label("Partition expression"));
        grid.setWidget(2, 1, partitionRegExpTextBox);
        partitionRegExpTextBox.setVisibleLength(80);

        grid.setWidget(3, 0, new Label("Separator expression"));
        grid.setWidget(3, 1, separatorRegExpTextBox);
        separatorRegExpTextBox.setVisibleLength(80);

        grid.setWidget(4, 1, parseLogButton);

        grid.setStyleName("inputForm grid");
        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getCellCount(i); j++) {
                grid.getCellFormatter().setStyleName(i, j, "tableCell");
            }
        }

        // Set up the error label's style\visibility
        parseErrorMsgLabel.setStyleName("serverResponseLabelError");
        parseErrorMsgLabel.setVisible(false);

        // Set up the logTextArea
        logTextArea.setFocus(true);
        logTextArea
                .setText("1 0 c\n2 0 b\n3 0 a\n4 0 d\n1 1 f\n2 1 b\n3 1 a\n4 1 e\n1 2 f\n2 2 b\n3 2 a\n4 2 d");
        logTextArea.selectAll();

        // Set up the test areas
        regExpsTextArea.setText("^(?<TIME>)(?<nodename>)(?<TYPE>)$");
        partitionRegExpTextBox.setText("\\k<nodename>");

        // Nothing to be done for now for the invariants panel.

        // Construct the Model panel.

        VerticalPanel controlsPanel = new VerticalPanel();
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.add(modelRefineButton);
        buttonsPanel.add(modelCoarsenButton);
        buttonsPanel.add(modelGetFinalButton);
        buttonsPanel.add(modelExportDownloadButton);
        modelRefineButton.setWidth("100px");
        modelCoarsenButton.setWidth("100px");
        modelGetFinalButton.setWidth("100px");
        modelExportDownloadButton.setWidth("110px");
        buttonsPanel.setStyleName("buttonPanel");
        controlsPanel.add(buttonsPanel);

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
        modelPanel.add(controlsPanel, DockPanel.WEST);

        // Coarsening is disabled until refinement is completed.
        modelCoarsenButton.setEnabled(false);
        modelRefineButton.addClickHandler(new RefineModelHandler());
        modelCoarsenButton.addClickHandler(new CoarsenModelHandler());
        modelGetFinalButton.addClickHandler(new GetFinalModelHandler());
        modelExportDownloadButton.addClickHandler(new ExportDownloadModelHandler());

        // Set up invariants tab.
        invariantsPanel.add(invariantsButtonPanel);
        invariantsButtonPanel.add(invRemoveButton);
        invariantsButtonPanel.setStyleName("buttonPanel");
        invRemoveButton.addClickHandler(new RemoveInvariantsHandler());
        invRemoveButton.setWidth("188px");
        invRemoveButton.setEnabled(false);

        // Associate handler with the Parse Log button
        parseLogButton.addClickHandler(new ParseLogHandler());
    }

    /* Injects an error message at the top of the page when an RPC call fails */
    public void injectRPCError(String message) {
        Label error = new Label(message);
        error.setStyleName("ErrorMessage");
        RootPanel.get("progressDiv").add(error);
    }
}
