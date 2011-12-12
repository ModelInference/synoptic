package synopticgwt.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A pop-up window that contains the help page.
 */
public class HelpPopUp extends PopupPanel {

    public HelpPopUp() {
        // PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
        // If this is set, the panel closes itself automatically when the user
        // clicks outside of it.
        super(true);
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("HelpPopUp");

        Anchor closeLink = new Anchor("Close");
        panel.add(closeLink);
        closeLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HelpPopUp.this.hide();
            }
        });

        panel.add(new HTML("TODO: help message goes here."));

        this.setWidget(panel);
    }
}
