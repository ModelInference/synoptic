package synopticgwt.client.input;

import java.util.LinkedList;
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
import synopticgwt.shared.GWTParseException;

/**
 * Panel that contains all text fields to enter log/re values. Contains upload
 * button to upload a log file.
 */
public class InputPanel extends Tab<VerticalPanel> {
    private static final String UPLOAD_LOGFILE_URL = GWT.getModuleBaseURL()
            + "log_file_upload";

    final String regExpDefault = "(?<TYPE>.*)";
    final String partitionRegExpDefault = "\\k<FILE>";

    final VerticalPanel poExamplesPanel = new VerticalPanel();
    final VerticalPanel toExamplesPanel = new VerticalPanel();
    final Grid examplesGrid = new Grid(5, 1);
    final Label exampleLogLabel = new Label("Load example logs");
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

    final ExtendedTextArea logTextArea = new ExtendedTextArea(
            new ScheduledCommand() {
                @Override
                public void execute() {
                    // Enable parse log button if pasting non-empty text.
                    if (logTextArea.getValue().trim().length() != 0) {
                        parseLogButton.setEnabled(true);
                    }
                }
            });

    final ExtendedTextBox primaryRegExpsTextBox = new ExtendedTextBox(
            new ScheduledCommand() {
                @Override
                public void execute() {
                    // Hide the default reg-exp label if pasting non-empty text.
                    if (primaryRegExpsTextBox.getValue().trim().length() != 0) {
                        regExpDefaultLabel.setVisible(false);
                    }
                }
            });

    final ExtendedTextBox partitionRegExpTextBox = new ExtendedTextBox(
            new ScheduledCommand() {
                @Override
                public void execute() {
                    // Hide the default reg-exp label if pasting non-empty text.
                    if (partitionRegExpTextBox.getValue().trim().length() != 0) {
                        partitionRegExpDefaultLabel.setVisible(false);
                    }
                }
            });

    final TextBox separatorRegExpTextBox = new TextBox();
    final FileUpload uploadLogFileButton = new FileUpload();
    final VerticalPanel extraRegExpPanel = new VerticalPanel();
    final Button addRegExpButton = new Button("+");
    final Button parseLogButton = new Button("Parse Log");
    final Button clearInputsButton = new Button("Clear");

    public InputPanel(ISynopticServiceAsync synopticService) {
        super(synopticService);

        panel = new VerticalPanel();

        // Holds the examples panel and input panel
        HorizontalPanel examplesAndInputForm = new HorizontalPanel();
        VerticalPanel inputForm = new VerticalPanel();

        // Construct the inputs panel using a grid.
        inputForm.add(parseErrorMsgLabel);

        // Set up links and labels for examples panel.
        exampleLogLabel.setStyleName("exampleLogLabel");
        examplesGrid.setWidget(0, 0, exampleLogLabel);

        Label poLabel = new Label("Partially ordered");
        Label toLabel = new Label("Totally ordered");
        poLabel.setStyleName("logTypeLabel");
        toLabel.setStyleName("logTypeLabel");

        examplesGrid.setWidget(1, 0, poLabel);
        examplesGrid.setWidget(3, 0, toLabel);

        examplesGrid.setWidget(2, 0, poExamplesPanel);
        examplesGrid.setWidget(4, 0, toExamplesPanel);
        poExamplesPanel.setStyleName("poLinkTable");
        toExamplesPanel.setStyleName("toLinkTable");

        InputExample[] examples = InputExample.values();
        for (int i = 0; i < examples.length; i++) {
            // Create anchor for every InputExample enum.
            Anchor exampleLink = new Anchor(examples[i].getName());
            exampleLink.addClickHandler(new ExampleLinkHandler());
            if (examples[i].isPartiallyOrdered()) {
                poExamplesPanel.add(exampleLink);
            } else {
                toExamplesPanel.add(exampleLink);
            }
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
        logTextRadioButton.setValue(true); // Initially checked.

        // Set up inner panel containing file upload and submit button.
        HorizontalPanel uploadPanel = new HorizontalPanel();
        uploadLogFileButton.setName("uploadFormElement");
        uploadLogFileButton.setEnabled(false);
        uploadPanel.add(uploadLogFileButton);

        HorizontalPanel radioButtonPanel = new HorizontalPanel();
        radioButtonPanel.add(logInputTypeLabel);
        radioButtonPanel.add(logTextRadioButton);
        radioButtonPanel.add(logFileRadioButton);

        // Set up inner panel containing textarea and upload.
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
        extraRegExpPanel.addStyleName("ExtraRegExps");
        regExpDefaultLabel.setStyleName("DefaultExpLabel");

        Grid regExpsPanelHolder = new Grid(4, 1);
        regExpsPanelHolder.setWidget(0, 0, regExpDefaultLabel);
        regExpsPanelHolder.setWidget(1, 0, primaryRegExpsTextBox);
        regExpsPanelHolder.setWidget(2, 0, extraRegExpPanel);
        regExpsPanelHolder.setWidget(3, 0, addRegExpButton);
        grid.setWidget(1, 1, regExpsPanelHolder);
        setUpTextBox(primaryRegExpsTextBox);

        VerticalPanel partitionExpPanel = new VerticalPanel();
        partitionExpPanel.add(partitionRegExpDefaultLabel);
        partitionExpPanel.add(partitionRegExpTextBox);
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
        partitionRegExpTextBox.setText("");
        separatorRegExpTextBox.setText("");

        // Associate handler with add extra reg exp button.
        addRegExpButton.addClickHandler(new AddRegExpHandler());

        // Associate KeyPress handler to enable default labels appearing.
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
    // TODO: make this take in a List<String> regExpText to allow multiple
    // regExps. Noted in Issue151.
    public void setInputs(String logText, String regExpText,
            String partitionRegExpText, String separatorRegExpText) {
        this.logTextArea.setText(logText);
        this.primaryRegExpsTextBox.setText(regExpText);
        this.partitionRegExpTextBox.setText(partitionRegExpText);
        this.separatorRegExpTextBox.setText(separatorRegExpText);
    }

    /**
     * Extracts all regular expressions into a list.
     */
    private List<String> extractAllRegExps() {
        String currRegExp;
        List<String> result = new LinkedList<String>();

        currRegExp = getTextBoxRegExp(primaryRegExpsTextBox);
        if (!currRegExp.equals("")) {
            result.add(currRegExp);
        }

        for (int i = 0; i < extraRegExpPanel.getWidgetCount(); i++) {
            // Extract each addition text box from panel within extraRegExpPanel
            HorizontalPanel currPanel = (HorizontalPanel) extraRegExpPanel
                    .getWidget(i);
            TextBox currTextBox = (TextBox) currPanel.getWidget(0);

            currRegExp = getTextBoxRegExp(currTextBox);
            if (!currRegExp.equals("")) {
                result.add(currRegExp);
            }
        }
        return result;
    }

    /**
     * Extracts expression from text box for log parsing.
     */
    private String getTextBoxRegExp(TextBox textBox) {
        String expression = textBox.getText();
        if (expression == null) {
            return "";
        }
        return expression;
    }

    /**
     * Sets all input field values to be empty strings.
     */
    private void clearInputValues() {
        logTextArea.setValue("");
        primaryRegExpsTextBox.setValue("");
        partitionRegExpTextBox.setValue("");
        separatorRegExpTextBox.setValue("");
    }

    /**
     * Sets up properties for given TextArea.
     */
    private void setUpTextBox(ExtendedTextBox textBox) {
        textBox.setValue("");
        textBox.setVisibleLength(80);
        textBox.setName("regExpsTextArea");
        textBox.addKeyUpHandler(new KeyUpInputHandler());
    }

    /**
     * Adds a new reg exp text area with a corresponding delete button.
     */
    class AddRegExpHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            ExtendedTextBox newTextBox = new ExtendedTextBox(
                    new ScheduledCommand() {
                        @Override
                        public void execute() {
                            if (primaryRegExpsTextBox.getValue().trim()
                                    .length() != 0) {
                                regExpDefaultLabel.setVisible(false);
                            }
                        }
                    });
            setUpTextBox(newTextBox);
            Button deleteButton = new Button("-");
            deleteButton.addStyleName("DeleteButton");
            deleteButton.addClickHandler(new DeleteTextAreaHandler());
            HorizontalPanel textAreaAndDeleteHolder = new HorizontalPanel();
            textAreaAndDeleteHolder.add(newTextBox);
            textAreaAndDeleteHolder.add(deleteButton);
            extraRegExpPanel.add(textAreaAndDeleteHolder);
        }
    }

    /**
     * Clears inputs and enables all the log example links and disables
     * upload/parse log buttons.
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
     * Removes the text area to the left of the clicked delete button.
     */
    class DeleteTextAreaHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            Button clicked = (Button) event.getSource();
            clicked.getParent().removeFromParent();
        }

    }

    /**
     * Handles clicks on example log anchors. Loads the associated log/re
     * content into the text areas and text boxes to the left.
     */
    class ExampleLinkHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            // Clears all inputs and uploads.
            logFileUploadForm.reset();
            Anchor anchorClicked = (Anchor) event.getSource();
            InputExample example = InputExample.valueOf(anchorClicked.getText()
                    .toUpperCase());
            setInputs(example.getLogText(), example.getRegExpText(),
                    example.getPartitionRegExpText(),
                    example.getSeparatorRegExpText());
            if (primaryRegExpsTextBox.getValue().trim().length() != 0) {
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

    // TODO: ExtendedTextArea and ExtendedTextBox have identical code. They need
    // to be refactored into the same object

    /**
     * A subclass of text area that allows the browser to capture a paste event
     * and runs a custom command for the event.
     */
    class ExtendedTextArea extends TextArea {
        ScheduledCommand cmd;

        public ExtendedTextArea(ScheduledCommand cmd) {
            super();
            this.cmd = cmd;
            sinkEvents(Event.ONPASTE);
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            switch (DOM.eventGetType(event)) {
            case Event.ONPASTE:
                Scheduler.get().scheduleDeferred(this.cmd);
                break;
            }
        }
    }

    /**
     * A subclass of text box that allows the browser to capture a paste event
     * and runs a custom command for the event.
     */
    class ExtendedTextBox extends TextBox {
        ScheduledCommand cmd;

        public ExtendedTextBox(ScheduledCommand cmd) {
            super();
            this.cmd = cmd;
            sinkEvents(Event.ONPASTE);
        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            switch (DOM.eventGetType(event)) {
            case Event.ONPASTE:
                Scheduler.get().scheduleDeferred(this.cmd);
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
            } else if (event.getSource() == primaryRegExpsTextBox) {
                if (primaryRegExpsTextBox.getValue().trim().length() != 0) {
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
     * Called after log file uploaded is saved on server side. Handles calling
     * SynopticService to read and parse contents of the log file uploaded by
     * client.
     */
    class LogFileFormCompleteHandler implements FormPanel.SubmitCompleteHandler {
        @Override
        public void onSubmitComplete(SubmitCompleteEvent event) {
            // Extract arguments for parseLog call.
            List<String> regExps = extractAllRegExps();
            String partitionRegExp = getTextBoxRegExp(partitionRegExpTextBox);
            String separatorRegExp = getTextBoxRegExp(separatorRegExpTextBox);

            // ////////////////////// Call to remote service.
            synopticService.parseUploadedLog(regExps, partitionRegExp,
                    separatorRegExp, new ParseLogAsyncCallback());
            // //////////////////////
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
     * Handles parse log button clicks.
     */
    class ParseLogHandler implements ClickHandler {
        @SuppressWarnings("synthetic-access")
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
                List<String> regExps = extractAllRegExps();
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
            if (!(caught instanceof GWTParseException)) {
                return;
            }
            GWTParseException exception = (GWTParseException) caught;
            // If the exception has both a regex and a logline, then only
            // the TextArea
            // that sets their highlighting last will have highlighting.
            // A secret dependency for TextArea highlighting is focus.
            // As of now, 9/12/11, SerializableParseExceptions do not get
            // thrown with both a regex and a logline.
            if (exception.hasRegex()) {
                String regex = exception.getRegex();
                // TODO: currently error handling only for primary regexps
                // textarea, extend to all extra reg exp text area also.
                // Noted in Issue152
                String regexes = primaryRegExpsTextBox.getText();
                int pos = indexOf(regexes, regex);
                primaryRegExpsTextBox.setFocus(true);
                primaryRegExpsTextBox.setSelectionRange(pos, regex.length());
            }
            if (exception.hasLogLine()) {
                String log = exception.getLogLine();
                String logs = logTextArea.getText();
                int pos = indexOf(logs, log);
                logTextArea.setFocus(true);
                logTextArea.setSelectionRange(pos, log.length());

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