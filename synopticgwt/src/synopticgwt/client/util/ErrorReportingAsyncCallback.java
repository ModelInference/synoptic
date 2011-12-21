package synopticgwt.client.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.client.SynopticGWT;
import synopticgwt.shared.GWTServerException;

/**
 * A new callback object, which takes care of starting/stopping the associated
 * progress wheel animation, and error message display.
 * 
 * @param <T>
 *            The callback return value type.
 */
public abstract class ErrorReportingAsyncCallback<T> implements
        AsyncCallback<T> {

    /** Progress indicator associated with the asynchronous call. */
    protected ProgressWheel pWheel;

    /** The default error message to display if the caught error has no message. */
    protected String defaultErrorMsg;

    /**
     * Constructs a new callback object, which takes care of starting/stopping
     * the associated progress wheel animation, and error message display.
     * 
     * @param pWheel
     * @param defaultErrorMsg
     */
    public ErrorReportingAsyncCallback(ProgressWheel pWheel,
            String defaultErrorMsg) {
        this.defaultErrorMsg = defaultErrorMsg;
        this.pWheel = pWheel;
        // Clear any currently displayed errors.
        SynopticGWT.entryPoint.clearError();
        // Assume that we've just made the call -- begin animating the pWheel.
        if (pWheel != null) {
            pWheel.startAnimation();
        }
    }

    /**
     * Constructor for the case when there is no progress wheel associated with
     * an RPC.
     */
    public ErrorReportingAsyncCallback(String defaultErrorMsg) {
        this(null, defaultErrorMsg);
    }

    @Override
    public void onFailure(Throwable caught) {
        // Returned from an RPC -- stop animating the pWheel.
        if (this.pWheel != null) {
            pWheel.stopAnimation();
        }
        // Construct the failure message and stack trace.
        String msg = caught.getMessage();
        if (msg == null) {
            msg = "[RPC failure] " + defaultErrorMsg;
        }

        String stackTrace = "";
        for (Object line : caught.getStackTrace()) {
            stackTrace += line + "\n";
        }

        String serverStackTrace = "";
        if (caught instanceof GWTServerException) {
            serverStackTrace = ((GWTServerException) caught).serverStackTrace;
        }

        // Show the failure information.
        SynopticGWT.entryPoint.showError(msg, stackTrace, serverStackTrace);
    }

    @Override
    public void onSuccess(T result) {
        // Returned from an RPC -- stop animating the pWheel.
        if (this.pWheel != null) {
            pWheel.stopAnimation();
        }
    }
}
