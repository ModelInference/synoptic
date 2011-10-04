package synopticgwt.client.input;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.SynopticGWT;
import synopticgwt.client.Tab;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.SerializableParseException;

public class InputSubTab extends Tab<VerticalPanel> {
    private static final String UPLOAD_ACTION_URL = GWT.getModuleBaseURL() + "upload"; 

    final Label parseErrorMsgLabel = new Label();
    final FormPanel form = new FormPanel();
    final TextArea logTextArea = new TextArea();
    final FileUpload uploadButton = new FileUpload();
    final Button submitButton = new Button("Load Log File (.txt)");
    final TextArea regExpsTextArea = new TextArea();
    final TextBox partitionRegExpTextBox = new TextBox();
    final TextBox separatorRegExpTextBox = new TextBox();
    final Button parseLogButton = new Button("Parse Log");
    
    public InputSubTab(ISynopticServiceAsync synopticService, String logText,
            String regExpText, String partitionRegExpText,
            String separatorRegExpText) {
        super(synopticService);
        
        panel = new VerticalPanel();
        
        // Construct the inputs panel using a grid.
        panel.add(parseErrorMsgLabel);

        Grid grid = new Grid(5, 2);
        panel.add(grid);
        
        // Set up form to handle file upload.
        form.setAction(UPLOAD_ACTION_URL);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
        form.setWidget(grid);
       
        // Set up inner panel containing file upload and submit. 
        HorizontalPanel uploadPanel = new HorizontalPanel();
        uploadButton.setName("uploadFormElement"); 
        uploadPanel.add(uploadButton);
        uploadPanel.add(submitButton); 
        
        // Set up inner panel containing textarea and upload section.
        VerticalPanel logPanel = new VerticalPanel();
        logPanel.add(logTextArea);
        logPanel.add(uploadPanel);

        grid.setWidget(0, 0, new Label("Log lines"));
        grid.setWidget(0, 1, logPanel);
        logTextArea.setCharacterWidth(80);
        logTextArea.setVisibleLines(10);

        grid.setWidget(1, 0, new Label("Regular expressions"));
        grid.setWidget(1, 1, regExpsTextArea);
        regExpsTextArea.setCharacterWidth(80);

        grid.setWidget(2, 0, new Label("Partition expression"));
        grid.setWidget(2, 1, partitionRegExpTextBox);
        partitionRegExpTextBox.setVisibleLength(80);

        grid.setWidget(3, 0, new Label("Separator expression"));
        grid.setWidget(3, 1, separatorRegExpTextBox);
        separatorRegExpTextBox.setVisibleLength(80);

        grid.setWidget(4, 1, parseLogButton);

        grid.setStyleName("inputForm grid");
        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getCellCount(i); j++) {
                grid.getCellFormatter().setStyleName(i, j, "tableCell");
            }
        }

        // Set up the error label's style\visibility.
        parseErrorMsgLabel.setStyleName("serverResponseLabelError");
        parseErrorMsgLabel.setVisible(false);

        // Set up the logTextArea.
        logTextArea.setFocus(true);
        logTextArea.setText(logText);
        logTextArea.selectAll();

        // Set up the other text areas.
        regExpsTextArea.setText(regExpText);
        partitionRegExpTextBox.setText(partitionRegExpText);
        separatorRegExpTextBox.setText(separatorRegExpText);

        // Associate handler with the Parse Log button.
        parseLogButton.addClickHandler(new ParseLogHandler());
        
        // Associate handler with Submit button.
        submitButton.addClickHandler(new SubmitUploadHandler());
        
        // Associate handler with Form.
        form.addSubmitCompleteHandler(new FormCompleteHandler());
        
        panel.add(form);
    }
    
    /**
     * Handles submit button clicks.
     */
    class SubmitUploadHandler implements ClickHandler {
    	@Override
    	public void onClick(ClickEvent event) {
    		form.submit();
    	}
    }
   
    /**
     * Handles filling text area with contents of uploaded text file.
     */
    class FormCompleteHandler implements FormPanel.SubmitCompleteHandler {
    	@Override
    	public void onSubmitComplete(SubmitCompleteEvent event) {
			logTextArea.setText(event.getResults());
		}
    }

    /**
     * Handles parse log button clicks.
     */
    class ParseLogHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            // Disallow the user from making concurrent Parse Log calls.
            parseLogButton.setEnabled(false);

            // Reset the parse error msg.
            parseErrorMsgLabel.setText("");

            // Extract arguments for parseLog call.
            String logLines = logTextArea.getText();
            String regExpLines[] = regExpsTextArea.getText().split("\\r?\\n");
            List<String> regExps = Arrays.asList(regExpLines);

            String partitionRegExp = partitionRegExpTextBox.getText();
            if (partitionRegExp == "") {
                partitionRegExp = null;
            }
            String separatorRegExp = separatorRegExpTextBox.getText();
            if (separatorRegExp == "") {
                separatorRegExp = null;
            }

            // TODO: validate the arguments to parseLog.

            // ////////////////////// Call to remote service.
            synopticService.parseLog(logLines, regExps, partitionRegExp,
                    separatorRegExp, new ParseLogAsyncCallback());
            // //////////////////////
        }
    }

    /**
     * Callback handler for the parseLog() Synoptic service call.
     */
    class ParseLogAsyncCallback implements
            AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> {
        @Override
        public void onFailure(Throwable caught) {
            displayRPCErrorMessage("Remote Procedure Call Failure while parsing log: " +
            		caught.getMessage());
            parseErrorMsgLabel.setText(caught.getMessage());
            parseLogButton.setEnabled(true);
            if (caught instanceof SerializableParseException) {
            	SerializableParseException exception = (SerializableParseException) caught;
            	// If the exception has both a regex and a logline, then only the TextArea
            	// that sets their highlighting last will have highlighting.
            	// A secret dependency for TextArea highlighting is focus.
            	// As of now, 9/12/11, SerializableParseExceptions do not get 
            	// thrown with both a regex and a logline.
            	if (exception.hasRegex()) {
	            	String regex = exception.getRegex();
	                String regexes = regExpsTextArea.getText();
	                int pos = indexOf(regexes, regex);
	                regExpsTextArea.setFocus(true);
	                regExpsTextArea.setSelectionRange(pos, regex.length());
            	}
            	if (exception.hasLogLine()) {
            		String log = exception.getLogLine();
            		String logs = logTextArea.getText();
            		int pos = indexOf(logs, log);
            		logTextArea.setFocus(true);
            		logTextArea.setSelectionRange(pos, log.length());
            	}
            }
        }
        
        public int indexOf(String string, String searchString) {
        	if (string == null || searchString == null) {
        		throw new NullPointerException();
        	}
        	
        	
        	int movingPosition = string.indexOf(searchString);
        	int cumulativePosition = movingPosition;
        	
        	if (movingPosition == -1) {
        		return movingPosition;
        	}       	
        	
        	while (movingPosition + searchString.length() < string.length() &&
        			!(string.charAt(movingPosition + searchString.length()) == '\r' || 
        			string.charAt(movingPosition + searchString.length()) == '\n')) {
        		
        		string = string.substring(movingPosition + searchString.length());
        		movingPosition = string.indexOf(searchString);
        			
            	if (movingPosition == -1) {
            		return movingPosition;
            	}
            	
            	cumulativePosition += movingPosition + searchString.length();
        	}
        	return cumulativePosition;
        }

        @Override
        public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> ret) {
            parseLogButton.setEnabled(true);
            SynopticGWT.entryPoint.logParsed(ret.getLeft(), ret.getRight());
        }
    }

}