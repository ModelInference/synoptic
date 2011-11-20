package synopticgwt.client.util;

/**
 * Exposes an interface for GWT events to be tracked via Google analytics. All
 * of the exposed methods are static JSNI methods. The client is required to
 * include google-analytics.com/ga.js on the page, and make it available via
 * $wnd, and this code assume that _setAccount has already been called. Code is
 * taken from this page:
 * 
 * <pre>
 * http://stackoverflow.com/questions/4785036/how-to-integrate-google-analytics-into-gwt-using-the-asynchronous-script
 * </pre>
 */
public class AnalyticsTracker {

    public static native void trackEvent(String category, String action,
            String label) /*-{
		if ($wnd.analyticsTrackerID != null) {
			alert("hello!");
			$wnd._gaq.push([ '_trackEvent', category, action, label ]);
		}
    }-*/;

    public static native void trackEvent(String category, String action,
            String label, int intArg) /*-{
		if ($wnd.analyticsTrackerID != null) {
			$wnd._gaq.push([ '_trackEvent', category, action, label, intArg ]);
		}
    }-*/;

    public static native void trackPageview(String url) /*-{
		if ($wnd.analyticsTrackerID != null) {
			$wnd._gaq.push([ '_trackPageview', url ]);
		}
    }-*/;
}
