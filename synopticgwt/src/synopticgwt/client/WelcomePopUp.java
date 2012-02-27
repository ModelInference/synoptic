package synopticgwt.client;

import java.util.Date;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * A pop-up window that contains the welcome page.
 */
public class WelcomePopUp extends ClosingPopUp {
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
        super();
        doNotShowAgain = new CheckBox("Do not show this again");
        closeLink.addStyleName("closePopUpLink");

        FlowPanel panel = new FlowPanel();
        panel.setStyleName("WelcomePopUp");
        panel.add(closeLink);
        panel.add(new HTML(
                "<h2>Welcome!</h2><p>If you are a new user, then we recommend<br/> that you <a href=\"http://synoptic.googlecode.com/\">learn more</a> about Synoptic and <a href=\"http://code.google.com/p/synoptic/wiki/DocsWebAppTutorial\">this website</a>.</p><br/>"));
        panel.add(doNotShowAgain);
        this.setWidget(panel);
    }

    @Override
    protected void closingPopUpEvent() {
        if (this.doNotShowAgain.getValue()) {
            // Create a cookie to remember not to show the welcome
            // screen again (expires 1 year from now).
            Date expireDate = new Date();
            expireDate.setTime(expireDate.getTime() + 31556926);
            Cookies.setCookie(noShowCookieName, "", expireDate);
        }
    }
}
