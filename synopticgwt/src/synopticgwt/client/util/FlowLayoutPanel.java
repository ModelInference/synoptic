package synopticgwt.client.util;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

public class FlowLayoutPanel extends FlowPanel implements RequiresResize,
        ProvidesResize {
    public void onResize() {
        for (Widget child : getChildren())
            if (child instanceof RequiresResize)
                ((RequiresResize) child).onResize();
    }
}
