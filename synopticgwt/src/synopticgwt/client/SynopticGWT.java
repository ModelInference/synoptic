package synopticgwt.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import synopticgwt.shared.GWTInvariants;
import synopticgwt.shared.GWTPair;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
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
    private final VerticalPanel invariantsPanel = new VerticalPanel();

    // Model tab widgets:
    private final VerticalPanel modelPanel = new VerticalPanel();
    private final Button modelRefineButton = new Button("Refine");

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
    public static native void createGraph(JavaScriptObject nodes,
            JavaScriptObject edges, int width, int height, String canvasId) /*-{

		var g = new $wnd.Graph();
		g.edgeFactory.template.style.directed = true;

		// First: Write a custom node render function.
		var render = function(r, n) {
			// the Raphael set is obligatory, containing all you want to display
			var set = r.set().push(
			// custom objects go here
			r.rect(n.point[0] - 30, n.point[1] - 13, 62, 86).attr({
				"fill" : "#fa8",
				"stroke-width" : 1,
				r : "9px"
			})).push(r.text(n.point[0], n.point[1] + 30, n.label).attr({
				"font-size" : "12px"
			}));
			return set;
		};

		for ( var i = 0; i < nodes.length; i += 2) {
			g.addNode(nodes[i], {
				label : nodes[i + 1],
				render : render
			});
		}

		for ( var i = 0; i < edges.length; i += 2) {
			g.addEdge(edges[i], edges[i + 1]);
		}

		var layouter = new $wnd.Graph.Layout.Ordered(g, $wnd
				.topological_sort(g));
		var renderer = new $wnd.Graph.Renderer.Raphael(canvasId, g, width,
				height);

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

    public void showGraph(GWTGraph graph) {
        // Clear the second (non-button ) widget model
        // panel
        if (modelPanel.getWidgetCount() > 1) {
            modelPanel.remove(modelPanel.getWidget(1));
            assert (modelPanel.getWidgetCount() == 1);
        }
        String canvasId = "canvas";

        FlowPanel f = new FlowPanel();
        f.getElement().setId(canvasId);
        modelPanel.add(f);

        HashMap<Integer, String> nodes = graph.getNodes();
        JavaScriptObject jsNodes = JavaScriptObject.createArray();
        for (Integer key : nodes.keySet()) {
            pushArray(jsNodes, key.toString());
            pushArray(jsNodes, nodes.get(key));
        }

        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        List<GWTPair<Integer, Integer>> edges = graph.getEdges();
        for (GWTPair<Integer, Integer> edge : edges) {
            pushArray(jsEdges, edge.getLeft().toString());
            pushArray(jsEdges, edge.getRight().toString());
        }

        createGraph(jsNodes, jsEdges, f.getOffsetWidth(), f.getOffsetHeight(),
                canvasId);
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

            tabPanel.add(invariantsPanel, "Invariants");
            tabPanel.add(modelPanel, "Model");

            // Clear the invariants panel if it has any
            // widgets (it only has one)
            if (invariantsPanel.getWidgetCount() != 0) {
                invariantsPanel.remove(invariantsPanel.getWidget(0));
                assert (invariantsPanel.getWidgetCount() == 0);
            }

            HorizontalPanel vPanel = new HorizontalPanel();
            invariantsPanel.add(vPanel);

            GWTInvariants gwtInvs = result.getLeft();
            Set<String> invTypes = gwtInvs.getInvTypes();
            for (String invType : invTypes) {
                List<String> invs = gwtInvs.getInvs(invType);
                Grid grid = new Grid(invs.size() + 1, 1);
                vPanel.add(grid);

                grid.setWidget(0, 0, new Label(invType));
                grid.getCellFormatter().setStyleName(0, 0, "topTableCell");

                int i = 1;
                for (String inv : invs) {
                    grid.setWidget(i, 0, new Label(inv));
                    i += 1;
                }

                grid.setStyleName("panel grid");
                for (i = 1; i < grid.getRowCount(); i++) {
                    grid.getCellFormatter().setStyleName(i, 0, "tableCell");
                }
            }

            parseLogButton.setEnabled(true);
            tabPanel.selectTab(2);

            GWTGraph graph = result.getRight();
            showGraph(graph);

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
    class RefineOneStepAsyncCallback implements AsyncCallback<GWTGraph> {
        @Override
        public void onFailure(Throwable caught) {
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
            modelRefineButton.setEnabled(true);
        }

        @Override
        public void onSuccess(GWTGraph graph) {
            if (graph == null) {
                return;
            }
            modelRefineButton.setEnabled(true);
            tabPanel.selectTab(2);
            showGraph(graph);
        }
    }

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        // Build the page layout.
        RootPanel.get("mainDiv").add(tabPanel);
        tabPanel.setWidth("80%");
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

        grid.setStyleName("panel grid");
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
        modelPanel.add(modelRefineButton);
        RefineModelHandler refineHandler = new RefineModelHandler();
        modelRefineButton.addClickHandler(refineHandler);

        // Associate handler with the Parse Log button
        ParseLogHandler parseLogHandler = new ParseLogHandler();
        parseLogButton.addClickHandler(parseLogHandler);
    }
}
