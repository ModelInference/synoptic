package synopticgwt.client.model;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ClosingPopUp;

/**
 * A pop-up window that contains the exported PNG of the model. The solution to
 * fitting the image within the height of the browser window is borrowed from
 * here:
 * 
 * <pre>
 * http://stackoverflow.com/questions/6169666/how-to-resize-an-image-to-fit-in-the-browser-window
 * </pre>
 */
public class ModelPngPopUp extends ClosingPopUp {

    public ModelPngPopUp(String fileString) {
        super();
        VerticalPanel panel = new VerticalPanel();
        panel.add(this.closeLink);
        // TODO: 1. Center the image in the div container.
        // 2. Extract all the CSS into the global style file.
        // 3. Catch window resize events and resize the pop-up.
        panel.add(new HTML(
                "<div style=\"height: 100%;\">"
                        + "<img style=\"max-width: 100%; max-height: 100%; display: block; margin: auto;\" src=\"../"
                        + fileString + "\" /></div>"));
        this.setWidget(panel);
    }

    @Override
    protected void closingPopUpEvent() {
        // No-op.
    }
}
