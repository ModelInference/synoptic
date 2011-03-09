package synoptic.gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*
 *	Class to manage the widths of columns in a table.
 *
 */
public class TableColumnAdjuster // implements PropertyChangeListener//, TableModelListener
{
	private JTable table;
	private int spacing;
	private boolean isOnlyAdjustLarger;
	private Map<TableColumn, Integer> columnSizes = new HashMap<TableColumn, Integer>();

	/*
	 *  Specify the table and use default spacing
	 */
	public TableColumnAdjuster(JTable table) {
		this(table, 6);
	}

	/*
	 *  Specify the table and spacing
	 */
	public TableColumnAdjuster(JTable table, int spacing) {
		this.table = table;
		this.spacing = spacing;
		setOnlyAdjustLarger( true );
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
		int maxWidth = table.getColumnModel().getColumn(column).getMaxWidth();

		for (int row = 0; row < table.getRowCount(); row++) {
    		preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, column));

			//  We've exceeded the maximum width, no need to check other rows
			if (preferredWidth >= maxWidth)
			    break;
		}

		return preferredWidth;
	}

	/*
	 *  Get the preferred width for the specified cell
	 */
	private int getCellDataWidth(int row, int column) {
		
		//  Inovke the renderer for the cell to calculate the preferred width
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

		width += spacing;

		//  Don't shrink the column width
		if (isOnlyAdjustLarger)
			width = Math.max(width, tableColumn.getPreferredWidth());

		columnSizes.put(tableColumn, new Integer(tableColumn.getWidth()));
		table.getTableHeader().setResizingColumn(tableColumn);
		tableColumn.setWidth(width);
	}


	/*
	 *	Indicates whether columns can only be increased in size
	 */
	public void setOnlyAdjustLarger(boolean isOnlyAdjustLarger) {
		this.isOnlyAdjustLarger = isOnlyAdjustLarger;
	}

}