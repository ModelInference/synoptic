package synopticgwt.client.util;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Implements an animated spinning wheel that can be used to indicate progress,
 * or some kind of background (e.g., server) activity.
 */
public class ProgressWheel {
    /** Timer controlling the pace of the animation. */
    private final Timer timer;

    /** The panel containing the animation. */
    private final RootPanel divPanel;

    /**
     * Create a progress wheel with default dimensions.
     * 
     * @param divPanelId
     *            HTML id of the DIV where the progress wheel will be placed.
     */
    public ProgressWheel(String divPanelId) {
        this(divPanelId, 10, 2, 7);
    }

    /**
     * Creates a progress wheel with custom dimensions.
     * 
     * @param divPanelId
     *            HTML id of the DIV where the progress wheel will be placed.
     * @param radius
     *            size of the SVG graphic / 2
     * @param r1
     *            (smaller) inner radius of wheel
     * @param r2
     *            (larger) outer radius of wheel
     */
    public ProgressWheel(String divPanelId, int radius, int r1, int r2) {
        // Get the RootPanel containing the progress wheel based on divPanelId
        // and hide it since the progress wheel isn't scheduled yet.
        divPanel = RootPanel.get(divPanelId);
        divPanel.setVisible(false);

        // Add the progress wheel to the DIV.
        addProgressWheel(divPanelId, radius, r1, r2);

        // Create the timer, but do not schedule it.
        timer = new Timer() {
            @Override
            public void run() {
                // Whenever the timer fires, it animates the progress wheel by
                // one step.
                animateProgressWheel();
            }
        };
    }

    /**
     * Starts the wheel animation.
     */
    public void startAnimation() {
        divPanel.setVisible(true);
        timer.scheduleRepeating(100);
    }

    /**
     * Stops the wheel animation.
     */
    public void stopAnimation() {
        timer.cancel();
        divPanel.setVisible(false);
    }

    // //////////////////////////////////////////////////////////////////////////
    // JSNI methods -- JavaScript Native Interface methods. The method body of
    // these calls is pure JavaScript.

    /**
     * A JSNI method to animate the progress wheel by one step, by changing
     * opacity of all the sectors making up the wheel.
     */
    native static void animateProgressWheel() /*-{
		$wnd.opacity.unshift($wnd.opacity.pop());
		for ( var i = 0; i < $wnd.sectorsCount; i++) {
			$wnd.sectors[i].attr("opacity", $wnd.opacity[i]);
		}
    }-*/;

    /**
     * A JSNI method to add a progress wheel to a DIV. To animate the progress
     * wheel by a single step, call the animateProgressWheel() function.
     * 
     * @param radius
     *            size of the SVG graphic / 2
     * @param r1
     *            (smaller) inner radius of wheel
     * @param r2
     *            (larger) outer radius of wheel
     */
    private native static void addProgressWheel(String divPanelId, int radius,
            int r1, int r2) /*-{
		var r = $wnd.Raphael($doc.getElementById(divPanelId), radius * 2,
				radius * 2);
		r.clear();
		$wnd.sectorsCount = 12;
		var color = "#000";
		var width = 1;
		var cx = radius;
		var cy = radius;
		$wnd.sectors = [];
		$wnd.opacity = [];
		var beta = 2 * $wnd.Math.PI / $wnd.sectorsCount,

		pathParams = {
			stroke : color,
			"stroke-width" : width,
			"stroke-linecap" : "round"
		};

		for ( var i = 0; i < $wnd.sectorsCount; i++) {
			var alpha = (beta * i);
			var cos = $wnd.Math.cos(alpha);
			var sin = $wnd.Math.sin(alpha);
			$wnd.opacity[i] = 1 / $wnd.sectorsCount * i;

			$wnd.sectors[i] = r.path("M" + (cx + r1 * cos) + " "
					+ (cy + r1 * sin) + "L" + (cx + r2 * cos) + " "
					+ (cy + r2 * sin));
			$wnd.sectors[i].attr(pathParams);
		}
    }-*/;

    // </JSNI methods>
    // //////////////////////////////////////////////////////////////////////////
}
