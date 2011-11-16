package synopticgwt.client.util;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;

import synopticgwt.client.model.ModelTab;

/**
 * A resize handler that takes care of updating the model graphic whenever the
 * model tab is resized.
 */
public class ModelResizeHandler implements ResizeHandler {

    // Timer is here to delay any unnecessary updating.
    // since it gets rather heavy and can cause problems
    // when rendering the canvas too frequently.
    private final Timer resizeTimer;

    private final ModelTab modelTab;
    private final int ms;

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
    public ModelResizeHandler(ModelTab mdlTab, int milliseconds) {
        super();
        this.modelTab = mdlTab;
        this.ms = milliseconds;

        resizeTimer = new Timer() {
            @Override
            public void run() {
                // If the tab is active, resize the canvas
                // and redraw the graph (with fancy animation).
                if (modelTab.isEnabled())
                    modelTab.updateGraphPanel();
            }
        };
    }

    /**
     * On the resize event, fires the event after a given number of milliseconds
     * has passed.
     */
    @Override
    public void onResize(ResizeEvent event) {
        resizeTimer.schedule(this.ms);
    }
}
