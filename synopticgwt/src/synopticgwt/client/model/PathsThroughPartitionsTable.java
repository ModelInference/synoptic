package synopticgwt.client.model;

import java.util.Map;
import java.util.Set;

import synopticgwt.shared.GWTEdge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;

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

    // Set whenever "showPaths" method is called.
    private Map<Set<GWTEdge>, Set<Integer>> paths;

    /**
     * Clears the table of any displayed paths.
     */
    public void clearPaths() {
        while (this.getRowCount() > 0) {
            this.removeRow(0);
        }
    }

    /**
     * Accepts a set of paths mapped to traceIDs, and then adds a radio button
     * for each traceID. When a radio button is clicked, the model will
     * highlight the corresponding edges related to the trace, and clear any
     * previous edge highlights if selected again. (TODO: Implement edge
     * highlighting functionality).
     * 
     * @param paths
     *            A set of paths mapped to traceIDs. Each path is one that has
     *            been inferred from a single trace in the log.
     */
    public void showPaths(Map<Set<GWTEdge>, Set<Integer>> paths) {
        this.clearPaths();
        this.paths = paths;
        int row = 0;

        Set<Set<GWTEdge>> keys = paths.keySet();

        PathButtonClickHandler clickHandler = new PathButtonClickHandler();

        // Create a set of radio buttons related to each path.
        for (Set<GWTEdge> path : keys) {
            PathDisplayRadioButton button = new PathDisplayRadioButton(
                    RADIO_BUTTON_GROUP, "Path " + (row + 1), paths.get(path),
                    path);
            button.addClickHandler(clickHandler);
            this.setWidget(row, 0, button);
            row++;
        }
    }

    /**
     * A click handler that inserts all of the trace IDs for the corresponding
     * path displaying radio button. Any other trace IDs listed prior will be
     * removed.
     */
    private class PathButtonClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            PathDisplayRadioButton button = (PathDisplayRadioButton) event
                    .getSource();

            // TODO Clear the last table shown when a new button has been
            // clicked.

            // Create a table showing all of the traces to be inserted
            // after the button that has been clicked.
            // Make sure to sort it for readability.
            FlexTable traceIDsTable = new FlexTable();
            Integer[] traceIDs = new Integer[paths.get(button.getPath())
                    .size()];
            paths.get(button.getPath()).toArray(traceIDs);
            int row = 0;
            for (Integer traceID : traceIDs) {
                traceIDsTable.setText(row, 0, "Trace " + traceID);
                row++;
            }

            // Find the row in which the event took place on the table
            // (the one housing this class), and then insert the created
            // table right after the button.
            int rowSource = getCellForEvent(event).getRowIndex() + 1;
            insertRow(rowSource);
            setWidget(rowSource, 1, traceIDsTable);
        }
    }
}
