package synopticgwt.client.input;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
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
    private static final String UPLOAD_LOGFILE_URL = GWT.getModuleBaseURL() + "log_file_upload"; 

    final Label parseErrorMsgLabel = new Label();
    final Label logInputTypeLabel = new Label("Log input type:");
    final FormPanel logFileUploadForm = new FormPanel();
    final RadioButton logTextRadioButton = new RadioButton("logInputType", "Text area");
    final RadioButton logFileRadioButton = new RadioButton("logInputType", "Text file");
    final TextArea logTextArea = new TextArea();
    final FileUpload uploadLogFileButton = new FileUpload();
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
        logFileUploadForm.setAction(UPLOAD_LOGFILE_URL);
        logFileUploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        logFileUploadForm.setMethod(FormPanel.METHOD_POST);
        logFileUploadForm.setWidget(grid);
       
        logTextRadioButton.setStyleName("LogTypeRadio");
        logFileRadioButton.setStyleName("LogTypeRadio");
        logTextRadioButton.setValue(true); // Log text area input initially checked
        
        // Set up inner panel containing file upload and submit. 
        HorizontalPanel uploadPanel = new HorizontalPanel();
        uploadLogFileButton.setName("uploadFormElement");
        uploadLogFileButton.setEnabled(false);
        uploadPanel.add(uploadLogFileButton);
        
        HorizontalPanel radioButtonPanel = new HorizontalPanel();
        radioButtonPanel.add(logInputTypeLabel);
        radioButtonPanel.add(logTextRadioButton);
        radioButtonPanel.add(logFileRadioButton);
        
        // Set up inner panel containing textarea and upload section.
        VerticalPanel logPanel = new VerticalPanel();
        
        logPanel.add(radioButtonPanel);
        logPanel.add(logTextArea);
        logPanel.add(uploadPanel);

        grid.setWidget(0, 0, new Label("Log lines"));
        grid.setWidget(0, 1, logPanel);
        logTextArea.setCharacterWidth(80);
        logTextArea.setVisibleLines(10);
        logTextArea.setName("logTextArea");

        grid.setWidget(1, 0, new Label("Regular expressions"));
        grid.setWidget(1, 1, regExpsTextArea);
        regExpsTextArea.setCharacterWidth(80);
        regExpsTextArea.setName("regExpsTextArea");

        grid.setWidget(2, 0, new Label("Partition expression"));
        grid.setWidget(2, 1, partitionRegExpTextBox);
        partitionRegExpTextBox.setVisibleLength(80);
        partitionRegExpTextBox.setName("partitionRegExpTextBox");

        grid.setWidget(3, 0, new Label("Separator expression"));
        grid.setWidget(3, 1, separatorRegExpTextBox);
        separatorRegExpTextBox.setVisibleLength(80);
        separatorRegExpTextBox.setName("separatorRegExpTextBox");

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
           
        // Associate handler with form.
        logFileUploadForm.addSubmitCompleteHandler(new LogFileFormCompleteHandler());
        logTextRadioButton.addValueChangeHandler(new LogTypeRadioHandler());
        logFileRadioButton.addValueChangeHandler(new LogTypeRadioHandler());
        panel.add(logFileUploadForm);
    }
    
    // Extracts regular expressions in text area for log parsing
    private List<String> getRegExps(TextArea textArea) {
    	String regExpLines[] = textArea.getText().split("\\r?\\n");
        List<String> regExps = Arrays.asList(regExpLines);
        return regExps;
    }
    
    // Extracts expression from text box for log parsing
    private String getTextBoxRegExp(TextBox textBox) {
    	 String expression = textBox.getText();
         if (expression == "") {
             expression = null;
         }
         return expression;
    }
    
    /**
     * Handles enabling/disabling of text area or file upload button
     * when log type radio buttons are changed. 
     */
    class LogTypeRadioHandler implements ValueChangeHandler<Boolean> {
		@Override
		public void onValueChange(ValueChangeEvent<Boolean> event) {
			if (event.getSource() == logTextRadioButton) {
				logTextArea.setEnabled(true);
				if (uploadLogFileButton.isEnabled()) {
					uploadLogFileButton.setEnabled(false);
				}
			} else {
				uploadLogFileButton.setEnabled(true);
				if (logTextArea.isEnabled()) {
					logTextArea.setEnabled(false);
				}
			}
		}
    }
    
    /**
     * Called after log file uploaded is saved on server side. Handles calling SynopticService 
     * to read and parse contents of the log file uploaded by client.
     */
    class LogFileFormCompleteHandler implements FormPanel.SubmitCompleteHandler {
    	@Override
    	public void onSubmitComplete(SubmitCompleteEvent event) {
    		 // Extract arguments for parseLog call.
            List<String> regExps = getRegExps(regExpsTextArea);
            String partitionRegExp = getTextBoxRegExp(partitionRegExpTextBox);
            String separatorRegExp = getTextBoxRegExp(separatorRegExpTextBox);
                	
    		// ////////////////////// Call to remote service.
            synopticService.parseUploadedLog(regExps, partitionRegExp,
                    separatorRegExp, new ParseLogAsyncCallback());
            // //////////////////////	
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
            
        	if (logFileRadioButton.getValue()) { // log file
        		logFileUploadForm.submit();
        		
        	} else { // log in text area
        
	           // Extract arguments for parseLog call.
	            String logLines = logTextArea.getText();
	            List<String> regExps = getRegExps(regExpsTextArea);
	            String partitionRegExp = getTextBoxRegExp(partitionRegExpTextBox);
	            String separatorRegExp = getTextBoxRegExp(separatorRegExpTextBox);
	
	            // TODO: validate the arguments to parseLog.
	
	            // ////////////////////// Call to remote service.
	            synopticService.parseLog(logLines, regExps, partitionRegExp,
	                    separatorRegExp, new ParseLogAsyncCallback());
	            // //////////////////////
        	}
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