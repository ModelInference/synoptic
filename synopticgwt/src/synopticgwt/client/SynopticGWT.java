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
 * Implements the visual and interactive components of the SynopticGWT wep-app
 */
public class SynopticGWT implements EntryPoint {
    /**
     * Create an RPC proxy to talk to the Synoptic service
     */
    private final ISynopticServiceAsync synopticService = GWT
            .create(ISynopticService.class);

    TabPanel tabPanel = new TabPanel();

    // TODO: there should be a pWheel for every tab.
    private ProgressWheel pWheel;

    public static SynopticGWT entryPoint;

    private InputTab inputTab;
    private InvariantsTab invTab;
    private ModelTab modelTab;

    /**
     * Entry point method.
     */
    @Override
    public void onModuleLoad() {
        // NOTE: An entry point is instantiated only once and onModuleLoad() is
        // also called once, so there is no issue in storing the entry point
        // reference in a static field. In addition, there is no multi-threading
        // in GWT, via:
        // http://groups.google.com/group/google-web-toolkit/browse_thread/thread/5a2335ffb117bd08
        SynopticGWT.entryPoint = this;

        // Build the page layout.
        RootPanel.get("mainDiv").add(tabPanel);

        // Create a new progress wheel object, and associate it with the
        // progressWheelDiv container.
        pWheel = new ProgressWheel("progressWheelDiv",
                RootPanel.get("progressWheelDiv"));

        inputTab = new InputTab(synopticService, pWheel);

        tabPanel.setWidth("100%");
        tabPanel.add(inputTab.getInputsPanel(), "Inputs");
        tabPanel.selectTab(0);

    }

    public void afterRemovingInvariants(GWTInvariantSet gwtInvs,
            GWTGraph gwtGraph) {
        modelTab.showGraph(gwtGraph);
        invTab.showInvariants(gwtInvs);
        tabPanel.selectTab(2);

        // An error occurs when the tabPanel stays
        // in something other than the model tab when the graph is drawn,
        // so the tab is switched to the graph for drawing, and then back to
        // the invariants tab for now.
        tabPanel.selectTab(1);
    }

    public void parsedOk(GWTInvariantSet gwtInvs, GWTGraph gwtGraph) {
        // Build the invariants tab.
        invTab = new InvariantsTab(synopticService, pWheel);
        tabPanel.add(invTab.getInputsPanel(), "Invariants");
        invTab.showInvariants(gwtInvs);

        // Build the model tab.
        modelTab = new ModelTab(synopticService, pWheel);
        tabPanel.add(modelTab.getInputsPanel(), "Model");
        // The modelTab MUST be made visible for showGraph() to work below.
        tabPanel.selectTab(2);
        modelTab.showGraph(gwtGraph);
    }

}
