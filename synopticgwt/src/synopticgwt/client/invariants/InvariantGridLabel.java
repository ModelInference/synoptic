package synopticgwt.client.invariants;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.Label;

import synopticgwt.client.invariants.model.Invariant;
import synopticgwt.shared.GWTInvariant;

/**
 * Represents a label in a grid of invariants. Keeps track of the associated
 * GWTInvariant instance for the label in the grid so the label can be clicked
 * on/off to activate/deactivate the invariant from being considered in model
 * construction (e.g., refinement/coarsening)
 */
public class InvariantGridLabel extends Label {
    /** Cellformatter for label's corresponding grid cell */
    private CellFormatter cForm;
    /** Row for Label's corresponding grid cell */
    private int row;
    /** Column for Label's corresponding grid cell */
    private int col;
    private GWTInvariant inv;
    /*
     * This is a list so we can point to multiple GraphicConcurrentInvariants
     */
    private List<Invariant> gInvs;
    // T.101.JV: Activation state is stored in the invLabel, and should
    // probably instead be stored in the GWTInvariant
    private boolean active = true;

    public InvariantGridLabel(GWTInvariant inv, CellFormatter cForm, int row,
            int col) {
        super(inv.getSource() + " " + inv.getUnicodeTransitionType() + " "
                + inv.getTarget());
        this.inv = inv;
        this.cForm = cForm;
        this.row = row;
        this.col = col;
        this.gInvs = new ArrayList<Invariant>();
        setActive(true);
    }

    public GWTInvariant getInvariant() {
        return inv;
    }

    public boolean getActive() {
        return inv.getActive();
    }

    public void setGraphicInvariant(Invariant gInv) {
        gInvs.add(gInv);
    }

    public void setActive(boolean active) {
        if (!gInvs.isEmpty()) {
            if (active) {
                for (Invariant gInv : gInvs) {
                    gInv.show();
                }
            } else {
                for (Invariant gInv : gInvs) {
                    gInv.hide();
                }
            }
        }
        inv.setActive(active);
        this.active = active;
    }

    public void mouseOver() {
        if (!gInvs.isEmpty()) {
            for (Invariant gInv : gInvs) {
                gInv.highlightOn();
            }
        }
    }

    public void mouseOut() {
        if (!gInvs.isEmpty()) {
            for (Invariant gInv : gInvs) {
                gInv.highlightOff();
            }
        }
    }

    /**
     * Note: highlighting only works for NFby/AP/AFby invariants
     */
    public void highlightOn() {
        if (active) {
            String tType = inv.getTransitionType();
            if (tType.equals("NFby") || tType.equals("NCwith")) {
                cForm.setStyleName(row, col, "tableCellHighlightRed");
            } else if (tType.equals("AP") || tType.equals("AFby")
                    || tType.equals("ACwith")) {
                cForm.setStyleName(row, col, "tableCellHighlightBlue");
            }
        }
    }

    public void highlightOff() {
        if (active) {
            cForm.setStyleName(row, col, "tableCellInvActivated");
        } else {
            cForm.setStyleName(row, col, "tableCellInvDeactivated");
        }
    }

}
