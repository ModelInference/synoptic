package synopticgwt.client.invariants;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ClosingPopUp;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;

/**
 * A pop-up window that contains the mined invariants as a simple text list.
 */
public class TextualInvariantsPopUp extends ClosingPopUp {

    public TextualInvariantsPopUp(GWTInvariantSet gwtInvs) {
        super();
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("InvariantsPopUp");
        panel.add(this.closeLink);
        panel.add(new HTML(invariantsToText(gwtInvs)));
        this.setWidget(panel);
    }

    /**
     * Converts a set of invariants to a text list, with line breaks in between
     * each invariant.
     */
    private String invariantsToText(GWTInvariantSet invs) {
        String ret = "";

        for (String iType : InvariantsTab.invOrdering) {
            if (!invs.getInvTypes().contains(iType)) {
                continue;
            }
            for (GWTInvariant inv : invs.getInvs(iType)) {
                ret += inv.getSource() + " " + iType + " " + inv.getTarget()
                        + "<br />";
            }
        }
        return ret;
    }

    @Override
    protected void closingPopUpEvent() {
        // No-op.
    }
}
