package synopticgwt.client.input;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
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

    public InputTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        super(synopticService, pWheel);

        // This panel will hold inputAndExamplesPanel.
        panel = new VerticalPanel();
        
        inputAndExamplesPanel.add(inputForm.getPanel());

        // Add the panel of tabs to the page.
        panel.add(inputAndExamplesPanel);
        
        // Sets up label for log examples panel.
        Label loadExamples = new Label("Load example logs");
        loadExamples.setStyleName("LoadExampleLabel");
        examplesGrid.setWidget(0, 0, loadExamples);
        
        // Sets up and adds buttons to panel.
        InputExample[] inputExamples = InputExample.values();
        for (int i = 0; i < inputExamples.length; i++) {
        	// Create a button for every InputExample enum.
        	Button exampleButton = new Button(inputExamples[i].getName());
        	
        	// Associate click listener to buttons.
        	exampleButton.addClickHandler(new ExampleButtonHandler());
        	examplesGrid.setWidget((i + 1), 0, exampleButton);
            examplesGrid.getCellFormatter().setStyleName((i + 1), 0, "tableCell");
        }
        
        examplesGrid.setStyleName("inputForm");
        inputAndExamplesPanel.add(examplesGrid);
    }
    
    /**
     * 	Handles clicks on example log buttons. Loads the associated log/re content into
     *  the text areas and text boxes to the left.
     */
    class ExampleButtonHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
	        InputExample[] inputExamples = InputExample.values();
			for (int i = 1; i < examplesGrid.getRowCount(); i++) {
				Button currButton = (Button) examplesGrid.getWidget(i, 0);
				if (event.getSource() == currButton) {
					InputExample currExample = inputExamples[i-1];
					inputForm.setInputs(currExample.getLogText(), currExample.getRegExpText(), 
							currExample.getPartitionRegExpText(), currExample.getSeparatorRegExpText());
				}
			}	
		}	
    }
}
