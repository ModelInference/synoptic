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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
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
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.client.util.TooltipListener;
import synopticgwt.shared.GWTSynOpts;

/**
 * The inputs tab. This tab contains text fields to enter log/re values, an
 * upload button to upload a log file, and a panel with example logs.
 */
public class InputTab extends Tab<VerticalPanel> {
    private static final String UPLOAD_LOGFILE_URL = GWT.getModuleBaseURL()
            + "log_file_upload";

    private static final String TOOLTIP_URL = "http://code.google.com/p/synoptic/wiki/DocsWebAppTutorial#Inputs_Tab";

    final String regExpDefault = "(?<TYPE>.*)";
    final String partitionRegExpDefault = "\\k<FILE>";

    final VerticalPanel poExamplesPanel = new VerticalPanel();
    final VerticalPanel toExamplesPanel = new VerticalPanel();
    final Grid examplesGrid = new Grid(5, 1);
    final Label exampleLogLabel = new Label("Load example logs");
    final Label parseErrorMsgLabel = new Label();
    final Label logLabel = new Label("Log");
    final Label regExpLabel = new Label("Regular expressions");
    final Label regExpDefaultLabel = new Label("Defaults to " + regExpDefault
            + " when empty");
    final Label partitionRegExpLabel = new Label("Partition expression");
    final Label partitionRegExpDefaultLabel = new Label("Defaults to "
            + partitionRegExpDefault + " when empty");
    final Label separatorRegExpLabel = new Label("Separator expression");
    final FormPanel logFileUploadForm = new FormPanel();
    final RadioButton logTextRadioButton = new RadioButton("logInputType",
            "Text");
    final RadioButton logFileRadioButton = new RadioButton("logInputType",
            "File");

    final TextArea logTextArea = new ExtendedTextArea(new ScheduledCommand() {
        @Override
        public void execute() {
            // Enable parse log button if pasting non-empty text.
            if (logTextArea.getValue().trim().length() != 0) {
                parseLogButton.setEnabled(true);
            }
        }
    });

    final TextBox partitionRegExpTextBox = new ExtendedTextBox(
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
    final VerticalPanel regExpsPanel = new VerticalPanel();
    final Button addRegExpButton = new Button("+");
    final CheckBox ignoreNonMatchedLines = new CheckBox();
    final CheckBox manualRefineCoarsen = new CheckBox();
    final CheckBox onlyMineInvs = new CheckBox();
    final Button parseLogButton = new Button("Parse Log");
    final Button clearInputsButton = new Button("Clear");

    public InputTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        super(synopticService, pWheel, "input-tab");

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

        Grid grid = new Grid(6, 2);
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
        uploadLogFileButton.setVisible(false);
        uploadPanel.add(uploadLogFileButton);

        // Set up inner panel containing textarea and upload.
        VerticalPanel logPanel = new VerticalPanel();
        logPanel.add(logTextArea);
        logPanel.add(uploadPanel);

        VerticalPanel logInputPanel = new VerticalPanel();
        grid.setWidget(0, 0, logInputPanel);
        logInputPanel.add(logLabel);
        logInputPanel.add(logTextRadioButton);
        logInputPanel.add(logFileRadioButton);

        grid.setWidget(0, 1, logPanel);
        logTextArea.setCharacterWidth(80);
        logTextArea.setVisibleLines(10);
        logTextArea.setName("logTextArea");

        grid.setWidget(1, 0, regExpLabel);
        regExpsPanel.addStyleName("ExtraRegExps");
        regExpDefaultLabel.setStyleName("DefaultExpLabel");

        Grid regExpsHolder = new Grid(3, 1);
        regExpsHolder.setWidget(0, 0, regExpDefaultLabel);
        regExpsHolder.setWidget(1, 0, regExpsPanel);
        HorizontalPanel firstInput = getTextBoxAndDeletePanel();
        setUpTextBox(((ExtendedTextBox) firstInput.getWidget(0)));
        regExpsPanel.add(firstInput);
        ((Button) firstInput.getWidget(1)).setVisible(false);
        regExpsHolder.setWidget(2, 0, addRegExpButton);
        TooltipListener.setTooltip(addRegExpButton,
                "Add one more regular expression for matching log lines.",
                TOOLTIP_URL);
        grid.setWidget(1, 1, regExpsHolder);

        VerticalPanel partitionExpPanel = new VerticalPanel();
        partitionExpPanel.add(partitionRegExpDefaultLabel);
        partitionExpPanel.add(partitionRegExpTextBox);
        partitionRegExpDefaultLabel.setStyleName("DefaultExpLabel");
        grid.setWidget(2, 0, partitionRegExpLabel);
        grid.setWidget(2, 1, partitionExpPanel);
        partitionRegExpTextBox.setVisibleLength(80);
        partitionRegExpTextBox.setName("partitionRegExpTextBox");

        grid.setWidget(3, 0, separatorRegExpLabel);
        grid.setWidget(3, 1, separatorRegExpTextBox);
        separatorRegExpTextBox.setVisibleLength(80);
        separatorRegExpTextBox.setName("separatorRegExpTextBox");

        HorizontalPanel optsPanel = new HorizontalPanel();
        DisclosurePanel parOpts = new DisclosurePanel("Parsing options");
        parOpts.setAnimationEnabled(true);

        Grid parOptsGrid = new Grid(2, 2);
        parOptsGrid.setCellSpacing(6);
        parOptsGrid.setWidget(0, 0, ignoreNonMatchedLines);
        parOptsGrid.setHTML(0, 1, "Ignore non-matched lines");
        TooltipListener
                .setTooltip(
                        ignoreNonMatchedLines,
                        "If a log line is not matched by any regular expression, ignore it and move on.",
                        TOOLTIP_URL);
        parOpts.setContent(parOptsGrid);
        parOpts.setStyleName("SpecialOptions");
        optsPanel.add(parOpts);

        DisclosurePanel debugOpts = new DisclosurePanel("Debugging options");
        Grid debugOptsGrid = new Grid(2, 2);
        debugOptsGrid.setCellSpacing(6);
        debugOptsGrid.setWidget(0, 0, manualRefineCoarsen);
        debugOptsGrid.setHTML(0, 1, "Manual refinement/coarsening");
        TooltipListener
                .setTooltip(
                        manualRefineCoarsen,
                        "Provides manual controls for stepping through refinement and coarsening.",
                        TOOLTIP_URL);

        debugOptsGrid.setWidget(1, 0, onlyMineInvs);
        debugOptsGrid.setHTML(1, 1, "Only mine invariants");
        TooltipListener.setTooltip(onlyMineInvs,
                "Mine and show invariants, but do not show the model.",
                TOOLTIP_URL);

        debugOpts.setContent(debugOptsGrid);
        debugOpts.setAnimationEnabled(true);
        debugOpts.setStyleName("SpecialOptions");
        optsPanel.add(debugOpts);
        grid.setWidget(4, 1, optsPanel);

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.add(parseLogButton);
        buttonsPanel.add(clearInputsButton);
        parseLogButton.addStyleName("parseButton");
        parseLogButton.setEnabled(false); // initially disabled
        grid.setWidget(5, 1, buttonsPanel);

        grid.setStyleName("inputForm grid");
        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getCellCount(i); j++) {
                grid.getCellFormatter().setStyleName(i, j, "tableCell");
            }
        }

        // Set up the error label's style\visibility.
        parseErrorMsgLabel.setStyleName("ErrorMessage");
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

        // Set tooltips for controls.
        initTooltips();
    }

    /**
     * Associates controls with tooltips explaining what each control represents
     * or does.
     */
    private void initTooltips() {
        TooltipListener.setTooltip(exampleLogLabel,
                "Select a pre-made example to populate the input fields.",
                TOOLTIP_URL);
        TooltipListener
                .setTooltip(
                        logLabel,
                        "Select \"Text\" and paste in a log or click on \"File\" to upload a log file.",
                        TOOLTIP_URL);
        TooltipListener
                .setTooltip(
                        regExpLabel,
                        "Input regular expression(s) to match each event instance. Click on \"+\" to input additional regular expressions.",
                        TOOLTIP_URL);
        TooltipListener
                .setTooltip(
                        partitionRegExpLabel,
                        "Input a regular expression to associate events from the same partition.",
                        TOOLTIP_URL);
        TooltipListener
                .setTooltip(
                        separatorRegExpLabel,
                        "Input a regular expression to indicate the boundary between sequential partitions.",
                        TOOLTIP_URL);
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
        HorizontalPanel firstPanel = (HorizontalPanel) regExpsPanel
                .getWidget(0);
        ((TextBox) firstPanel.getWidget(0)).setText(regExpText);
        this.partitionRegExpTextBox.setText(partitionRegExpText);
        this.separatorRegExpTextBox.setText(separatorRegExpText);
    }

    /**
     * Sets all input field values to be empty strings and displays default reg
     * exp labels.
     */
    private void clearInputValues() {
        logTextArea.setValue("");
        regExpDefaultLabel.setVisible(true);
        for (int i = 0; i < regExpsPanel.getWidgetCount(); i++) {
            HorizontalPanel currPanel = (HorizontalPanel) regExpsPanel
                    .getWidget(i);
            TextBox textBox = (TextBox) currPanel.getWidget(0);
            textBox.setValue("");
        }
        partitionRegExpTextBox.setValue("");
        partitionRegExpDefaultLabel.setVisible(true);
        separatorRegExpTextBox.setValue("");
    }

    /**
     * Extracts all regular expressions into a list.
     */
    private List<String> extractAllRegExps() {
        String currRegExp;
        List<String> result = new LinkedList<String>();
        for (int i = 0; i < regExpsPanel.getWidgetCount(); i++) {
            // Extract each addition text box from panel within extraRegExpPanel
            HorizontalPanel currPanel = (HorizontalPanel) regExpsPanel
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
     * Returns a HorizontalPanel containing an ExtendedTextBox and a "minus"
     * button, which the user can click to remove the panel altogether. This is
     * used for regular expression inputs.
     */
    private HorizontalPanel getTextBoxAndDeletePanel() {
        ExtendedTextBox newTextBox = new ExtendedTextBox(
                new ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (!isEmptyRegExps(regExpsPanel)) {
                            regExpDefaultLabel.setVisible(false);
                        }
                    }
                });
        setUpTextBox(newTextBox);
        Button deleteButton = new Button("-");
        TooltipListener.setTooltip(deleteButton,
                "Remove this regular expression.", TOOLTIP_URL);
        deleteButton.addStyleName("DeleteButton");
        deleteButton.addClickHandler(new DeleteTextBoxHandler());
        HorizontalPanel textBoxAndDeleteHolder = new HorizontalPanel();
        textBoxAndDeleteHolder.add(newTextBox);
        textBoxAndDeleteHolder.add(deleteButton);
        return textBoxAndDeleteHolder;
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
     * Returns true if the regular expression input(s) are empty. Returns false
     * otherwise.
     */
    private boolean isEmptyRegExps(VerticalPanel regExVerticalPanel) {
        for (int i = 0; i < regExVerticalPanel.getWidgetCount(); i++) {
            HorizontalPanel hp = (HorizontalPanel) regExVerticalPanel
                    .getWidget(i);
            TextBox textBox = (ExtendedTextBox) hp.getWidget(0);
            if (textBox.getValue().trim().length() != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets up properties for given ExtendedTextBox.
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
            regExpsPanel.add(getTextBoxAndDeletePanel());
            HorizontalPanel firstPanel = (HorizontalPanel) regExpsPanel
                    .getWidget(0);
            Button firstDelete = (Button) firstPanel.getWidget(1);
            firstDelete.setVisible(true);
        }
    }

    /**
     * Handler for when "Clear" button selected. Clears all inputs and disables
     * upload/parse log buttons. Displays empty reg exp labels and log text
     * area.
     */
    class ClearInputsHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            logFileUploadForm.reset();
            logTextArea.setVisible(true);
            uploadLogFileButton.setVisible(false);
            parseLogButton.setEnabled(false);
            regExpDefaultLabel.setVisible(true);
            partitionRegExpDefaultLabel.setVisible(true);
        }
    }

    /**
     * Removes the text box to the left of the clicked delete button.
     */
    class DeleteTextBoxHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            Button clicked = (Button) event.getSource();
            clicked.getParent().removeFromParent();
            HorizontalPanel firstPanel = (HorizontalPanel) regExpsPanel
                    .getWidget(0);
            Button firstDelete = (Button) firstPanel.getWidget(1);
            // Display a "minus" button only if there is more than one text
            // area.
            if (regExpsPanel.getWidgetCount() > 1) {
                firstDelete.setVisible(true);
            } else {
                firstDelete.setVisible(false);
            }
            if (isEmptyRegExps(regExpsPanel)) {
                regExpDefaultLabel.setVisible(true);
            } else {
                regExpDefaultLabel.setVisible(false);
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
            // Clears all inputs and uploads.
            logFileUploadForm.reset();
            Anchor anchorClicked = (Anchor) event.getSource();
            InputExample example = InputExample.valueOf(anchorClicked.getText()
                    .toUpperCase());
            setInputs(example.getLogText(), example.getRegExpText(),
                    example.getPartitionRegExpText(),
                    example.getSeparatorRegExpText());
            if (isEmptyRegExps(regExpsPanel)) {
                regExpDefaultLabel.setVisible(true);
            } else {
                regExpDefaultLabel.setVisible(false);
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
     * Enables parse log button if a file is selected. Disables parse log button
     * otherwise.
     */
    class FileUploadHandler implements ChangeHandler {

        @Override
        public void onChange(ChangeEvent event) {
            if (!uploadLogFileButton.getFilename().isEmpty()) {
                parseLogButton.setEnabled(true);
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
                if (logTextArea.getValue().trim().length() != 0) {
                    parseLogButton.setEnabled(true);
                } else {
                    parseLogButton.setEnabled(false);
                }
            } else if (event.getSource() == partitionRegExpTextBox) {
                if (partitionRegExpTextBox.getValue().trim().length() != 0) {
                    partitionRegExpDefaultLabel.setVisible(false);
                } else {
                    partitionRegExpDefaultLabel.setVisible(true);
                }
            } else { // KeyUp event in a reg exp textbox input.
                if (isEmptyRegExps(regExpsPanel)) {
                    regExpDefaultLabel.setVisible(true);
                } else {
                    regExpDefaultLabel.setVisible(false);
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
        @SuppressWarnings("synthetic-access")
        @Override
        public void onSubmitComplete(SubmitCompleteEvent event) {
            // Extract arguments for parseLog call.
            List<String> regExps = extractAllRegExps();
            String partitionRegExp = getTextBoxRegExp(partitionRegExpTextBox);
            String separatorRegExp = getTextBoxRegExp(separatorRegExpTextBox);
            SynopticGWT.entryPoint.manualRefineCoarsen = manualRefineCoarsen
                    .getValue();
            GWTSynOpts synOpts = new GWTSynOpts(null, regExps, partitionRegExp,
                    separatorRegExp, ignoreNonMatchedLines.getValue(),
                    manualRefineCoarsen.getValue(), onlyMineInvs.getValue());
            // ////////////////////// Call to remote service.
            synopticService.parseUploadedLog(synOpts,
                    new ParseLogAsyncCallback(pWheel, InputTab.this));
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
                logTextArea.setVisible(true);
                uploadLogFileButton.setVisible(false);
                if (logTextArea.getValue().trim().length() == 0) {
                    parseLogButton.setEnabled(false);
                } else {
                    parseLogButton.setEnabled(true);
                }
            } else { // logFileRadioButton
                logTextArea.setVisible(false);
                uploadLogFileButton.setVisible(true);
                clearInputValues();
                if (!uploadLogFileButton.getFilename().isEmpty()) {
                    parseLogButton.setEnabled(true);
                } else {
                    parseLogButton.setEnabled(false);
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
                SynopticGWT.entryPoint.manualRefineCoarsen = manualRefineCoarsen
                        .getValue();

                GWTSynOpts synOpts = new GWTSynOpts(logLines, regExps,
                        partitionRegExp, separatorRegExp,
                        ignoreNonMatchedLines.getValue(),
                        manualRefineCoarsen.getValue(), onlyMineInvs.getValue());
                // ////////////////////// Call to remote service.
                synopticService.parseLog(synOpts, new ParseLogAsyncCallback(
                        pWheel, InputTab.this));
                // //////////////////////
            }
        }
    }

}