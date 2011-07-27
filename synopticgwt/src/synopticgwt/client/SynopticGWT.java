package synopticgwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

import synopticgwt.client.input.InputTab;
import synopticgwt.client.invariants.InvariantsTab;
import synopticgwt.client.model.ModelTab;
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
    /** Create an RPC proxy to talk to the Synoptic service */
    private final ISynopticServiceAsync synopticService = GWT
            .create(ISynopticService.class);

    /** This contains the three main application tabs. */
    TabPanel tabPanel = new TabPanel();

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

        // Add the panel of tabs to the page.
        RootPanel.get("mainDiv").add(tabPanel);

        // Create a new progress wheel object, and associate it with the
        // progressWheelDiv container.
        pWheel = new ProgressWheel("progressWheelDiv");

        // Create the tab on which the user will submit inputs to Synoptic.
        inputTab = new InputTab(synopticService, pWheel);

        // Associate the tab with the tab panel.
        tabPanel.add(inputTab.getPanel(), "Inputs");
        tabPanel.setWidth("100%");
        tabPanel.selectTab(0);
    }

    public void afterRemovingInvariants(GWTInvariantSet gwtInvs,
            GWTGraph gwtGraph) {
        tabPanel.selectTab(2);
        modelTab.showGraph(gwtGraph);
        invTab.showInvariants(gwtInvs);

        // An error occurs when the tabPanel stays
        // in something other than the model tab when the graph is drawn,
        // so the tab is switched to the graph for drawing, and then back to
        // the invariants tab for now.
        tabPanel.selectTab(1);
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
        // If this is the first time a log was parsed then create a new
        // invariants and model tabs.
        if (invTab == null) {
            assert (modelTab == null);
            invTab = new InvariantsTab(synopticService, pWheel);
            tabPanel.add(invTab.getPanel(), "Invariants");
            modelTab = new ModelTab(synopticService, pWheel);
            tabPanel.add(modelTab.getPanel(), "Model");
        }
        assert (modelTab != null);
        invTab.showInvariants(logInvs);
        // The modelTab MUST be made visible for showGraph() to work below.
        tabPanel.selectTab(2);
        modelTab.showGraph(initialModel);
    }
}
