package synopticgwt.client.util;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.UIObject;

import synopticgwt.client.SynopticGWT;
import synopticgwt.client.invariants.InvariantsTab;

/**
 * A resize handler that takes care of updating the model graphic whenever the
 * model tab is resized.
 */
public class InvariantsResizeHandler implements ResizeHandler {
    
    private TabBar tabBar;
    private InvariantsTab invTab;
    
    public InvariantsResizeHandler(TabBar tabBar, InvariantsTab invTab) {
        super();
        this.tabBar = tabBar;
        this.invTab = invTab;
    }

    @Override
    public void onResize(ResizeEvent event) {
        /*
        if (UIObject.isVisible(invTab.getTableAndGraphicPanel().getElement())) {
            invTab.resize();
        }
        */
    }

}
