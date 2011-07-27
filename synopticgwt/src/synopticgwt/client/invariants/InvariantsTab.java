package synopticgwt.client.invariants;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.SynopticGWT;
import synopticgwt.client.util.JsniUtil;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;

public class InvariantsTab {
    private ISynopticServiceAsync synopticService;
    private ProgressWheel pWheel;

    // Invariants tab widgets:
    private final VerticalPanel invariantsPanel = new VerticalPanel();
    private final HorizontalPanel invariantsButtonPanel = new HorizontalPanel();

    // List of hash codes to be removed from the server's set of invariants.
    // Each hash code represents a temporal invariant.
    private final Set<Integer> invariantRemovalIDs = new HashSet<Integer>();
    private final Button invRemoveButton = new Button("Remove Invariants");

    public InvariantsTab(ISynopticServiceAsync synopticService,
            ProgressWheel pWheel) {
        this.synopticService = synopticService;
        this.pWheel = pWheel;

        // Set up invariants tab.
        invariantsPanel.add(invariantsButtonPanel);
        invariantsButtonPanel.add(invRemoveButton);
        invariantsButtonPanel.setStyleName("buttonPanel");
        invRemoveButton.addClickHandler(new RemoveInvariantsHandler());
        invRemoveButton.setWidth("188px");
        invRemoveButton.setEnabled(false);
    }

    public VerticalPanel getInputsPanel() {
        return invariantsPanel;
    }

    /**
     * Injects an error message at the top of the page when an RPC call fails
     */
    public void injectRPCError(String message) {
        Label error = new Label(message);
        error.setStyleName("ErrorMessage");
        RootPanel.get("rpcErrorDiv").add(error);
    }

    /**
     * Shows the invariant graphic on the screen in the invariantsPanel
     * 
     * @param graph
     */
    public void showInvariants(GWTInvariantSet gwtInvs) {
        // Clear the invariants panel of the non-button widget
        // (the horizontal panel for the table and graphics).
        if (invariantsPanel.getWidgetCount() > 1) {
            invariantsPanel.remove(invariantsPanel.getWidget(1));
            assert (invariantsPanel.getWidgetCount() == 1);
        }

        // Create and populate the panel with the invariants table and the
        // invariants graphic.
        HorizontalPanel tableAndGraphicPanel = new HorizontalPanel();
        invariantsPanel.add(tableAndGraphicPanel);

        Set<String> invTypes = gwtInvs.getInvTypes();
        int eTypesCnt = 0;
        JavaScriptObject eventTypesJS = JavaScriptObject.createArray();
        JavaScriptObject AFbyJS = JavaScriptObject.createArray();
        JavaScriptObject NFbyJS = JavaScriptObject.createArray();
        JavaScriptObject APJS = JavaScriptObject.createArray();
        Set<String> eventTypes = new LinkedHashSet<String>();
        int longestEType = 0;

        // Iterate through all invariants to (1) add them to the grid / table,
        // and (2) to create the JS objects for drawing the invariants graphic.
        for (String invType : invTypes) {
            final List<GWTInvariant<String, String>> invs = gwtInvs
                    .getInvs(invType);

            final Grid grid = new Grid(invs.size() + 1, 1);
            tableAndGraphicPanel.add(grid);

            grid.setWidget(0, 0, new Label(invType));
            grid.getCellFormatter().setStyleName(0, 0, "topTableCell");

            int i = 1;
            for (GWTInvariant<String, String> inv : invs) {
                if (!eventTypes.contains(inv.getSource())) {
                    JsniUtil.pushArray(eventTypesJS, inv.getSource());
                    eventTypes.add(inv.getSource());
                    if (inv.getSource().length() > longestEType) {
                        longestEType = inv.getSource().length();
                    }
                    eTypesCnt++;
                }
                if (!eventTypes.contains(inv.getTarget())) {
                    JsniUtil.pushArray(eventTypesJS, inv.getTarget());
                    eventTypes.add(inv.getTarget());
                    if (inv.getTarget().length() > longestEType) {
                        longestEType = inv.getTarget().length();
                    }
                    eTypesCnt++;
                }

                String x = inv.getSource();
                String y = inv.getTarget();
                if (invType.equals("AFby")) {
                    JsniUtil.addToKeyInArray(AFbyJS, x, y);
                } else if (invType.equals("NFby")) {
                    JsniUtil.addToKeyInArray(NFbyJS, x, y);
                } else if (invType.equals("AP")) {
                    JsniUtil.addToKeyInArray(APJS, x, y);
                }

                grid.setWidget(i, 0,
                        new Label(inv.getSource() + ", " + inv.getTarget()));
                i += 1;
            }

            grid.setStyleName("invariantsGrid grid");
            for (i = 1; i < grid.getRowCount(); i++) {
                grid.getCellFormatter().setStyleName(i, 0, "tableButtonCell");
            }

            // Allow the user to toggle invariants on the grid.
            addInvariantToggleHandler(grid, invs);

        }

        // Show the invariant graphic.
        String invCanvasId = "invCanvasId";
        HorizontalPanel invGraphicId = new HorizontalPanel();
        invGraphicId.getElement().setId(invCanvasId);
        invGraphicId.setStylePrimaryName("modelCanvas");
        tableAndGraphicPanel.add(invGraphicId);

        // A little magic to size things right.
        int lX = (longestEType * 30) / 2 - 60;
        int mX = lX + (longestEType * 30);
        int rX = mX + (longestEType * 30);
        int width = rX + 50;

        InvariantsGraph.createInvariantsGraphic(AFbyJS, NFbyJS, APJS,
                eventTypesJS, width, (eTypesCnt + 1) * 50, lX, mX, rX,
                invCanvasId);
    }

    /**
     * Accesses the list of copies of server-side invariant hash codes and uses
     * them to remove their corresponding server-side invariants. The
     * client-side graph and invariants are then recalculated and redisplayed.
     */
    class RemoveInvariantsHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {

            // Keep the user from clicking the button multiple times.
            invRemoveButton.setEnabled(false);

            // Remove the invariants from the server based on
            // the hash code copies, then recalculate the graph
            // and invariants so they can be redrawn in their respective
            // panels.
            try {
                synopticService.removeInvs(invariantRemovalIDs,
                        new RemoveInvariantsAsyncCallback());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Since the invariants have been removed, the queue should be
            // emptied.
            invariantRemovalIDs.clear();
        }
    }

    /**
     * Callback method for removing user-specified invariants. Redraws the
     * content in the model and invariants tab.
     */
    class RemoveInvariantsAsyncCallback implements
            AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> {

        /**
         * Handles any general problems that may arise.
         */
        @Override
        public void onFailure(Throwable caught) {
            injectRPCError("Remote Procedure Call Failure while removing invariants");
        }

        /**
         * Redraws the model and invariants tabs.
         */
        @Override
        public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> result) {
            GWTInvariantSet gwtInvs = result.getLeft();
            GWTGraph gwtGraph = result.getRight();
            SynopticGWT.entryPoint.afterRemovingInvariants(gwtInvs, gwtGraph);
        }

    }

    /**
     * This makes the grid clickable, so that when clicked, the grid's cell data
     * will be looked up in the client-side set of invariants. This client-side
     * invariant then contains the server-side hashcode for the corresponding
     * server-side invariant. This hash code is then queued up so that each
     * server-side hash code specifies a server-side invariant for removal. When
     * a cell is "active," this means that it's corresponding invariant is
     * queued up to be removed at the click of the removal button. When one or
     * more cells are active, then the removal button will also be activated.
     * When a cell is deactivated, the corresponding invariant is removed from
     * the queue. If all cells are not active, the removal button will also be
     * deactivated.
     * 
     * @param invs
     *            The set of client-side invariants
     * @param grid
     *            The grid which will become clickable.
     */
    private void addInvariantToggleHandler(final Grid grid,
            final List<GWTInvariant<String, String>> invs) {

        // Add the basic click handler to the graph.
        grid.addClickHandler(new ClickHandler() {

            // Add the aforementioned functionality to the click handler.
            @Override
            public void onClick(ClickEvent event) {

                // Specify which cell was clicked.
                HTMLTable.Cell cell = ((Grid) event.getSource())
                        .getCellForEvent(event);

                // Check to see (from the row index), whether the cell clicked
                // is the top (zeroth) cell. This shouldn't be activated, as it
                // is the
                // column title.
                int cellRowIndex = cell.getRowIndex();
                if (cellRowIndex > 0) {
                    // Extract the cell data from the grid's cell.
                    // TODO: This is likely an ineffective way of doing this,
                    // as the invariants on the left and right may not be
                    // separated by a
                    // comma. They also may have more than just a single comma
                    // in the
                    // entire string.
                    String[] cellData = cell.getElement().getInnerText()
                            .split(", ", 2);

                    // Create an invariant to be looked up in the client-side
                    // list.
                    GWTInvariant<String, String> invFromCell = new GWTInvariant<String, String>(
                            cellData[0], cellData[1], "");
                    int matchingIndex = invs.indexOf(invFromCell);

                    // Extract a copy of the server-side's invariant hash code
                    // (the invariant's ID).
                    int invID = invs.get(matchingIndex).getID();

                    // Check whether the cell is active (style of
                    // "tableButtonCell")
                    // or not (style of "tableCellSelected").
                    CellFormatter formatter = grid.getCellFormatter();
                    if (formatter.getStyleName(cellRowIndex, 0).equals(
                            "tableButtonCell")) {

                        // Activate the cell and queue up the hash code.
                        formatter.setStyleName(cellRowIndex, 0,
                                "tableCellSelected");
                        invariantRemovalIDs.add(invID);

                        // Activate the removal button
                        invRemoveButton.setEnabled(true);
                    } else {

                        // Deactivate the cell and remove the hash code from the
                        // queue.
                        formatter.setStyleName(cellRowIndex, 0,
                                "tableButtonCell");
                        invariantRemovalIDs.remove(invID);

                        // Deactivate the removal button if there are no
                        // invariants
                        // queued up.
                        if (invariantRemovalIDs.isEmpty()) {
                            invRemoveButton.setEnabled(false);
                        }
                    }
                }
            }
        });
    }
}
