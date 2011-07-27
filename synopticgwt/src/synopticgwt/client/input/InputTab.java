package synopticgwt.client.input;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.client.SynopticGWT;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;

public class InputTab {

    private ISynopticServiceAsync synopticService;
    private ProgressWheel pWheel;

    // Inputs tab widgets:
    private final VerticalPanel inputsPanel = new VerticalPanel();
    private final Label parseErrorMsgLabel = new Label();
    private final TextArea logTextArea = new TextArea();
    private final TextArea regExpsTextArea = new TextArea();
    private final TextBox partitionRegExpTextBox = new TextBox();
    private final TextBox separatorRegExpTextBox = new TextBox();
    private final Button parseLogButton = new Button("Parse Log");

    public InputTab(ISynopticServiceAsync synopticService, ProgressWheel pWheel) {
        this.synopticService = synopticService;
        this.pWheel = pWheel;

        // Construct the inputs panel using a grid.
        inputsPanel.add(parseErrorMsgLabel);

        Grid grid = new Grid(5, 2);
        inputsPanel.add(grid);

        grid.setWidget(0, 0, new Label("Log lines"));
        grid.setWidget(0, 1, logTextArea);
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

        // Set up the error label's style\visibility
        parseErrorMsgLabel.setStyleName("serverResponseLabelError");
        parseErrorMsgLabel.setVisible(false);

        // Set up the logTextArea
        logTextArea.setFocus(true);
        logTextArea
                .setText("1 0 c\n2 0 b\n3 0 a\n4 0 d\n1 1 f\n2 1 b\n3 1 a\n4 1 e\n1 2 f\n2 2 b\n3 2 a\n4 2 d");
        logTextArea.selectAll();

        // Set up the test areas
        regExpsTextArea.setText("^(?<TIME>)(?<nodename>)(?<TYPE>)$");
        partitionRegExpTextBox.setText("\\k<nodename>");

        // Associate handler with the Parse Log button
        parseLogButton.addClickHandler(new ParseLogHandler());
    }

    public VerticalPanel getInputsPanel() {
        return inputsPanel;
    }

    /**
     * Used for handling Parse Log button clicks
     */
    class ParseLogHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            // Disallow the user from making concurrent Parse Log calls
            parseLogButton.setEnabled(false);

            // Reset the parse error msg
            parseErrorMsgLabel.setText("");

            // Extract arguments for parseLog call
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

            // TODO: validate the args to parseLog()
            synopticService.parseLog(logLines, regExps, partitionRegExp,
                    separatorRegExp, new ParseLogAsyncCallback());
        }
    }

    /**
     * Injects an error message at the top of the page when an RPC call fails
     */
    public void injectRPCError(String message) {
        Label error = new Label(message);
        error.setStyleName("ErrorMessage");
        RootPanel.get("rpcErrorDiv").add(error);
    }

    /**
     * onSuccess\onFailure callback handler for parseLog()
     */
    class ParseLogAsyncCallback implements
            AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> {
        @Override
        public void onFailure(Throwable caught) {
            injectRPCError("Remote Procedure Call Failure while parsing log");
            parseErrorMsgLabel.setText("Remote Procedure Call - Failure");
            parseLogButton.setEnabled(true);
        }

        @Override
        public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> result) {
            parseLogButton.setEnabled(true);

            GWTInvariantSet gwtInvs = result.getLeft();
            GWTGraph gwtGraph = result.getRight();
            SynopticGWT.entryPoint.parsedOk(gwtInvs, gwtGraph);
        }
    }
}
