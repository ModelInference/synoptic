package synopticgwt.client.invariants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;

/**
 * A pop-up window that contains the mined invariants as a simple text list.
 */
public class TextualInvariantsPopUp extends PopupPanel {

    public TextualInvariantsPopUp(GWTInvariantSet gwtInvs) {
        // PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
        // If this is set, the panel closes itself automatically when the user
        // clicks outside of it.
        super(true);
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("InvariantsPopUp");

        Anchor closeLink = new Anchor("[Close]");
        panel.add(closeLink);
        closeLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                TextualInvariantsPopUp.this.hide();
            }
        });

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
}
