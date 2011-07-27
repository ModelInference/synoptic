package synoptic.gui;

import javax.swing.table.AbstractTableModel;

/**
 * Extension of AbstractTabelModel which allows the log line table to be updated with new data
 * 
 * @author jenny
 * 
 */
@SuppressWarnings("serial")
public class LogLineTableModel extends AbstractTableModel {
	private final String [] columnNames = new String [] {"Line #", "Line", "File"};
	private Object[][] data;

	public LogLineTableModel(Object[][] data) {
		this.data = data;
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}
	
	public void setData(Object[][] newData) {  
		data = newData;  
		fireTableDataChanged();  
	}  

}