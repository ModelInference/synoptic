package synopticgwt.client.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import synopticgwt.shared.GWTEdge;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * A table used to display information about paths through
 * groups of partitions that have been specified by the user.
 * When given a set of paths to display, the table will sort the
 * trace IDs and display each as a radio button, which, when clicked,
 * will highlight the edges in the model related to the specified
 * trace ID.
 */
public class PathsThroughPartitionsTable extends FlexTable {

    // The group under which to put all radio buttons.
    private static final String RADIO_BUTTON_GROUP = "traceRadioButton";

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
    public void showPaths(Map<Integer, Set<GWTEdge>> paths) {
        this.clearPaths();
        int row = 0;

        // Sort the traces for readability.
        Set<Integer> keys = paths.keySet();
        Integer[] traceIDs = new Integer[keys.size()];
        Arrays.sort(keys.toArray(traceIDs));

        // TODO Add an action listener to the radio button so that it
        // somehow displays the paths associated with it (perhaps create a new
        // class).
        for (Integer trace : traceIDs) {
            RadioButton button = new RadioButton(RADIO_BUTTON_GROUP, "Trace "
                    + trace);
            this.setWidget(row, 0, button);
            row++;
        }
    }
}
