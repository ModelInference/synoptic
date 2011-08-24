package synopticgwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
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

        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                tabSelected(event);
            }
        });
    }

    /**
     * Used by the invariants tab to signal that the user has modified the
     * invariant set, by e.g., activating/deactivating some of the invariants.
     */
    public void invSetChanged() {
        invSetChanged = true;
    }

    /** Called when commit invariants call to the Synoptic service fails. */
    public void commitInvsFailure(Throwable caught) {
        Label error = new Label(
                "Remote Procedure Call Failure while updating invariants: "
                        + caught.toString());
        error.setStyleName("ErrorMessage");
        RootPanel.get("rpcErrorDiv").add(error);
    }

    /** Called when commit invariants call to the Synoptic service succeeds. */
    public void commitInvsSuccess(GWTGraph gwtGraph) {
        invSetChanged = false;
        tabPanel.selectTab(2);
        modelTab.showGraph(gwtGraph);
    }

    /**
     * Fired by SynopticTabPanel whenever a tab is selected.
     */
    public void tabSelected(SelectionEvent<Integer> event) {
        int tabId = event.getSelectedItem();
        if (tabId != 2) {
            return;
        }

        if (!invSetChanged) {
            return;
        }
        // If the invariant set has changed, then we (1) ask the server to re-do
        // refinement/coarsening with the new constraints
        // model, and (2) re-draw everything in the model tab.

        // ////////////////////// Call to remote service.
        try {
            synopticService.commitInvariants(invTab.activeInvsHashes,
                    new AsyncCallback<GWTGraph>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            commitInvsFailure(caught);
                        }

                        @Override
                        public void onSuccess(GWTGraph gwtGraph) {
                            commitInvsSuccess(gwtGraph);
                        }
                    });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // //////////////////////
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
