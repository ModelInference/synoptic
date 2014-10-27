package synopticgwt.client.model;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.DataGrid.Resources;

public interface LogLinesDataGridResources extends Resources {

    @Source({ DataGrid.Style.DEFAULT_CSS, "../resources/logLinesDataGrid.css" })
    CustomStyle dataGridStyle();

    interface CustomStyle extends DataGrid.Style {
        //
    }

}
