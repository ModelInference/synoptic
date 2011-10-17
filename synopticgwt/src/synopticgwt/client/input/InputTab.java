package synopticgwt.client.input;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.Tab;
import synopticgwt.client.util.ProgressWheel;

/**
 * Represents the tab that holds both the log/re inputs panel and the example buttons
 * panel. Text inputs panel is to the left and example buttons panel is to the right.
 * When an example button is clicked, it loads associated value into log/re inputs.
 */
public class InputTab extends Tab<VerticalPanel> {
    /** This contains panel to enter text input and panel containing example buttons. */
    HorizontalPanel inputAndExamplesPanel = new HorizontalPanel();
    
    final InputForm inputForm = new InputForm(synopticService);
    final Grid examplesGrid = new Grid(5, 1);
    private Anchor prevAnchor = null;

    public InputTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        super(synopticService, pWheel);

        // This panel will hold inputAndExamplesPanel.
        panel = new VerticalPanel();
        
        // Add the panel of tabs to the page.
        panel.add(inputAndExamplesPanel);
        
        // Sets up label for log examples panel.
        Label loadExamples = new Label("Load example logs");
        loadExamples.setStyleName("LoadExampleLabel");
        examplesGrid.setWidget(0, 0, loadExamples);
        
        // Sets up and adds buttons to panel.
        InputExample[] inputExamples = InputExample.values();
        for (int i = 0; i < inputExamples.length; i++) {
        	// Create anchor for every InputExample enum.
        	Anchor exampleLink = new Anchor(inputExamples[i].getName());
        	
        	// Associate click listener to anchors.
        	exampleLink.addClickHandler(new ExampleLinkHandler());
        	examplesGrid.setWidget((i + 1), 0, exampleLink);
            examplesGrid.getCellFormatter().setStyleName((i + 1), 0, "tableCell");
        }
        
        examplesGrid.setStyleName("inputForm");
        inputAndExamplesPanel.add(examplesGrid);
        inputAndExamplesPanel.add(inputForm.getPanel());
    }
    
    /**
     * 	Handles clicks on example log anchors. Loads the associated log/re content into
     *  the text areas and text boxes to the left. Sets an anchor as unclickable after
     *  being clicked.
     */
    class ExampleLinkHandler implements ClickHandler {
    	
		//TODO optimize - don't use two for loops
		@Override
		public void onClick(ClickEvent event) {
	        InputExample[] inputExamples = InputExample.values();
	        // Convert from label to anchor for previously clicked
	        for (int j = 1; j < examplesGrid.getRowCount(); j++) {
	        	if (prevAnchor != null && examplesGrid.getWidget(j,0) instanceof Label) {
					examplesGrid.getWidget(j,0).removeFromParent();
					examplesGrid.setWidget(j,0,prevAnchor);
				}
	        }
	        // Convert from anchor to label for currently clicked
			for (int i = 1; i < examplesGrid.getRowCount(); i++) {
				if (event.getSource() == examplesGrid.getWidget(i, 0)) {
					InputExample currExample = inputExamples[i-1];
					inputForm.setInputs(currExample.getLogText(), currExample.getRegExpText(), 
							currExample.getPartitionRegExpText(), currExample.getSeparatorRegExpText());
					prevAnchor = (Anchor) examplesGrid.getWidget(i,0);
					examplesGrid.getWidget(i, 0).removeFromParent();
					//TODO make flyweight??
					examplesGrid.setWidget(i, 0, new Label(prevAnchor.getText()));
				}
			}	
		}	
    }
}
