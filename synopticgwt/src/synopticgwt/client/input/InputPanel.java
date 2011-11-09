package synopticgwt.client.input;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
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

/**
 * Panel that contains all text fields to enter log/re values. Contains upload
 * button to upload a log file.
 */
public class InputPanel extends Tab<VerticalPanel> {
    private static final String UPLOAD_LOGFILE_URL = GWT.getModuleBaseURL()
            + "log_file_upload";

    final String regExpDefault = "(?<TYPE>.*)";
    final String partitionRegExpDefault = "\\k<FILE>";

    final Grid examplesGrid = new Grid(5, 1);
    final Label loadExamples = new Label("Load example logs");
    final Label parseErrorMsgLabel = new Label();
    final Label logInputTypeLabel = new Label("Log input type:");
    final Label regExpDefaultLabel = new Label("Defaults to " + regExpDefault
            + " when empty");
    final Label partitionRegExpDefaultLabel = new Label("Defaults to "
            + partitionRegExpDefault + " when empty");
    final FormPanel logFileUploadForm = new FormPanel();
    final RadioButton logTextRadioButton = new RadioButton("logInputType",
            "Text area");
    final RadioButton logFileRadioButton = new RadioButton("logInputType",
            "Text file");

    final ExtendedTextArea logTextArea = new ExtendedTextArea();
    final FileUpload uploadLogFileButton = new FileUpload();
    final ExtendedTextArea regExpsTextArea = new ExtendedTextArea();
    final ExtendedTextBox partitionRegExpTextBox = new ExtendedTextBox();
    final TextBox separatorRegExpTextBox = new TextBox();
    final Button parseLogButton = new Button("Parse Log");
    final Button clearInputsButton = new Button("Clear");

    public InputPanel(ISynopticServiceAsync synopticService) {
        super(synopticService);

        panel = new VerticalPanel();

        HorizontalPanel examplesAndInputForm = new HorizontalPanel();
        VerticalPanel inputForm = new VerticalPanel();

        // Construct the inputs panel using a grid.
        inputForm.add(parseErrorMsgLabel);

        // Set up the links for log examples panel.
        examplesGrid.setWidget(0, 0, loadExamples);
        InputExample[] inputExamples = InputExample.values();
        for (int i = 0; i < inputExamples.length; i++) {
            VerticalPanel linkAndType = new VerticalPanel();
            // Create anchor for every InputExample enum.
            Anchor exampleLink = new Anchor(inputExamples[i].getName());
            Label logType;
            if (inputExamples[i].isPartiallyOrdered()) {
                logType = new Label("(Partially Ordered Log)");
            } else {
                logType = new Label("(Totally Ordered Log)");
            }
            logType.setStyleName("logTypeLabel");
            linkAndType.add(exampleLink);
            linkAndType.add(logType);
            // Associate click listener to anchors.
            exampleLink.addClickHandler(new ExampleLinkHandler());
            examplesGrid.setWidget((i + 1), 0, linkAndType);
            examplesGrid.getCellFormatter().setStyleName((i + 1), 0,
                    "tableCell");
        }
        examplesGrid.setStyleName("inputForm");

        Grid grid = new Grid(5, 2);
        inputForm.add(grid);

        // Set up form to handle file upload.
        logFileUploadForm.setAction(UPLOAD_LOGFILE_URL);
        logFileUploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        logFileUploadForm.setMethod(FormPanel.METHOD_POST);
        logFileUploadForm.setWidget(grid);

        logTextRadioButton.setStyleName("LogTypeRadio");
        logFileRadioButton.setStyleName("LogTypeRadio");
        logTextRadioButton.setValue(true); // Log text area input initially
                                           // checked

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

        VerticalPanel regExpPanel = new VerticalPanel();
        regExpPanel.add(regExpsTextArea);
        regExpPanel.add(regExpDefaultLabel);
        regExpDefaultLabel.setStyleName("DefaultExpLabel");
        grid.setWidget(1, 0, new Label("Regular expressions"));
        grid.setWidget(1, 1, regExpPanel);
        regExpsTextArea.setCharacterWidth(80);
        regExpsTextArea.setName("regExpsTextArea");

        VerticalPanel partitionExpPanel = new VerticalPanel();
        partitionExpPanel.add(partitionRegExpTextBox);
        partitionExpPanel.add(partitionRegExpDefaultLabel);
        partitionRegExpDefaultLabel.setStyleName("DefaultExpLabel");
        grid.setWidget(2, 0, new Label("Partition expression"));
        grid.setWidget(2, 1, partitionExpPanel);
        partitionRegExpTextBox.setVisibleLength(80);
        partitionRegExpTextBox.setName("partitionRegExpTextBox");

        grid.setWidget(3, 0, new Label("Separator expression"));
        grid.setWidget(3, 1, separatorRegExpTextBox);
        separatorRegExpTextBox.setVisibleLength(80);
        separatorRegExpTextBox.setName("separatorRegExpTextBox");

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.add(parseLogButton);
        buttonsPanel.add(clearInputsButton);
        parseLogButton.addStyleName("parseButton");
        parseLogButton.setEnabled(false); // initially disabled
        grid.setWidget(4, 1, buttonsPanel);

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
        logTextArea.setText("");
        logTextArea.selectAll();
        logTextArea.addKeyUpHandler(new KeyUpInputHandler());

        // Set up the other text areas.
        regExpsTextArea.setText("");
        partitionRegExpTextBox.setText("");
        separatorRegExpTextBox.setText("");

        // Associate KeyPress handlers to enable default labels appearing.
        regExpsTextArea.addKeyUpHandler(new KeyUpInputHandler());
        partitionRegExpTextBox.addKeyUpHandler(new KeyUpInputHandler());

        // Associate handler with the Parse Log button.
        parseLogButton.addClickHandler(new ParseLogHandler());
        parseLogButton.addStyleName("ParseLogButton");

        // Associate handler with the Clear Inputs button.
        clearInputsButton.addClickHandler(new ClearInputsHandler());

        // Associate handler with form.
        logFileUploadForm
                .addSubmitCompleteHandler(new LogFileFormCompleteHandler());
        logTextRadioButton.addValueChangeHandler(new LogTypeRadioHandler());
        logFileRadioButton.addValueChangeHandler(new LogTypeRadioHandler());
        uploadLogFileButton.addChangeHandler(new FileUploadHandler());

        inputForm.add(logFileUploadForm);
        examplesAndInputForm.add(examplesGrid);
        examplesAndInputForm.add(inputForm);
        panel.add(examplesAndInputForm);
    }

    /**
     * Sets each input text field to corresponding parameter.
     * 
     * @param logText
     *            content of log file
     * @param regExpText
     *            regular expression
     * @param partitionRegExpText
     *            partition regular expression
     * @param separatorRegExpText
     *            separator regular expression
     */
    public void setInputs(String logText, String regExpText,
            String partitionRegExpText, String separatorRegExpText) {
        this.logTextArea.setText(logText);
        this.regExpsTextArea.setText(regExpText);
        this.partitionRegExpTextBox.setText(partitionRegExpText);
        this.separatorRegExpTextBox.setText(separatorRegExpText);
    }

    // Extracts regular expressions in text area for log parsing.
    private List<String> getRegExps(TextArea textArea) {
        String regExpLines[] = textArea.getText().split("\\r?\\n");
        List<String> regExps = Arrays.asList(regExpLines);
        return regExps;
    }

    // Extracts expression from text box for log parsing.
    private String getTextBoxRegExp(TextBox textBox) {
        String expression = textBox.getText();
        if (expression == "") {
            expression = null;
        }
        return expression;
    }

    // Sets all input field values to be empty strings.
    private void clearInputValues() {
        logTextArea.setValue("");
        regExpsTextArea.setValue("");
        partitionRegExpTextBox.setValue("");
        separatorRegExpTextBox.setValue("");
    }

    /**
     * Clears inputs and enables all the log example links.
     */
    class ClearInputsHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            logFileUploadForm.reset();
            logTextArea.setEnabled(true);
            uploadLogFileButton.setEnabled(false);
            parseLogButton.setEnabled(false);
            regExpDefaultLabel.setVisible(true);
            partitionRegExpDefaultLabel.setVisible(true);
        }
    }

    /**
     * A subclass of text area that allows the browser to capture a paste event
     * and enable that parse log button.
     */
    class ExtendedTextArea extends TextArea {
        public ExtendedTextArea() {
            super();
            sinkEvents(Event.ONPASTE);
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            switch (DOM.eventGetType(event)) {
            case Event.ONPASTE:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        // Enable parse log button if pasting non-empty text.
                        if (logTextArea.getValue().trim().length() != 0) {
                            parseLogButton.setEnabled(true);
                        }
                        if (regExpsTextArea.getValue().trim().length() != 0) {
                            regExpDefaultLabel.setVisible(false);
                        }
                    }
                });
                break;
            }
        }
    }

    /**
     * A subclass of text box that allows the browser capture a paste event.
     */
    class ExtendedTextBox extends TextBox {
        public ExtendedTextBox() {
            super();
            sinkEvents(Event.ONPASTE);
        }

        // Displays the default partition reg exp. if a paste event results
        // in an empty textbox.
        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            switch (DOM.eventGetType(event)) {
            case Event.ONPASTE:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (partitionRegExpTextBox.getValue().trim().length() != 0) {
                            partitionRegExpDefaultLabel.setVisible(false);
                        }
                    }
                });
                break;
            }
        }
    }

    /**
     * Handles when a file is selected is to be uploaded. Enables parse log
     * button and clears any prior inputs in fields when a file is chosen.
     */
    class FileUploadHandler implements ChangeHandler {

        @Override
        public void onChange(ChangeEvent event) {
            if (uploadLogFileButton.getFilename() != null
                    && logFileRadioButton.isEnabled()) {
                parseLogButton.setEnabled(true);
                clearInputValues();
                regExpDefaultLabel.setVisible(true);
                partitionRegExpDefaultLabel.setVisible(true);
            } else {
                parseLogButton.setEnabled(false);
            }
        }
    }

    /**
     * Handles clicks on example log anchors. Loads the associated log/re
     * content into the text areas and text boxes to the left.
     */
    class ExampleLinkHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            // Clears all inputs and uploads
            logFileUploadForm.reset();
            InputExample[] inputExamples = InputExample.values();
            for (int i = 1; i < examplesGrid.getRowCount(); i++) {
                VerticalPanel curr = (VerticalPanel) examplesGrid.getWidget(i,
                        0);
                if (event.getSource() == curr.getWidget(0)) {
                    InputExample currExample = inputExamples[i - 1];
                    setInputs(currExample.getLogText(),
                            currExample.getRegExpText(),
                            currExample.getPartitionRegExpText(),
                            currExample.getSeparatorRegExpText());
                }
            }
            if (regExpsTextArea.getValue().trim().length() != 0) {
                regExpDefaultLabel.setVisible(false);
            } else {
                regExpDefaultLabel.setVisible(true);
            }
            if (partitionRegExpTextBox.getValue().trim().length() != 0) {
                partitionRegExpDefaultLabel.setVisible(false);
            } else {
                partitionRegExpDefaultLabel.setVisible(true);
            }
            parseLogButton.setEnabled(true);
        }
    }

    /**
     * Handles KeyPress events for all the log input fields. Enables/disables
     * fields, labels, or buttons according to empty or non-empty fields.
     */
    class KeyUpInputHandler implements KeyUpHandler {
        @Override
        public void onKeyUp(KeyUpEvent event) {
            if (event.getSource() == logTextArea) {
                // Parse log enabled if log text area is not empty.
                if (logTextRadioButton.isEnabled()
                        && logTextArea.getValue().trim().length() != 0) {
                    parseLogButton.setEnabled(true);
                } else {
                    parseLogButton.setEnabled(false);
                }
            } else if (event.getSource() == regExpsTextArea) {
                if (regExpsTextArea.getValue().trim().length() != 0) {
                    regExpDefaultLabel.setVisible(false);
                } else {
                    regExpDefaultLabel.setVisible(true);
                }
            } else if (event.getSource() == partitionRegExpTextBox) {
                if (partitionRegExpTextBox.getValue().trim().length() != 0) {
                    partitionRegExpDefaultLabel.setVisible(false);
                } else {
                    partitionRegExpDefaultLabel.setVisible(true);
                }
            }
        }
    }

    /**
     * Handles enabling/disabling of text area or file upload button when log
     * type radio buttons are changed.
     */
    class LogTypeRadioHandler implements ValueChangeHandler<Boolean> {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            if (event.getSource() == logTextRadioButton) {
                logTextArea.setEnabled(true);
                if (uploadLogFileButton.isEnabled()) {
                    uploadLogFileButton.setEnabled(false);
                }
                if (logTextArea.getValue().trim().length() == 0) {
                    parseLogButton.setEnabled(false);
                } else {
                    parseLogButton.setEnabled(true);
                }
            } else { // logFileRadioButton
                uploadLogFileButton.setEnabled(true);
                if (logTextArea.isEnabled()) {
                    logTextArea.setEnabled(false);
                }
                if (uploadLogFileButton.getFilename() != null) {
                    parseLogButton.setEnabled(false);
                } else {
                    parseLogButton.setEnabled(true);
                }
            }
        }
    }

    /**
     * Called after log file uploaded is saved on server side. Handles calling
     * SynopticService to read and parse contents of the log file uploaded by
     * client.
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
            displayRPCErrorMessage("Remote Procedure Call Failure while parsing log: "
                    + caught.getMessage());
            parseErrorMsgLabel.setText(caught.getMessage());
            parseLogButton.setEnabled(true);
            if (caught instanceof SerializableParseException) {
                SerializableParseException exception = (SerializableParseException) caught;
                // If the exception has both a regex and a logline, then only
                // the TextArea
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

        /**
         * Returns the index of searchString as a substring of string with the
         * condition that the searchString is followed by a newline character,
         * carriage return character, or nothing(end of string). Returns -1 if
         * searchString is not found in string with the previous conditions.
         * Throws a NullPointerException if string or searchString is null.
         */
        public int indexOf(String string, String searchString) {
            if (string == null || searchString == null) {
                throw new NullPointerException();
            }

            int movingPosition = string.indexOf(searchString);
            int cumulativePosition = movingPosition;

            if (movingPosition == -1) {
                return movingPosition;
            }

            while (movingPosition + searchString.length() < string.length()
                    && !(string.charAt(movingPosition + searchString.length()) == '\r' || string
                            .charAt(movingPosition + searchString.length()) == '\n')) {

                string = string.substring(movingPosition
                        + searchString.length());
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