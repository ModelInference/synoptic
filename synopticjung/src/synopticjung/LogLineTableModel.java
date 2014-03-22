package synopticjung;

import javax.swing.table.AbstractTableModel;

/**
 * Extension of AbstractTabelModel which allows the log line table to be updated
 * with new data
 * 
 */
public class LogLineTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private final String[] columnNames = new String[] { "Line #", "Line",
            "File" };
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
