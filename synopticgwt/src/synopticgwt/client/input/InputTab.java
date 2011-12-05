package synopticgwt.client.input;

import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.Tab;
import synopticgwt.client.util.ProgressWheel;

/**
 * Tab that contains the panel with the input fields and example logs.
 */
public class InputTab extends Tab<VerticalPanel> {
    final InputPanel inputForm = new InputPanel(synopticService);

    public InputTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        super(synopticService, pWheel, "input-tab");

        // This panel will hold inputAndExamplesPanel.
        panel = new VerticalPanel();

        // Add the panel of tabs to the page.
        panel.add(inputForm.getPanel());
    }
}
