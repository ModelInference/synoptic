package synopticgwt.client.model;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;

import synopticgwt.shared.LogLine;

/**
 * An instance of this class represents a table that shows a set of log lines
 * corresponding to some partition in the model.
 */
public class LogLinesTable extends DataGrid<LogLine> {
    Column<LogLine, String> lineNumCol;
    Column<LogLine, String> lineCol;

    /** Initialize a blank table, with a header row. */
    public LogLinesTable() {
        super();

        Cell<String> lineNumCell = new TextCell();
        lineNumCol = new Column<LogLine, String>(lineNumCell) {
            @Override
            public String getValue(LogLine line) {
                return ((Integer) line.getLineNum()).toString();
            }
        };
        this.addColumn(lineNumCol, "Line #");

        Cell<String> lineCell = new TextCell();
        lineCol = new Column<LogLine, String>(lineCell) {
            @Override
            public String getValue(LogLine line) {
                return line.getLine();
            }
        };
        this.addColumn(lineCol, "Line");
        this.setWidth("100%");
        this.setColumnWidth(lineNumCol, 70, Unit.PX);
        this.setColumnWidth(lineCol, 100, Unit.PCT);
    }

    /**
     * Clears the current data from the table.
     */
    public void clear() {
        this.setRowCount(0);
    }
}
