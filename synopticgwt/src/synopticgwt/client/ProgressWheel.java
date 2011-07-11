package synopticgwt.client;

import com.google.gwt.user.client.Timer;

public class ProgressWheel {
    public void start() {
        timer = new Timer() {
            @Override
            public void run() {
                animateProgressWheel();
            }
        };
        timer.scheduleRepeating(100);

    }

    private Timer timer;

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
    private native static void addProgressWheel(String divHolder, int radius,
            int r1, int r2) /*-{
		var r = $wnd.Raphael($doc.getElementById(divHolder), radius * 2,
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

}
