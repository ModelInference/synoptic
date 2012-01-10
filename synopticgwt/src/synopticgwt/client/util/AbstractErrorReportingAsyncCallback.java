package synopticgwt.client.util;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import synopticgwt.shared.GWTServerException;

/**
 * A callback object, which takes care of starting/stopping the associated
 * progress wheel animation, and controlling the error message display. This
 * class is abstract to maintain an invariant that every constructor must call
 * initialize() as the final statement. All classes that extend this class must
 * do this. For more information, see:
 * 
 * <pre>
 * http://benpryor.com/blog/2008/01/02/dont-call-subclass-methods-from-a-superclass-constructor/
 * </pre>
 * 
 * @param <T>
 *            The callback return value type.
 */
public abstract class AbstractErrorReportingAsyncCallback<T> implements AsyncCallback<T> {

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
    public AbstractErrorReportingAsyncCallback(ProgressWheel pWheel, String defaultErrorMsg) {
        this.defaultErrorMsg = defaultErrorMsg;
        this.pWheel = pWheel;

        // Assume that we've just made the call -- begin animating the pWheel.
        if (pWheel != null) {
            pWheel.startAnimation();
        }
    }

    /**
     * MUST be called at the end of a constructor.
     */
    protected final void initialize() {
        // Clear out any currently displayed errors.
        clearError();
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
        showError(msg, stackTrace, serverStackTrace);
    }

    @Override
    public void onSuccess(T result) {
        // Returned from an RPC -- stop animating the pWheel.
        if (this.pWheel != null) {
            pWheel.stopAnimation();
        }
    }

    /**
     * Clears the current error message, if any.
     */
    protected void clearError() {
        RootPanel rpcErrorDiv = RootPanel.get("ErrorDiv");
        RootPanel straceDiv = RootPanel.get("StackTraceDiv");
        rpcErrorDiv.clear();
        straceDiv.clear();
    }

    /**
     * Shows an error message in the errorDiv.
     */
    protected void showError(String msg, String clientStackTrace,
            String serverStackTrace) {
        // First, clear whatever error might be currently displayed.
        clearError();

        // All error-related messages will be added to this flow panel.
        RootPanel errorDiv = RootPanel.get("ErrorDiv");
        // FlowPanel fPanel = new FlowPanel();
        // errorDiv.add(fPanel);

        // Add the principle error message.
        Label errorMsg = new Label(msg);
        errorMsg.setStyleName("ErrorMessage");
        errorDiv.add(errorMsg);

        RootPanel straceDiv = RootPanel.get("StackTraceDiv");
        FlowPanel fPanel = new FlowPanel();
        straceDiv.add(fPanel);

        // Client-side stack trace can be revealed/hidden.
        if (clientStackTrace != "") {
            DisclosurePanel strace = new DisclosurePanel("Client stack trace");
            strace.setAnimationEnabled(true);
            strace.setContent(new HTML(clientStackTrace.replace("\n", "<br/>")));
            strace.setStyleName("ClientExceptionTraceBack");
            fPanel.add(strace);
        }

        // Server-side stack trace can be revealed/hidden.
        if (serverStackTrace != "") {
            DisclosurePanel strace = new DisclosurePanel("Server stack trace");
            strace.setAnimationEnabled(true);
            strace.setContent(new HTML(serverStackTrace.replace("\n", "<br/>")));
            strace.setStyleName("ServerExceptionTraceBack");
            fPanel.add(strace);
        }
    }

}
