package synopticgwt.client.invariants;

import com.google.gwt.user.client.ui.Label;

import synopticgwt.shared.GWTInvariant;

/**
 * Represents a label in a grid of invariants. Keeps track of the associated
 * GWTInvariant instance for the label in the grid.
 */
public class InvariantGridLabel extends Label {
    private GWTInvariant inv;
    private boolean activated;

    public InvariantGridLabel(GWTInvariant inv) {
        super(inv.getSource() + ", " + inv.getTarget());
        this.inv = inv;
        this.activated = true;
    }

    public GWTInvariant getInvariant() {
        return this.inv;
    }

    public boolean getActivated() {
        return activated;
    }

    public void setActivated(boolean newVal) {
        inv.setDisplayed(newVal);
        activated = newVal;
    }
}
