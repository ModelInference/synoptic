package synopticgwt.client.model;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ClosingPopUp;

/**
 * A pop-up window that contains the dot file contents that represent the model
 * in GraphViz language.
 */
public class ModelDotPopUp extends ClosingPopUp {

    public ModelDotPopUp(String dotString) {
        super();
        VerticalPanel panel = new VerticalPanel();
        panel.add(this.closeLink);
        panel.add(new HTML("<pre>" + dotString + "</pre>"));
        this.setWidget(panel);
    }

    @Override
    protected void closingPopUpEvent() {
        // No-op.
    }
}
