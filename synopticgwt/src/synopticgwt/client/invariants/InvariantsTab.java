package synopticgwt.client.invariants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.SynopticGWT;
import synopticgwt.client.Tab;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.client.util.TooltipListener;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;

/**
 * Represents the invariants tab. This tab visualizes the mined invariants, and
 * allows the users to select a subset of the invariants that they would like to
 * be satisfied by the model.
 */
public class InvariantsTab extends Tab<VerticalPanel> {
    private static final String TOOLTIP_URL = "http://code.google.com/p/synoptic/wiki/DocsWebAppTutorial?ts=1325124406&updated=DocsWebAppTutorial#Invariants_Tab";
    
    public Set<Integer> activeInvsHashes;

    /** Relate GWTInvariants to InvariantGridLabels */
    public Map<GWTInvariant, InvariantGridLabel> gwtInvToGridLabel;

    /** The ordering of invariant types used across all interfaces. */
    public static final String[] invOrdering = { "AP", "AFby", "NFby",
            "ACwith", "NCwith" };

    public InvariantsGraph iGraph;

    HorizontalPanel tableAndGraphicPanel;

    public InvariantsTab(ISynopticServiceAsync synopticService,
            ProgressWheel pWheel) {
        super(synopticService, pWheel, "inv-tab");
        panel = new VerticalPanel();
        gwtInvToGridLabel = new HashMap<GWTInvariant, InvariantGridLabel>();
        iGraph = new InvariantsGraph();
        tableAndGraphicPanel = new HorizontalPanel();
        activeInvsHashes = new LinkedHashSet<Integer>();
    }

    /**
     * Shows the invariant graphic on the screen in the invariantsPanel.
     * 
     * @param gwtInvs
     *            The invariants mined from the log.
     */
    public void showInvariants(final GWTInvariantSet gwtInvs) {
        // Clear the invariants panel of any widgets it might have.
        panel.clear();
        tableAndGraphicPanel.clear();

        Anchor exportInvsLink = new Anchor("[Show invariants as text]");
        panel.add(exportInvsLink);
        exportInvsLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                TextualInvariantsPopUp popUp = new TextualInvariantsPopUp(
                        gwtInvs);
                popUp.setHeight("" + (Window.getClientHeight() / 2) + "px");
                popUp.setGlassEnabled(true);
                popUp.center();
                popUp.show();
            }

        });

        // Populate the panel with the invariants grid.
        panel.add(tableAndGraphicPanel);

        Set<String> invTypes = gwtInvs.getInvTypes();

        // Add the invariant type columns in a specific order.
        for (String invName : invOrdering) {
            if (invTypes.contains(invName)) {
                addInvariantColumnToGrid(invName, gwtInvs);
            }
        }

        String invCanvasId = "invCanvasId";
        HorizontalPanel invGraphicPanel = new HorizontalPanel();
        invGraphicPanel.getElement().setId(invCanvasId);
        invGraphicPanel.setStylePrimaryName("modelCanvas");
        tableAndGraphicPanel.add(invGraphicPanel);
        iGraph.createInvariantsGraphic(gwtInvs, invCanvasId, gwtInvToGridLabel);
        initTooltips();
    }
    
    

    /**
     * Adds a grid column of an invariant type to tableAndGraphicPanel
     * 
     * @param invType
     *            Invariant type of column
     * @param invs
     *            List of invariants
     */
    public void addInvariantColumnToGrid(String invType, GWTInvariantSet gwtInvs) {
        // This creates a grid for each Invariant type with one column
        // and as many rows necessary to contain all of the invariants

        List<GWTInvariant> invs = gwtInvs.getInvs(invType);
        Collections.sort(invs);

        List<GWTInvariant> initialInvs = new LinkedList<GWTInvariant>();

        // Put invariants with an "INITIAL" first event into initialInvs
        // in GWTInvariant.compareTo order
        Iterator<GWTInvariant> gInvIterator = invs.iterator();
        while (gInvIterator.hasNext()) {
            GWTInvariant gInv = gInvIterator.next();
            if (gInv.getSource().equals("INITIAL")) {
                gInvIterator.remove();
                initialInvs.add(gInv);
            }
        }

        // Adds the "INITIAL" invariants to the head of the invariant list
        invs.addAll(0, initialInvs);

        Grid grid = new Grid(invs.size() + 1, 1);
        grid.setStyleName("invariantsGrid grid");
        String longType = "Unknown Invariant Type";
        String unicodeType = GWTInvariant.getUnicodeTransitionType(invType);

        if (invType.equals("AFby")) {
            longType = "AlwaysFollowedBy ( " + unicodeType + " )";
        } else if (invType.equals("AP")) {
            longType = "AlwaysPrecedes ( " + unicodeType + " )";
        } else if (invType.equals("NFby")) {
            longType = "NeverFollowedBy ( " + unicodeType + " )";
        } else if (invType.equals("NCwith")) {
            longType = "NeverConcurrentWith ( " + unicodeType + " )";
        } else if (invType.equals("ACwith")) {
            longType = "AlwaysConcurrentWith ( " + unicodeType + " )";
        }
        grid.setWidget(0, 0, new InvariantGridTitleLabel(longType));
        grid.getCellFormatter().setStyleName(0, 0, "tableCellTopRow");
        tableAndGraphicPanel.add(grid);

        // Activated and deactivated grid cells are uniquely styled
        for (int i = 0; i < invs.size(); i++) {
            GWTInvariant inv = invs.get(i);
            InvariantGridLabel iGridLabel = new InvariantGridLabel(inv,
                    grid.getCellFormatter(), i + 1, 0);
            iGridLabel.addMouseOverHandler(new InvLabelMouseOverHandler());
            iGridLabel.addMouseOutHandler(new InvLabelMouseOutHandler());
            gwtInvToGridLabel.put(inv, iGridLabel);
            grid.setWidget(i + 1, 0, iGridLabel);
            grid.getCellFormatter().setStyleName(i + 1, 0,
                    "tableCellInvActivated");
        }
        
        // Add a click handler to the grid that allows users to
        // include/exclude invariants for use by Synoptic.
        grid.addClickHandler(new InvGridClickHandler(grid, invs));
    }
    
    /**
     * Initialize tooltips.
     */
    private void initTooltips() {
        /* Add tooltip to invariant labels in the table */
        for (int i = 0; i < tableAndGraphicPanel.getWidgetCount() - 1; i++) {
            Grid g = (Grid) tableAndGraphicPanel.getWidget(i);
            InvariantGridTitleLabel l = (InvariantGridTitleLabel) g.getWidget(0, 0);
            if (l.getText().startsWith("AlwaysFollowedBy")) {
                TooltipListener.setTooltip(l, "If event type a is always followed by b (a \u2192 b), then whenever the event type a appears, the event type b always appears later in the same trace. Hover over an invariant to see it highlighted in the invariant graphic. Click on an invariant to activate/de-activate it. If the log is totally ordered, click on the AlwaysFollowedBy (\u2192) header to activate/de-activate all these invariants.", TOOLTIP_URL);
            } else if (l.getText().startsWith("AlwaysPrecedes")) {
                TooltipListener.setTooltip(l, "If event type a always precedes event type b (a \u2190 b), then whenever the event type b appears, the event type a always appears before b in the same trace. Hover over an invariant to see it highlighted in the invariant graphic. Click on an invariant to activate/de-activate it. If the log is totally ordered, click on the AlwaysPrecedes (\u2190) header to activate/de-activate all these invariants.", TOOLTIP_URL);
            } else if (l.getText().startsWith("NeverFollowedBy")) {
                TooltipListener.setTooltip(l, "If event type a is never followed by b (a \u219b b), then whenever the event type a appears, the event type b never appears later in the same trace. Hover over an invariant to see it highlighted in the invariant graphic. Click on an invariant to activate/de-activate it. If the log is totally ordered, click on the NeverFollowedBy (\u219b) header to activate/de-activate all these invariants.", TOOLTIP_URL);
            } else if (l.getText().startsWith("NeverConcurrentWith")) {
                
            } else if (l.getText().startsWith("AlwaysConcurrentWith")) {
                
            }
        }
    }

    /**
     * Handler for clicks on the grid showing mined invariants. On click, the
     * grid's cell data will be looked up in the client-side set of invariants.
     * This client-side invariant then contains the server-side hashcode for the
     * corresponding server-side invariant. If the cell contains an "activated"
     * invariant (an invariant that is used to constrain the model), then an RPC
     * to deactivate the invariant is invoked. Otherwise, the RPC call to
     * activate the invariant is invoked.
     * 
     * @param invs
     *            The set of client-side invariants
     * @param grid
     *            The grid which will become clickable.
     */
    class InvGridClickHandler implements ClickHandler {
        List<GWTInvariant> invs;
        Grid grid;
        CellFormatter cFormatter;
        InvariantGridTitleLabel titleLabel;

        public InvGridClickHandler(Grid grid, List<GWTInvariant> invs) {
            this.invs = invs;
            this.grid = grid;
            this.cFormatter = grid.getCellFormatter();
            this.titleLabel = ((InvariantGridTitleLabel) grid.getWidget(0, 0));
        }

        /** Mark a specific inv cell as deactivated; update activeInvsHashes. */
        private void deactivateCell(int rowID) {
            InvariantGridLabel invLabel = ((InvariantGridLabel) grid.getWidget(
                    rowID, 0));
            int invID = invLabel.getInvariant().getID();
            activeInvsHashes.remove(invID);
            invLabel.setActive(false);
            cFormatter.setStyleName(rowID, 0, "tableCellInvDeactivated");
        }

        /** Mark a specific inv cell as activated; update activeInvsHashes. */
        private void activateCell(int rowID) {
            InvariantGridLabel invLabel = ((InvariantGridLabel) grid.getWidget(
                    rowID, 0));
            int invID = invLabel.getInvariant().getID();
            activeInvsHashes.add(invID);
            invLabel.setActive(true);
            cFormatter.setStyleName(rowID, 0, "tableCellInvActivated");
        }

        /** Mark a title cell as activated. */
        private void activateTitleCell() {
            titleLabel.active = true;
            cFormatter.setStyleName(0, 0, "tableCellTopRow");
        }

        /** Mark a title cell as deactivated. */
        private void deactivateTitleCell() {
            titleLabel.active = false;
            cFormatter.setStyleName(0, 0, "tableCellInvDeactivated");
        }

        @Override
        public void onClick(ClickEvent event) {
            // Do not activate/deactivate invariants if the model tab is
            // disabled (i.e., if no model is currently displayed).
            if (!SynopticGWT.entryPoint.getTabPanel().getTabBar()
                    .isTabEnabled(SynopticGWT.modelTabIndex)) {
                return;
            }

            // The clicked cell.
            Cell cell = ((Grid) event.getSource()).getCellForEvent(event);

            // If the click did not actually hit a cell, then cell will be null.
            if (cell == null) {
                return;
            }

            // Cell's row index.
            int rowID = cell.getRowIndex();

            // This call signals to SynopticGWT that the invariants have
            // changed.
            // TODO: Perhaps there is a better way to communicate this
            // information to the Model tab, perhaps by using a mutation counter
            // in GWTInvariantSet.
            SynopticGWT.entryPoint.invSetChanged();

            // Title cells are special -- they make entire sets of invariants
            // activated/deactivated.
            if (rowID == 0) {
                if (titleLabel.active) {
                    deactivateTitleCell();
                    // Skip the first (title) cell.
                    for (int i = 1; i < grid.getRowCount(); i++) {
                        deactivateCell(i);
                    }
                } else {
                    activateTitleCell();
                    // Skip the first (title) cell.
                    for (int i = 1; i < grid.getRowCount(); i++) {
                        activateCell(i);
                    }
                }
            } else {
                // Invariant label corresponding to the cell.
                InvariantGridLabel invLabel = ((InvariantGridLabel) grid
                        .getWidget(rowID, 0));

                if (invLabel.getActive()) {
                    // Corresponding invariant is activated => deactivate it.
                    deactivateCell(rowID);
                    // If all invariants are deactivated, also deactivate the
                    // title cell.
                    // (Skip the first (title) cell.)
                    deactivateTitleCell();
                    for (int i = 1; i < grid.getRowCount(); i++) {
                        InvariantGridLabel invL = ((InvariantGridLabel) grid
                                .getWidget(i, 0));
                        if (invL.getActive()) {
                            // Nope, some invariant is active -- reset title
                            // cell.
                            activateTitleCell();
                            break;
                        }
                    }
                } else {
                    // Corresponding invariant is deactivated => activate it.
                    activateCell(rowID);
                    // Reset title cell.
                    activateTitleCell();
                }
            }
        }
    }

    /**
     * Highlights corresponding GraphicInvariant on InvariantGridLabel
     * mouseover.
     */
    class InvLabelMouseOverHandler implements MouseOverHandler {
        @Override
        public void onMouseOver(MouseOverEvent event) {
            InvariantGridLabel iGridLabel = (InvariantGridLabel) event
                    .getSource();
            iGridLabel.mouseOver();
        }
    }

    /**
     * Removes highlight from corresponding GraphicInvariant on
     * InvariantGridLabel mouseout.
     */
    class InvLabelMouseOutHandler implements MouseOutHandler {
        @Override
        public void onMouseOut(MouseOutEvent event) {
            InvariantGridLabel iGridLabel = (InvariantGridLabel) event
                    .getSource();
            iGridLabel.mouseOut();
        }
    }
}
