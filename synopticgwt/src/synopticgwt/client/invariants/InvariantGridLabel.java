package synopticgwt.client.invariants;

import com.google.gwt.user.client.ui.Label;

import synopticgwt.shared.GWTInvariant;

/**
 * Represents a label in a grid of invariants. Keeps track of the associated
 * GWTInvariant instance for the label in the grid.
 */
public class InvariantGridLabel extends Label {
    private GWTInvariant inv;
    private GraphicInvariant gInv;

    public InvariantGridLabel(GWTInvariant inv) {
        super(inv.getSource() + " "
        		+ inv.getUnicodeTransitionType()
        		+ " " + inv.getTarget());
        this.inv = inv;
        setActive(true);
    }
    
	public GWTInvariant getInvariant() {
		return inv;
	}

    public boolean getActive() {
        return inv.getActive();
    }
    
    public void setGraphicInvariant(GraphicInvariant gInv) {
    	this.gInv = gInv;
    }

    public void setActive(boolean active) {
    	if (gInv != null) {
    		if (active) {
    			gInv.show();
    		} else {
    			gInv.hide();
    		}
    	}
        inv.setActive(active);
    }
    
    public void mouseOver() {
    	gInv.highlightOn();
    }
    
    public void mouseOut() {
    	gInv.highlightOff();
    }

}
