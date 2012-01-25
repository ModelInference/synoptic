package synopticgwt.client.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import synopticgwt.shared.GWTEdge;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.RadioButton;

public class PathsThroughPartitionsTable extends FlexTable {

    // The group under which to put all radio buttons.
    private static final String RADIO_BUTTON_GROUP = "traceRadioButton";

    // Clears all rows from the table.
    public void clear() {
        while (this.getRowCount() > 0) {
            this.removeRow(0);
        }
    }

    public void showPaths(Map<Integer, Set<GWTEdge>> paths) {
        this.clear();
        int row = 0;
        
        // Sort the traces for readability.
        Set<Integer> keys = paths.keySet();
        Integer[] traceIDs = new Integer[keys.size()];
        Arrays.sort(keys.toArray(traceIDs));

        for (Integer trace : traceIDs) {
            RadioButton button = new RadioButton(RADIO_BUTTON_GROUP, "Trace "
                    + trace);
            this.setWidget(row, 0, button);
            row++;
        }
    }
}
