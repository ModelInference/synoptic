package synopticgwt.client.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.RadioButton;

import synopticgwt.shared.GWTEdge;

/**
 * A table used to display information about paths through groups of partitions
 * that have been specified by the user. When given a set of paths to display,
 * the table will sort the trace IDs and display each as a radio button, which,
 * when clicked, will highlight the edges in the model related to the specified
 * trace ID.
 */
public class PathsThroughPartitionsTable extends FlexTable {

    // The group under which to put all radio buttons.
    private static final String RADIO_BUTTON_GROUP = "traceRadioButton";

    // A map of buttons to GWT-centric paths. Each button corresponds to
    // a unique path that Synoptic has inferred through the occurrence of one
    // or more corresponding traces in the log.
    private final Map<RadioButton, Set<GWTEdge>> buttonToPathMap = new HashMap<RadioButton, Set<GWTEdge>>();

    /**
     * Accepts a set of paths mapped to traceIDs, and then adds a radio button
     * for each traceID. When a radio button is clicked, the model will
     * highlight the corresponding edges related to the trace, and clear any
     * previous edge highlights if selected again.
     * 
     * @param pathsToShow
     *            A set of paths mapped to traceIDs. Each path is one that has
     *            been inferred from a single trace in the log.
     */
    public void showPaths(Map<Set<GWTEdge>, Set<Integer>> pathsToShow) {
        this.clear();
        int row = 0;
        int pathNum = 1;

        ValueChangeHandler<Boolean> radioButtonChangeHandler = new PathViewChangeHandler();

        // Create a set of radio buttons and related to each path,
        // and a panel to show traces associated with said path.
        for (Set<GWTEdge> path : pathsToShow.keySet()) {
            // Create the widgets for showing info about the paths: The radio
            // button
            // that will highlight the model when selected, and the table that
            // shows the
            // IDs of the traces Synoptic used to infer said paths (each set of
            // traces
            // is related to the corresponding button).
            RadioButton button = new RadioButton(RADIO_BUTTON_GROUP, "Path "
                    + pathNum);
            FlexTable tracesTable = getSortedTracesTable(pathsToShow.get(path));
            DisclosurePanel tracesPanel = new DisclosurePanel("Traces ("
                    + tracesTable.getRowCount() + ")");

            // Add the traces table to the panel so it can be viewed by the
            // users.
            tracesPanel.add(tracesTable);

            // Map the button to the corresponding path.
            this.buttonToPathMap.put(button, path);

            // Attach relevant action listeners.
            button.addValueChangeHandler(radioButtonChangeHandler);

            // Attach the widgets to the table.
            this.setWidget(row, 0, button);
            this.setWidget(row + 1, 1, tracesPanel);
            row += 2;
            pathNum++;
        }
    }

    /**
     * @param traces
     *            The set of traces that will be sorted and added to the
     *            {@code FlexTable}
     * @return a {@code FlexTable} that contains a sorted list of traces.
     */
    private FlexTable getSortedTracesTable(Set<Integer> traces) {

        // TODO Perhaps make this FlexTable a separate class altogether
        // so that extra functionality can be given to each individual
        // trace.

        // Create an array and fill it with the sorted
        // traces.
        FlexTable traceIDsTable = new FlexTable();
        Integer[] traceIDs = new Integer[traces.size()];
        Arrays.sort(traces.toArray(traceIDs));

        // Add each trace to the table.
        int row = 0;
        for (Integer traceID : traceIDs) {
            traceIDsTable.setText(row, 0, "Trace " + traceID);
            row++;
        }

        return traceIDsTable;
    }

    private class PathViewChangeHandler implements ValueChangeHandler<Boolean> {
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            // If the button has, in fact, changed, highlight the path
            // in the model.
            if (event.getValue()) {
                RadioButton sourceButton = (RadioButton) event.getSource();
                
                // Convert the path to a java script array that
                // can be displayed on top of the model.
                JavaScriptObject jsEdges = GWTToJSUtils
                        .createJSArrayFromGWTEdges(buttonToPathMap
                                .get(sourceButton));
                ModelGraphic.highlightEdges(jsEdges);
            }
        }
    }
}
