package synopticgwt.client.util;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TabBar;

import synopticgwt.client.SynopticGWT;
import synopticgwt.client.model.ModelTab;

/**
 * A resize handler that takes care of updating the model graphic whenever the
 * model tab is resized.
 */
public class ModelResizeHandler implements ResizeHandler {

    // Timer is used to delay unnecessary updating, as the animation becomes
    // compute intensive when rendering the canvas too frequently.
    private final Timer resizeTimer;

    private final TabBar tabBar;
    private final ModelTab modelTab;

    // How often to schedule the re-animation timer (in milliseconds).
    private final int msTimerGranularity;

    /**
     * Adds a resize handler to the window to manage resize events for the model
     * tab. Upon resizing the window, after the window has remained unchanged
     * for the established amount of time (in ms), the model tab's model will be
     * updated.
     * 
     * @param mdlTab
     *            The instance of the model tab which will be handled by this
     *            event handler.
     * @param milliseconds
     *            The amount of time to wait after the window has updated before
     *            running the modelTab update.
     */
    public ModelResizeHandler(TabBar tbBar, ModelTab mdlTab, int milliseconds) {
        super();
        this.tabBar = tbBar;
        this.modelTab = mdlTab;
        this.msTimerGranularity = milliseconds;

        resizeTimer = new Timer() {
            @Override
            public void run() {
                // If the tab is enabled, resize the canvas
                // and redraw the graph (with fancy animation).
                if (tabBar.isTabEnabled(SynopticGWT.modelTabIndex)) {
                    modelTab.updateGraphPanel();
                }
            }
        };
    }

    /**
     * On the resize event, fires the event after a given number of milliseconds
     * has passed.
     */
    @Override
    public void onResize(ResizeEvent event) {
        resizeTimer.schedule(this.msTimerGranularity);
    }
}
