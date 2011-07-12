package synopticgwt.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Animates a spinning wheel to indicate progress, or some kind of activity.
 * 
 * @author ivan
 */
public class ProgressWheel {
    // Timer controling the pace of the animation.
    private final Timer timer;
    // The div containing the animation.
    private final RootPanel divPanel;

    public ProgressWheel(String divPanelId, RootPanel divPanel) {
        this(divPanelId, divPanel, 10, 2, 7);
    }

    public ProgressWheel(String divPanelId, RootPanel divPanel, int radius,
            int r1, int r2) {
        // Add the progress wheel to the div.
        addProgressWheel(divPanelId, radius, r1, r2);

        this.divPanel = divPanel;

        // Create the timer, but don't schedule it.
        timer = new Timer() {
            @Override
            public void run() {
                animateProgressWheel();
            }
        };
        divPanel.setVisible(false);
    }

    // //////////////////////////////////////////////////////////////////////////
    // JSNI methods -- JavaScript Native Interface methods. The method body of
    // these calls is pure JavaScript.

    private native static void animateProgressWheel() /*-{
		$wnd.opacity.unshift($wnd.opacity.pop());
		for ( var i = 0; i < $wnd.sectorsCount; i++) {
			$wnd.sectors[i].attr("opacity", $wnd.opacity[i]);
		}
    }-*/;

    /**
     * A JSNI method to add a progress wheel to a div. To animate the progress
     * wheel by a single step, call the animateProgressWheel() function.
     * 
     * @param radius
     *            size of the svg graphic / 2
     * @param r1
     *            (smaller) inner radius of wheel
     * @param r2
     *            (larger) outter radius of wheel
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
}
