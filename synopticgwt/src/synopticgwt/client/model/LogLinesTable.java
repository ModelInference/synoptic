package synopticgwt.client.model;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;

import synopticgwt.shared.LogLine;

/**
 * An instance of this class represents a table that shows a set of log lines
 * corresponding to some partition in the model.
 */
public class LogLinesTable extends FlexTable {

    /** Initialize a blank table, with a header row. */
    public LogLinesTable() {
        super();
        this.setText(0, 0, "Line #");
        this.setText(0, 1, "Line");

        // TODO: as we only support either text-based or single file log
        // uploads, the "Filename" column is not useful right now.
        // this.setText(0, 2, "Filename");

        // Style the table
        this.addStyleName("FlexTable");
        HTMLTable.RowFormatter rf = this.getRowFormatter();
        rf.addStyleName(0, "TableHeader");
        HTMLTable.ColumnFormatter cf = this.getColumnFormatter();
        cf.addStyleName(0, "LineNumCol");
        cf.addStyleName(1, "LineCol");
        // Unused. See TODO above.
        // cf.addStyleName(2, "FilenameCol");
    }

    /** Removes all currently displayed log lines from the table. */
    public void clear() {
        // Do not remove the header row.
        while (this.getRowCount() != 1) {
            this.removeRow(1);
        }
    }

    /** Clears the table and then shows a set of new lines in the table. */
    public void showLines(List<LogLine> lines) {
        this.clear();
        int row = 1;
        // Skip the header row (row = 0) when setting text.
        for (LogLine log : lines) {
            this.setText(row, 0, log.getLineNum() + "");
            this.setText(row, 1, log.getLine());
            // Unused. See TODO above.
            // this.setText(row, 2, log.getFilename());
            row++;
        }
    }
}
