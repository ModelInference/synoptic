package synopticgwt.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A pop-up window that has a link to close the pop-up window. This link is
 * created, and its click handler is activated, but it is not associated with
 * any pop-up element -- this needs to be done by sub-classing classes.
 */
public abstract class ClosingPopUp extends PopupPanel {
    protected Anchor closeLink;

    public ClosingPopUp() {
        // PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
        // If this is set, the panel closes itself automatically when the user
        // clicks outside of it.
        super(true);

        closeLink = new Anchor("[Close]");
        closeLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClosingPopUp.this.closingPopUpEvent();
                ClosingPopUp.this.hide();
            }
        });
    }

    /**
     * This method is called whenever the pop-up window is closed. It allows
     * sub-classes to inject addition logic in response to the close event.
     */
    abstract protected void closingPopUpEvent();
}
