package synopticgwt.client.invariants;

import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.Label;

import synopticgwt.shared.GWTInvariant;

/**
 * Represents a label in a grid of invariants. Keeps track of the associated
 * GWTInvariant instance for the label in the grid.
 */
public class InvariantGridLabel extends Label {
	/** Cellformatter for label's corresponding grid cell */
	private CellFormatter cForm;
	/** Row for Label's corresponding grid cell*/
	private int row;
	/** Column for Label's corresponding grid cell*/
	private int col;
    private GWTInvariant inv;
    private GraphicInvariant gInv;
    private boolean active = true;

    public InvariantGridLabel(GWTInvariant inv, CellFormatter cForm, int row, int col) {
        super(inv.getSource() + " "
        		+ inv.getUnicodeTransitionType()
        		+ " " + inv.getTarget());
        this.inv = inv;
        setActive(true);
        this.cForm = cForm;
        this.row = row;
        this.col = col;
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
        this.active = active;
    }
    
    public void mouseOver() {
    	if (gInv != null) {
    		gInv.highlightOn();
    	}
    }
    
    public void mouseOut() {
    	if (gInv != null) {
    		gInv.highlightOff();
    	}
    }
    
    public void highlightOn() {
    	if (active) {
	    	String tType = inv.getTransitionType();
	    	if (tType.equals("NFby")) {
	    		cForm.setStyleName(row, col, "tableCellHighlightRed");
	    	} else if (tType.equals("AP") || tType.equals("AFby")){
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
