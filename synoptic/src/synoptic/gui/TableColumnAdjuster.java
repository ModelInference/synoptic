package synoptic.gui;

import java.awt.Component;


import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/*
 *	Class to manage the widths of columns in a table.
 *
 */
public class TableColumnAdjuster {
	
	private static final int SPACING = 10;
	
	private JTable table;

	/*
	 *  Specify the table
	 */
	public TableColumnAdjuster(JTable table) {
		this.table = table;
	}

	/*
	 *  Adjust the widths of all the columns in the table
	 */
	public void adjustColumns() {
		TableColumnModel tcm = table.getColumnModel();
		for (int i = 0; i < tcm.getColumnCount(); i++) 
			adjustColumn(i);
	}
	
	/*
	 *  Adjust the width of the specified column in the table
	 */
	public void adjustColumn(final int column) {
		TableColumn tableColumn = table.getColumnModel().getColumn(column);

		if (! tableColumn.getResizable()) return;

		int columnHeaderWidth = getColumnHeaderWidth( column );
		int columnDataWidth   = getColumnDataWidth( column );
		int preferredWidth    = Math.max(columnHeaderWidth, columnDataWidth);

		updateTableColumn(column, preferredWidth);
	}

	/*
	 *  Calculated the width based on the column name
	 */
	private int getColumnHeaderWidth(int column) {

		TableColumn tableColumn = table.getColumnModel().getColumn(column);
		Object value = tableColumn.getHeaderValue();
		TableCellRenderer renderer = tableColumn.getHeaderRenderer();

		if (renderer == null)
			renderer = table.getTableHeader().getDefaultRenderer();

		Component c = renderer.getTableCellRendererComponent(table, value, false, false, -1, column);
		return c.getPreferredSize().width;
	}

	/*
	 *  Calculate the width based on the widest cell renderer for the
	 *  given column.
	 */
	private int getColumnDataWidth(int column) {
		int preferredWidth = 0;

		for (int row = 0; row < table.getRowCount(); row++) 
    		preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, column));

		return preferredWidth;
	}

	/*
	 *  Get the preferred width for the specified cell
	 */
	private int getCellDataWidth(int row, int column) {
		
		//  Invoke the renderer for the cell to calculate the preferred width
		TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
		Object value = table.getValueAt(row, column);
		Component c = cellRenderer.getTableCellRendererComponent(table, value, false, false, row, column);
		int width = c.getPreferredSize().width + table.getIntercellSpacing().width;

		return width;
	}

	/*
	 *  Update the TableColumn with the newly calculated width
	 */
	private void updateTableColumn(int column, int width) {
		final TableColumn tableColumn = table.getColumnModel().getColumn(column);

		if (! tableColumn.getResizable()) return;
		
		width += SPACING;
		
		table.getTableHeader().setResizingColumn(tableColumn);
		tableColumn.setWidth(width);
	}
}