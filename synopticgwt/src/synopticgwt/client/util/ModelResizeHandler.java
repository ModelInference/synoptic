package synopticgwt.client.util;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TabBar;

import synopticgwt.client.SynopticGWT;
import synopticgwt.client.model.ModelTab;

/**
 * A resize handler that updates the model graphic whenever the window is
 * resized.
 */
public class ModelResizeHandler implements ResizeHandler {

    // Timer is used to delay unnecessary updating, as the animation becomes
    // compute intensive when rendering the canvas too frequently.
    private final Timer resizeTimer;

    final TabBar tabBar;
    final ModelTab modelTab;

    // Delay (in milliseconds) between resize event and resizing.
    private final int resizingDelay;

    /**
     * A window resize handler to manage resizing of the model graphic. Upon
     * resizing the window, after the window has remained unchanged for some
     * time (in ms), the model graphic is updated.
     * 
     * @param modelTab
     *            The instance of the model tab which will be handled by this
     *            event handler.
     * @param resizingDelay
     *            The amount of time to wait after the window has updated before
     *            running the graphic update (in milliseconds).
     */
    public ModelResizeHandler(TabBar tabBar, ModelTab modelTab,
            int resizingDelay) {
        super();
        this.tabBar = tabBar;
        this.modelTab = modelTab;
        this.resizingDelay = resizingDelay;

        resizeTimer = new Timer() {
            @Override
            public void run() {
                // If the tab is enabled, resize the canvas
                // and redraw the graph (with fancy animation).
                if (ModelResizeHandler.this.tabBar.getSelectedTab() == SynopticGWT.modelTabIndex) {
                    ModelResizeHandler.this.modelTab.updateGraphPanel();
                } else {
                    ModelResizeHandler.this.modelTab.updateSizeOnTabSelection = true;
                }
            }
        };
    }

    /**
     * On resize, schedule a timer to resize graphic in a few ms.
     */
    @Override
    public void onResize(ResizeEvent event) {
        resizeTimer.schedule(this.resizingDelay);
    }
}
