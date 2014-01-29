package synopticgwt.client.util;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TabBar;

import synopticgwt.client.SynopticGWT;
import synopticgwt.client.invariants.InvariantsTab;

/**
 * A resize handler that updates the invariant graphic whenever the window is
 * resized.
 */
public class InvariantsResizeHandler implements ResizeHandler {
    // Timer is used to delay unnecessary updating, as the animation becomes
    // compute intensive when rendering the canvas too frequently.
    private final Timer resizeTimer;

    final TabBar tabBar;
    final InvariantsTab invTab;

    // Delay (in milliseconds) between resize event and resizing.
    private final int resizingDelay;

    /**
     * A window resize handler to manage resizing of the invariants graphic.
     * Upon resizing the window, after the window has remained unchanged for
     * some time (in ms), the invariant graphic is resized.
     * 
     * @param modelTab
     *            The instance of the model tab which will be handled by this
     *            event handler.
     * @param resizingDelay
     *            The amount of time to wait after the window has updated before
     *            running the graphic resize (in milliseconds).
     */
    public InvariantsResizeHandler(TabBar tabBar, InvariantsTab invTab,
            int resizingDelay) {
        super();
        this.tabBar = tabBar;
        this.invTab = invTab;
        this.resizingDelay = resizingDelay;

        resizeTimer = new Timer() {
            @Override
            public void run() {
                // If the tab is selected, resize the canvas and redraw the
                // invariants graph.
                if (InvariantsResizeHandler.this.tabBar.getSelectedTab() == SynopticGWT.invariantsTabIndex) {
                    InvariantsResizeHandler.this.invTab.resize();
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
