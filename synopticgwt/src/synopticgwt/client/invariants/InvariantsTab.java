package synopticgwt.client.invariants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.SynopticGWT;
import synopticgwt.client.Tab;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;

/**
 * Represents the invariants tab. This tab visualizes the mined invariants, and
 * allows the users to select a subset of the invariants that they would like to
 * be satisfied by the model.
 */
public class InvariantsTab extends Tab<VerticalPanel> {
    private final HorizontalPanel buttonPanel = new HorizontalPanel();

    // List of hash codes to be removed from the server's set of invariants.
    // Each hash code represents a temporal invariant.
    private final Set<Integer> invariantRemovalIDs = new HashSet<Integer>();
    private final Button invRemoveButton = new Button("Remove Invariants");

    public InvariantsTab(ISynopticServiceAsync synopticService,
            ProgressWheel pWheel) {
        super(synopticService, pWheel);
        panel = new VerticalPanel();

        // Set up invariants tab.
        panel.add(buttonPanel);
        buttonPanel.add(invRemoveButton);
        buttonPanel.setStyleName("buttonPanel");
        invRemoveButton.addClickHandler(new RemoveInvariantsHandler());
        invRemoveButton.setWidth("188px");
        invRemoveButton.setEnabled(false);
    }

    /**
     * Shows the invariant graphic on the screen in the invariantsPanel.
     * 
     * @param gwtInvs
     *            The invariants mined from the log.
     */
    public void showInvariants(GWTInvariantSet gwtInvs) {
        // Clear the invariants panel of the non-button widget
        // (the horizontal panel for the table and graphics).
        if (panel.getWidgetCount() > 1) {
            panel.remove(panel.getWidget(1));
            assert (panel.getWidgetCount() == 1);
        }

        // Create and populate the panel with the invariants table and the
        // invariants graphic.
        HorizontalPanel tableAndGraphicPanel = new HorizontalPanel();
        panel.add(tableAndGraphicPanel);

        Set<String> invTypes = gwtInvs.getInvTypes();

        // Iterate through all invariants to add them to the grid / table.
        for (String invType : invTypes) {
            final List<GWTInvariant> invs = gwtInvs.getInvs(invType);

            final Grid grid = new Grid(invs.size() + 1, 1);
            tableAndGraphicPanel.add(grid);

            grid.setWidget(0, 0, new Label(invType));
            grid.getCellFormatter().setStyleName(0, 0, "topTableCell");

            int i = 1;
            for (GWTInvariant inv : invs) {
                grid.setWidget(i, 0,
                        new Label(inv.getSource() + ", " + inv.getTarget()));
                i += 1;
            }

            grid.setStyleName("invariantsGrid grid");
            for (i = 1; i < grid.getRowCount(); i++) {
                grid.getCellFormatter().setStyleName(i, 0, "tableButtonCell");
            }

            // Add a click handler to the grid that allows users to
            // include/exclude invariants from use by Synoptic.
            grid.addClickHandler(new InvGridClickHandler(grid, invs));
        }

        // Show the invariant graphic.
        String invCanvasId = "invCanvasId";
        HorizontalPanel invGraphicPanel = new HorizontalPanel();
        invGraphicPanel.getElement().setId(invCanvasId);
        invGraphicPanel.setStylePrimaryName("modelCanvas");
        tableAndGraphicPanel.add(invGraphicPanel);
        InvariantsGraph.createInvariantsGraphic(gwtInvs, invCanvasId);
    }

    /**
     * Accesses the list of copies of server-side invariant hash codes and uses
     * them to remove their corresponding server-side invariants.
     */
    class RemoveInvariantsHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            // Keep the user from clicking the button multiple times.
            invRemoveButton.setEnabled(false);

            // ////////////////////// Call to remote service.
            try {
                synopticService.removeInvs(invariantRemovalIDs,
                        new RemoveInvariantsAsyncCallback());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // //////////////////////

            // Empty the set of invariants to remove.
            // TODO: If the server fails to remove the invariants then we lose
            // the set of invariants the user wants to remove. Ideally, we would
            // keep this set until we know that the server has removed them --
            // this however, requires that the server can deal with cales to
            // removeInvs() with invariants that have already been removed
            // (i.e., the call must be idempotent w.r.t an invariant).
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
            displayRPCErrorMessage("Remote Procedure Call Failure while removing invariants");
        }

        /**
         * Redraws the model and invariants tabs.
         */
        @Override
        public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> ret) {
            // Communicate with the model tab and this invariants tab via
            // SynopticGWT.
            SynopticGWT.entryPoint.afterRemovingInvariants(ret.getLeft(),
                    ret.getRight());
        }

    }

    /**
     * This is a handler for clicks on the grid showing mined invariants. On
     * click, the grid's cell data will be looked up in the client-side set of
     * invariants. This client-side invariant then contains the server-side
     * hashcode for the corresponding server-side invariant. This hash code is
     * then queued up so that each server-side hash code specifies a server-side
     * invariant for removal. When a cell is "active," this means that it's
     * corresponding invariant is queued up to be removed at the click of the
     * removal button. When one or more cells are active, then the removal
     * button will also be activated. When a cell is deactivated, the
     * corresponding invariant is removed from the queue. If all cells are not
     * active, the removal button will also be deactivated.
     * 
     * @param invs
     *            The set of client-side invariants
     * @param grid
     *            The grid which will become clickable.
     */
    class InvGridClickHandler implements ClickHandler {
        List<GWTInvariant> invs;
        Grid grid;

        public InvGridClickHandler(Grid grid, List<GWTInvariant> invs) {
            this.invs = invs;
            this.grid = grid;
        }

        // Add the aforementioned functionality to the click handler.
        @Override
        public void onClick(ClickEvent event) {
            // Identify the clicked cell.
            HTMLTable.Cell cell = ((Grid) event.getSource())
                    .getCellForEvent(event);

            // Check to see (from the row index), whether the cell clicked
            // is the top (zeroth) cell. This shouldn't be activated, as it
            // is the
            // column title.
            int cellRowIndex = cell.getRowIndex();
            if (cellRowIndex == 0) {
                return;
            }

            // Extract the cell data from the grid's cell.
            // TODO: This is likely an ineffective way of doing this,
            // as the invariants on the left and right may not be
            // separated by a
            // comma. They also may have more than just a single comma
            // in the
            // entire string.
            String[] cellData = cell.getElement().getInnerText().split(", ", 2);

            // Create an invariant to be looked up in the client-side
            // list.
            GWTInvariant invFromCell = new GWTInvariant(cellData[0],
                    cellData[1], "");
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
                formatter.setStyleName(cellRowIndex, 0, "tableCellSelected");
                invariantRemovalIDs.add(invID);

                // Activate the removal button
                invRemoveButton.setEnabled(true);
            } else {

                // Deactivate the cell and remove the hash code from the
                // queue.
                formatter.setStyleName(cellRowIndex, 0, "tableButtonCell");
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

}
