package synopticgwt.client.invariants;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.SynopticGWT;
import synopticgwt.client.Tab;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;

/**
 * Represents the invariants tab. This tab visualizes the mined invariants, and
 * allows the users to select a subset of the invariants that they would like to
 * be satisfied by the model.
 */
public class InvariantsTab extends Tab<VerticalPanel> {

    public InvariantsTab(ISynopticServiceAsync synopticService,
            ProgressWheel pWheel) {
        super(synopticService, pWheel);
        panel = new VerticalPanel();
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

        // Create and populate the panel with the invariants grid.
        HorizontalPanel tableAndGraphicPanel = new HorizontalPanel();
        panel.add(tableAndGraphicPanel);

        // Iterate through all invariants to add them to the grid / table.
        for (String invType : gwtInvs.getInvTypes()) {
            List<GWTInvariant> invs = gwtInvs.getInvs(invType);

            // Create a grid to contain invariants of invType.
            Grid grid = new Grid(invs.size() + 1, 1);
            grid.setStyleName("invariantsGrid grid");
            grid.setWidget(0, 0, new Label(invType));
            grid.getCellFormatter().setStyleName(0, 0, "topTableCell");
            tableAndGraphicPanel.add(grid);

            for (int i = 0; i < invs.size(); i++) {
                GWTInvariant inv = invs.get(i);
                grid.setWidget(i + 1, 0, new InvariantGridLabel(inv));
                grid.getCellFormatter().setStyleName(i + 1, 0,
                        "tableButtonCell");
            }

            // Add a click handler to the grid that allows users to
            // include/exclude invariants for use by Synoptic.
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
     * <pre>
     * TODO: This class is very similar to the ActivateInvAsyncCallback class.
     *       Refactor their common functionality to a base class.
     * </pre>
     * 
     * Callback method for removing user-specified invariants. Redraws the
     * content in the model and invariants tab.
     */
    class DeactivateInvAsyncCallback implements AsyncCallback<Integer> {
        Grid grid;
        int cIndex;

        public DeactivateInvAsyncCallback(Grid grid, int cIndex) {
            this.grid = grid;
            this.cIndex = cIndex;
        }

        @Override
        public void onFailure(Throwable caught) {
            displayRPCErrorMessage("Remote Procedure Call Failure while removing invariants: "
                    + caught.toString());
        }

        @Override
        public void onSuccess(Integer invHash) {
            InvariantGridLabel invLabel = ((InvariantGridLabel) grid.getWidget(
                    cIndex, 0));

            // The invHash the server echo's back must be correspond to the
            // invariant in the client's grid.
            assert (invLabel.getInvariant().getID() == invHash);

            invLabel.setActivated(false);

            // Signal that the invariant set has changed.
            SynopticGWT.entryPoint.invSetChanged();

            // TODO: update the invariant graph.

            // Change the look of the cell to deactivated.
            CellFormatter cFormatter = grid.getCellFormatter();
            cFormatter.setStyleName(cIndex, 0, "tableCellSelected");
        }
    }

    /**
     * Callback method for removing user-specified invariants. Redraws the
     * content in the model and invariants tab.
     */
    class ActivateInvAsyncCallback implements AsyncCallback<Integer> {
        Grid grid;
        int cIndex;

        public ActivateInvAsyncCallback(Grid grid, int cIndex) {
            this.grid = grid;
            this.cIndex = cIndex;
        }

        @Override
        public void onFailure(Throwable caught) {
            displayRPCErrorMessage("Remote Procedure Call Failure while removing invariants");
        }

        @Override
        public void onSuccess(Integer invHash) {
            InvariantGridLabel invLabel = ((InvariantGridLabel) grid.getWidget(
                    cIndex, 0));

            // The invHash the server echo's back must be correspond to the
            // invariant in the client's grid.
            assert (invLabel.getInvariant().getID() == invHash);

            invLabel.setActivated(true);

            // Signal that the invariant set has changed.
            SynopticGWT.entryPoint.invSetChanged();

            // TODO: update the invariant graph.

            // Change the look of the cell to deactivated.
            CellFormatter cFormatter = grid.getCellFormatter();
            cFormatter.setStyleName(cIndex, 0, "tableButtonCell");
        }
    }

    /**
     * Handler for clicks on the grid showing mined invariants. On click, the
     * grid's cell data will be looked up in the client-side set of invariants.
     * This client-side invariant then contains the server-side hashcode for the
     * corresponding server-side invariant. This hash code is then queued up so
     * that each server-side hash code specifies a server-side invariant for
     * removal. When a cell is "active," this means that it's corresponding
     * invariant is queued up to be removed at the click of the removal button.
     * When one or more cells are active, then the removal button will also be
     * activated. When a cell is deactivated, the corresponding invariant is
     * removed from the queue. If all cells are not active, the removal button
     * will also be deactivated.
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
            // The clicked cell.
            Cell cell = ((Grid) event.getSource()).getCellForEvent(event);

            // Cell's row index.
            int cIndex = cell.getRowIndex();

            // Ignore the title cells.
            if (cIndex == 0) {
                return;
            }

            // Invariant label corresponding to the cell.
            InvariantGridLabel invLabel = ((InvariantGridLabel) grid.getWidget(
                    cIndex, 0));

            int invID = invLabel.getInvariant().getID();

            // Corresponding invariant is activated.
            if (invLabel.getActivated()) {
                // ////////////////////// Call to remote service.
                try {
                    synopticService.deactivateInvariant(invID,
                            new DeactivateInvAsyncCallback(grid, cIndex));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // //////////////////////
                return;
            }

            // Corresponding invariant is deactivated.
            // ////////////////////// Call to remote service.
            try {
                synopticService.activateInvariant(invID,
                        new ActivateInvAsyncCallback(grid, cIndex));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // //////////////////////

        }
    }

}
