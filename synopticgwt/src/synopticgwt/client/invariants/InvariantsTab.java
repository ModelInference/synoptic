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
        // Clear the invariants panel of any widgets it might have.
        panel.clear();

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
            grid.getCellFormatter().setStyleName(0, 0, "tableCellTopRow");
            tableAndGraphicPanel.add(grid);

            for (int i = 0; i < invs.size(); i++) {
                GWTInvariant inv = invs.get(i);
                grid.setWidget(i + 1, 0, new InvariantGridLabel(inv));
                grid.getCellFormatter().setStyleName(i + 1, 0,
                        "tableCellInvActivated");
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
     * Callback method for adding/removing user-specified invariants.
     */
    class AddRemoveInvAsyncCallback implements AsyncCallback<Integer> {
        Grid grid;
        int cIndex;
        boolean activateInv;

        /**
         * Creates a new callback that will execute when the result of an RPC to
         * activate/deactivate an invariant returns.
         * 
         * @param grid
         *            The grid object containing invariant labels
         * @param cIndex
         *            The column index of the invariant label in the grid this
         *            RPC call references.
         * @param activateInv
         *            Whether or not the invariant is being added/activated or
         *            removed/deactivated.
         */
        public AddRemoveInvAsyncCallback(Grid grid, int cIndex,
                boolean activateInv) {
            this.grid = grid;
            this.cIndex = cIndex;
            this.activateInv = activateInv;
        }

        @Override
        public void onFailure(Throwable caught) {
            displayRPCErrorMessage("Remote Procedure Call Failure while adding/removing an invariant: "
                    + caught.toString());
        }

        @Override
        public void onSuccess(Integer invHash) {
            InvariantGridLabel invLabel = ((InvariantGridLabel) grid.getWidget(
                    cIndex, 0));

            // The invHash the server echo's back must be correspond to the
            // invariant in the client's grid.
            assert (invLabel.getInvariant().getID() == invHash);

            invLabel.setActivated(activateInv);

            // Signal that the invariant set has changed.
            SynopticGWT.entryPoint.invSetChanged();

            // Change the look of the cell.
            CellFormatter cFormatter = grid.getCellFormatter();
            if (activateInv) {
                cFormatter.setStyleName(cIndex, 0, "tableCellInvActivated");
            } else {
                cFormatter.setStyleName(cIndex, 0, "tableCellInvDeactivated");
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

        public InvGridClickHandler(Grid grid, List<GWTInvariant> invs) {
            this.invs = invs;
            this.grid = grid;
        }

        @Override
        public void onClick(ClickEvent event) {
            // The clicked cell.
            Cell cell = ((Grid) event.getSource()).getCellForEvent(event);

            // Cell's row index.
            int cIndex = cell.getRowIndex();

            // Ignore title cells.
            if (cIndex == 0) {
                return;
            }

            // Invariant label corresponding to the cell.
            InvariantGridLabel invLabel = ((InvariantGridLabel) grid.getWidget(
                    cIndex, 0));

            int invID = invLabel.getInvariant().getID();

            // Corresponding invariant is activated => deactive it.
            if (invLabel.getActivated()) {
                // ////////////////////// Call to remote service.
                try {
                    synopticService.deactivateInvariant(invID,
                            new AddRemoveInvAsyncCallback(grid, cIndex, false));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // //////////////////////
                return;
            }

            // Corresponding invariant is deactivated => activate it.
            // ////////////////////// Call to remote service.
            try {
                synopticService.activateInvariant(invID,
                        new AddRemoveInvAsyncCallback(grid, cIndex, true));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // //////////////////////
        }
    }
}
