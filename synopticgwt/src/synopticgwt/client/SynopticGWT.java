package synopticgwt.client;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import synopticgwt.client.input.InputTab;
import synopticgwt.client.invariants.InvariantsTab;
import synopticgwt.client.model.ModelTab;
import synopticgwt.client.util.AnalyticsTracker;
import synopticgwt.client.util.ErrorReportingAsyncCallback;
import synopticgwt.client.util.InvariantsResizeHandler;
import synopticgwt.client.util.ModelResizeHandler;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;

/**
 * This class implements the entry point to the application -- it contains the
 * first method that will be run when the application is loaded by the browser.
 * This class ties together the various application tabs, and is the central
 * component through which functionality in different tabs communicate with each
 * other.
 */
public class SynopticGWT implements EntryPoint {
    /** Default global logger to use for logging all messages to the console. */
    // public static Logger logger = Logger.getLogger("SynopticGWT");

    /** These hold specific Tab indices (set in SynopticGWT.onModuleLoad()). */
    public static int inputsTabIndex;
    public static int invariantsTabIndex;
    public static int modelTabIndex;

    /** Create an RPC proxy to talk to the Synoptic service */
    private final ISynopticServiceAsync synopticService = GWT
            .create(ISynopticService.class);

    /** This contains the three main application tabs. */
    private TabPanel tabPanel = new TabPanel();

    // TODO: there should be a pWheel for every tab.
    /** The progress wheel is an visual indicator of progress for the user. */
    private ProgressWheel pWheel = null;

    /**
     * This static variable allows other class instances to find/use the
     * singleton SynopticGWT instance.
     */
    public static SynopticGWT entryPoint = null;

    /** Encapsulates logic associated with the input tab (e.g., log, reg exps). */
    private InputTab inputTab = null;

    /** Encapsulates logic having to do with the invariants tab */
    private InvariantsTab invTab = null;

    /** Encapsulates logic having to do with the model (e.g., vis, refinement..) */
    private ModelTab modelTab = null;

    private Map<Integer, Tab<?>> tabIndexToTab = new LinkedHashMap<Integer, Tab<?>>();

    /** Check box to control visibility of tool-tips. */
    public final CheckBox showHelpToolTips = new CheckBox("Show help tool-tips");
    static final String hideHelpToolTipsCookieName = new String(
            "hide-help-tool-tips");

    /**
     * Whether or not the user wants to manually control the
     * refinement/coarsening process.
     */
    public boolean manualRefineCoarsen;

    boolean invSetChanged = false;

    /**
     * Entry point to the entire application.
     */
    @Override
    public void onModuleLoad() {
        // NOTE: An entry point is instantiated only once and onModuleLoad() is
        // also called once, so there is no issue in storing the entry point
        // reference in a static field. In addition, there is no multi-threading
        // in GWT, via:
        // http://groups.google.com/group/google-web-toolkit/browse_thread/thread/5a2335ffb117bd08
        SynopticGWT.entryPoint = this;

        // logger.setLevel(Level.FINEST);

        // Show check box to control visibility of tool-tips.
        RootPanel.get("div-top-bar").add(showHelpToolTips);
        if (Cookies.getCookie(hideHelpToolTipsCookieName) == null) {
            // Cookie does not exist => default to showing tool-tips
            showHelpToolTips.setValue(true);
        } else {
            // Cookie exists => do not show tool-tips
            showHelpToolTips.setValue(false);
        }
        // When tool tips are disabled, we store a cookie to remember this
        // across sessions.
        showHelpToolTips
                .addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        boolean newVal = event.getValue();
                        if (!newVal) {
                            // Create a cookie that expires 1 year from now.
                            Date expireDate = new Date();
                            expireDate.setTime(expireDate.getTime() + 31556926);
                            Cookies.setCookie(hideHelpToolTipsCookieName, "",
                                    expireDate);
                        } else {
                            Cookies.removeCookie(hideHelpToolTipsCookieName);
                        }
                    }
                });

        // Add the panel of tabs to the page.
        RootPanel.get("mainDiv").add(tabPanel);

        // Create a new progress wheel object, and associate it with the
        // progressWheelDiv container.
        pWheel = new ProgressWheel("progressWheelDiv");

        // Tab on which the user will submit inputs.
        inputTab = new InputTab(synopticService, pWheel);
        // Tab that will display miner invariants.
        invTab = new InvariantsTab(synopticService, pWheel);
        // Tab that will display the model.
        modelTab = new ModelTab(synopticService, pWheel);

        // Associate the tabs with the tab panel.
        tabPanel.add(inputTab.getPanel(), "Inputs");
        inputsTabIndex = tabPanel.getWidgetIndex(inputTab.getPanel());

        tabPanel.add(invTab.getPanel(), "Invariants");
        invariantsTabIndex = tabPanel.getWidgetIndex(invTab.getPanel());

        tabPanel.add(modelTab.getPanel(), "Model");
        modelTabIndex = tabPanel.getWidgetIndex(modelTab.getPanel());

        // Build up a map between the tab index in the tab panel and the tab --
        // this is useful when processing events that change the selected tab.
        tabIndexToTab.put(inputsTabIndex, inputTab); 
        tabIndexToTab.put(invariantsTabIndex, invTab);
        tabIndexToTab.put(modelTabIndex, modelTab);

        tabPanel.setWidth("100%");

        // On load show the inputs tab, and disable the invariants/model tabs
        // (until the user parses a log).
        tabPanel.selectTab(inputsTabIndex);
        tabPanel.getTabBar().setTabEnabled(invariantsTabIndex, false);
        tabPanel.getTabBar().setTabEnabled(modelTabIndex, false);

        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                tabSelected(event);
            }
        });

        tabPanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
            @Override
            public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
                tabBeforeSelected(event);
            }
        });

        // Add handler for when the window is resized while viewing the model.
        // wait until 200 milliseconds after the last window update event
        // to redraw the model.
        // TODO: Have the handler enabled only when the model tab is selected.
        // That is, register the handler when the model tab is clicked, and
        // remove it when any one of the other tabs is clicked.
        Window.addResizeHandler(new ModelResizeHandler(tabPanel.getTabBar(),
                modelTab, 200));
        
        /* 
         * Handler for redrawing invariants when invariants tab is selected and
         * window is resized
         */
        Window.addResizeHandler(new InvariantsResizeHandler(tabPanel.getTabBar(),
                invTab));

        // Check whether or not to show the welcome screen.
        if (WelcomePopUp.showWelcome()) {
            WelcomePopUp welcome = new WelcomePopUp();
            welcome.setGlassEnabled(true);
            welcome.center();
            welcome.show();
        }

    }

    /**
     * Used by the invariants tab to signal that the user has modified the
     * invariant set, by e.g., activating/deactivating some of the invariants.
     */
    public void invSetChanged() {
        invSetChanged = true;
    }

    /** Called when commit invariants call to the Synoptic service succeeds. */
    public void commitInvsSuccess(GWTGraph gwtGraph) {
        invSetChanged = false;
        tabPanel.selectTab(modelTabIndex);
        modelTab.showGraph(gwtGraph);

        // Retrieve and show the final model, if this process is not being
        // controlled manually.
        if (!manualRefineCoarsen) {
            modelTab.getFinalModelButtonClick(null);
        }
    }

    /**
     * Fired by SynopticTabPanel _before_ the tab is selected. We capture this
     * event for two reasons: (1) cancel the event in case the tab is disabled,
     * and (2) to track the event for analytics.
     */
    public void tabBeforeSelected(BeforeSelectionEvent<Integer> event) {
        int tabIndex = event.getItem();

        // Sanity check of tabIndex.
        if (!tabIndexToTab.containsKey(tabIndex)) {
            return;
        }

        // 1. Check if the tab is enabled. If not, cancel the event.
        if (!tabPanel.getTabBar().isTabEnabled(tabIndex)) {
            event.cancel();
            return;
        }
        // 2. Only track the event if it has not been canceled.
        Tab<?> t = tabIndexToTab.get(tabIndex);
        AnalyticsTracker.trackEvent(t.trackerCategoryName, "selected",
                "navigation");
    }

    /**
     * Fired by SynopticTabPanel whenever a tab is selected.
     * 
     * This code executes before the tab's associated panel is rendered.
     * 
     * TODO: Migrate to a SelectionHandler and use addSelectionHandler 
     * since this is deprecated
     */
    public void tabSelected(SelectionEvent<Integer> event) {
        int tabIndex = event.getSelectedItem();
        
        /*  Ignore non-model-tab tab selections.
         * 
         *  commitInvsSuccesses calls tabPanel.selectTab(modelTabIndex) and
         *  prevents an infinite recursion of tabSelected events by setting 
         *  invSetChanged to false
         */
        if (tabIndex == modelTabIndex && invSetChanged) {
         // If we are clicking on the model tab, and the invariant set has
            // changed, then we (1) ask the server to re-do refinement/coarsening
            // with the new set of invariants, and (2) re-draw everything in the
            // model tab.

            // ////////////////////// Call to remote service.
            try {
                synopticService.commitInvariants(invTab.activeInvsHashes,
                        new ErrorReportingAsyncCallback<GWTGraph>(
                                "commitInvariants call") {
                            @Override
                            public void onSuccess(GWTGraph gwtGraph) {
                                super.onSuccess(gwtGraph);
                                commitInvsSuccess(gwtGraph);
                            }
                        });
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // //////////////////////
        }
        
    }

    /**
     * Called by the InputTab whenever the user clicks a button to parse the
     * log. This method creates the invariant/model tabs and initializes them
     * with the result of the parseLog() call to the Synoptic service -- namely,
     * the invariants and the initial model.
     * 
     * @param logInvs
     *            The invariants returned by the service corresponding to the
     *            input log.
     * @param initialModel
     *            The initial model for the log.
     */
    public void logParsed(GWTInvariantSet logInvs, GWTGraph initialModel) {
        // Enable the invariants tab, and show the invariants.
        tabPanel.getTabBar().setTabEnabled(invariantsTabIndex, true);
        invTab.setInvariants(logInvs);
        invTab.showInvariants();

        // TODO: Communicate whether we are processing a TO or a PO log
        // explicitly, instead of through (initialModel =?= null).
        if (initialModel != null) {
            // TO log.
            // Enable the model tab.
            tabPanel.getTabBar().setTabEnabled(modelTabIndex, true);

            modelTab.setManualMode(manualRefineCoarsen);

            // The modelTab MUST be selected before calling showGraph().
            tabPanel.selectTab(modelTabIndex);
            modelTab.showGraph(initialModel);

            // Retrieve and show the final model, if this process is not being
            // controlled manually.
            if (!manualRefineCoarsen) {
                modelTab.getFinalModelButtonClick(null);
            }
        } else {
            // PO log.
            // Switch to the invariant tab, and disable the model tab.
            tabPanel.selectTab(invariantsTabIndex);
            tabPanel.getTabBar().setTabEnabled(modelTabIndex, false);
            // TODO: we also want to clear model state here, in the case
            // that the prior generated model is large.
        }
    }

    public TabPanel getTabPanel() {
        return tabPanel;
    }

    /**
     * Clears the current error message, if any.
     */
    public void clearError() {
        RootPanel rpcErrorDiv = RootPanel.get("ErrorDiv");
        RootPanel straceDiv = RootPanel.get("StackTraceDiv");
        rpcErrorDiv.clear();
        straceDiv.clear();
    }

    /**
     * Shows an error message in the errorDiv.
     */
    public void showError(String msg, String clientStackTrace,
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
