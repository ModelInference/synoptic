package synopticgwt.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A pop-up window that contains the welcome page.
 */
public class WelcomePopUp extends PopupPanel {
    static String noShowCookieName = new String("do-not-show-welcome-screen");

    CheckBox doNotShowAgain;

    /**
     * Checks whether or not the welcome screen should be shown. Returns true if
     * it should be shown, and false if not.
     */
    public static boolean showWelcome() {
        return (Cookies.getCookie(noShowCookieName) == null);
    }

    public WelcomePopUp() {
        // PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
        // If this is set, the panel closes itself automatically when the user
        // clicks outside of it.
        super(true);
        FlowPanel panel = new FlowPanel();
        panel.setStyleName("WelcomePopUp");

        Anchor closeLink = new Anchor("Close");
        closeLink.addStyleName("closePopUpLink");
        panel.add(closeLink);
        closeLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (WelcomePopUp.this.doNotShowAgain.getValue()) {
                    // Create a cookie to remember not to show the welcome
                    // screen again.
                    Cookies.setCookie(noShowCookieName, "");
                }

                WelcomePopUp.this.hide();
            }
        });

        panel.add(new HTML(
                "<h2>Welcome!</h2><p>If you are a new user, then we recommend<br/> that you <a href=\"http://synoptic.googlecode.com/\">learn more</a> about Synoptic and <a href=\"http://code.google.com/p/synoptic/wiki/DocsWebAppTutorial\">this website</a>.</p><br/>"));

        doNotShowAgain = new CheckBox("Do not show this again");
        panel.add(doNotShowAgain);

        this.setWidget(panel);
    }
}
