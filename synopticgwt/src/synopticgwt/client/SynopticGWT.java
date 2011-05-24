package synopticgwt.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariants;
import synopticgwt.shared.GWTPair;

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
    private final HorizontalPanel invariantsPanel = new HorizontalPanel();

    // Model tab widgets:
    private final VerticalPanel modelPanel = new VerticalPanel();
    private final Button modelRefineButton = new Button("Refine");
    private final Button modelCoarsenButton = new Button("Coarsen");
    private final Button modelGetFinalButton = new Button("Final Model");

    // Most recent GWTGraph for use in animation

    private FlowPanel graphPanel;

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

		var tMiddlesArr = [];
		var tRightsArr = [];
		var tLeftsArr = [];

		var ypos = new Array();

		// Create the three columns of text labels.
		for ( var i = 0; i < eTypes.length; i++) {
			var eType = eTypes[i]

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

			var tMiddle = paper.text(mX, dY * i + topMargin, eType);
			tMiddlesArr.push(tMiddle);
			tMiddle.attr({
				'font-size' : "30px",
				fill : "grey"
			});

			// Remember the y position of every row of labels.
			ypos[eType] = dY * i + 10;
		}

		// Create all the lines by iterating through labels in the middle column.
		for ( var i = 0; i < eTypes.length; i++) {
			var eType = eTypes[i]
			var tMiddle = tMiddlesArr[i];
			lines[eType] = []

			// AP:
			for ( var j in AP[eType]) {
				var line = paper.path(("M" + mX + " " + ypos[eType] + "L" + lX
						+ " " + ypos[AP[eType][j]]));
				line.attr({
					stroke : "grey",
					highlight : "green",
					dest : tLeftsArr[AP[eType][j]]
				});
				lines[eType].push(line);
			}

			// AFby:
			for ( var j in AFby[eType]) {
				var line = paper.path(("M" + mX + " " + ypos[eType] + "L" + rX
						+ " " + ypos[AFby[eType][j]]));
				line.attr({
					stroke : "grey",
					highlight : "green",
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
            JavaScriptObject edges, int width, int height, String canvasId) /*-{
		var g = new $wnd.Graph();
		g.edgeFactory.template.style.directed = true;

		for ( var i = 0; i < nodes.length; i += 2) {
			g.addNode(nodes[i], {
				label : nodes[i + 1],
				render : $wnd.CUSTOM.render
			});
		}

		for ( var i = 0; i < edges.length; i += 2) {
			g.addEdge(edges[i], edges[i + 1]);
		}

		var layouter = new $wnd.Graph.Layout.Stable(g, "I.INITIAL", "T.TERMINAL");
		var renderer = new $wnd.Graph.Renderer.Raphael(canvasId, g, width,
				height);
		var _this = this;
		$wnd.CUSTOM.initializeStableIDs(nodes, edges, renderer, layouter, g, _this);
    }-*/;
    
    /**
     * Should enable export the LogLineRequestHandler globally
     */
  //  public native void exportGWTMethod() /*-{
  //  	var _this = this;
  //	$wnd.viewLogLines = $entry(_this.@synopticgwt.client.SynopticGWT::LogLineRequestHandler(I));
  //}-*/;


    /**
     * Updates the current graph to display the new edges, animates transition to new graph layout
     */
    public static native void createChangingGraph(JavaScriptObject allNodes, JavaScriptObject allEdges,
            int refinedNode, int width, int height, String canvasId) /*-{
		var newNodes = $wnd.CUSTOM.updateGraph(allNodes, allEdges, refinedNode);

		var layouter = $wnd.CUSTOM.getLayouter();
		layouter.updateLayout($wnd.CUSTOM.getGraph(), newNodes);

		var renderer = $wnd.CUSTOM.getRenderer();
		renderer.draw();
    }-*/;
    
    /**
     * Displays the given array of log lines
     */
    public static native void displayLogLines(JavaScriptObject lines) /*-{
    	var all = "Line#		Line				File";
		for(var i = 0; i < lines.length; i+= 3) {
			all += lines[i] + "		" + lines[i+1] + "				" + lines[i+2] + "\n";
		}
		alert(all);
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

        String canvasId = "canvasId";

        graphPanel = new FlowPanel();
        graphPanel.getElement().setId(canvasId);
        graphPanel.setStylePrimaryName("modelCanvas");
        modelPanel.add(graphPanel);
        // Create the list of graph node labels and their Ids.
        HashMap<Integer, String> nodes = graph.getNodes();
        JavaScriptObject jsNodes = JavaScriptObject.createArray();
        for (Integer key : nodes.keySet()) {
            pushArray(jsNodes, key.toString());
            pushArray(jsNodes, nodes.get(key));
        }

        // Create the list of edges, where two consecutive node Ids is an edge.
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        List<GWTPair<Integer, Integer>> edges = graph.getEdges();
        for (GWTPair<Integer, Integer> edge : edges) {
            pushArray(jsEdges, edge.getLeft().toString());
            pushArray(jsEdges, edge.getRight().toString());
        }

        // Determine the size of the graphic -- make it depend on the current
        // window size.
        // TODO: make sizing more robust, and allow users to resize the graphic
        int width = Math.max(Window.getClientWidth() - 300, 300);
        int height = Math.max(Window.getClientHeight() - 300, 300);
        graphPanel.setPixelSize(width, height);
        //exportGWTMethod();
        createGraph(jsNodes, jsEdges, width, height, canvasId);
    }

    public void showChangingGraph(GWTGraph graph, int refinedNode) {
        String canvasId = "canvasId";
        
        HashMap<Integer, String> nodes = graph.getNodes();
        JavaScriptObject jsNodes = JavaScriptObject.createArray();
        for (Integer key : nodes.keySet()) {
            pushArray(jsNodes, key.toString());
            pushArray(jsNodes, nodes.get(key));
        }

        // Create the list of edges, where two consecutive node Ids is an edge.
        //JavaScriptObject newEdges = JavaScriptObject.createArray();
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        List<GWTPair<Integer, Integer>> edges = graph.getEdges();
        for (GWTPair<Integer, Integer> edge : edges) {
            pushArray(jsEdges, edge.getLeft().toString());
            pushArray(jsEdges, edge.getRight().toString());
        }

        // Determine the size of the graphic -- make it depend on the current
        // window size.
        // TODO: make sizing more robust, and allow users to resize the graphic
        int width = Math.max(Window.getClientWidth() - 300, 300);
        int height = Math.max(Window.getClientHeight() - 300, 300);

        graphPanel.setPixelSize(width, height);        
        createChangingGraph(jsNodes, jsEdges, refinedNode, width, height, canvasId);
    }

    /**
     * Shows the invariant graphic on the screen in the invariantsPanel
     * 
     * @param graph
     */
    public void showInvariants(GWTInvariants gwtInvs) {
        // Clear the invariants panel if it has any
        // widgets (it only has one)
        while (invariantsPanel.getWidgetCount() != 0) {
            invariantsPanel.remove(invariantsPanel.getWidget(0));
        }

        // Create and populate the panel with the invariants table.
        HorizontalPanel hPanel = new HorizontalPanel();
        invariantsPanel.add(hPanel);

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
            List<GWTPair<String, String>> invs = gwtInvs.getInvs(invType);

            Grid grid = new Grid(invs.size() + 1, 1);
            hPanel.add(grid);

            grid.setWidget(0, 0, new Label(invType));
            grid.getCellFormatter().setStyleName(0, 0, "topTableCell");

            int i = 1;
            for (GWTPair<String, String> inv : invs) {
                if (!eventTypes.contains(inv.getLeft())) {
                    pushArray(eventTypesJS, inv.getLeft());
                    eventTypes.add(inv.getLeft());
                    if (inv.getLeft().length() > longestEType) {
                        longestEType = inv.getLeft().length();
                    }
                    eTypesCnt++;
                }
                if (!eventTypes.contains(inv.getRight())) {
                    pushArray(eventTypesJS, inv.getRight());
                    eventTypes.add(inv.getRight());
                    if (inv.getRight().length() > longestEType) {
                        longestEType = inv.getRight().length();
                    }
                    eTypesCnt++;
                }

                String x = inv.getLeft();
                String y = inv.getRight();
                if (invType.equals("AFby")) {
                    addToKeyInArray(AFbyJS, x, y);
                } else if (invType.equals("NFby")) {
                    addToKeyInArray(NFbyJS, x, y);
                } else if (invType.equals("AP")) {
                    addToKeyInArray(APJS, x, y);
                }

                grid.setWidget(i, 0,
                        new Label(inv.getLeft() + ", " + inv.getRight()));
                i += 1;
            }

            grid.setStyleName("invariantsGrid grid");
            for (i = 1; i < grid.getRowCount(); i++) {
                grid.getCellFormatter().setStyleName(i, 0, "tableCell");
            }
        }

        // Show the invariant graphic.
        String invCanvasId = "invCanvasId";
        HorizontalPanel invGraphicId = new HorizontalPanel();
        invGraphicId.getElement().setId(invCanvasId);
        invGraphicId.setStylePrimaryName("modelCanvas");
        invariantsPanel.add(invGraphicId);

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
     * 
     */
    public void LogLineRequestHandler (int nodeID) throws Exception {
    	synopticService.handleLogRequest(nodeID, new ViewLogLineAsyncCallback());
    }

    /**
     * Displays the returned log lines from a LogLineRequest
     */
    class ViewLogLineAsyncCallback implements AsyncCallback<List<String[]>> {

		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSuccess(List<String[]> result) {
			JavaScriptObject jsLogLines = JavaScriptObject.createArray();
			for (String[] line : result) {
				for (String piece : line)
					pushArray(jsLogLines, piece);
	        }
			displayLogLines(jsLogLines);
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
            AsyncCallback<GWTPair<GWTInvariants, GWTGraph>> {
        @Override
        public void onFailure(Throwable caught) {
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
            parseLogButton.setEnabled(true);
        }

        @Override
        public void onSuccess(GWTPair<GWTInvariants, GWTGraph> result) {
            // Create new tabs.
            tabPanel.add(invariantsPanel, "Invariants");
            tabPanel.add(modelPanel, "Model");

            // Create buttons on the Model tab.
            parseLogButton.setEnabled(true);
            tabPanel.selectTab(2);
            modelRefineButton.setEnabled(true);
            modelCoarsenButton.setEnabled(false);
            modelGetFinalButton.setEnabled(true);

            // Show the model graph.
            GWTGraph graph = result.getRight();
            showGraph(graph);
            
            // Show the invariants table and graphics.
            GWTInvariants gwtInvs = result.getLeft();
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

            try {
                synopticService.getFinalModel(new GetFinalModelAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * onSuccess\onFailure callback handler for coarsenOneStep()
     **/
    class GetFinalModelAsyncCallback implements AsyncCallback<GWTGraph> {
        @Override
        public void onFailure(Throwable caught) {
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
        }

        @Override
        public void onSuccess(GWTGraph graph) {
            tabPanel.selectTab(2);
            showGraph(graph);
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
        VerticalPanel buttonsPanel = new VerticalPanel();
        buttonsPanel.add(modelRefineButton);
        buttonsPanel.add(modelCoarsenButton);
        buttonsPanel.add(modelGetFinalButton);
        modelRefineButton.setWidth("100px");
        modelCoarsenButton.setWidth("100px");
        modelGetFinalButton.setWidth("100px");
        buttonsPanel.setStyleName("buttonPanel");
        modelPanel.add(buttonsPanel);
        // Coarsening is disabled until refinement is completed.
        modelCoarsenButton.setEnabled(false);
        modelRefineButton.addClickHandler(new RefineModelHandler());
        modelCoarsenButton.addClickHandler(new CoarsenModelHandler());
        modelGetFinalButton.addClickHandler(new GetFinalModelHandler());

        // Associate handler with the Parse Log button
        parseLogButton.addClickHandler(new ParseLogHandler());
    }
}
